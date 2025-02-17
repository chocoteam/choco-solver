/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.binary.PropNotEqualX_Y;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

/**
 * Ensures that all variables from VARS take a different value.
 * The consistency level should be chosen among "AC", "BC", "FC" and "DEFAULT".
 */
public class AllDifferent extends Constraint {

    public static final String AC = "AC";
    public static final String AC_REGIN = "AC_REGIN";
    public static final String AC_ZHANG = "AC_ZHANG";
    public static final String BC = "BC";
    public static final String FC = "FC";
    public static final String NEQS = "NEQS";
    public static final String DEFAULT = "DEFAULT";

    public AllDifferent(IntVar[] vars, String type) {
        super(ConstraintsName.ALLDIFFERENT, createPropagators(vars, type));
    }

    private static Propagator[] createPropagators(IntVar[] VARS, String consistency) {
        Model model = VARS[0].getModel();
        if (model.getSolver().isLCG()) {
            String message = "";
            if (consistency.equals("AC") || consistency.equals("AC_ZHANG")) {
                consistency = "AC_REGIN";
                message = "Warning: Adjust consistency level of AllDifferent to \"AC_REGIN\" due to LCG resolution.";
            }
            boolean allEnum = Arrays.stream(VARS).allMatch(IntVar::hasEnumeratedDomain);
            if (!allEnum) {
                if (consistency.equals("AC_REGIN") || consistency.equals("DEFAULT")) {
                    consistency = "BC";
                    message = "Warning: Adjust consistency level of AllDifferent to \"BC\" due to LCG resolution.";
                }
            }
            if (!message.isEmpty() && model.getSettings().warnUser()) {
                model.getSolver().log().white().println(message);
            }
        }
        switch (consistency) {
            case NEQS: {
                int s = VARS.length;
                int k = 0;
                Propagator[] props = new Propagator[(s * s - s) / 2];
                for (int i = 0; i < s - 1; i++) {
                    for (int j = i + 1; j < s; j++) {
                        props[k++] = new PropNotEqualX_Y(VARS[i], VARS[j]);
                    }
                }
                return props;
            }
            case FC:
                return new Propagator[]{new PropAllDiffInst(VARS)};
            case BC:
                return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffBC(VARS)};
            case AC_REGIN:
                return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffAC(VARS, false)};
            case AC:
            case AC_ZHANG:
                return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffAC(VARS, true)};
            case DEFAULT:
            default: {
                // adds a Probabilistic AC (only if at least some variables have an enumerated domain)
                boolean enumDom = false;
                for (int i = 0; i < VARS.length && !enumDom; i++) {
                    if (VARS[i].hasEnumeratedDomain()) {
                        enumDom = true;
                    }
                }
                if (enumDom) {
                    return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffBC(VARS), new PropAllDiffAdaptative(VARS)};
                } else {
                    return createPropagators(VARS, "BC");
                }
            }
        }
    }
}
