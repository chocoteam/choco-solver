/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import gnu.trove.set.hash.TIntHashSet;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
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
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 59049);
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
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 25980);
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
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 531441);
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
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 1594323);
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
        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4371696);
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
        model.getSolver().setSearch(inputOrderLBSearch(vars));


        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 162);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
    public void testNeg() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 4, -10, 10, false);
        model.regular(CS, new FiniteAutomaton("<-9>1*")).post();

        List<Solution> solutions = new ArrayList<>();
        while (model.getSolver().solve()) {
            solutions.add(new Solution(model).record());
        }

        assertEquals(1, solutions.size());
        assertEquals(-9, solutions.get(0).getIntVal(CS[0]));
        assertEquals(1, solutions.get(0).getIntVal(CS[1]));
        assertEquals(1, solutions.get(0).getIntVal(CS[2]));
        assertEquals(-5, solutions.get(0).getIntVal(CS[3]));
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp1() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("[12]*")).post();
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp2() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("[^12]*", 0, 3)).post();
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp3() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("3?.3?", 0, 3)).post();


        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 7);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp4() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton(".*", 0, 3)).post();
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 16);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp5() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 2, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("1{2}")).post();

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp6() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 6, 0, 3, false);
        model.regular(CS, new FiniteAutomaton("0{2,3}1*")).post();

        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp7() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 10, 0, 2, false);
        model.regular(CS, new FiniteAutomaton("0*(1{2,4}0{0,2}0)*0*")).post();
        model.getSolver().setSearch(inputOrderLBSearch(CS));
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 84);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp8() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 3, new int[]{43, 59, 117});
        model.regular(CS, new FiniteAutomaton("<43><59><117>")).post();
        model.getSolver().setSearch(inputOrderLBSearch(CS));
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 1);

    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
    public void testregExp9(){
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 30, new int[]{0, 20, 127436});
        model.regular(CS, new FiniteAutomaton("(0|<10>|<20>)*(0|<127436>)")).post();
    }


    @Test(groups="1s", timeOut=60000)
    public void testregExp10() {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 6, 0, 4, false);
        model.regular(CS, new FiniteAutomaton("0*[^0]{4,}?0*",0,4)).post();
//
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 6913);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = IndexOutOfBoundsException.class)
    public void testregExp11(){
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 30, new int[]{0, 20, 127436});

        FiniteAutomaton auto = new FiniteAutomaton();
        auto.addTransition(0, 1, 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testregExp12() throws ContradictionException {
        Model model = new Model();
        IntVar[] CS = model.intVarArray("CS", 9, 3,6);
        model.regular(CS, new FiniteAutomaton("([^56]*(44[56]{3}44){1,})*", 3,6)).post();
        model.getSolver().propagate();
        while (model.getSolver().solve());
        assertEquals(model.getSolver().getSolutionCount(), 32);
    }


    @DataProvider(name = "two")
    public Object[][] two(){
        return new Object[][]{{true},{false}};
    }


    @Test(groups = "10s", dataProvider = "two")
    public void testRG11(boolean which) throws Exception {
        Model model = new Model();
        int n = 30;
        IntVar[] x = model.intVarArray("x", n, 0,2);
        IntVar[] x1 = new IntVar[n];
        System.arraycopy(x, n / 3, x1, 0, 2 * n / 3);
        System.arraycopy(x, 0, x1, 2 * n / 3, n / 3);
        IntVar[] x2 = new IntVar[n];
        System.arraycopy(x, 2 * n / 3, x2, 0, n / 3);
        System.arraycopy(x, 0, x2, n / 3, 2 * n / 3);

//
        IntVar[] d = model.intVarArray("d", 3, 0,20);


        d[0].ge(4).post();
        d[1].ge(2).post();
        d[2].ge(3).post();
//
        for(int i = 0 ; i < 3; i++) {
            model.count(i, Arrays.copyOfRange(x, n / 3, 2 * n / 3), d[i]).post();
        }
        FiniteAutomaton auto = makeAuto(which);

        model.regular(x, auto.clone()).post();
        model.regular(x1, auto.clone()).post();
        model.regular(x2, auto.clone()).post();

        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderUBSearch(x));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 272315);
    }

    private FiniteAutomaton makeAuto(boolean which){
        if(which){
            TIntHashSet alphabet = new TIntHashSet();
            alphabet.add(0);
            alphabet.add(1);
            alphabet.add(2);
            RegExp r1 = new RegExp("(\u0001|\u0002){0,4}(\u0000(\u0001|\u0002){0,4})*");
            RegExp r2 = new RegExp("(\u0000|\u0002){0,3}(\u0001(\u0000|\u0002){0,3})*");
            RegExp r3 = new RegExp("(\u0000|\u0001){0,2}(\u0002(\u0000|\u0001){0,2})*");
            Automaton a = r1.toAutomaton().intersection(r2.toAutomaton()).intersection(r3.toAutomaton());
            Constructor<FiniteAutomaton> constructor = null;
            try {
                constructor = FiniteAutomaton.class.getDeclaredConstructor(Automaton.class, TIntHashSet.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            constructor.setAccessible(true);
            try {
                return constructor.newInstance(a, alphabet);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }else{
            FiniteAutomaton auto = new FiniteAutomaton("(1|2){0,4}(0(1|2){0,4})*", 0,2);
            auto = auto.intersection(new FiniteAutomaton("(0|2){0,3}(1(0|2){0,3})*", 0,2));
            return auto.intersection(new FiniteAutomaton("(0|1){0,2}(2(0|1){0,2})*", 0,2));
        }
        return null;
    }

}