/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;

/**
 * Conditional AllDifferent constraint.
 */
public class CondAllDifferent extends Constraint {

    public static final String AC= "AC";
    public static final String AC_REGIN= "AC_REGIN";
    public static final String AC_ZHANG = "AC_ZHANG";
    public static final String BC= "BC";
    public static final String FC= "FC";
    public static final String DEFAULT= "DEFAULT";

    public CondAllDifferent(IntVar[] variables, Condition condition, String consistency, boolean singleCondition) {
        super(ConstraintsName.CONDALLDIFFERENT, createPropagators(variables, condition, consistency, singleCondition));
    }

    private static Propagator[] createPropagators(IntVar[] variables, Condition condition, String consistency, boolean singleCondition) {
        if (!singleCondition) {
            return new Propagator[] {new PropCondAllDiffInst(variables, condition, singleCondition)};
        }
        switch (consistency) {
            case FC:
                return new Propagator[]{new PropCondAllDiffInst(variables, condition, singleCondition)};
            case BC:
                return new Propagator[]{
                        new PropCondAllDiffInst(variables, condition, singleCondition),
                        new PropCondAllDiffBC(variables, condition)
                };
            case AC_REGIN:
                return new Propagator[]{
                        new PropCondAllDiffInst(variables, condition, singleCondition),
                        new PropCondAllDiffAC(variables, condition, false)
                };
            case AC:
            case AC_ZHANG:
                return new Propagator[]{
                        new PropCondAllDiffInst(variables, condition, singleCondition),
                        new PropCondAllDiffAC(variables, condition, true)
                };
            case DEFAULT:
            default: {
                // adds a Probabilistic AC (only if at least some variables have an enumerated domain)
                boolean enumDom = false;
                for (int i = 0; i < variables.length && !enumDom; i++) {
                    if (variables[i].hasEnumeratedDomain()) {
                        enumDom = true;
                    }
                }
                if (enumDom) {
                    return new Propagator[]{
                            new PropCondAllDiffInst(variables, condition, singleCondition),
                            new PropCondAllDiffBC(variables, condition),
                            new PropCondAllDiffAdaptative(variables, condition, true)
                    };
                } else {
                    return createPropagators(variables, condition, "BC", true);
                }
            }
        }
    }
}
