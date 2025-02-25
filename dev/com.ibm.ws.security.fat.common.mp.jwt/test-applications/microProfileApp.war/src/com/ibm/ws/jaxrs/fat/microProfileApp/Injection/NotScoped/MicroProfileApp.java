/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation and others.
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

package com.ibm.ws.jaxrs.fat.microProfileApp.Injection.NotScoped;

import javax.ws.rs.Path;

import com.ibm.ws.jaxrs.fat.microProfileApp.JsonWebTokenInjectionMicroProfileApp;

// http://localhost:<nonSecurePort>/microProfileApp/rest/InjectionNotScoped/MicroProfileApp
// allow the same methods to invoke GET, POST, PUT, ... invocation type determines which is invoked.

@Path("InjectionNotScoped")
public class MicroProfileApp extends JsonWebTokenInjectionMicroProfileApp {

}
