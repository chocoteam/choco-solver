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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.chocosolver.solver.trace.Chatterbox.showDecisions;
import static org.chocosolver.solver.trace.Chatterbox.showSolutions;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class RegularTest {

    @Test(groups="1s", timeOut=60000)
    public void testSimpleAuto() {
        Model model = new Model();

        int n = 10;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
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

        model.regular(vars, auto).post();
        model.set(lexico_LB(vars));

        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 59049);
    }

    @Test(groups="1s", timeOut=60000)
    public void ccostregular2() {
        Model model = new Model();

        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
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
        assertEquals(auto.getNbStates(), 54);

        model.regular(vars, auto).post();
        model.set(lexico_LB(vars));

        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 25980);
    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrect() {
        Model model = new Model();

        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
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

        model.regular(vars, auto).post();
        model.set(lexico_LB(vars));

        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 531441);
//        assertEquals(124927,s.getNodeCount());

    }

    @Test(groups="10s", timeOut=60000)
    public void isCorrect2() {
        Model model = new Model();

        int n = 13;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
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

        model.regular(vars, auto).post();
        model.set(lexico_LB(vars));

        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 1594323);
    }

    @Test(groups="10s", timeOut=60000)
    public void compareVersionSpeedNew() {
        int n = 14;
        FiniteAutomaton auto = new FiniteAutomaton("(0|1|2)*(0|1)(0|1)(0|1)(0|1|2)*");

        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, 0, 2, false);
        }
        model.regular(vars, auto).post();
        model.set(lexico_LB(vars));

        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 4371696);
    }

    @Test(groups="1s", timeOut=60000)
    public void compareVersionSpeedNew2() {
        int n = 5;
        FiniteAutomaton auto = new FiniteAutomaton("(0|<10>|<20>)*(0|<10>)");

        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = model.intVar("x_" + i, new int[]{0, 10, 20});
        }
        model.regular(vars, auto).post();
        model.set(lexico_LB(vars));

        showSolutions(model);
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 162);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
    public void testNeg() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 4, -10, 10, false);
        model.regular(CS, new FiniteAutomaton("<-9>1*")).post();
        showSolutions(model);
        model.findAllSolutions();

        final List<Solution> solutions = model.getSolutionRecorder().getSolutions();

        out.println(solutions);

        assertEquals(1, solutions.size());
        assertEquals(-9, (int) solutions.get(0).getIntVal(CS[0]));
        assertEquals(1, (int) solutions.get(0).getIntVal(CS[1]));
        assertEquals(1, (int) solutions.get(0).getIntVal(CS[2]));
        assertEquals(-5, (int) solutions.get(0).getIntVal(CS[3]));
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp1() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("[12]*")).post();
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp2() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("[^12]*", 0, 3)).post();
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp3() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("3?.3?", 0, 3)).post();
        showSolutions(model);
        showDecisions(model);
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 7);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp4() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton(".*", 0, 3)).post();
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 16);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp5() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("1{2}")).post();
        showSolutions(model);
        showDecisions(model);
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp6() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 4, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("0{2,3}1*")).post();
        showSolutions(model);
        showDecisions(model);
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp7() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 10, 0, 2, false);
        model.regular(CS, new FiniteAutomaton("0*(1{2,4}0{0,2}0)*0*")).post();
        showSolutions(model, () -> {
            for (int i = 0; i < 10; i++) {
                out.printf("%d", CS[i].getValue());
            }
//            System.out.printf("\n");
            return "";
        });
        model.set(lexico_LB(CS));
//        Chatterbox.showDecisions(solver);
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 84);
    }


    @Test(groups="1s", timeOut=60000)
    public void testregExp8() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 3, new int[]{43, 59, 117});
        model.regular(CS, new FiniteAutomaton("<43><59><117>")).post();
        model.set(lexico_LB(CS));
        model.findAllSolutions();
        assertEquals(model.getMeasures().getSolutionCount(), 1);

    }


}