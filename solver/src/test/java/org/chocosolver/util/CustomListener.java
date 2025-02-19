/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 20/08/2014
 */
public class CustomListener extends TestListenerAdapter {

    String className = "";
    PrintStream original = System.out;
    PrintStream fake = new PrintStream(new OutputStream() {
        public void write(int b) {
            //DO NOTHING
        }
    });

    @Override
    public void onStart(ITestContext testContext) {
        original = System.out;
        System.setOut(fake);
    }

    @Override
    public void onTestStart(ITestResult tr) {
        if (!className.equals(tr.getTestClass().getName())) {
            original.printf("\t%s\n", tr.getTestClass().getName());
            className = tr.getTestClass().getName();
        }
    }


    @Override
    public void onTestFailure(ITestResult tr) {
        original.printf("\t%s.%s ... FAILURE (%dms)\n",
                tr.getTestClass().getName(), tr.getName(), tr.getEndMillis() - tr.getStartMillis());
    }
}
