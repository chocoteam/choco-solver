/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package samples;

import common.ESat;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * It consists of two vectors v and w.
 * v is of length 5 and the variables have domain {1 . . . 50}.
 * An AllDifferent constraint is placed on v, and also v[4] = v[5].
 * Therefore there are no solutions.
 * w is a vector of length l &ge; 4, containing variables with domain {1...d}.
 * A AllDifferent constraint is placed on w,
 * and the two vectors v,w are linked by
 * v[1] = w[1], v[2] = w[2], v[3] = w[3], and v[4] = w[4]
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/08/11
 */
public class Contrived extends AbstractProblem {

    @Option(name = "-l", usage = "Size of vector w (l>=4).", required = false)
    int l = 100;
    @Option(name = "-d", usage = "Upper bound of vector w.", required = false)
    int d = l + 1;

    IntVar[] v, w;

    @Override
    public void createSolver() {
        solver = new Solver("Contrived");
    }

    @Override
    public void buildModel() {
        l = Math.max(4, l);
        if (d == 0) {
            d = l + 1;
        }
        v = VariableFactory.boundedArray("v", 5, 1, 50, solver);
        w = VariableFactory.boundedArray("v", l, 1, d, solver);

        solver.post(IntConstraintFactory.alldifferent(v, "BC"));
        solver.post(IntConstraintFactory.alldifferent(w, "BC"));
        solver.post(IntConstraintFactory.arithm(v[3], "=", v[4]));
        solver.post(IntConstraintFactory.arithm(v[0], "=", w[0]));
        solver.post(IntConstraintFactory.arithm(v[1], "=", w[1]));
        solver.post(IntConstraintFactory.arithm(v[2], "=", w[2]));
        solver.post(IntConstraintFactory.arithm(v[3], "=", w[3]));

    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.firstFail_InDomainMin(v
        ));
    }

    @Override
    public void configureEngine() {
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Contrived problem ({},{})", new Object[]{l, d});
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == ESat.TRUE) {
            st.append("\tV :");
            for (int i = 0; i < v.length; i++) {
                st.append(v[i].getValue()).append(" ");
            }
            st.append("\n");
            st.append("\tW :");
            for (int i = 0; i < w.length; i++) {
                st.append(w[i].getValue()).append(" ");
            }
            st.append("\n");
        } else {
            st.append("\tINFEASIBLE");
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Contrived().execute(args);
    }
}
