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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/03/12
 */
public class OpenStacks extends AbstractProblem {

    @Option(name = "-d", aliases = "--data", usage = "Open stacks instance.", required = false)
    Data data = Data.small;

    int nc; // nb of customers
    int np; // nb of products
    int[][] orders; // which customer orders which product
    int[] norders; // nb of orders per customer
    IntVar[] scheds; // schedule of products
    IntVar[][] o; // orders fill after time t
    BoolVar[][] o2b;
    IntVar[] open; // schedule of products
    IntVar objective;


    public void setUp() {
        int k = 0;
        nc = data.data[k++];
        np = data.data[k++];
        orders = new int[nc][np];
        norders = new int[nc];
        for (int j = 0; j < nc; j++) {
            int s = 0;
            for (int i = 0; i < np; i++) {
                orders[j][i] = data.data[k++];
                s += orders[j][i];
            }
            norders[j] = s;
        }
    }

    @Override
    public void createSolver() {
        solver = new Solver("Open stacks");
    }

    @Override
    public void buildModel() {
        setUp();
        scheds = solver.makeIntVarArray("s", np, 0, np - 1, false);
        solver.post(IntConstraintFactory.alldifferent(scheds, "BC"));
        o = new IntVar[nc][np + 1];
        for (int i = 0; i < nc; i++) {
            o[i] = solver.makeIntVarArray("o_" + i, np + 1, 0, norders[i], false);
            // no order at t = 0
            solver.post(IntConstraintFactory.arithm(o[i][0], "=", 0));
        }
        for (int t = 1; t < np + 1; t++) {
            for (int i = 0; i < nc; i++) {
                // o[i,t] = o[i,t-1] + orders[i,s[t]] );
                IntVar value = solver.makeIntVar("val_" + t + "_" + i, 0, norders[i], false);
                solver.post(IntConstraintFactory.element(value, orders[i], scheds[t - 1], 0, "detect"));
                solver.post(IntConstraintFactory.sum(new IntVar[]{o[i][t - 1], value}, "=", o[i][t]));
            }
        }
        o2b = solver.makeBoolVarMatrix("b", np, nc);
        for (int i = 0; i < nc; i++) {
            for (int j = 1; j < np + 1; j++) {
                BoolVar[] btmp = solver.makeBoolVarArray("bT_" + i + "_" + j, 2);
                LogicalConstraintFactory.ifThenElse(btmp[0],
                        IntConstraintFactory.arithm(o[i][j - 1], "<", solver.makeIntVar(norders[i])),
                        IntConstraintFactory.arithm(o[i][j - 1], ">=", solver.makeIntVar(norders[i])));

                LogicalConstraintFactory.ifThenElse(btmp[1],
                        IntConstraintFactory.arithm(o[i][j], ">", solver.makeIntVar(0)),
                        IntConstraintFactory.arithm(o[i][j], "<=", solver.makeIntVar(0)));
                SatFactory.addClauses(LogOp.ifOnlyIf(o2b[j - 1][i], LogOp.and(btmp[0], btmp[1])), solver);
            }
        }
        open = solver.makeIntVarArray("open", np, 0, nc + 1, true);
        for (int i = 0; i < np; i++) {
            solver.post(IntConstraintFactory.sum(o2b[i], "=", open[i]));
        }


        objective = solver.makeIntVar("OBJ", 0, nc * np, true);
        solver.post(ICF.maximum(objective, open));
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_LB(scheds));
    }

    @Override
    public void solve() {
        SearchMonitorFactory.limitNode(solver, 200000);
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, objective);
    }

    @Override
    public void prettyOut() {
        StringBuilder st = new StringBuilder("Open stacks problem\n");
        st.append("\t");
        for (int i = 0; i < nc; i++) {
            for (int j = 0; j < np; j++) {
                st.append(orders[i][j]).append(" ");
            }
            st.append("(").append(norders[i]).append(")\n\t");
        }
        st.append("\n\t");
        if (solver.isFeasible() == ESat.TRUE) {
            for (int j = 0; j < np; j++) {
                st.append(scheds[j].getValue()).append(" ");
            }
            st.append("\n\n\t");
            for (int j = 0; j < np; j++) {
                for (int i = 0; i < nc; i++) {
                    st.append(o2b[j][i].getValue()).append(" ");
                }
                st.append(" ").append(open[j].getValue()).append("\n\t");
            }

            st.append("\n\t").append("OBJ:").append(objective.getValue());
        } else {
            st.append("INFEASIBLE");
        }
        //st.append(solver.toString());
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new OpenStacks().execute(args);
    }

    ////////////////////////////////////////// DATA ////////////////////////////////////////////////////////////////////
    static enum Data {
        V_small(new int[]{
                5, 6, //nb customers= 5, nb products = 6,
                // orders
                0, 0, 1, 0, 1, 0,
                0, 1, 0, 0, 0, 0,
                1, 0, 1, 1, 0, 0,
                1, 1, 0, 0, 0, 1,
                0, 0, 0, 1, 1, 1
        }),
        small(new int[]{
                7, 14,
                // orders
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1,
                1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1,
                0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 0,
                0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0,
                0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0,
                0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
                0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0,
                0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0,
                1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0,
        }),
        med(new int[]{
                10, 20,
                // orders
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1,
                1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1,
                0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 0,
                0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0,
                0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0,
                0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
                0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0,
                0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0,
                1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0,
        });
        final int[] data;

        Data(int[] data) {
            this.data = data;
        }

        public int get(int i) {
            return data[i];
        }
    }
}
