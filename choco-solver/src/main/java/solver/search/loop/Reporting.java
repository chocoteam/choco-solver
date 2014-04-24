/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.search.loop;

import solver.Solver;
import solver.constraints.Constraint;
import solver.search.strategy.decision.Decision;
import solver.variables.Variable;
import util.ESat;
import util.tools.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/02/12
 */
public enum Reporting {
    ;

    public static String onDecisions(Solver solver) {
        SearchLoop searchLoop = solver.getSearchLoop();
        Decision last = searchLoop.decision;
        Deque<Decision> stack = new ArrayDeque<Decision>();
        while (last != null) {
            stack.push(last);
            last = last.getPrevious();
        }
        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            sb.append(stack.removeFirst().toString()).append(" & ");
        }
        sb.append("\n").append(solver.getObjectiveManager().toString());
        return sb.toString();
    }

    public static String onUninstiatedVariables(Solver solver) {
        Variable[] variables = solver.getVars();
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < variables.length; c++) {
            boolean insV = variables[c].isInstantiated();
            if (!insV) {
                sb.append("FAILURE >> ").append(variables[c].toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public static String onUnsatisfiedConstraints(Solver solver) {
        Constraint[] constraints = solver.getCstrs();
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < constraints.length; c++) {
            ESat satC = constraints[c].isSatisfied();
            if (!ESat.TRUE.equals(satC)) {
                sb.append("FAILURE >> ").append(constraints[c].toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public static String fullReport(Solver solver) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(StringUtils.pad("", 50, "#")).append("\n");
        sb.append(onUninstiatedVariables(solver)).append("\n");
        sb.append(StringUtils.pad("", 50, "#")).append("\n");
        sb.append(onUnsatisfiedConstraints(solver)).append("\n");
        sb.append(StringUtils.pad("", 50, "=")).append("\n");
        sb.append(onDecisions(solver)).append("\n");
        sb.append(solver.getMeasures().toOneShortLineString());
        sb.append(StringUtils.pad("", 50, "#")).append("\n");
        return sb.toString();
    }
}
