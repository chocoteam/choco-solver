/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.docs;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.Condition;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.circuit.CircuitConf;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;
import org.testng.annotations.Test;

/**
 * BEWARE: 5_elements.rst SHOULD BE UPDATED ANYTIME THIS CLASS IS CHANGED
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 16/09/2014
 */
public class IntConstraintExamples {

    @Test
    public void arithm1() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 1, 4, solver);
        solver.post(ICF.arithm(X, ">", 2));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testmember1() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 1, 4, solver);
        solver.post(ICF.member(X, new int[]{-2, -1, 0, 1, 2}));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testmember2() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 1, 4, solver);
        solver.post(ICF.member(X, 2, 5));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testnotmember1() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 1, 4, solver);
        solver.post(ICF.not_member(X, new int[]{-2, -1, 0, 1, 2}));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }


    @Test(groups = "1s")
    public void testnotmember2() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 1, 4, solver);
        solver.post(ICF.not_member(X, 2, 5));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testabsolute() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 0, 2, solver);
        IntVar Y = VF.enumerated("X", -6, 1, solver);
        solver.post(ICF.absolute(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testarithm3() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 0, 2, solver);
        IntVar Y = VF.enumerated("X", -6, 1, solver);
        solver.post(ICF.arithm(X, "<=", Y, "+", 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testdistance1() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 0, 2, solver);
        IntVar Y = VF.enumerated("X", -3, 1, solver);
        solver.post(ICF.distance(X, Y, "=", 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testelement1() {
        Solver solver = new Solver();
        IntVar V = VF.enumerated("V", -2, 2, solver);
        IntVar I = VF.enumerated("I", 0, 5, solver);
        solver.post(ICF.element(V, new int[]{2, -2, 1, -1, 0}, I, 0, "none"));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testsquare() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 0, 5, solver);
        IntVar Y = VF.enumerated("Y", -1, 3, solver);
        solver.post(ICF.square(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testtable1() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 0, 5, solver);
        IntVar Y = VF.enumerated("Y", -1, 3, solver);
        Tuples tuples = new Tuples(true);
        tuples.add(1, -2);
        tuples.add(1, 1);
        tuples.add(4, 2);
        tuples.add(1, 4);
        solver.post(ICF.table(X, Y, tuples, "AC2001"));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testdistance2() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 1, 3, solver);
        IntVar Y = VF.enumerated("Y", -1, 1, solver);
        IntVar Z = VF.enumerated("Z", 2, 3, solver);
        solver.post(ICF.distance(X, Y, "<", Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testeucli_div() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 1, 3, solver);
        IntVar Y = VF.enumerated("Y", -1, 1, solver);
        IntVar Z = VF.enumerated("Z", 2, 3, solver);
        solver.post(ICF.eucl_div(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testmaximum() {
        Solver solver = new Solver();
        IntVar MAX = VF.enumerated("MAX", 1, 3, solver);
        IntVar Y = VF.enumerated("Y", -1, 1, solver);
        IntVar Z = VF.enumerated("Z", 2, 3, solver);
        solver.post(ICF.maximum(MAX, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testminimum() {
        Solver solver = new Solver();
        IntVar MIN = VF.enumerated("MIN", 1, 3, solver);
        IntVar Y = VF.enumerated("Y", -1, 1, solver);
        IntVar Z = VF.enumerated("Z", 2, 3, solver);
        solver.post(ICF.minimum(MIN, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testmod() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", 2, 4, solver);
        IntVar Y = VF.enumerated("Y", -1, 4, solver);
        IntVar Z = VF.enumerated("Z", 1, 3, solver);
        solver.post(ICF.mod(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testtimes() {
        Solver solver = new Solver();
        IntVar X = VF.enumerated("X", -1, 2, solver);
        IntVar Y = VF.enumerated("Y", 2, 4, solver);
        IntVar Z = VF.enumerated("Z", 5, 7, solver);
        solver.post(ICF.times(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testalldifferent() {
        Solver solver = new Solver();
        IntVar W = VF.enumerated("W", 0, 1, solver);
        IntVar X = VF.enumerated("X", -1, 2, solver);
        IntVar Y = VF.enumerated("Y", 2, 4, solver);
        IntVar Z = VF.enumerated("Z", 5, 7, solver);
        solver.post(ICF.alldifferent(new IntVar[]{W, X, Y, Z}));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testalldifferent_cond() {
        Solver solver = new Solver();
        IntVar[] XS = VF.enumeratedArray("XS", 5, 0, 3, solver);
        solver.post(ICF.alldifferent_conditionnal(XS,
                new Condition() {
                    @Override
                    public boolean holdOnVar(IntVar x) {
                        return !x.contains(1) && !x.contains(3);
                    }
                }));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testalldifferent_exc0() {
        Solver solver = new Solver();
        IntVar[] XS = VF.enumeratedArray("XS", 4, 0, 2, solver);
        solver.post(ICF.alldifferent_except_0(XS));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testamong() {
        Solver solver = new Solver();
        IntVar N = VF.enumerated("N", 2, 3, solver);
        IntVar[] XS = VF.enumeratedArray("XS", 4, 0, 6, solver);
        solver.post(ICF.among(N, XS, new int[]{1, 2, 3}));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();

    }

    @Test(groups = "1s")
    public void testatleast_nvalues() {
        Solver solver = new Solver();
        IntVar[] XS = VF.enumeratedArray("XS", 4, 0, 2, solver);
        IntVar N = VF.enumerated("N", 2, 3, solver);
        solver.post(ICF.atleast_nvalues(XS, N, true));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testatmost_nvalues() {
        Solver solver = new Solver();
        IntVar[] XS = VF.enumeratedArray("XS", 4, 0, 2, solver);
        IntVar N = VF.enumerated("N", 1, 3, solver);
        solver.post(ICF.atmost_nvalues(XS, N, false));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testbin_packing() {
        Solver solver = new Solver();
        IntVar[] IBIN = VF.enumeratedArray("IBIN", 5, 1, 3, solver);
        int[] sizes = new int[]{2, 3, 1, 4, 2};
        IntVar[] BLOADS = VF.enumeratedArray("BLOADS", 3, 0, 5, solver);
        solver.post(ICF.bin_packing(IBIN, sizes, BLOADS, 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testboolean_channeling() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BVARS", 5, solver);
        IntVar VAR = VF.enumerated("VAR", 1, 5, solver);
        solver.post(ICF.boolean_channeling(BVARS, VAR, 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testcircuit() {
        Solver solver = new Solver();
        IntVar[] NODES = VF.enumeratedArray("NODES", 5, 0, 4, solver);
        solver.post(ICF.circuit(NODES, 0, CircuitConf.LIGHT));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testcost_regular() {
        Solver solver = new Solver();
        IntVar[] VARS = VF.enumeratedArray("VARS", 5, 0, 2, solver);
        IntVar COST = VF.enumerated("COST", 0, 10, solver);
        FiniteAutomaton fauto = new FiniteAutomaton();
        int start = fauto.addState();
        int end = fauto.addState();
        fauto.setInitialState(start);
        fauto.setFinal(start, end);

        fauto.addTransition(start, start, 0, 1);
        fauto.addTransition(start, end, 2);

        fauto.addTransition(end, end, 1);
        fauto.addTransition(end, start, 0, 2);

        int[][] costs = new int[5][3];
        costs[0] = new int[]{1, 2, 3};
        costs[1] = new int[]{2, 3, 1};
        costs[2] = new int[]{3, 1, 2};
        costs[3] = new int[]{3, 2, 1};
        costs[4] = new int[]{2, 1, 3};

        solver.post(ICF.cost_regular(VARS, COST, CostAutomaton.makeSingleResource(fauto, costs, COST.getLB(), COST.getUB())));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testcount() {
        Solver solver = new Solver();
        IntVar[] VS = VF.enumeratedArray("VS", 4, 0, 3, solver);
        IntVar VA = VF.enumerated("VA", new int[]{1, 3}, solver);
        IntVar CO = VF.enumerated("CO", new int[]{0, 2, 4}, solver);
        solver.post(ICF.count(VA, VS, CO));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testcumulative() {
        Solver solver = new Solver();
        Task[] TS = new Task[5];
        IntVar[] HE = new IntVar[5];
        for (int i = 0; i < TS.length; i++) {
            IntVar S = VF.bounded("S_" + i, 0, 4, solver);
            TS[i] = VF.task(
                    S,
                    VF.fixed("D_" + i, i + 1, solver),
                    VF.offset(S, i + 1)
            );
            HE[i] = VF.bounded("HE_" + i, i - 1, i + 1, solver);
        }
        IntVar CA = VF.enumerated("CA", 1, 3, solver);
        solver.post(ICF.cumulative(TS, HE, CA, true));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testdiffn() {
        Solver solver = new Solver();
        IntVar[] X = VF.boundedArray("X", 4, 0, 1, solver);
        IntVar[] Y = VF.boundedArray("Y", 4, 0, 2, solver);
        IntVar[] D = new IntVar[4];
        IntVar[] W = new IntVar[4];
        for (int i = 0; i < 4; i++) {
            D[i] = VF.fixed("D_" + i, 1, solver);
            W[i] = VF.fixed("W_" + i, i + 1, solver);
        }
        solver.post(ICF.diffn(X, Y, D, W, true));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testglobal_cardinality() {
        Solver solver = new Solver();
        IntVar[] VS = VF.boundedArray("VS", 4, 0, 4, solver);
        int[] values = new int[]{-1, 1, 2};
        IntVar[] OCC = VF.boundedArray("OCC", 3, 0, 2, solver);
        solver.post(ICF.global_cardinality(VS, values, OCC, true));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testinverse_channeling() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 3, 0, 3, solver);
        IntVar[] Y = VF.enumeratedArray("Y", 3, 1, 4, solver);
        solver.post(ICF.inverse_channeling(X, Y, 0, 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testknapsack() {
        Solver solver = new Solver();
        IntVar[] IT = new IntVar[3]; // 3 items
        IT[0] = VF.bounded("IT_0", 0, 3, solver);
        IT[1] = VF.bounded("IT_1", 0, 2, solver);
        IT[2] = VF.bounded("IT_2", 0, 1, solver);
        IntVar WE = VF.bounded("WE", 0, 8, solver);
        IntVar EN = VF.bounded("EN", 0, 6, solver);
        int[] weights = new int[]{1, 3, 4};
        int[] energies = new int[]{1, 4, 6};
        solver.post(ICF.knapsack(IT, WE, EN, weights, energies));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testlex_chain_less() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 3, -1, 1, solver);
        IntVar[] Y = VF.enumeratedArray("Y", 3, 1, 2, solver);
        IntVar[] Z = VF.enumeratedArray("Z", 3, 0, 2, solver);
        solver.post(ICF.lex_chain_less(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testlex_chain_less_eq() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 3, -1, 1, solver);
        IntVar[] Y = VF.enumeratedArray("Y", 3, 1, 2, solver);
        IntVar[] Z = VF.enumeratedArray("Z", 3, 0, 2, solver);
        solver.post(ICF.lex_chain_less_eq(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testlex_less() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 3, -1, 1, solver);
        IntVar[] Y = VF.enumeratedArray("Y", 3, 1, 2, solver);
        solver.post(ICF.lex_less(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testlex_less_eq() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 3, -1, 1, solver);
        IntVar[] Y = VF.enumeratedArray("Y", 3, 1, 2, solver);
        solver.post(ICF.lex_less_eq(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testmulticost_regular() {
        Solver solver = new Solver();
        IntVar[] VARS = VF.enumeratedArray("VARS", 5, 0, 2, solver);
        IntVar[] CVARS = VF.enumeratedArray("CVARS", 5, 0, 10, solver);
        FiniteAutomaton fauto = new FiniteAutomaton();
        int start = fauto.addState();
        int end = fauto.addState();
        fauto.setInitialState(start);
        fauto.setFinal(start, end);

        fauto.addTransition(start, start, 0, 1);
        fauto.addTransition(start, end, 2);

        fauto.addTransition(end, end, 1);
        fauto.addTransition(end, start, 0, 2);

        int[][][] costs = new int[5][3][];
//        costs[0] = new int[]{1, 2, 3};
//        costs[1] = new int[]{2, 3, 1};
//        costs[2] = new int[]{3, 1, 2};
//        costs[3] = new int[]{3, 2, 1};
//        costs[4] = new int[]{2, 1, 3};

        solver.post(ICF.multicost_regular(VARS, CVARS, CostAutomaton.makeMultiResources(fauto, costs, CVARS)));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testnvalues() {
        Solver solver = new Solver();
        IntVar[] VS = VF.enumeratedArray("VS", 4, 0, 2, solver);
        IntVar N = VF.enumerated("N", 0, 3, solver);
        solver.post(ICF.nvalues(VS, N));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testpath() {
        Solver solver = new Solver();
        IntVar[] VS = VF.enumeratedArray("VS", 4, 0, 4, solver);
        IntVar S = VF.enumerated("S", 0, 3, solver);
        IntVar E = VF.enumerated("E", 0, 3, solver);
        solver.post(ICF.path(VS, S, E, 0));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testregular() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 4, 1, 5, solver);
        solver.post(ICF.regular(CS,
                new FiniteAutomaton("(1|2)(3*)(4|5)")));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testscalar() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 4, 1, 4, solver);
        int[] coeffs = new int[]{1, 2, 3, 4};
        IntVar R = VF.bounded("R", 0, 20, solver);
        solver.post(ICF.scalar(CS, coeffs, R));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testsort() {
        Solver solver = new Solver();
        IntVar[] X = VF.enumeratedArray("X", 3, 0, 2, solver);
        IntVar[] Y = VF.enumeratedArray("Y", 3, 0, 2, solver);
        solver.post(ICF.sort(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testsubcircuit() {
        Solver solver = new Solver();
        IntVar[] NODES = VF.enumeratedArray("NS", 5, 0, 4, solver);
        IntVar SI = VF.enumerated("SI", 2, 3, solver);
        solver.post(ICF.subcircuit(NODES, 0, SI));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testsubpath() {
        Solver solver = new Solver();
        IntVar[] VS = VF.enumeratedArray("VS", 4, 0, 4, solver);
        IntVar S = VF.enumerated("S", 0, 3, solver);
        IntVar E = VF.enumerated("E", 0, 3, solver);
        IntVar SI = VF.enumerated("SI", 2, 3, solver);
        solver.post(ICF.subpath(VS, S, E, 0, SI));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testsum() {
        Solver solver = new Solver();
        IntVar[] VS = VF.enumeratedArray("VS", 4, 0, 4, solver);
        IntVar SU = VF.enumerated("SU", 2, 3, solver);
        solver.post(ICF.sum(VS, "<=", SU));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testtree() {
        Solver solver = new Solver();
        IntVar[] VS = VF.enumeratedArray("VS", 4, 0, 4, solver);
        IntVar NT = VF.enumerated("NT", 2, 3, solver);
        solver.post(ICF.tree(VS, NT, 0));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testtsp() {
        Solver solver = new Solver();
        IntVar[] VS = VF.enumeratedArray("VS", 4, 0, 4, solver);
        IntVar CO = VF.enumerated("CO", 0, 15, solver);
        int[][] costs = new int[][]{{0, 1, 3, 7}, {1, 0, 1, 3}, {3, 1, 0, 1}, {7, 3, 1, 0}};
        solver.post(ICF.tsp(VS, CO, costs));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testbit_channeling() {
        Solver solver = new Solver();
        BoolVar[] BVARS = VF.boolArray("BVARS", 4, solver);
        IntVar VAR = VF.enumerated("VAR", 0, 15, solver);
        solver.post(ICF.bit_channeling(BVARS, VAR));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }
}
