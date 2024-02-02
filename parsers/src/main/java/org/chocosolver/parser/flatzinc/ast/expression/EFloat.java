/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.expression;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/12/2023
 */
public class EFloat extends Expression {
    public final float value;

    private EFloat(float value) {
        super(EType.FLT);
        this.value = value;
    }

    public static EFloat make(String svalue) {
        float value = Float.parseFloat(svalue);
        return make(value);
    }

    public static EFloat make(float value) {
        return new EFloat(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public float floatValue() {
        return this.value;
    }
}
