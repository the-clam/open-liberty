/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
package com.ibm.wsspi.udpchannel;

import java.io.IOException;

import com.ibm.wsspi.channelfw.VirtualConnection;

/**
 * A callback object whose methods are called by the UDPChannel upon the
 * completion (or error) of a read request.
 */
public interface UDPReadCompletedCallbackThreaded {

    /**
     * Called when the request has completeted successfully.
     * 
     * @param vc associated with this request.
     * @param buffer associated with this request.
     */
    void complete(VirtualConnection vc, UDPBuffer buffer);

    /**
     * Called back if an exception occurres while processing the request. The
     * implementer of this interface can then decide how to handle this
     * exception.
     * 
     * @param vc associated with this request.
     * @param rsc associated with this request.
     * @param ioe The exception.
     */
    void error(VirtualConnection vc, UDPReadRequestContext rsc, IOException ioe);

}
