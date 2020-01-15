/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer.util;

import org.chocosolver.solver.constraints.Propagator;

import java.lang.reflect.Field;

/**
 * Utility class to get parameters by reflection <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 13/09/2017.
 */
public class Reflection {

    /**
     * @param prop      propagator to inspect
     * @param fieldname name of the field to get
     * @return the value as int of the field name 'fieldname' in 'object'
     */
    public static int getInt(Propagator prop, String fieldname) {
        return getInt(prop, prop.getClass(), fieldname);
    }

    private static int getInt(Propagator prop, Class clazz, String fieldname) {
        try {
            Field field = clazz.getDeclaredField(fieldname);
            field.setAccessible(true);
            return field.getInt(prop);
        } catch (NoSuchFieldException e) {
            return getInt(prop, clazz.getSuperclass(), fieldname);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param o      object to inspect
     * @param fieldname name of the field to get
     * @return the value as int of the field name 'fieldname' in 'object'
     */
    @SuppressWarnings("unchecked")
    public static <F> F getObj(Object o, String fieldname) {
        return getObj(o, o.getClass(), fieldname);
    }

    /**
     * @param prop      propagator to inspect
     * @param fieldname name of the field to get
     * @return the value as int of the field name 'fieldname' in 'object'
     */
    @SuppressWarnings("unchecked")
    public static <F> F getObj(Propagator prop, String fieldname) {
        return getObj(prop, prop.getClass(), fieldname);
    }

    @SuppressWarnings("unchecked")
    private static <F> F getObj(Object o, Class clazz, String fieldname) {
        try {
            Field field = clazz.getDeclaredField(fieldname);
            field.setAccessible(true);
            return (F) field.get(o);
        } catch (NoSuchFieldException e) {
            return getObj(o, clazz.getSuperclass(), fieldname);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
