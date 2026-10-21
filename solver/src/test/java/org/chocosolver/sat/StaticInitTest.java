/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Checks that the static fields of the SAT classes do not depend on the order in which the classes are initialized.
 * Each test loads the package in a fresh class loader, so that the classes already initialized by other tests
 * do not hide the problem.
 *
 * @author Charles Prud'homme
 */
public class StaticInitTest {

    /**
     * Loads the classes of this package itself, and delegates the others to its parent.
     */
    private static final class FreshLoader extends ClassLoader {
        private static final String PACKAGE = "org.chocosolver.sat.";

        FreshLoader() {
            super(StaticInitTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith(PACKAGE) || name.substring(PACKAGE.length()).contains(".")) {
                return super.loadClass(name, resolve);
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    String path = name.replace('.', '/') + ".class";
                    try (InputStream in = getParent().getResourceAsStream(path)) {
                        if (in == null) {
                            throw new ClassNotFoundException(name);
                        }
                        byte[] bytes = in.readAllBytes();
                        c = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
    }

    private static void checkMiniSatStatics(String first) throws Exception {
        ClassLoader loader = new FreshLoader();
        // initializes 'first' before MiniSat
        Class.forName(first, true, loader);
        Class<?> miniSat = Class.forName("org.chocosolver.sat.MiniSat", true, loader);
        for (String name : new String[]{"C_Undef", "R_Undef", "VD_Undef", "C_Fail"}) {
            Field f = miniSat.getDeclaredField(name);
            f.setAccessible(true);
            Assert.assertNotNull(f.get(null), name + " is null when " + first + " is initialized first");
        }
    }

    @Test(groups = "1s")
    public void testReasonFirst() throws Exception {
        checkMiniSatStatics("org.chocosolver.sat.Reason");
    }

    @Test(groups = "1s")
    public void testClauseFirst() throws Exception {
        checkMiniSatStatics("org.chocosolver.sat.Clause");
    }

    @Test(groups = "1s")
    public void testArrayClauseFirst() throws Exception {
        checkMiniSatStatics("org.chocosolver.sat.ArrayClause");
    }

    @Test(groups = "1s")
    public void testMiniSatFirst() throws Exception {
        checkMiniSatStatics("org.chocosolver.sat.MiniSat");
    }
}
