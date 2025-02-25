/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
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
package com.ibm.ws.wsatApp.client;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "Hello", targetNamespace = "http://server.wsatApp.ws.ibm.com/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface Hello {


    /**
     * 
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "sayHello", targetNamespace = "http://server.wsatApp.ws.ibm.com/", className = "com.ibm.ws.wsatApp.client.SayHello")
    @ResponseWrapper(localName = "sayHelloResponse", targetNamespace = "http://server.wsatApp.ws.ibm.com/", className = "com.ibm.ws.wsatApp.client.SayHelloResponse")
    public String sayHello();

    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "sayHelloToOther", targetNamespace = "http://server.wsatApp.ws.ibm.com/", className = "com.ibm.ws.wsatApp.client.SayHelloToOther")
    @ResponseWrapper(localName = "sayHelloToOtherResponse", targetNamespace = "http://server.wsatApp.ws.ibm.com/", className = "com.ibm.ws.wsatApp.client.SayHelloToOtherResponse")
    public String sayHelloToOther(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,  @WebParam(name = "arg1", targetNamespace = "")
        String arg1);

    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */    
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "sayHelloToOtherWithout", targetNamespace = "http://server.wsatApp.ws.ibm.com/", className = "com.ibm.ws.wsatApp.client.SayHelloToOtherWithout")
    @ResponseWrapper(localName = "sayHelloToOtherWithoutResponse", targetNamespace = "http://server.wsatApp.ws.ibm.com/", className = "com.ibm.ws.wsatApp.client.SayHelloToOtherWithoutResponse")
    public String sayHelloToOtherWithout(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,  @WebParam(name = "arg1", targetNamespace = "")
        String arg1);

}
