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
package org.chocosolver.docs;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.circuit.CircuitConf;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.annotations.Test;

/**
 * BEWARE: 5_elements.rst SHOULD BE UPDATED ANYTIME THIS CLASS IS CHANGED
 *
 * @author Charles Prud'homme
 * @version choco
 */
public class IntConstraintExamples {

    @Test(groups="1s", timeOut=60000)
    public void arithm1() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 1, 4, false);
        solver.post(solver.arithm(X, ">", 2));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmember1() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 1, 4, false);
        solver.post(solver.member(X, new int[]{-2, -1, 0, 1, 2}));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmember2() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 1, 4, false);
        solver.post(solver.member(X, 2, 5));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testnotmember1() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 1, 4, false);
        solver.post(solver.notMember(X, new int[]{-2, -1, 0, 1, 2}));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }


    @Test(groups="1s", timeOut=60000)
    public void testnotmember2() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 1, 4, false);
        solver.post(solver.notMember(X, 2, 5));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testabsolute() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 0, 2, false);
        IntVar Y = solver.intVar("X", -6, 1, false);
        solver.post(solver.absolute(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testarithm3() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 0, 2, false);
        IntVar Y = solver.intVar("X", -6, 1, false);
        solver.post(solver.arithm(X, "<=", Y, "+", 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testdistance1() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 0, 2, false);
        IntVar Y = solver.intVar("X", -3, 1, false);
        solver.post(solver.distance(X, Y, "=", 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testelement1() {
        Solver solver = new Solver();
        IntVar V = solver.intVar("V", -2, 2, false);
        IntVar I = solver.intVar("I", 0, 5, false);
        solver.post(solver.element(V, new int[]{2, -2, 1, -1, 0}, I, 0));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsquare() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 0, 5, false);
        IntVar Y = solver.intVar("Y", -1, 3, false);
        solver.post(solver.square(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testtable1() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 0, 5, false);
        IntVar Y = solver.intVar("Y", -1, 3, false);
        Tuples tuples = new Tuples(true);
        tuples.add(1, -2);
        tuples.add(1, 1);
        tuples.add(4, 2);
        tuples.add(1, 4);
        solver.post(solver.table(X, Y, tuples, "AC2001"));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testdistance2() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 1, 3, false);
        IntVar Y = solver.intVar("Y", -1, 1, false);
        IntVar Z = solver.intVar("Z", 2, 3, false);
        solver.post(solver.distance(X, Y, "<", Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testeucli_div() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 1, 3, false);
        IntVar Y = solver.intVar("Y", -1, 1, false);
        IntVar Z = solver.intVar("Z", 2, 3, false);
        solver.post(solver.div(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmaximum() {
        Solver solver = new Solver();
        IntVar MAX = solver.intVar("MAX", 1, 3, false);
        IntVar Y = solver.intVar("Y", -1, 1, false);
        IntVar Z = solver.intVar("Z", 2, 3, false);
        solver.post(solver.max(MAX, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testminimum() {
        Solver solver = new Solver();
        IntVar MIN = solver.intVar("MIN", 1, 3, false);
        IntVar Y = solver.intVar("Y", -1, 1, false);
        IntVar Z = solver.intVar("Z", 2, 3, false);
        solver.post(solver.min(MIN, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmod() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", 2, 4, false);
        IntVar Y = solver.intVar("Y", -1, 4, false);
        IntVar Z = solver.intVar("Z", 1, 3, false);
        solver.post(solver.mod(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testtimes() {
        Solver solver = new Solver();
        IntVar X = solver.intVar("X", -1, 2, false);
        IntVar Y = solver.intVar("Y", 2, 4, false);
        IntVar Z = solver.intVar("Z", 5, 7, false);
        solver.post(solver.times(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testalldifferent() {
        Solver solver = new Solver();
        IntVar W = solver.intVar("W", 0, 1, false);
        IntVar X = solver.intVar("X", -1, 2, false);
        IntVar Y = solver.intVar("Y", 2, 4, false);
        IntVar Z = solver.intVar("Z", 5, 7, false);
        solver.post(solver.allDifferent(new IntVar[]{W, X, Y, Z}));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testalldifferent_cond() {
        Solver solver = new Solver();
        IntVar[] XS = solver.intVarArray("XS", 5, 0, 3, false);
        solver.post(solver.allDifferentUnderCondition(XS,
                x -> !x.contains(1) && !x.contains(3)));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testalldifferent_exc0() {
        Solver solver = new Solver();
        IntVar[] XS = solver.intVarArray("XS", 4, 0, 2, false);
        solver.post(solver.allDifferentExcept0(XS));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testamong() {
        Solver solver = new Solver();
        IntVar N = solver.intVar("N", 2, 3, false);
        IntVar[] XS = solver.intVarArray("XS", 4, 0, 6, false);
        solver.post(solver.among(N, XS, new int[]{1, 2, 3}));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();

    }

    @Test(groups="1s", timeOut=60000)
    public void testatleast_nvalues() {
        Solver solver = new Solver();
        IntVar[] XS = solver.intVarArray("XS", 4, 0, 2, false);
        IntVar N = solver.intVar("N", 2, 3, false);
        solver.post(solver.atLeastNValues(XS, N, true));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testatmost_nvalues() {
        Solver solver = new Solver();
        IntVar[] XS = solver.intVarArray("XS", 4, 0, 2, false);
        IntVar N = solver.intVar("N", 1, 3, false);
        solver.post(solver.atMostNVvalues(XS, N, false));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbin_packing() {
        Solver solver = new Solver();
        IntVar[] IBIN = solver.intVarArray("IBIN", 5, 1, 3, false);
        int[] sizes = new int[]{2, 3, 1, 4, 2};
        IntVar[] BLOADS = solver.intVarArray("BLOADS", 3, 0, 5, false);
        solver.post(solver.binPacking(IBIN, sizes, BLOADS, 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolean_channeling() {
        Solver solver = new Solver();
        BoolVar[] BVARS = solver.boolVarArray("BVARS", 5);
        IntVar VAR = solver.intVar("VAR", 1, 5, false);
        solver.post(solver.boolsIntChanneling(BVARS, VAR, 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testcircuit() {
        Solver solver = new Solver();
        IntVar[] NODES = solver.intVarArray("NODES", 5, 0, 4, false);
        solver.post(solver.circuit(NODES, 0, CircuitConf.LIGHT));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testcost_regular() {
        Solver solver = new Solver();
        IntVar[] VARS = solver.intVarArray("VARS", 5, 0, 2, false);
        IntVar COST = solver.intVar("COST", 0, 10, false);
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

        solver.post(solver.costRegular(VARS, COST, CostAutomaton.makeSingleResource(fauto, costs, COST.getLB(), COST.getUB())));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testcount() {
        Solver solver = new Solver();
        IntVar[] VS = solver.intVarArray("VS", 4, 0, 3, false);
        IntVar VA = solver.intVar("VA", new int[]{1, 3});
        IntVar CO = solver.intVar("CO", new int[]{0, 2, 4});
        solver.post(solver.count(VA, VS, CO));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testcumulative() {
        Solver solver = new Solver();
        Task[] TS = new Task[5];
        IntVar[] HE = new IntVar[5];
        for (int i = 0; i < TS.length; i++) {
            IntVar S = solver.intVar("S_" + i, 0, 4, true);
            TS[i] = new Task(
                    S,
                    solver.intVar("D_" + i, i + 1),
                    solver.intOffsetView(S, i + 1)
            );
            HE[i] = solver.intVar("HE_" + i, i - 1, i + 1, true);
        }
        IntVar CA = solver.intVar("CA", 1, 3, false);
        solver.post(solver.cumulative(TS, HE, CA, true));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testdiffn() {
        Solver solver = new Solver();
        IntVar[] X = solver.intVarArray("X", 4, 0, 1, true);
        IntVar[] Y = solver.intVarArray("Y", 4, 0, 2, true);
        IntVar[] D = new IntVar[4];
        IntVar[] W = new IntVar[4];
        for (int i = 0; i < 4; i++) {
            D[i] = solver.intVar("D_" + i, 1);
            W[i] = solver.intVar("W_" + i, i + 1);
        }
        solver.post(solver.diffN(X, Y, D, W, true));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testglobal_cardinality() {
        Solver solver = new Solver();
        IntVar[] VS = solver.intVarArray("VS", 4, 0, 4, true);
        int[] values = new int[]{-1, 1, 2};
        IntVar[] OCC = solver.intVarArray("OCC", 3, 0, 2, true);
        solver.post(solver.globalCardinality(VS, values, OCC, true));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testinverse_channeling() {
        Solver solver = new Solver();
        IntVar[] X = solver.intVarArray("X", 3, 0, 3, false);
        IntVar[] Y = solver.intVarArray("Y", 3, 1, 4, false);
        solver.post(solver.inverseChanneling(X, Y, 0, 1));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testknapsack() {
        Solver solver = new Solver();
        IntVar[] IT = new IntVar[3]; // 3 items
        IT[0] = solver.intVar("IT_0", 0, 3, true);
        IT[1] = solver.intVar("IT_1", 0, 2, true);
        IT[2] = solver.intVar("IT_2", 0, 1, true);
        IntVar WE = solver.intVar("WE", 0, 8, true);
        IntVar EN = solver.intVar("EN", 0, 6, true);
        int[] weights = new int[]{1, 3, 4};
        int[] energies = new int[]{1, 4, 6};
        solver.post(solver.knapsack(IT, WE, EN, weights, energies));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testlex_chain_less() {
        Solver solver = new Solver();
        IntVar[] X = solver.intVarArray("X", 3, -1, 1, false);
        IntVar[] Y = solver.intVarArray("Y", 3, 1, 2, false);
        IntVar[] Z = solver.intVarArray("Z", 3, 0, 2, false);
        solver.post(solver.lexChainLess(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testlex_chain_less_eq() {
        Solver solver = new Solver();
        IntVar[] X = solver.intVarArray("X", 3, -1, 1, false);
        IntVar[] Y = solver.intVarArray("Y", 3, 1, 2, false);
        IntVar[] Z = solver.intVarArray("Z", 3, 0, 2, false);
        solver.post(solver.lexChainLessEq(X, Y, Z));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testlex_less() {
        Solver solver = new Solver();
        IntVar[] X = solver.intVarArray("X", 3, -1, 1, false);
        IntVar[] Y = solver.intVarArray("Y", 3, 1, 2, false);
        solver.post(solver.lexLess(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testlex_less_eq() {
        Solver solver = new Solver();
        IntVar[] X = solver.intVarArray("X", 3, -1, 1, false);
        IntVar[] Y = solver.intVarArray("Y", 3, 1, 2, false);
        solver.post(solver.lexLessEq(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmulticost_regular() {
        Solver solver = new Solver();
        IntVar[] VARS = solver.intVarArray("VARS", 5, 0, 2, false);
        IntVar[] CVARS = solver.intVarArray("CVARS", 5, 0, 10, false);
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

        solver.post(solver.multiCostRegular(VARS, CVARS, CostAutomaton.makeMultiResources(fauto, costs, CVARS)));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testnvalues() {
        Solver solver = new Solver();
        IntVar[] VS = solver.intVarArray("VS", 4, 0, 2, false);
        IntVar N = solver.intVar("N", 0, 3, false);
        solver.post(solver.nValues(VS, N));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testpath() {
        Solver solver = new Solver();
        IntVar[] VS = solver.intVarArray("VS", 4, 0, 4, false);
        IntVar S = solver.intVar("S", 0, 3, false);
        IntVar E = solver.intVar("E", 0, 3, false);
        solver.post(solver.path(VS, S, E, 0));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testregular() {
        Solver solver = new Solver();
        IntVar[] CS = solver.intVarArray("CS", 4, 1, 5, false);
        solver.post(solver.regular(CS,
                new FiniteAutomaton("(1|2)(3*)(4|5)")));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testscalar() {
        Solver solver = new Solver();
        IntVar[] CS = solver.intVarArray("CS", 4, 1, 4, false);
        int[] coeffs = new int[]{1, 2, 3, 4};
        IntVar R = solver.intVar("R", 0, 20, true);
        solver.post(solver.scalar(CS, coeffs, "=", R));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsort() {
        Solver solver = new Solver();
        IntVar[] X = solver.intVarArray("X", 3, 0, 2, false);
        IntVar[] Y = solver.intVarArray("Y", 3, 0, 2, false);
        solver.post(solver.sort(X, Y));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsubcircuit() {
        Solver solver = new Solver();
        IntVar[] NODES = solver.intVarArray("NS", 5, 0, 4, false);
        IntVar SI = solver.intVar("SI", 2, 3, false);
        solver.post(solver.subCircuit(NODES, 0, SI));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsubpath() {
        Solver solver = new Solver();
        IntVar[] VS = solver.intVarArray("VS", 4, 0, 4, false);
        IntVar S = solver.intVar("S", 0, 3, false);
        IntVar E = solver.intVar("E", 0, 3, false);
        IntVar SI = solver.intVar("SI", 2, 3, false);
        solver.post(solver.subPath(VS, S, E, 0, SI));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsum() {
        Solver solver = new Solver();
        IntVar[] VS = solver.intVarArray("VS", 4, 0, 4, false);
        IntVar SU = solver.intVar("SU", 2, 3, false);
        solver.post(solver.sum(VS, "<=", SU));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testtree() {
        Solver solver = new Solver();
        IntVar[] VS = solver.intVarArray("VS", 4, 0, 4, false);
        IntVar NT = solver.intVar("NT", 2, 3, false);
        solver.post(solver.tree(VS, NT, 0));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbit_channeling() {
        Solver solver = new Solver();
        BoolVar[] BVARS = solver.boolVarArray("BVARS", 4);
        IntVar VAR = solver.intVar("VAR", 0, 15, false);
        solver.post(solver.bitsIntChanneling(BVARS, VAR));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }
}
