/*******************************************************************************
 * Copyright (c) 2015, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.jsf22.fat.html5.faces40;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 *
 */
@Named("testPassthroughTextareaBean")
@RequestScoped
public class TestPassthroughTextareaBean implements Serializable {
    /**  */
    private static final long serialVersionUID = 1L;

    private String phone;
    private String complainText;

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the complainText
     */
    public String getComplainText() {
        return complainText;
    }

    /**
     * @param complainText the complainText to set
     */
    public void setComplainText(String complainText) {
        this.complainText = complainText;
    }

    public void submit() {
        System.out.println("phone: " + phone);
        System.out.println("complainText: " + complainText);
    }
}
