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
package org.chocosolver.samples;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/04/11
 */
public class DecomposedAllDifferent extends AbstractProblem {

    @Option(name = "-n", usage = "Number of variables.", required = false)
    int n = 5;

    IntVar[] X;
    BoolVar[] B;

    @Override
    public void createSolver() {
        solver = new Solver("Decomp allDiff");
    }


    @Override
    public void buildModel() {
        int i = n;
        X = VariableFactory.enumeratedArray("v", n, 0, n, solver);
        int[] union = new int[n];
        for (int j = 0; j < i; j++) {
            union[j] = j;
        }

        int l = union[0];
        int u = union[union.length - 1];

        BoolVar[][][] mA = new BoolVar[i][][];
        List<BoolVar> listA = new ArrayList<>();
//                List<BoolVar> Blist = new ArrayList<BoolVar>();
        for (int j = 0; j < i; j++) {
            mA[j] = new BoolVar[u - l + 1][];
            for (int p = l; p <= u; p++) {
                mA[j][p - l] = new BoolVar[u - p + 1];
//                        BoolVar b = VariableFactory.bool("B" + j + "_" + p, solver);
//                        Blist.add(b);
//                        Constraint cB = ConstraintFactory.leq(X[j], l, solver, eng2);
//                        Constraint ocB = ConstraintFactory.geq(X[j], l + 1, solver, eng2);
//                        lcstrs.add(new ReifiedConstraint(b, cB, ocB, solver, eng2));
                for (int q = p; q <= u; q++) {
                    BoolVar a = VariableFactory.bool("A" + j + "_" + p + "_" + q, solver);
                    mA[j][p - l][q - p] = a;
                    listA.add(a);

                    Constraint cA = IntConstraintFactory.member(X[j], p, q);
                    Constraint ocA = IntConstraintFactory.not_member(X[j], p, q);

                    LogicalConstraintFactory.ifThenElse(a, cA, ocA);
                }
            }
        }
//                BoolVar[] B =  Blist.toArray(new BoolVar[Blist.size()]);

        ArrayList<ArrayList<ArrayList<BoolVar>>> apmA = new ArrayList<>();

        for (int p = l; p <= u; p++) {
            apmA.add(p - l, new ArrayList<>());
            for (int q = p; q <= u; q++) {
                apmA.get(p - l).add(q - p, new ArrayList<>());
                for (int j = 0; j < i; j++) {
                    apmA.get(p - l).get(q - p).add(mA[j][p - l][q - p]);
                }
            }
        }


        for (int p = l; p <= u; p++) {
            for (int q = p; q <= u; q++) {
                BoolVar[] ai = null;
                for (int j = 0; j < i; j++) {
                    ai = apmA.get(p - l).get(q - p).toArray(new BoolVar[apmA.get(p - l).get(q - p).size()]);
                }
                solver.post(IntConstraintFactory.sum(ai, VariableFactory.bounded("scal", 0, q - p + 1, solver)));
            }
        }
        B = listA.toArray(new BoolVar[listA.size()]);
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.lexico_LB(X));
        /*IPropagationEngine engine = solver.getEngine();
        engine.addGroup(
                Group.buildGroup(
                        Predicates.member(B),
                        IncrArityV.get(),
                        Policy.ITERATE
                ));

        // default group
        engine.addGroup(
                Group.buildGroup(
                        Predicates.all(),
                        IncrArityP.get(),
                        Policy.ITERATE
                ));*/
    }

    @Override
    public void solve() {
        //solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) solver.getVars()[m - 1]);
        solver.findAllSolutions();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new DecomposedAllDifferent().execute(args);
    }
}
