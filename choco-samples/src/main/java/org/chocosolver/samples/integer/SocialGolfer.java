/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;

/**
 * CSPLib prob010:<br/>
 * "The coordinator of a local golf club has come to you with the following problem.
 * In her club, there are 32 social golfers, each of whom play golf once a week,
 * and always in groups of 4.
 * She would like you to come up with a schedule of play for these golfers,
 * to last as many weeks as possible, such that
 * no golfer plays in the same group as any other golfer on more than one occasion.
 * <p/>
 * The problem can easily be generalized to that of scheduling
 * m groups of
 * n golfers over
 * p weeks,
 * such that no golfer plays in the same group as any other golfer twice
 * (i.e. maximum socialisation is achieved)
 * "
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/08/11
 */
public class SocialGolfer extends AbstractProblem {

    // parameters g-w-s:
    // 3-5-2, 3-4-3, 4-7-2, 4-4-3, 5-7-3

    // SPORT SCHEDULING: s=2, w = g - 1

    @Option(name = "-g", aliases = "--group", usage = "Number of groups.", required = false)
    int g = 4;

    @Option(name = "-w", aliases = "--week", usage = "Number of weeks.", required = false)
    int w = 4;

    @Option(name = "-s", aliases = "--player", usage = "Number of players per group.", required = false)
    int s = 3;


    BoolVar[][][] P, M;

    @Override
    public void createSolver() {
        solver = new Solver("Social golfer " + g + "-" + w + "-" + s);
    }

    @Override
    public void buildModel() {
        int p = g * s;  // number of players

        P = new BoolVar[p][g][w];
        // p plays in group g in week w
        for (int i = 0; i < p; i++) {
            P[i] = VariableFactory.boolMatrix("p_" + i, g, w, solver);
        }
        M = new BoolVar[p][p][w];
        // i meets j in week w (i<j)
        for (int i = 0; i < p; i++) {
            M[i] = VariableFactory.boolMatrix("m_" + i, p, w, solver);
        }

        // each player is part of exactly one group in each week
        for (int i = 0; i < p; i++) {
            for (int k = 0; k < w; k++) {
                IntVar[] player = new IntVar[g];
                for (int j = 0; j < g; j++) {
                    player[j] = P[i][j][k];
                }
                solver.post(IntConstraintFactory.sum(player, VariableFactory.fixed(1, solver)));
            }
        }

        // each group has exactly s players
        for (int j = 0; j < g; j++) {
            for (int k = 0; k < w; k++) {
                IntVar[] group = new IntVar[p];
                for (int i = 0; i < p; i++) {
                    group[i] = P[i][j][k];
                }
                solver.post(IntConstraintFactory.sum(group, VariableFactory.fixed(s, solver)));
            }
        }

		// obvious filtering for M
		for (int i = 0; i < p; i++) {
			for (int l = 0; l < w; l++) {
				for (int j = i+1; j < p; j++) {
					solver.post(IntConstraintFactory.arithm(M[i][j][l],"=",M[j][i][l]));
				}
				solver.post(IntConstraintFactory.arithm(M[i][i][l],"=",1));
			}
		}

        // link the M and P variables
        for (int i = 0; i < p - 1; i++) {
            for (int j = i + 1; j < p; j++) {
				for (int l = 0; l < w; l++) {
					BoolVar[] group = new BoolVar[g];
					for (int k = 0; k < g; k++) {
						group[k] = LogicalConstraintFactory.and(P[i][k][l], P[j][k][l]).reif();
						solver.post(IntConstraintFactory.arithm(group[k], "<=", M[i][j][l]));
                    }
					solver.post(IntConstraintFactory.sum(group,M[i][j][l]));
                }
            }
        }

        // each pair of players only meets once
        for (int i = 0; i < p - 1; i++) {
            for (int j = i + 1; j < p; j++) {
                solver.post(IntConstraintFactory.sum(M[i][j], VariableFactory.bool("sum", solver)));
            }
        }

        // break symmetries on first group
        for (int i = 1; i < p; i++) {
            solver.post(IntConstraintFactory.lex_less_eq(P[i][0], P[i - 1][0]));
        }
    }

    @Override
    public void configureSearch() {
        BoolVar[] vars = ArrayUtils.flatten(P);
        solver.set(IntStrategyFactory.lexico_UB(vars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        System.out.println(String.format("Social golfer(%d,%d,%d)", g, s, w));
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == ESat.TRUE) {
            int p = g * s;
            for (int i = 0; i < w; i++) {
                st.append("\tWeek ").append(i + 1).append("\n");
                for (int j = 0; j < g; j++) {
                    st.append("\t\tGroup ").append(j + 1).append(": ");
                    for (int k = 0; k < p; k++) {
                        if (P[k][j][i].getValue() > 0) {
                            st.append(k).append(", ");
                        }
                    }
                    st.append("\n");
                }
                st.append("\n");
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new SocialGolfer().execute(args);
    }
}
