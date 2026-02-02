/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
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

    public enum Consistency {
        AC,
        AC_REGIN,
        AC_ZHANG,
        AC_CLASSIC,
        AC_COMPLEMENT,
        AC_PARTIAL,
        AC_TUNED,
        BC,
        FC,
        NEQS,
        DEFAULT
    }

    public AllDifferent(IntVar[] vars, String type) {
        super(ConstraintsName.ALLDIFFERENT, createPropagators(vars, type));
    }

    private static Propagator[] createPropagators(IntVar[] VARS, String consistency) {
        Model model = VARS[0].getModel();
        if (model.getSolver().isLCG()) {
            String message = "";
            if (consistency.equals("AC_ZHANG")) {
                consistency = "AC_TUNED";
                message = "Warning: Adjust consistency level of AllDifferent from \"AC_ZHANG\" to " +
                        "\"AC_TUNED\" due to LCG.";
            }
            boolean allEnum = Arrays.stream(VARS).allMatch(IntVar::hasEnumeratedDomain);
            if (!allEnum) {
                if (consistency.startsWith("AC") || consistency.equals("DEFAULT")) {
                    consistency = "BC";
                    message = "Warning: Adjust consistency level of AllDifferent to \"BC\" due to LCG" +
                            "because not all variables are enumerated.";
                }
            }
            if (!message.isEmpty() && model.getSettings().warnUser()) {
                model.getSolver().log().white().println(message);
            }
        }
        Consistency choice = Consistency.valueOf(consistency);
        switch (choice) {
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
            case AC:
            case AC_REGIN:
            case AC_ZHANG:
            case AC_CLASSIC:
            case AC_COMPLEMENT:
            case AC_PARTIAL:
            case AC_TUNED:
                return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffAC(VARS, choice)};
            case DEFAULT:
            default: {
                // adds a Probabilistic AC (only if at least some variables have an enumerated domain)
                boolean allEnum = Arrays.stream(VARS).allMatch(IntVar::hasEnumeratedDomain);
                if (allEnum) {
                    return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffBC(VARS),
                            new PropAllDiffAdaptative(VARS, Consistency.valueOf("AC_TUNED"))};
                } else {
                    return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffBC(VARS)};
                }
            }
        }
    }
}
