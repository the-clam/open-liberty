/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package concurrent.mp.fat.v20.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.junit.Test;

import componenttest.app.FATServlet;

@ContextServiceDefinition(name = "java:module/AppAndPriorityContext",
                          cleared = ContextServiceDefinition.SECURITY,
                          propagated = { ContextServiceDefinition.APPLICATION, "Priority" },
                          unchanged = ContextServiceDefinition.TRANSACTION)
@ManagedExecutorDefinition(name = "java:comp/eeExecutor",
                           context = "java:module/AppAndPriorityContext",
                           maxAsync = 2)
@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/MPContextProp2_0_TestServlet")
public class MPContextProp2_0_TestServlet extends FATServlet {
    /**
     * 2 minutes. Maximum number of nanoseconds to wait for a task or action to complete.
     */
    private static final long TIMEOUT_NS = TimeUnit.MINUTES.toNanos(2);

    @Inject
    MPAppBean appBean;

    @Inject
    AsyncClassBean acBean;

    @Resource(name = "java:module/env/defaultExecutorRef")
    private ManagedExecutor defaultExecutor;

    @Resource(lookup = "java:comp/eeExecutor")
    ManagedExecutorService eeExecutor;

    @Inject
    MPFTBean ftBean;

    ManagedExecutor mpExecutor;

    /**
     * Executor that can be used when tests don't want to tie up threads from the Liberty global thread pool to perform test logic
     */
    private ExecutorService testThreads;

    @Resource
    UserTransaction tx;

    @Override
    public void destroy() {
        AccessController.doPrivileged((PrivilegedAction<List<Runnable>>) () -> testThreads.shutdownNow());
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        testThreads = Executors.newFixedThreadPool(10);
        mpExecutor = ManagedExecutor.builder()
                        .cleared(ThreadContext.SECURITY, ThreadContext.TRANSACTION)
                        .propagated(ThreadContext.ALL_REMAINING)
                        .maxAsync(3)
                        .build();
    }

    /**
     * Verify that Asynchronous is not allowed at the class level.
     */
    @Test
    public void testAsynchronousNotAllowedOnClass() throws Exception {
        try {
            CompletableFuture<Object> stage = acBean.asyncLookup("java:comp/eeExecutor");
            fail("@Asynchronous should not be supported at class level. " + stage);
        } catch (UnsupportedOperationException x) {
            // expected
        }
    }

    /**
     * Invoke an asynchronous method that relies on a Jakarta EE ManagedExecutorService
     * while MicroProfile Context Propagation is enabled.
     */
    @Test
    public void testAsyncMethodUsesJakartaEEManagedExecutorService() throws Exception {
        CompletableFuture<Object> result = appBean.eeAsyncLookup("java:comp/eeExecutor");
        assertNotNull(result.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
    }

    /**
     * Invoke an asynchronous method that uses a resource reference to the
     * default managed executor as a MicroProfile ManagedExecutor.
     */
    @Test
    public void testAsyncMethodUsesResourceRefToManagedExecutor() throws Exception {
        CompletableFuture<Object> result = appBean.mpAsyncLookup("java:module/env/defaultExecutorRef");
        assertNotNull(result.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
    }

    /**
     * ManagedExecutor.copy was added in 1.2 and should still be working.
     * With this method, you should be able to copy from an unmanaged completion stage
     * that lacks context propagation to create a copied stage that uses the
     * managed executor to propagate thread context.
     */
    @Test
    public void testCopy() throws Exception {
        CompletableFuture<StringBuilder> unmanagedStage = CompletableFuture.supplyAsync(() -> new StringBuilder());

        CompletableFuture<StringBuilder> managedStage = defaultExecutor
                        .copy(unmanagedStage)
                        .thenApplyAsync(b -> {
                            try {
                                return b.append((ManagedExecutor) InitialContext.doLookup("java:module/env/defaultExecutorRef"));
                            } catch (NamingException x) {
                                throw new CompletionException(x);
                            }
                        });
        StringBuilder b = managedStage.get(TIMEOUT_NS, TimeUnit.NANOSECONDS);

        assertTrue(b.toString(), b.toString().startsWith("ManagedExecutor@"));
    }

    /**
     * Verify that custom thread context types are not propagated by the default ManagedExecutorService.
     */
    @Test
    public void testCustomContextIsNotPropagatedByDefault() throws Exception {
        int defaultPriority = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(defaultPriority - 1);
        try {
            CompletableFuture<Integer> executorThreadPriority = defaultExecutor.completedFuture(0)
                            .thenApplyAsync(i -> i + Thread.currentThread().getPriority());
            assertEquals(Integer.valueOf(defaultPriority), executorThreadPriority.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
        } finally {
            Thread.currentThread().setPriority(defaultPriority);
        }
    }

    /**
     * Verify that custom thread context types are propagated if configured on the ContextServiceDefinition.
     */
    // TODO @Test this should start working once mpContextPropagation-2.0 is added
    public void testCustomEEContextIsPropagatedWhenConfigured() throws Exception {
        CompletableFuture<String> initialStage = eeExecutor.newIncompleteFuture();
        CompletableFuture<String> executorThreadPriorities;

        int defaultPriority = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(defaultPriority - 1);
        try {
            executorThreadPriorities = initialStage.thenApplyAsync(s -> s + Thread.currentThread().getPriority());

            Thread.currentThread().setPriority(defaultPriority - 2);

            executorThreadPriorities = executorThreadPriorities.thenApply(s -> s + ' ' + Thread.currentThread().getPriority());
        } finally {
            Thread.currentThread().setPriority(defaultPriority);
        }
        initialStage.complete("");

        String expected = (defaultPriority - 1) + " " + (defaultPriority - 2);
        assertEquals(expected, executorThreadPriorities.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
    }

    /**
     * Verify that custom thread context types are propagated if configured on the builder.
     */
    @Test
    public void testCustomMPContextIsPropagatedWhenConfigured() throws Exception {
        CompletableFuture<String> initialStage = mpExecutor.newIncompleteFuture();
        CompletableFuture<String> executorThreadPriorities;

        int defaultPriority = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(defaultPriority - 1);
        try {
            executorThreadPriorities = initialStage.thenApplyAsync(s -> s + Thread.currentThread().getPriority());

            Thread.currentThread().setPriority(defaultPriority - 2);

            executorThreadPriorities = executorThreadPriorities.thenApply(s -> s + ' ' + Thread.currentThread().getPriority());
        } finally {
            Thread.currentThread().setPriority(defaultPriority);
        }
        initialStage.complete("");

        String expected = (defaultPriority - 1) + " " + (defaultPriority - 2);
        assertEquals(expected, executorThreadPriorities.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
    }

    /**
     * java:comp/DefaultManagedExecutorService can be used interchangeably as a MicroProfile ManagedExecutor
     * or as a Jakarta EE ManagedExecutorService.
     */
    @Test
    public void testDefaultManagedExecutorService() throws Exception {
        assertTrue(defaultExecutor.toString(), defaultExecutor instanceof ManagedExecutorService);

        Object executor = InitialContext.doLookup("java:module/env/defaultExecutorRef");

        assertTrue(executor.toString(), executor instanceof ManagedExecutor);
        assertTrue(executor.toString(), executor instanceof ManagedExecutorService);
    }

    /**
     * Jakarta EE Concurrency should not interfere with MicroProfile FaultTolerance Asynchronous
     * when only the MicroProfile annotation is present.
     */
    @Test
    public void testFaultToleranceAsyncMethod() throws Exception {
        CompletionStage<Object> result = ftBean.ftAsyncLookup("java:comp/eeExecutor");
        assertNotNull(result.toCompletableFuture().get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
    }

    // TODO testFaultToleranceClassLevelAsynchronousCollidesWithMethod

    // TODO testFaultToleranceCollidesOnSameAsyncMethod

    /**
     * Use a MicroProfile ManagedExecutor and a Jakarta EE ManagedExecutorService
     * to create completion stages and combine the two to create dependent stages,
     * ensuring that each dependent stage runs with the configured context propagation
     * of the managed executor of the stage that creates the dependent stage.
     */
    @Test
    public void testIntermixMicroProfileAndJakartaEECompletionStages() throws Exception {
        CompletableFuture<String> eeStage1 = eeExecutor.supplyAsync(() -> {
            try {
                return InitialContext.doLookup("java:comp/eeExecutor").toString();
            } catch (NamingException x) {
                throw new CompletionException(x);
            }
        });

        CompletableFuture<String> mpStage1 = mpExecutor.supplyAsync(() -> {
            try {
                return InitialContext.doLookup("java:module/env/defaultExecutorRef").toString();
            } catch (NamingException x) {
                throw new CompletionException(x);
            }
        });

        assertNotNull(eeStage1.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
        assertNotNull(mpStage1.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));

        CompletableFuture<Integer> eeStage2;
        CompletableFuture<Integer> mpStage2;

        BiFunction<String, String, Integer> getTransactionStatus = (s1, s2) -> {
            try {
                UserTransaction tx1 = InitialContext.doLookup("java:comp/UserTransaction");
                return tx1.getStatus();
            } catch (NamingException | SystemException x) {
                throw new CompletionException(x);
            }
        };

        tx.begin();
        try {
            eeStage2 = eeStage1.thenCombine(mpStage1, getTransactionStatus);
            mpStage2 = mpStage1.thenCombine(eeStage1, getTransactionStatus);
        } finally {
            tx.rollback();
        }

        assertEquals(Integer.valueOf(Status.STATUS_ACTIVE), eeStage2.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
        assertEquals(Integer.valueOf(Status.STATUS_NO_TRANSACTION), mpStage2.get(TIMEOUT_NS, TimeUnit.NANOSECONDS));
    }
}