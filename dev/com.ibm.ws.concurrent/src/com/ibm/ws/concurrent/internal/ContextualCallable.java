/*******************************************************************************
 * Copyright (c) 2018,2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.concurrent.internal;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.concurrent.ContextualAction;
import com.ibm.wsspi.threadcontext.ThreadContext;
import com.ibm.wsspi.threadcontext.ThreadContextDescriptor;

/**
 * Proxy for Callable that applies thread context before running and removes it afterward
 *
 * @param <T> type of the result that is returned by the Callable
 */
class ContextualCallable<T> implements Callable<T>, ContextualAction<Callable<T>> {
    private final Callable<T> action;
    private final ThreadContextDescriptor threadContextDescriptor;

    ContextualCallable(ThreadContextDescriptor threadContextDescriptor, Callable<T> action) {
        this.action = action;
        this.threadContextDescriptor = threadContextDescriptor;
    }

    @Override
    public T call() throws Exception {
        ArrayList<ThreadContext> contextApplied = threadContextDescriptor.taskStarting();
        try {
            return action.call();
        } finally {
            threadContextDescriptor.taskStopping(contextApplied);
        }
    }

    @Override
    @Trivial
    public Callable<T> getAction() {
        return action;
    }

    @Override
    @Trivial
    public ThreadContextDescriptor getContextDescriptor() {
        return threadContextDescriptor;
    }
}