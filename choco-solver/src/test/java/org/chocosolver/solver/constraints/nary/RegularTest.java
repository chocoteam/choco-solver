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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class RegularTest {

    @Test(groups = "1s")
    public void testSimpleAuto() {
        Solver solver = new Solver();

        int n = 10;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }


        FiniteAutomaton auto = new FiniteAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        solver.post(IntConstraintFactory.regular(vars, auto));
        solver.set(IntStrategyFactory.lexico_LB(vars));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 59049);
    }

    @Test(groups = "1s")
    public void ccostregular2() {
        Solver solver = new Solver();

        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }

        // different rules are formulated as patterns that must NOT be matched by x
        List<String> forbiddenRegExps = new ArrayList<>();
        // do not end with '00' if start with '11'
        forbiddenRegExps.add("11(0|1|2)*00");
        // at most three consecutive 0
        forbiddenRegExps.add("(0|1|2)*0000(0|1|2)*");
        // no pattern '112' at position 5
        forbiddenRegExps.add("(0|1|2){4}112(0|1|2)*");
        // pattern '12' after a 0 or a sequence of 0
        forbiddenRegExps.add("(0|1|2)*02(0|1|2)*");
        forbiddenRegExps.add("(0|1|2)*01(0|1)(0|1|2)*");
        // at most three 2 on consecutive even positions
        forbiddenRegExps.add("(0|1|2)((0|1|2)(0|1|2))*2(0|1|2)2(0|1|2)2(0|1|2)*");

        // a unique automaton is built as the complement language composed of all the forbidden patterns
        FiniteAutomaton auto = new FiniteAutomaton();
        for (String reg : forbiddenRegExps) {
            FiniteAutomaton a = new FiniteAutomaton(reg);
            auto = auto.union(a);
            auto.minimize();
        }
        auto = auto.complement();
        auto.minimize();
        Assert.assertEquals(auto.getNbStates(), 54);

        solver.post(IntConstraintFactory.regular(vars, auto));
        solver.set(IntStrategyFactory.lexico_LB(vars));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 25980);
    }

    @Test(groups = "10s")
    public void isCorrect() {
        Solver solver = new Solver();

        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }

        FiniteAutomaton auto = new FiniteAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        solver.post(IntConstraintFactory.regular(vars, auto));
        solver.set(IntStrategyFactory.lexico_LB(vars));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 531441);
//        assertEquals(124927,s.getNodeCount());

    }

    @Test(groups = "10s")
    public void isCorrect2() {
        Solver solver = new Solver();

        int n = 13;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        FiniteAutomaton auto = new FiniteAutomaton();
        int start = auto.addState();
        int end = auto.addState();
        auto.setInitialState(start);
        auto.setFinal(start);
        auto.setFinal(end);

        auto.addTransition(start, start, 0, 1);
        auto.addTransition(start, end, 2);

        auto.addTransition(end, start, 2);
        auto.addTransition(end, start, 0, 1);

        solver.post(IntConstraintFactory.regular(vars, auto));
        solver.set(IntStrategyFactory.lexico_LB(vars));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1594323);
    }

    @Test(groups = "10s")
    public void compareVersionSpeedNew() {
        int n = 14;
        FiniteAutomaton auto = new FiniteAutomaton("(0|1|2)*(0|1)(0|1)(0|1)(0|1|2)*");

        Solver solver = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, 0, 2, solver);
        }
        solver.post(IntConstraintFactory.regular(vars, auto));
        solver.set(IntStrategyFactory.lexico_LB(vars));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4371696);
    }

    @Test(groups = "1s")
    public void compareVersionSpeedNew2() {
        int n = 5;
        FiniteAutomaton auto = new FiniteAutomaton("(0|<10>|<20>)*(0|<10>)");

        Solver solver = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("x_" + i, new int[]{0, 10, 20}, solver);
        }
        solver.post(IntConstraintFactory.regular(vars, auto));
        solver.set(IntStrategyFactory.lexico_LB(vars));

        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 162);
    }

    @Test(groups = "1s", expectedExceptions = SolverException.class)
    public void testNeg() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 4, -10, 10, solver);
        solver.post(ICF.regular(CS, new FiniteAutomaton("<-9>1*")));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();

        final List<Solution> solutions = solver.getSolutionRecorder().getSolutions();

        System.out.println(solutions);

        Assert.assertEquals(1, solutions.size());
        Assert.assertEquals(-9, (int) solutions.get(0).getIntVal(CS[0]));
        Assert.assertEquals(1, (int) solutions.get(0).getIntVal(CS[1]));
        Assert.assertEquals(1, (int) solutions.get(0).getIntVal(CS[2]));
        Assert.assertEquals(-5, (int) solutions.get(0).getIntVal(CS[3]));
    }

    @Test(groups = "1s")
    public void testregExp1() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 2, 0, 3, solver);
        solver.post(ICF.regular(CS, new FiniteAutomaton("[12]*")));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testregExp2() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 2, 0, 3, solver);
        solver.post(ICF.regular(CS, new FiniteAutomaton("[^12]*", 0, 3)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testregExp3() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 2, 0, 3, solver);
        solver.post(ICF.regular(CS, new FiniteAutomaton("3?.3?", 0, 3)));
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 7);
    }

    @Test(groups = "1s")
    public void testregExp4() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 2, 0, 3, solver);
        solver.post(ICF.regular(CS, new FiniteAutomaton(".*", 0, 3)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 16);
    }

    @Test(groups = "1s")
    public void testregExp5() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 2, 0, 3, solver);
        solver.post(ICF.regular(CS, new FiniteAutomaton("1{2}")));
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void testregExp6() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 4, 0, 3, solver);
        solver.post(ICF.regular(CS, new FiniteAutomaton("0{2,3}1*")));
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void testregExp7() {
        Solver solver = new Solver();
        IntVar[] CS = VF.enumeratedArray("CS", 10, 0, 2, solver);
        solver.post(ICF.regular(CS, new FiniteAutomaton("0*(1{2,4}0{0,2}0)*0*")));
        Chatterbox.showSolutions(solver, () -> {
            for (int i = 0; i < 10; i++) {
                System.out.printf("%d", CS[i].getValue());
            }
//            System.out.printf("\n");
            return "";
        });
        solver.set(ISF.lexico_LB(CS));
//        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 84);
    }
}