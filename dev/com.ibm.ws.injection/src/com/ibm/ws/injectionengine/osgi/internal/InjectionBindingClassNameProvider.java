/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
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
package com.ibm.ws.injectionengine.osgi.internal;

import com.ibm.ws.container.service.naming.JavaColonNamespaceBindings;
import com.ibm.wsspi.injectionengine.InjectionBinding;

public class InjectionBindingClassNameProvider implements JavaColonNamespaceBindings.ClassNameProvider<InjectionBinding<?>> {
    public static final JavaColonNamespaceBindings.ClassNameProvider<InjectionBinding<?>> instance = new InjectionBindingClassNameProvider();

    @Override
    public String getBindingClassName(InjectionBinding<?> binding) {
        return binding.getInjectionClassTypeName();
    }
}
