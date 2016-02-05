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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton.makeMultiResources;
import static org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton.makeSingleResource;
import static org.chocosolver.solver.constraints.nary.circuit.CircuitConf.LIGHT;
import static org.chocosolver.solver.trace.Chatterbox.showSolutions;

/**
 * BEWARE: 5_elements.rst SHOULD BE UPDATED ANYTIME THIS CLASS IS CHANGED
 *
 * @author Charles Prud'homme
 * @version choco
 */
public class IntConstraintExamples {

    @Test(groups="1s", timeOut=60000)
    public void arithm1() {
        Model model = new Model();
        IntVar X = model.intVar("X", 1, 4, false);
        model.arithm(X, ">", 2).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmember1() {
        Model model = new Model();
        IntVar X = model.intVar("X", 1, 4, false);
        model.member(X, new int[]{-2, -1, 0, 1, 2}).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmember2() {
        Model model = new Model();
        IntVar X = model.intVar("X", 1, 4, false);
        model.member(X, 2, 5).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testnotmember1() {
        Model model = new Model();
        IntVar X = model.intVar("X", 1, 4, false);
        model.notMember(X, new int[]{-2, -1, 0, 1, 2}).post();
        showSolutions(model);
        model.findAllSolutions();
    }


    @Test(groups="1s", timeOut=60000)
    public void testnotmember2() {
        Model model = new Model();
        IntVar X = model.intVar("X", 1, 4, false);
        model.notMember(X, 2, 5).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testabsolute() {
        Model model = new Model();
        IntVar X = model.intVar("X", 0, 2, false);
        IntVar Y = model.intVar("X", -6, 1, false);
        model.absolute(X, Y).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testarithm3() {
        Model model = new Model();
        IntVar X = model.intVar("X", 0, 2, false);
        IntVar Y = model.intVar("X", -6, 1, false);
        model.arithm(X, "<=", Y, "+", 1).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testdistance1() {
        Model model = new Model();
        IntVar X = model.intVar("X", 0, 2, false);
        IntVar Y = model.intVar("X", -3, 1, false);
        model.distance(X, Y, "=", 1).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testelement1() {
        Model model = new Model();
        IntVar V = model.intVar("V", -2, 2, false);
        IntVar I = model.intVar("I", 0, 5, false);
        model.element(V, new int[]{2, -2, 1, -1, 0}, I, 0).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsquare() {
        Model model = new Model();
        IntVar X = model.intVar("X", 0, 5, false);
        IntVar Y = model.intVar("Y", -1, 3, false);
        model.square(X, Y).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testtable1() {
        Model model = new Model();
        IntVar X = model.intVar("X", 0, 5, false);
        IntVar Y = model.intVar("Y", -1, 3, false);
        Tuples tuples = new Tuples(true);
        tuples.add(1, -2);
        tuples.add(1, 1);
        tuples.add(4, 2);
        tuples.add(1, 4);
        model.table(X, Y, tuples, "AC2001").post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testdistance2() {
        Model model = new Model();
        IntVar X = model.intVar("X", 1, 3, false);
        IntVar Y = model.intVar("Y", -1, 1, false);
        IntVar Z = model.intVar("Z", 2, 3, false);
        model.distance(X, Y, "<", Z).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testeucli_div() {
        Model model = new Model();
        IntVar X = model.intVar("X", 1, 3, false);
        IntVar Y = model.intVar("Y", -1, 1, false);
        IntVar Z = model.intVar("Z", 2, 3, false);
        model.div(X, Y, Z).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmaximum() {
        Model model = new Model();
        IntVar MAX = model.intVar("MAX", 1, 3, false);
        IntVar Y = model.intVar("Y", -1, 1, false);
        IntVar Z = model.intVar("Z", 2, 3, false);
        model.max(MAX, Y, Z).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testminimum() {
        Model model = new Model();
        IntVar MIN = model.intVar("MIN", 1, 3, false);
        IntVar Y = model.intVar("Y", -1, 1, false);
        IntVar Z = model.intVar("Z", 2, 3, false);
        model.min(MIN, Y, Z).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmod() {
        Model model = new Model();
        IntVar X = model.intVar("X", 2, 4, false);
        IntVar Y = model.intVar("Y", -1, 4, false);
        IntVar Z = model.intVar("Z", 1, 3, false);
        model.mod(X, Y, Z).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testtimes() {
        Model model = new Model();
        IntVar X = model.intVar("X", -1, 2, false);
        IntVar Y = model.intVar("Y", 2, 4, false);
        IntVar Z = model.intVar("Z", 5, 7, false);
        model.times(X, Y, Z).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testalldifferent() {
        Model model = new Model();
        IntVar W = model.intVar("W", 0, 1, false);
        IntVar X = model.intVar("X", -1, 2, false);
        IntVar Y = model.intVar("Y", 2, 4, false);
        IntVar Z = model.intVar("Z", 5, 7, false);
        model.allDifferent(new IntVar[]{W, X, Y, Z}).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testalldifferent_cond() {
        Model model = new Model();
        IntVar[] XS = model.intVarArray("XS", 5, 0, 3, false);
        model.allDifferentUnderCondition(XS,
                x -> !x.contains(1) && !x.contains(3)).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testalldifferent_exc0() {
        Model model = new Model();
        IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        model.allDifferentExcept0(XS).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testamong() {
        Model model = new Model();
        IntVar N = model.intVar("N", 2, 3, false);
        IntVar[] XS = model.intVarArray("XS", 4, 0, 6, false);
        model.among(N, XS, new int[]{1, 2, 3}).post();
        showSolutions(model);
        model.findAllSolutions();

    }

    @Test(groups="1s", timeOut=60000)
    public void testatleast_nvalues() {
        Model model = new Model();
        IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        IntVar N = model.intVar("N", 2, 3, false);
        model.atLeastNValues(XS, N, true).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testatmost_nvalues() {
        Model model = new Model();
        IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        IntVar N = model.intVar("N", 1, 3, false);
        model.atMostNVvalues(XS, N, false).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbin_packing() {
        Model model = new Model();
        IntVar[] IBIN = model.intVarArray("IBIN", 5, 1, 3, false);
        int[] sizes = new int[]{2, 3, 1, 4, 2};
        IntVar[] BLOADS = model.intVarArray("BLOADS", 3, 0, 5, false);
        model.binPacking(IBIN, sizes, BLOADS, 1).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testboolean_channeling() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BVARS", 5);
        IntVar VAR = model.intVar("VAR", 1, 5, false);
        model.boolsIntChanneling(BVARS, VAR, 1).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testcircuit() {
        Model model = new Model();
        IntVar[] NODES = model.intVarArray("NODES", 5, 0, 4, false);
        model.circuit(NODES, 0, LIGHT).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testcost_regular() {
        Model model = new Model();
        IntVar[] VARS = model.intVarArray("VARS", 5, 0, 2, false);
        IntVar COST = model.intVar("COST", 0, 10, false);
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

        model.costRegular(VARS, COST, makeSingleResource(fauto, costs, COST.getLB(), COST.getUB())).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testcount() {
        Model model = new Model();
        IntVar[] VS = model.intVarArray("VS", 4, 0, 3, false);
        IntVar VA = model.intVar("VA", new int[]{1, 3});
        IntVar CO = model.intVar("CO", new int[]{0, 2, 4});
        model.count(VA, VS, CO).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testcumulative() {
        Model model = new Model();
        Task[] TS = new Task[5];
        IntVar[] HE = new IntVar[5];
        for (int i = 0; i < TS.length; i++) {
            IntVar S = model.intVar("S_" + i, 0, 4, true);
            TS[i] = new Task(
                    S,
                    model.intVar("D_" + i, i + 1),
                    model.intOffsetView(S, i + 1)
            );
            HE[i] = model.intVar("HE_" + i, i - 1, i + 1, true);
        }
        IntVar CA = model.intVar("CA", 1, 3, false);
        model.cumulative(TS, HE, CA, true).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testdiffn() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 4, 0, 1, true);
        IntVar[] Y = model.intVarArray("Y", 4, 0, 2, true);
        IntVar[] D = new IntVar[4];
        IntVar[] W = new IntVar[4];
        for (int i = 0; i < 4; i++) {
            D[i] = model.intVar("D_" + i, 1);
            W[i] = model.intVar("W_" + i, i + 1);
        }
        model.diffN(X, Y, D, W, true).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testglobal_cardinality() {
        Model model = new Model();
        IntVar[] VS = model.intVarArray("VS", 4, 0, 4, true);
        int[] values = new int[]{-1, 1, 2};
        IntVar[] OCC = model.intVarArray("OCC", 3, 0, 2, true);
        model.globalCardinality(VS, values, OCC, true).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testinverse_channeling() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, 0, 3, false);
        IntVar[] Y = model.intVarArray("Y", 3, 1, 4, false);
        model.inverseChanneling(X, Y, 0, 1).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testknapsack() {
        Model model = new Model();
        IntVar[] IT = new IntVar[3]; // 3 items
        IT[0] = model.intVar("IT_0", 0, 3, true);
        IT[1] = model.intVar("IT_1", 0, 2, true);
        IT[2] = model.intVar("IT_2", 0, 1, true);
        IntVar WE = model.intVar("WE", 0, 8, true);
        IntVar EN = model.intVar("EN", 0, 6, true);
        int[] weights = new int[]{1, 3, 4};
        int[] energies = new int[]{1, 4, 6};
        model.knapsack(IT, WE, EN, weights, energies).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testlex_chain_less() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, -1, 1, false);
        IntVar[] Y = model.intVarArray("Y", 3, 1, 2, false);
        IntVar[] Z = model.intVarArray("Z", 3, 0, 2, false);
        model.lexChainLess(X, Y, Z).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testlex_chain_less_eq() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, -1, 1, false);
        IntVar[] Y = model.intVarArray("Y", 3, 1, 2, false);
        IntVar[] Z = model.intVarArray("Z", 3, 0, 2, false);
        model.lexChainLessEq(X, Y, Z).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testlex_less() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, -1, 1, false);
        IntVar[] Y = model.intVarArray("Y", 3, 1, 2, false);
        model.lexLess(X, Y).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testlex_less_eq() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, -1, 1, false);
        IntVar[] Y = model.intVarArray("Y", 3, 1, 2, false);
        model.lexLessEq(X, Y).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testmulticost_regular() {
        Model model = new Model();
        IntVar[] VARS = model.intVarArray("VARS", 5, 0, 2, false);
        IntVar[] CVARS = model.intVarArray("CVARS", 5, 0, 10, false);
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

        model.multiCostRegular(VARS, CVARS, makeMultiResources(fauto, costs, CVARS)).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testnvalues() {
        Model model = new Model();
        IntVar[] VS = model.intVarArray("VS", 4, 0, 2, false);
        IntVar N = model.intVar("N", 0, 3, false);
        model.nValues(VS, N).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testpath() {
        Model model = new Model();
        IntVar[] VS = model.intVarArray("VS", 4, 0, 4, false);
        IntVar S = model.intVar("S", 0, 3, false);
        IntVar E = model.intVar("E", 0, 3, false);
        model.path(VS, S, E, 0).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testregular() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 4, 1, 5, false);
        model.regular(CS,
                new FiniteAutomaton("(1|2)(3*)(4|5)")).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testscalar() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 4, 1, 4, false);
        int[] coeffs = new int[]{1, 2, 3, 4};
        IntVar R = model.intVar("R", 0, 20, true);
        model.scalar(CS, coeffs, "=", R).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsort() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 3, 0, 2, false);
        IntVar[] Y = model.intVarArray("Y", 3, 0, 2, false);
        model.sort(X, Y).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsubcircuit() {
        Model model = new Model();
        IntVar[] NODES = model.intVarArray("NS", 5, 0, 4, false);
        IntVar SI = model.intVar("SI", 2, 3, false);
        model.subCircuit(NODES, 0, SI).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsubpath() {
        Model model = new Model();
        IntVar[] VS = model.intVarArray("VS", 4, 0, 4, false);
        IntVar S = model.intVar("S", 0, 3, false);
        IntVar E = model.intVar("E", 0, 3, false);
        IntVar SI = model.intVar("SI", 2, 3, false);
        model.subPath(VS, S, E, 0, SI).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testsum() {
        Model model = new Model();
        IntVar[] VS = model.intVarArray("VS", 4, 0, 4, false);
        IntVar SU = model.intVar("SU", 2, 3, false);
        model.sum(VS, "<=", SU).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testtree() {
        Model model = new Model();
        IntVar[] VS = model.intVarArray("VS", 4, 0, 4, false);
        IntVar NT = model.intVar("NT", 2, 3, false);
        model.tree(VS, NT, 0).post();
        showSolutions(model);
        model.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testbit_channeling() {
        Model model = new Model();
        BoolVar[] BVARS = model.boolVarArray("BVARS", 4);
        IntVar VAR = model.intVar("VAR", 0, 15, false);
        model.bitsIntChanneling(BVARS, VAR).post();
        showSolutions(model);
        model.findAllSolutions();
    }
}
