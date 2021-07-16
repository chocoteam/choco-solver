/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.StringUtils;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/02/12
 */
public enum Reporting {
    ;

    public static String onDecisions(Model model) {
        return model.getSolver().getDecisionPath().toString() +"\n"+ model.getSolver().getObjectiveManager().toString();
    }

    public static String onUninstiatedVariables(Model model) {
        Variable[] variables = model.getVars();
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < variables.length; c++) {
            boolean insV = variables[c].isInstantiated();
            if (!insV) {
                sb.append("FAILURE >> ").append(variables[c].toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public static String onUnsatisfiedConstraints(Model model) {
        Constraint[] constraints = model.getCstrs();
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < constraints.length; c++) {
            ESat satC = constraints[c].isSatisfied();
            if (!ESat.TRUE.equals(satC)) {
                sb.append("FAILURE >> ").append(constraints[c].toString()).append("\n");
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public static String fullReport(Model model) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(StringUtils.pad("", 50, "#")).append("\n");
        sb.append(onUninstiatedVariables(model)).append("\n");
        sb.append(StringUtils.pad("", 50, "#")).append("\n");
        sb.append(onUnsatisfiedConstraints(model)).append("\n");
        sb.append(StringUtils.pad("", 50, "=")).append("\n");
        sb.append(onDecisions(model)).append("\n");
        sb.append(model.getSolver().getMeasures().toOneLineString());
        sb.append(StringUtils.pad("", 50, "#")).append("\n");
        return sb.toString();
    }
}
