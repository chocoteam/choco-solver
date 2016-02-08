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
package org.chocosolver.samples.integer; /**
 *
 * Stable marriage problem in Choco3
 *
 * Translation of the OPL version from
 * Pascal Van Hentenryck "The OPL Optimization Programming Language"
 * E.g.
 * http://www.comp.rgu.ac.uk/staff/ha/ZCSP/additional_problems/stable_marriage/stable_marriage.pdf
 *
 * Choco3 model by Hakan Kjellerstrand (hakank@gmail.com)
 * http://www.hakank.org/choco3/
 *
 */

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

import static java.lang.System.out;
import static org.chocosolver.solver.constraints.SatFactory.addClauses;
import static org.chocosolver.solver.constraints.nary.cnf.LogOp.implies;
import static org.chocosolver.util.tools.ArrayUtils.append;

public class StableMarriage extends AbstractProblem {

    @Option(name = "-problem", usage = "Problem instance (default 1.", required = false)
    int problem = 1;


    //
    // From Pascal Van Hentenryck's OPL book
    //
    int[][][] van_hentenryck = {
            // rankWomen
            {{1, 2, 4, 3, 5},
                    {3, 5, 1, 2, 4},
                    {5, 4, 2, 1, 3},
                    {1, 3, 5, 4, 2},
                    {4, 2, 3, 5, 1}},

            // rankMen
            {{5, 1, 2, 4, 3},
                    {4, 1, 3, 2, 5},
                    {5, 3, 2, 4, 1},
                    {1, 5, 4, 3, 2},
                    {4, 3, 2, 1, 5}}
    };


    //
    // Data from MathWorld
    // http://mathworld.wolfram.com/StableMarriageProblem.html
    //
    int[][][] mathworld = {
            // rankWomen
            {{3, 1, 5, 2, 8, 7, 6, 9, 4},
                    {9, 4, 8, 1, 7, 6, 3, 2, 5},
                    {3, 1, 8, 9, 5, 4, 2, 6, 7},
                    {8, 7, 5, 3, 2, 6, 4, 9, 1},
                    {6, 9, 2, 5, 1, 4, 7, 3, 8},
                    {2, 4, 5, 1, 6, 8, 3, 9, 7},
                    {9, 3, 8, 2, 7, 5, 4, 6, 1},
                    {6, 3, 2, 1, 8, 4, 5, 9, 7},
                    {8, 2, 6, 4, 9, 1, 3, 7, 5}},

            // rankMen
            {{7, 3, 8, 9, 6, 4, 2, 1, 5},
                    {5, 4, 8, 3, 1, 2, 6, 7, 9},
                    {4, 8, 3, 9, 7, 5, 6, 1, 2},
                    {9, 7, 4, 2, 5, 8, 3, 1, 6},
                    {2, 6, 4, 9, 8, 7, 5, 1, 3},
                    {2, 7, 8, 6, 5, 3, 4, 1, 9},
                    {1, 6, 2, 3, 8, 5, 4, 9, 7},
                    {5, 6, 9, 1, 2, 8, 4, 3, 7},
                    {6, 1, 4, 7, 5, 8, 3, 9, 2}}};

    //
    // Data from
    // http://www.csee.wvu.edu/~ksmani/courses/fa01/random/lecnotes/lecture5.pdf
    //
    int[][][] problem3 = {
            // rankWomen
            {{1, 2, 3, 4},
                    {4, 3, 2, 1},
                    {1, 2, 3, 4},
                    {3, 4, 1, 2}},

            // rankMen"
            {{1, 2, 3, 4},
                    {2, 1, 3, 4},
                    {1, 4, 3, 2},
                    {4, 3, 1, 2}}};


    //
    // Data from
    // http://www.comp.rgu.ac.uk/staff/ha/ZCSP/additional_problems/stable_marriage/stable_marriage.pdf
    // page 4
    //
    int[][][] problem4 = {
            // rankWomen
            {{1, 5, 4, 6, 2, 3},
                    {4, 1, 5, 2, 6, 3},
                    {6, 4, 2, 1, 5, 3},
                    {1, 5, 2, 4, 3, 6},
                    {4, 2, 1, 5, 6, 3},
                    {2, 6, 3, 5, 1, 4}},

            // rankMen
            {{1, 4, 2, 5, 6, 3},
                    {3, 4, 6, 1, 5, 2},
                    {1, 6, 4, 2, 3, 5},
                    {6, 5, 3, 4, 2, 1},
                    {3, 1, 2, 4, 5, 6},
                    {2, 3, 1, 6, 5, 4}}};


    int[][][] ranks;
    int n;

    IntVar[] wife;
    IntVar[] husband;

    @Override
    public void buildModel() {
        model = new Model();

        //
        // data
        //
        out.println("\n#####################");
        out.println("Problem: " + problem);

        switch (problem) {
            case 1:
                ranks = van_hentenryck;
                break;
            case 2:
                ranks = mathworld;
                break;
            case 3:
                ranks = problem3;
                break;
            case 4:
                ranks = problem4;
                break;
        }

        int[][] rankWomen = ranks[0];
        int[][] rankMen = ranks[1];

        n = rankWomen.length;

        //
        // variables
        //
        wife = model.intVarArray("wife", n, 0, n - 1, false);
        husband = model.intVarArray("husband", n, 0, n - 1, false);

        model.allDifferent(wife, "BC").post();
        model.allDifferent(husband, "BC").post();


        //
        // (the comments are the Comet code)
        //   forall(m in Men)
        //      cp.post(husband[wife[m]] == m);
        for (int m = 0; m < n; m++) {
      /*
      solver.addConstraint(
          solver.makeEquality(solver.makeElement(husband, wife[m]), m));
      */
            model.element(model.intVar(m), husband, wife[m], 0).post();
        }

        //   forall(w in Women)
        //     cp.post(wife[husband[w]] == w);
        for (int w = 0; w < n; w++) {
      /*
      solver.addConstraint(
          solver.makeEquality(solver.makeElement(wife, husband[w]), w));
      */
            model.element(model.intVar(w), wife, husband[w], 0).post();
        }


        // VALUE = TABLE[INDEX-OFFSET]
        // element(IntVar VALUE, IntVar[] TABLE, IntVar INDEX, int OFFSET)
        // element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET, String SORT)

        //   forall(m in Men, o in Women)
        //       cp.post(rankMen[m,o] < rankMen[m, wife[m]] =>
        //               rankWomen[o,husband[o]] < rankWomen[o,m]);
        for (int m = 0; m < n; m++) {
            for (int o = 0; o < n; o++) {
        /*
        IntVar b1 = solver.makeIsGreaterCstVar(
                        solver.makeElement(rankMen[m], wife[m]).var(),
                        rankMen[m][o]);
        */
                IntVar v1 = model.intVar("v1", 0, n - 1, false);
                model.element(v1, rankMen[m], wife[m], 0).post();

                BoolVar b1 = model.boolVar("b1");
                model.ifThenElse(b1,
                        model.arithm(v1, ">", rankMen[m][o]),
                        model.arithm(v1, "<=", rankMen[m][o])
                );


        /*
        IntVar b2 = solver.makeIsLessCstVar(
                        solver.makeElement(rankWomen[o], husband[o]).var(),
                        rankWomen[o][m]);
        */
                IntVar v2 = model.intVar("v2", 0, n - 1, false);
                model.element(v2, rankWomen[o], husband[o], 0).post();

                BoolVar b2 = model.boolVar("b2");
                model.ifThenElse(b2,
                        model.arithm(v2, "<", rankWomen[o][m]),
                        model.arithm(v2, ">=", rankWomen[o][m])
                );


                    
        /*
        solver.addConstraint(
            solver.makeLessOrEqual(
                solver.makeDifference(b1, b2), 0));
        */

                // b1 -> b2
                LogOp t = implies(b1, b2);
                addClauses(t, model);

                // solver.post(solver.arithm(b1, "<=", b2));


            }
        }

        //   forall(w in Women, o in Men)
        //      cp.post(rankWomen[w,o] < rankWomen[w,husband[w]] =>
        //              rankMen[o,wife[o]] < rankMen[o,w]);
        for (int w = 0; w < n; w++) {
            for (int o = 0; o < n; o++) {
        /*
        IntVar b1 = solver.makeIsGreaterCstVar(
                        solver.makeElement(rankWomen[w], husband[w]).var(),
                        rankWomen[w][o]);
        */
                IntVar v1 = model.intVar("v1", 0, n - 1, false);
                model.element(v1, rankWomen[w], husband[w], 0).post();

                BoolVar b1 = model.boolVar("b1");
                model.ifThenElse(b1,
                        model.arithm(v1, ">", rankWomen[w][o]),
                        model.arithm(v1, "<=", rankWomen[w][o])
                );


        /*
        IntVar b2 = solver.makeIsLessCstVar(
                        solver.makeElement(rankMen[o], wife[o]).var(),
                        rankMen[o][w]);
        */
                IntVar v2 = model.intVar("v2", 0, n - 1, false);
                model.element(v2, rankMen[o], wife[o], 0).post();

                BoolVar b2 = model.boolVar("b2");
                model.ifThenElse(b2,
                        model.arithm(v2, "<", rankMen[o][w]),
                        model.arithm(v2, ">=", rankMen[o][w])
                );


        /*
        solver.addConstraint(
            solver.makeLessOrEqual(
                solver.makeDifference(b1, b2), 0));
        }
        */

                // b1 -> b2
                LogOp t = implies(b1, b2);
                addClauses(t, model);

                // solver.post(solver.arithm(b1, "<=", b2));

            }

        }

    }

    @Override
    public void configureSearch() {
        model.getResolver().set(model.getResolver().minDomLBSearch(append(husband, wife)));
    }

    @Override
    public void solve() {
        model.solve();
    }

    @Override
    public void prettyOut() {

        if (model.getResolver().isFeasible() == ESat.TRUE) {
            int num_sols = 0;
            do {

                System.out.print("wife   : ");
                for (int i = 0; i < n; i++) {
                    System.out.print(wife[i].getValue() + " ");
                }
                System.out.println();
                System.out.print("husband: ");
                for (int i = 0; i < n; i++) {
                    System.out.print(husband[i].getValue() + " ");
                }
                System.out.println();

                num_sols++;

            } while (model.solve() == Boolean.TRUE);

            System.out.println("It was " + num_sols + " solutions.");

        } else {

            System.out.println("Problem is not feasible.");

        }


    }

  /*
wife   :  [1, 0, 4, 2, 3]
husband:  [1, 0, 3, 4, 2]

wife   :  [1, 2, 4, 0, 3]
husband:  [3, 0, 1, 4, 2]

wife   :  [3, 0, 1, 2, 4]
husband:  [1, 2, 3, 0, 4]

   */

    //
    // main
    //
    public static void main(String args[]) {

        new StableMarriage().execute(args);

    }
}

