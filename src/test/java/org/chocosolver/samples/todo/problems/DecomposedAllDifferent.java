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
package org.chocosolver.samples.todo.problems;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;

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
    public void buildModel() {
        model = new Model();
        int i = n;
        X = model.intVarArray("v", n, 0, n, false);
        int[] union = new int[n];
        for (int j = 0; j < i; j++) {
            union[j] = j;
        }

        int l = union[0];
        int u = union[union.length - 1];

        BoolVar[][][] mA = new BoolVar[i][][];
        List<BoolVar> listA = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            mA[j] = new BoolVar[u - l + 1][];
            for (int p = l; p <= u; p++) {
                mA[j][p - l] = new BoolVar[u - p + 1];
                for (int q = p; q <= u; q++) {
                    BoolVar a = model.boolVar("A" + j + "_" + p + "_" + q);
                    mA[j][p - l][q - p] = a;
                    listA.add(a);

                    Constraint cA = model.member(X[j], p, q);
                    Constraint ocA = model.notMember(X[j], p, q);

                    model.ifThenElse(a, cA, ocA);
                }
            }
        }

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
                model.sum(ai, "=", model.intVar("scal", 0, q - p + 1, true)).post();
            }
        }
        B = listA.toArray(new BoolVar[listA.size()]);
    }

    @Override
    public void configureSearch() {
        model.getSolver().set(inputOrderLBSearch(X));
        /*IPropagationEngine engine = model.getResolver().getEngine();;
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
        while (model.getSolver().solve()) ;
    }

    public static void main(String[] args) {
        new DecomposedAllDifferent().execute(args);
    }
}
