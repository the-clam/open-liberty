/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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
package io.openliberty.xmlbinding.tools.internal;

/**
 * The general utility for io.openliberty.xmlbinding.tools projects.
 */
public class JaxbToolsUtil {
    /**
     * get the localized message provided in the message files in the io.openliberty.xmlbinding.tools.
     *
     * @param msgKey
     * @return
     */
    public static String formatMessage(String msgKey) {
        String msg;
        try {
            msg = JaxbToolsConstants.messages.getString(msgKey);
        } catch (Exception ex) {
            // no FFDC required
            return msgKey;
        }

        return msg;
    }
}
