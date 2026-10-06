/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Helpers shared by the classes of this package.
 *
 * @author Charles Prud'homme
 */
final class Specs {

    static final int[] NO_INT = new int[0];

    /**
     * Whether the instances of a class have no instance field.
     */
    private static final ClassValue<Boolean> STATELESS = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> c) {
            if (c.isArray()) {
                return false;
            }
            for (Class<?> k = c; k != null; k = k.getSuperclass()) {
                for (Field f : k.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        return false;
                    }
                }
            }
            return true;
        }
    };

    private Specs() {
    }

    /**
     * @return <i>true</i> if the instances of <i>c</i> have no instance field, hence are immutable
     */
    static boolean isStateless(Class<?> c) {
        return STATELESS.get(c);
    }

    /**
     * @return the type of the elements of <i>c</i>, whatever the number of dimensions (<i>c</i> if not an array)
     */
    static Class<?> elementType(Class<?> c) {
        while (c.isArray()) {
            c = c.getComponentType();
        }
        return c;
    }

    /**
     * Add the elements of <i>array</i>, row by row whatever the number of dimensions, to <i>flat</i>.
     */
    static void flatten(Object[] array, List<Object> flat) {
        for (Object o : array) {
            if (o instanceof Object[] sub) {
                flatten(sub, flat);
            } else {
                flat.add(o);
            }
        }
    }

    /**
     * @return the wrapper class of <i>c</i> if primitive, <i>c</i> otherwise
     */
    static Class<?> wrap(Class<?> c) {
        return c.isPrimitive() ? MethodType.methodType(c).wrap().returnType() : c;
    }
}
