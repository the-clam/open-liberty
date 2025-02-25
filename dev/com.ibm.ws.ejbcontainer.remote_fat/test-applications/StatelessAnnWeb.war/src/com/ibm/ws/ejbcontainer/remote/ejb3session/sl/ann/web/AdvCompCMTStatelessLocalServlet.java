/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
package com.ibm.ws.ejbcontainer.remote.ejb3session.sl.ann.web;

import static javax.transaction.Status.STATUS_COMMITTED;
import static javax.transaction.Status.STATUS_NO_TRANSACTION;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;
import javax.transaction.UserTransaction;

import org.junit.Test;

import com.ibm.websphere.ejbcontainer.test.tools.FATHelper;
import com.ibm.ws.ejbcontainer.remote.ejb3session.sl.ann.ejb.AdvCMTStatelessEJBLocal;
import com.ibm.ws.ejbcontainer.remote.ejb3session.sl.ann.ejb.AdvCMTStatelessEJBLocalHome;

import componenttest.app.FATServlet;

/**
 * <dl>
 * <dt><b>Test Name:</b> AdvCompCMTStatelessLocalTest .
 *
 * <dt><b>Test Author:</b> Tracy Burroughs
 * <p>
 *
 * <dt><b>Test Description:</b>
 * <dd>Tests EJB Container support for the Compatibility EJB 3.0 Container Managed Stateless Session bean functionality with advanced EJB 3.0 configurations: including multiple
 * Business Interfaces.
 * <p>
 *
 * <dt><b>Test Matrix:</b>
 * <dd>
 * <br>
 * Sub-tests
 * <ul>
 * <li>testCompIntTxAttribs - Component Interface: Verify methods with all ContainerManaged Tx Attributes
 * <li>testBizIntTxAttribs - Business Interface: Verify methods with all ContainerManaged Tx Attributes
 * <li>testCompIntBizGetters - Component Interface: Verify getInvokedBusinessInterface and getBusinessObject throw correct exception
 * <li>testBizIntBizGetters - Business Interface: Verify getInvokedBusinessInterface and getBusinessObject work correctly
 * <li>testCompIntInjection - Component Interface: Verify EJB field and method injection occurrs properly
 * </ul>
 * <br>
 * Data Sources - None
 * </dl>
 */
@SuppressWarnings("serial")
@WebServlet("/AdvCompCMTStatelessLocalServlet")
public class AdvCompCMTStatelessLocalServlet extends FATServlet {
    /**
     * Definitions for the logger
     */
    private final static String CLASSNAME = AdvCompCMTStatelessLocalServlet.class.getName();
    private final static Logger svLogger = Logger.getLogger(CLASSNAME);

    // Name of module... for lookup.
    private static final String Module = "StatelessAnnEJB";

    // Names of the beans used for the test... for lookup.
    private static final String AdvCompBean = "AdvCompCMTStatelessLocal";

    // Names of the interfaces used for the test
    private static final String AdvCMTStatelessEJBLocalHomeInterface = AdvCMTStatelessEJBLocalHome.class.getName();

    /**
     * Test calling methods on an EJB 3.0 CMT Stateless Session EJB, Component Interface, with each of the different Transaction Attributes.
     * <p>
     *
     * This test will confirm the following :
     * <ol>
     * <li>Stateless Session bean may be created.
     * <li>SLSB method with default tx attribute may be called.
     * <li>SLSB method with Required tx attribute may be called.
     * <li>SLSB method with NotSupported tx attribute may be called.
     * <li>SLSB method with RequiresNew tx attribute may be called.
     * <li>SLSB method with Supports tx attribute may be called.
     * <li>SLSB method with Never tx attribute may be called.
     * <li>SLSB method with Mandatory tx attribute may be called.
     * <li>Stateless Session bean may be removed.
     * </ol>
     */
    @Test
    public void testCompIntTxAttribs() throws Exception {
        UserTransaction userTran = null;
        try {
            // --------------------------------------------------------------------
            // Locate SL Local Home/Factory and execute the test
            // --------------------------------------------------------------------
            AdvCMTStatelessEJBLocalHome slHome = (AdvCMTStatelessEJBLocalHome) FATHelper.lookupDefaultBindingEJBJavaApp(AdvCMTStatelessEJBLocalHomeInterface, Module, AdvCompBean);
            AdvCMTStatelessEJBLocal bean = slHome.create();
            assertNotNull("1 ---> SLLSB created successfully.", bean);

            bean.tx_Default();
            bean.tx_Required();
            bean.tx_NotSupported();
            bean.tx_RequiresNew();
            bean.tx_Supports();
            bean.tx_Never();

            userTran = FATHelper.lookupUserTransaction();

            svLogger.info("Beginning User Transaction ...");
            userTran.begin();

            bean.tx_Mandatory();

            svLogger.info("Committing User Transaction ...");
            userTran.commit();

            bean.remove();
            svLogger.info("9 ---> SLLSB removed successfully.");
        } finally {
            if (userTran != null && userTran.getStatus() != STATUS_NO_TRANSACTION && userTran.getStatus() != STATUS_COMMITTED)
                userTran.rollback();
        }
    }

    /**
     * Test calling the context methods getInvokedBusinessInterface and getBusinessObject on an EJB 3.0 CMT Stateless Session EJB, Component Interface.
     * <p>
     *
     * This test will confirm the following :
     * <ol>
     * <li>Stateless Session bean may be created.
     * <li>getInvokedBusinessInterface results in an IllegalStateException.
     * <li>getBusinessObject(null) results in an IllegalStateException.
     * <li>getBusinessObject(Component) results in an IllegalStateException.
     * <li>getBusinessObject(Business) returns a business object.
     * <li>a method may be called on the returned business object.
     * <li>Stateless Session bean may be removed.
     * </ol>
     */
    @Test
    public void testCompIntBizGetters() throws Exception {
        // --------------------------------------------------------------------
        // Locate SL Local Home/Factory and execute the test
        // --------------------------------------------------------------------
        AdvCMTStatelessEJBLocalHome slHome = (AdvCMTStatelessEJBLocalHome) FATHelper.lookupDefaultBindingEJBJavaApp(AdvCMTStatelessEJBLocalHomeInterface, Module, AdvCompBean);
        AdvCMTStatelessEJBLocal bean = slHome.create();
        assertNotNull("1 ---> SLLSB created successfully.", bean);

        bean.test_getBusinessObject(false);

        bean.remove();
        svLogger.info("4 ---> SLLSB removed successfully.");
    }

    /**
     * Test that EJB field and method injection occurs properly on an EJB 3.0 CMT Stateless Session EJB, Component Interface.
     * <p>
     *
     * This test will confirm the following :
     * <ol>
     * <li>Stateless Session bean may be created.
     * <li>Verify SLSB field injection occurs properly.
     * <li>Verify SLSB field injection does not occur after field cleared in instance.
     * <li>Verify SLSB method injection occurs properly.
     * <li>Verify SLSB method injection does not occur after field cleared in instance.
     * <li>Stateless Session bean may be removed.
     * </ol>
     */
    @Test
    public void testCompIntInjection() throws Exception {
        // --------------------------------------------------------------------
        // Locate SL Local Home/Factory and execute the test
        // --------------------------------------------------------------------
        AdvCMTStatelessEJBLocalHome slHome = (AdvCMTStatelessEJBLocalHome) FATHelper.lookupDefaultBindingEJBJavaApp(AdvCMTStatelessEJBLocalHomeInterface, Module, AdvCompBean);
        AdvCMTStatelessEJBLocal bean = slHome.create();
        assertNotNull("1 ---> SLLSB created successfully.", bean);
        bean.verifyEJBFieldInjection();

        bean.remove();
        svLogger.info("16 --> SLLSB removed successfully.");
    }
}