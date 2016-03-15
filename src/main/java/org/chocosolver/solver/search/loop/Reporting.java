/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
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
        Solver solver = model.getSolver();
        StringBuilder sb = new StringBuilder();
        sb.append(solver.getDecisionPath().toString());
        sb.append("\n").append(model.getSolver().getObjectiveManager().toString());
        return sb.toString();
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
