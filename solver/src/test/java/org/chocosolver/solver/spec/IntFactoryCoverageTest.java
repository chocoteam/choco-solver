/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.IModel;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_Y;
import org.chocosolver.solver.constraints.ISchedulingFactory;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.hybrid.HybridTuples;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.Condition;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.circuit.CircuitConf;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Exhaustive check that the factory methods of {@link IModel} dealing only with integer and boolean variables
 * (variables, views, constraints, reifications, clauses, decompositions, tasks) are supported by
 * {@link ModelSpec}: each one is called by a case, and each case built directly, recorded then replayed gives
 * the same structure and the same search (all solutions). Constraints are also checked reified.
 * <p>
 * {@link #testEveryIntFactoryMethodIsCovered()} fails if a factory method of the scope is called by no case: add
 * a case when adding a factory method.
 *
 * @author Charles Prud'homme
 */
public class IntFactoryCoverageTest {

    /**
     * Factory methods not implemented by the solver (they always throw an exception).
     */
    private static final Set<String> NOT_IMPLEMENTED = Set.of("addTable(IntVar[],HybridTuples)");

    /**
     * Types out of the scope (set, real and graph variables and constraints).
     */
    private static final List<String> OUT_OF_SCOPE = List.of("SetVar", "RealVar", "GraphVar", "DirectedGraph",
            "UndirectedGraph", "ISet", "IGraph", "RealConstraint", "NodeInduced");

    private record Case(String name, Consumer<Model> body) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final List<Case> CASES = new ArrayList<>();

    /**
     * A case which posts the constraint, and a case which reifies it.
     */
    private static void c(String name, Function<Model, Constraint> f) {
        CASES.add(new Case(name, m -> f.apply(m).post()));
        CASES.add(new Case(name + " [reified]", m -> f.apply(m).reify()));
    }

    /**
     * A case which runs <i>f</i>.
     */
    private static void s(String name, Consumer<Model> f) {
        CASES.add(new Case(name, f));
    }

    private static IntVar[] x(Model m, int n, int lb, int ub) {
        return m.intVarArray("x", n, lb, ub);
    }

    private static IntVar v(Model m, String name, int lb, int ub) {
        return m.intVar(name, lb, ub);
    }

    private static BoolVar[] b(Model m, int n) {
        return m.boolVarArray("b", n);
    }

    private static int[] ints(int... values) {
        return values;
    }

    /**
     * Post a unary cumulative over the tasks, so that they matter.
     */
    private static void cum(Model m, Task... tasks) {
        IntVar[] h = new IntVar[tasks.length];
        Arrays.fill(h, m.intVar(1));
        m.cumulative(tasks, h, m.intVar(1)).post();
    }

    private static Tuples tuples(boolean feasible, int[]... ts) {
        Tuples t = new Tuples(feasible);
        t.add(ts);
        return t;
    }

    /**
     * The numbering of the states of an automaton built from a regular expression is not deterministic (it
     * depends on identity hash codes): the direct and the recorded models of a case must share the automaton.
     */
    private static final FiniteAutomaton AUTOMATON = new FiniteAutomaton("0*1*2*");

    private static FiniteAutomaton automaton() {
        return AUTOMATON;
    }

    private static Task[] tasks(Model m, int n) {
        Task[] ts = new Task[n];
        for (int i = 0; i < n; i++) {
            ts[i] = m.taskVar(v(m, "s" + i, 0, 3), 1 + (i % 2));
        }
        return ts;
    }

    static {
        // ---------------------------------------------------------------- variables
        s("boolVar()", m -> m.boolVar());
        s("boolVar(String)", m -> m.boolVar("b"));
        s("boolVar(String,boolean)", m -> m.arithm(m.boolVar("t", true), "=", m.boolVar("b")).post());
        s("boolVar(boolean)", m -> m.arithm(m.boolVar(false), "=", m.boolVar("b")).post());
        s("boolVarArray(String,int)", m -> m.boolVarArray("b", 3));
        s("boolVarArray(int)", m -> m.boolVarArray(3));
        s("boolVarMatrix(String,int,int)", m -> m.boolVarMatrix("b", 2, 2));
        s("boolVarMatrix(int,int)", m -> m.boolVarMatrix(2, 2));
        s("generateName()", m -> {
            m.generateName();
            m.boolVar();
        });
        s("generateName(String)", m -> {
            m.generateName("X_");
            m.intVar(0, 2);
        });
        s("intVar(IntVar)", m -> m.intVar(m.intVar("x", new int[]{1, 3, 5})));
        s("intVar(String,IntVar)", m -> m.intVar("y", v(m, "x", 0, 3)));
        s("intVar(String,int)", m -> m.arithm(m.intVar("c", 7), ">", v(m, "x", 5, 9)).post());
        s("intVar(String,int,int)", m -> m.intVar("x", 0, 3));
        s("intVar(String,int,int,boolean)", m -> m.intVar("x", 0, 3, true));
        s("intVar(String,int[])", m -> m.intVar("x", new int[]{1, 4, 6}));
        s("intVar(int)", m -> m.arithm(m.intVar(4), ">", v(m, "x", 2, 6)).post());
        s("intVar(int,int)", m -> m.intVar(0, 3));
        s("intVar(int,int,boolean)", m -> m.intVar(0, 3, false));
        s("intVar(int[])", m -> m.intVar(new int[]{2, 5}));
        s("intVarArray(String,int,int,int)", m -> m.intVarArray("x", 2, 0, 2));
        s("intVarArray(String,int,int,int,boolean)", m -> m.intVarArray("x", 2, 0, 2, true));
        s("intVarArray(String,int,int[])", m -> m.intVarArray("x", 2, new int[]{1, 3}));
        s("intVarArray(int,int,int)", m -> m.intVarArray(2, 0, 2));
        s("intVarArray(int,int,int,boolean)", m -> m.intVarArray(2, 0, 2, false));
        s("intVarArray(int,int[])", m -> m.intVarArray(2, new int[]{1, 3}));
        s("intVarMatrix(String,int,int,int,int)", m -> m.intVarMatrix("x", 2, 2, 0, 1));
        s("intVarMatrix(String,int,int,int,int,boolean)", m -> m.intVarMatrix("x", 2, 2, 0, 1, true));
        s("intVarMatrix(String,int,int,int[])", m -> m.intVarMatrix("x", 2, 2, new int[]{0, 2}));
        s("intVarMatrix(int,int,int,int)", m -> m.intVarMatrix(2, 2, 0, 1));
        s("intVarMatrix(int,int,int,int,boolean)", m -> m.intVarMatrix(2, 2, 0, 1, false));
        s("intVarMatrix(int,int,int[])", m -> m.intVarMatrix(2, 2, new int[]{0, 2}));
        // tasks
        s("taskVar(IntVar,IntVar)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), v(m, "d", 1, 2)), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,IntVar,BoolVar)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), v(m, "d", 1, 2), m.boolVar("p")), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,IntVar,IntVar)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), v(m, "d", 1, 2), v(m, "e", 1, 5)), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,IntVar,IntVar,BoolVar)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), v(m, "d", 1, 2), v(m, "e", 1, 5), m.boolVar("p")), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,IntVar,IntVar,boolean)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), v(m, "d", 1, 2), v(m, "e", 1, 5), true), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,IntVar,boolean)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), v(m, "d", 1, 2), true), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,int)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), 2), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,int,BoolVar)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), 2, m.boolVar("p")), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,int,IntVar)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), 2, v(m, "e", 2, 5)), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,int,IntVar,BoolVar)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), 2, v(m, "e", 2, 5), m.boolVar("p")), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,int,IntVar,boolean)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), 2, v(m, "e", 2, 5), true), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(IntVar,int,boolean)", m -> cum(m, m.taskVar(v(m, "s", 0, 3), 2, true), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(int,int,int,int,int)", m -> cum(m, m.taskVar(0, 3, 2, 2, 5), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(int,int,int,int,int,BoolVar)", m -> cum(m, m.taskVar(0, 3, 2, 2, 5, m.boolVar("p")), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVar(int,int,int,int,int,boolean)", m -> cum(m, m.taskVar(0, 3, 2, 2, 5, true), m.taskVar(v(m, "t", 0, 3), 1)));
        s("taskVarArray(IntVar[],IntVar[],IntVar[])", m -> cum(m, m.taskVarArray(x(m, 2, 0, 2), m.intVarArray("d", 2, 1, 2), m.intVarArray("e", 2, 1, 4))));
        s("taskVarMatrix(IntVar[][],IntVar[][],IntVar[][])", m -> {
            Task[][] t = m.taskVarMatrix(m.intVarMatrix("s", 1, 2, 0, 2), m.intVarMatrix("d", 1, 2, 1, 2), m.intVarMatrix("e", 1, 2, 1, 4));
            cum(m, t[0]);
        });

        // ---------------------------------------------------------------- views
        s("abs(IntVar)", m -> m.arithm(m.abs(v(m, "x", -2, 2)), "!=", 1).post());
        s("boolNotView(BoolVar)", m -> {
            BoolVar[] bs = b(m, 2);
            m.arithm(m.boolNotView(bs[0]), "=", bs[1]).post();
        });
        s("intAbsView(IntVar)", m -> m.arithm(m.intAbsView(v(m, "x", -2, 2)), "!=", 1).post());
        s("intAffineView(int,IntVar,int)", m -> m.arithm(m.intAffineView(2, v(m, "x", 0, 3), 1), "!=", 3).post());
        s("intEqView(IntVar,int)", m -> m.arithm(m.intEqView(v(m, "x", 0, 3), 1), "=", 0).post());
        s("intGeView(IntVar,int)", m -> m.arithm(m.intGeView(v(m, "x", 0, 3), 2), "=", 1).post());
        s("intLeView(IntVar,int)", m -> m.arithm(m.intLeView(v(m, "x", 0, 3), 1), "=", 1).post());
        s("intMinusView(IntVar)", m -> m.arithm(m.intMinusView(v(m, "x", 0, 3)), ">", -2).post());
        s("intNeView(IntVar,int)", m -> m.arithm(m.intNeView(v(m, "x", 0, 3), 1), "=", 1).post());
        s("intOffsetView(IntVar,int)", m -> m.arithm(m.intOffsetView(v(m, "x", 0, 3), 2), "!=", 3).post());
        s("intScaleView(IntVar,int)", m -> m.arithm(m.intScaleView(v(m, "x", 0, 3), 3), "!=", 3).post());
        s("intView(int,IntVar,int)", m -> m.arithm(m.intView(2, v(m, "x", 0, 3), -1), "!=", 1).post());
        s("isEq(IntVar,int)", m -> m.arithm(m.isEq(v(m, "x", 0, 3), 1), "=", 0).post());
        s("isGeq(IntVar,int)", m -> m.arithm(m.isGeq(v(m, "x", 0, 3), 2), "=", 1).post());
        s("isLeq(IntVar,int)", m -> m.arithm(m.isLeq(v(m, "x", 0, 3), 1), "=", 1).post());
        s("isNeq(IntVar,int)", m -> m.arithm(m.isNeq(v(m, "x", 0, 3), 1), "=", 1).post());
        s("mul(IntVar,int)", m -> m.arithm(m.mul(v(m, "x", 0, 3), 2), "!=", 2).post());
        s("neg(IntVar)", m -> m.arithm(m.neg(v(m, "x", 0, 3)), ">", -2).post());
        s("offset(IntVar,int)", m -> m.arithm(m.offset(v(m, "x", 0, 3), 2), "!=", 3).post());

        // ---------------------------------------------------------------- integer constraints
        c("absolute(IntVar,IntVar)", m -> m.absolute(v(m, "y", 0, 2), v(m, "x", -2, 2)));
        c("allDiffPrec(IntVar[],boolean[][])", m -> m.allDiffPrec(x(m, 3, 0, 2), new boolean[][]{{false, true, false}, {false, false, false}, {false, false, false}}));
        c("allDiffPrec(IntVar[],boolean[][],String)", m -> m.allDiffPrec(x(m, 3, 0, 2), new boolean[][]{{false, true, false}, {false, false, false}, {false, false, false}}, "BESSIERE"));
        c("allDiffPrec(IntVar[],int[][],int[][])", m -> m.allDiffPrec(x(m, 3, 0, 2), new int[][]{{}, {0}, {}}, new int[][]{{1}, {}, {}}));
        c("allDiffPrec(IntVar[],int[][],int[][],String)", m -> m.allDiffPrec(x(m, 3, 0, 2), new int[][]{{}, {0}, {}}, new int[][]{{1}, {}, {}}, "GREEDY"));
        c("allDifferent(IntVar[])", m -> m.allDifferent(x(m, 3, 0, 3)));
        c("allDifferent(IntVar[],String)", m -> m.allDifferent(x(m, 3, 0, 3), "AC"));
        c("allDifferentExcept0(IntVar[])", m -> m.allDifferentExcept0(x(m, 3, 0, 2)));
        c("allDifferentExceptValues(IntVar[],int[])", m -> m.allDifferentExceptValues(x(m, 3, 0, 2), ints(0, 1)));
        c("allDifferentUnderCondition(IntVar[],Condition,boolean)", m -> m.allDifferentUnderCondition(x(m, 3, 0, 2), Condition.EXCEPT_0, true));
        c("allDifferentUnderCondition(IntVar[],Condition,boolean,String)", m -> m.allDifferentUnderCondition(x(m, 3, 0, 2), v -> !v.contains(0), false, "AC"));
        c("allEqual(IntVar[])", m -> m.allEqual(x(m, 3, 0, 2)));
        c("among(IntVar,IntVar[],int[])", m -> m.among(v(m, "n", 0, 3), x(m, 3, 0, 2), ints(1, 2)));
        c("and(BoolVar[])", m -> m.and(b(m, 3)));
        c("and(Constraint[])", m -> {
            IntVar[] x = x(m, 2, 0, 2);
            return m.and(m.arithm(x[0], "<", x[1]), m.arithm(x[1], "!=", 1));
        });
        c("argmax(IntVar,int,IntVar[])", m -> m.argmax(v(m, "z", 0, 2), 0, x(m, 3, 0, 2)));
        c("argmin(IntVar,int,IntVar[])", m -> m.argmin(v(m, "z", 1, 3), 1, x(m, 3, 0, 2)));
        c("arithm(IntVar,String,IntVar)", m -> m.arithm(v(m, "x", 0, 3), "<", v(m, "y", 0, 3)));
        c("arithm(IntVar,String,IntVar,String,IntVar)", m -> m.arithm(v(m, "x", 0, 3), "+", v(m, "y", 0, 3), "=", v(m, "z", 0, 3)));
        c("arithm(IntVar,String,IntVar,String,int)", m -> m.arithm(v(m, "x", 0, 3), "-", v(m, "y", 0, 3), "<=", 1));
        c("arithm(IntVar,String,int)", m -> m.arithm(v(m, "x", 0, 3), "!=", 2));
        c("atLeastNValues(IntVar[],IntVar,boolean)", m -> m.atLeastNValues(x(m, 3, 0, 2), v(m, "n", 2, 3), true));
        c("atMostNValues(IntVar[],IntVar,boolean)", m -> m.atMostNValues(x(m, 3, 0, 2), v(m, "n", 1, 2), false));
        c("binPacking(IntVar[],int[],IntVar[],int)", m -> m.binPacking(x(m, 3, 0, 1), ints(1, 2, 2), m.intVarArray("l", 2, 0, 4), 0));
        c("bitsIntChanneling(BoolVar[],IntVar)", m -> m.bitsIntChanneling(b(m, 3), v(m, "x", 0, 7)));
        c("boolsIntChanneling(BoolVar[],IntVar,int)", m -> m.boolsIntChanneling(b(m, 3), v(m, "x", 0, 2), 0));
        c("circuit(IntVar[])", m -> m.circuit(x(m, 4, 0, 3)));
        c("circuit(IntVar[],int)", m -> m.circuit(x(m, 4, 1, 4), 1));
        c("circuit(IntVar[],int,CircuitConf)", m -> m.circuit(x(m, 4, 0, 3), 0, CircuitConf.RD));
        c("clausesIntChanneling(IntVar,BoolVar[],BoolVar[])", m -> m.clausesIntChanneling(v(m, "x", 0, 2), m.boolVarArray("e", 3), m.boolVarArray("l", 3)));
        c("costRegular(IntVar[],IntVar,ICostAutomaton)", m -> {
            int[][] costs = new int[3][3];
            for (int[] c : costs) Arrays.fill(c, 1);
            costs[1][2] = 3;
            return m.costRegular(x(m, 3, 0, 2), v(m, "c", 0, 5), CostAutomaton.makeSingleResource(automaton(), costs, 0, 5));
        });
        c("count(IntVar,IntVar[],IntVar)", m -> m.count(v(m, "val", 0, 2), x(m, 3, 0, 2), v(m, "n", 1, 2)));
        c("count(int,IntVar[],IntVar)", m -> m.count(1, x(m, 3, 0, 2), v(m, "n", 1, 2)));
        c("custom(String,Variable[],Function)", m -> m.custom("geq", x(m, 2, 0, 2), PropGreaterOrEqualX_Y::new));
        c("custom(String,Variable[],Object,BiFunction)", m -> m.custom("geq", x(m, 1, 0, 2), x(m, 1, 0, 2), (u, v) -> new PropGreaterOrEqualX_Y(new IntVar[]{u[0], v[0]})));
        c("decreasing(IntVar[],int)", m -> m.decreasing(x(m, 3, 0, 3), 1));
        c("diffN(IntVar[],IntVar[],IntVar[],IntVar[],boolean)", m -> m.diffN(m.intVarArray("x", 2, 0, 2), m.intVarArray("y", 2, 0, 2), m.intVarArray("w", 2, 1, 2), m.intVarArray("h", 2, 1, 2), true));
        c("diffN(IntVar[][],int[][])", m -> m.diffN(m.intVarMatrix("o", 2, 2, 0, 2), new int[][]{{1, 2}, {2, 1}}));
        c("distance(IntVar,IntVar,String,IntVar)", m -> m.distance(v(m, "x", 0, 3), v(m, "y", 0, 3), "<", v(m, "z", 1, 2)));
        c("distance(IntVar,IntVar,String,int)", m -> m.distance(v(m, "x", 0, 3), v(m, "y", 0, 3), "=", 1));
        c("div(IntVar,IntVar,IntVar)", m -> m.div(v(m, "x", 0, 6), v(m, "y", 1, 3), v(m, "z", 0, 6)));
        c("element(IntVar,IntVar[],IntVar,int)", m -> m.element(v(m, "v", 0, 2), x(m, 3, 0, 2), v(m, "i", 1, 3), 1));
        c("element(IntVar,int[],IntVar)", m -> m.element(v(m, "v", 0, 5), ints(3, 1, 4, 1, 5), v(m, "i", 0, 4)));
        c("element(IntVar,int[],IntVar,int)", m -> m.element(v(m, "v", 0, 5), ints(3, 1, 4, 1, 5), v(m, "i", 0, 6), 2));
        c("globalCardinality(IntVar[],int[],IntVar[],boolean)", m -> m.globalCardinality(x(m, 3, 0, 2), ints(0, 1), m.intVarArray("o", 2, 0, 3), false));
        c("globalCardinality(IntVar[],int[],IntVar[],boolean,String)", m -> m.globalCardinality(x(m, 3, 0, 2), ints(0, 1), m.intVarArray("o", 2, 0, 3), true, "AC"));
        c("increasing(IntVar[],int)", m -> m.increasing(x(m, 3, 0, 3), 0));
        c("intValuePrecedeChain(IntVar[],int,int)", m -> m.intValuePrecedeChain(x(m, 4, 0, 2), 1, 2));
        c("intValuePrecedeChain(IntVar[],int[])", m -> m.intValuePrecedeChain(x(m, 4, 0, 2), ints(0, 1, 2)));
        c("inverseChanneling(IntVar[],IntVar[])", m -> m.inverseChanneling(x(m, 3, 0, 2), m.intVarArray("y", 3, 0, 2)));
        c("inverseChanneling(IntVar[],IntVar[],int,int)", m -> m.inverseChanneling(x(m, 3, 1, 3), m.intVarArray("y", 3, 1, 3), 1, 1));
        c("inverseChanneling(IntVar[],IntVar[],int,int,boolean)", m -> m.inverseChanneling(x(m, 3, 1, 3), m.intVarArray("y", 3, 0, 2), 0, 1, true));
        c("keySort(IntVar[][],IntVar[],IntVar[][],int)", m -> m.keySort(m.intVarMatrix("a", 3, 2, 0, 1), m.intVarArray("p", 3, 1, 3), m.intVarMatrix("s", 3, 2, 0, 1), 1));
        c("knapsack(IntVar[],IntVar,IntVar,int[],int[])", m -> m.knapsack(x(m, 3, 0, 2), v(m, "w", 0, 6), v(m, "e", 0, 10), ints(1, 2, 3), ints(2, 3, 4)));
        c("lexChainLess(IntVar[][])", m -> m.lexChainLess(m.intVarMatrix("a", 3, 2, 0, 1)));
        c("lexChainLessEq(IntVar[][])", m -> m.lexChainLessEq(m.intVarMatrix("a", 3, 2, 0, 1)));
        c("lexLess(IntVar[],IntVar[])", m -> m.lexLess(x(m, 2, 0, 2), m.intVarArray("y", 2, 0, 2)));
        c("lexLessEq(IntVar[],IntVar[])", m -> m.lexLessEq(x(m, 2, 0, 2), m.intVarArray("y", 2, 0, 2)));
        c("max(BoolVar,BoolVar[])", m -> m.max(m.boolVar("m"), b(m, 3)));
        c("max(IntVar,IntVar,IntVar)", m -> m.max(v(m, "m", 0, 3), v(m, "x", 0, 3), v(m, "y", 0, 3)));
        c("max(IntVar,IntVar[])", m -> m.max(v(m, "m", 0, 3), x(m, 3, 0, 3)));
        c("mddc(IntVar[],MultivaluedDecisionDiagram)", m -> {
            IntVar[] x = x(m, 3, 0, 2);
            return m.mddc(x, new MultivaluedDecisionDiagram(x, tuples(true, ints(0, 1, 2), ints(1, 1, 1), ints(2, 0, 1))));
        });
        c("member(IntVar,IntIterableRangeSet)", m -> m.member(v(m, "x", 0, 5), new IntIterableRangeSet(ints(1, 3, 4))));
        c("member(IntVar,int,int)", m -> m.member(v(m, "x", 0, 5), 2, 4));
        c("member(IntVar,int[])", m -> m.member(v(m, "x", 0, 5), ints(1, 5)));
        c("min(BoolVar,BoolVar[])", m -> m.min(m.boolVar("m"), b(m, 3)));
        c("min(IntVar,IntVar,IntVar)", m -> m.min(v(m, "m", 0, 3), v(m, "x", 0, 3), v(m, "y", 0, 3)));
        c("min(IntVar,IntVar[])", m -> m.min(v(m, "m", 0, 3), x(m, 3, 0, 3)));
        c("mod(IntVar,IntVar,IntVar)", m -> m.mod(v(m, "x", 0, 6), v(m, "y", 1, 3), v(m, "z", 0, 2)));
        c("mod(IntVar,int,IntVar)", m -> m.mod(v(m, "x", 0, 6), 3, v(m, "z", 0, 2)));
        c("mod(IntVar,int,int)", m -> m.mod(v(m, "x", 0, 6), 3, 1));
        c("multiCostRegular(IntVar[],IntVar[],ICostAutomaton)", m -> {
            int[][][] costs = new int[3][3][2];
            for (int[][] l : costs) for (int[] c : l) Arrays.fill(c, 1);
            return m.multiCostRegular(x(m, 3, 0, 2), m.intVarArray("c", 2, 0, 3), CostAutomaton.makeMultiResources(automaton(), costs, ints(0, 0), ints(3, 3)));
        });
        c("multiCostRegular(IntVar[],IntVar[],ICostAutomaton,double)", m -> {
            int[][][] costs = new int[3][3][2];
            for (int[][] l : costs) for (int[] c : l) Arrays.fill(c, 1);
            return m.multiCostRegular(x(m, 3, 0, 2), m.intVarArray("c", 2, 0, 3), CostAutomaton.makeMultiResources(automaton(), costs, ints(0, 0), ints(3, 3)), 1e-4);
        });
        c("nValues(IntVar[],IntVar)", m -> m.nValues(x(m, 3, 0, 2), v(m, "n", 1, 2)));
        c("not(Constraint)", m -> m.not(m.arithm(v(m, "x", 0, 3), "=", 1)));
        c("notAllEqual(IntVar[])", m -> m.notAllEqual(x(m, 3, 0, 1)));
        c("notMember(IntVar,IntIterableRangeSet)", m -> m.notMember(v(m, "x", 0, 5), new IntIterableRangeSet(ints(1, 3, 4))));
        c("notMember(IntVar,int,int)", m -> m.notMember(v(m, "x", 0, 5), 2, 4));
        c("notMember(IntVar,int[])", m -> m.notMember(v(m, "x", 0, 5), ints(1, 5)));
        c("or(BoolVar[])", m -> m.or(b(m, 3)));
        c("or(Constraint[])", m -> {
            IntVar[] x = x(m, 2, 0, 2);
            return m.or(m.arithm(x[0], "<", x[1]), m.arithm(x[1], "=", 0));
        });
        c("path(IntVar[],IntVar,IntVar)", m -> m.path(x(m, 4, 0, 4), v(m, "s", 0, 3), v(m, "e", 0, 3)));
        c("path(IntVar[],IntVar,IntVar,int)", m -> m.path(x(m, 4, 1, 5), v(m, "s", 1, 4), v(m, "e", 1, 4), 1));
        c("pow(IntVar,IntVar,IntVar)", m -> m.pow(v(m, "b", 0, 2), v(m, "e", 0, 2), v(m, "r", 0, 4)));
        c("pow(IntVar,int,IntVar)", m -> m.pow(v(m, "b", -2, 2), 2, v(m, "r", 0, 4)));
        c("regular(IntVar[],IAutomaton)", m -> m.regular(x(m, 3, 0, 2), automaton()));
        c("scalar(IntVar[],int[],String,IntVar)", m -> m.scalar(x(m, 3, 0, 2), ints(1, 2, 3), "<=", v(m, "s", 0, 6)));
        c("scalar(IntVar[],int[],String,IntVar,int)", m -> m.scalar(x(m, 3, 0, 2), ints(1, 2, 3), "=", v(m, "s", 0, 6), 2));
        c("scalar(IntVar[],int[],String,int)", m -> m.scalar(x(m, 3, 0, 2), ints(1, 2, 3), "=", 4));
        c("scalar(IntVar[],int[],String,int,int)", m -> m.scalar(x(m, 3, 0, 2), ints(1, -2, 3), ">=", 2, 2));
        c("sort(IntVar[],IntVar[])", m -> m.sort(x(m, 3, 0, 2), m.intVarArray("y", 3, 0, 2)));
        c("square(IntVar,IntVar)", m -> m.square(v(m, "y", 0, 9), v(m, "x", -3, 3)));
        c("subCircuit(IntVar[],int,IntVar)", m -> m.subCircuit(x(m, 4, 0, 3), 0, v(m, "l", 0, 4)));
        c("subPath(IntVar[],IntVar,IntVar,int,IntVar)", m -> m.subPath(x(m, 4, 0, 4), v(m, "s", 0, 3), v(m, "e", 0, 3), 0, v(m, "l", 1, 4)));
        c("sum(BoolVar[],String,IntVar)", m -> m.sum(b(m, 3), "=", v(m, "s", 0, 2)));
        c("sum(BoolVar[],String,IntVar,int)", m -> m.sum(b(m, 3), "<=", v(m, "s", 0, 2), 2));
        c("sum(BoolVar[],String,int)", m -> m.sum(b(m, 3), "=", 2));
        c("sum(IntVar[],String,IntVar)", m -> m.sum(x(m, 3, 0, 2), "=", v(m, "s", 0, 6)));
        c("sum(IntVar[],String,IntVar,int)", m -> m.sum(x(m, 3, 0, 2), "<", v(m, "s", 0, 6), 2));
        c("sum(IntVar[],String,IntVar[])", m -> m.sum(x(m, 2, 0, 2), "=", m.intVarArray("y", 2, 0, 2)));
        c("sum(IntVar[],String,int)", m -> m.sum(x(m, 3, 0, 2), "=", 3));
        c("sum(IntVar[],String,int,int)", m -> m.sum(x(m, 3, 0, 2), "!=", 3, 2));
        c("table(IntVar,IntVar,Tuples)", m -> m.table(v(m, "x", 0, 2), v(m, "y", 0, 2), tuples(true, ints(0, 1), ints(1, 2), ints(2, 2))));
        c("table(IntVar,IntVar,Tuples,String)", m -> m.table(v(m, "x", 0, 2), v(m, "y", 0, 2), tuples(false, ints(0, 1), ints(1, 2)), "AC3"));
        c("table(IntVar[],HybridTuples)", m -> {
            HybridTuples h = new HybridTuples();
            h.add(HybridTuples.eq(1), HybridTuples.any(), HybridTuples.ge(1));
            h.add(HybridTuples.ne(1), HybridTuples.col(0), HybridTuples.lt(2));
            return m.table(x(m, 3, 0, 2), h);
        });
        c("table(IntVar[],Tuples)", m -> m.table(x(m, 3, 0, 2), tuples(true, ints(0, 1, 2), ints(1, 1, 1), ints(2, 0, 1))));
        c("table(IntVar[],Tuples,String)", m -> m.table(x(m, 3, 0, 2), tuples(true, ints(0, 1, 2), ints(1, 1, 1), ints(2, 0, 1)), "STR2+"));
        c("times(IntVar,IntVar,IntVar)", m -> m.times(v(m, "x", -2, 2), v(m, "y", 0, 2), v(m, "z", -4, 4)));
        c("times(IntVar,IntVar,int)", m -> m.times(v(m, "x", 0, 3), v(m, "y", 0, 3), 2));
        c("times(IntVar,int,IntVar)", m -> m.times(v(m, "x", 0, 3), 2, v(m, "z", 0, 6)));
        c("tree(IntVar[],IntVar)", m -> m.tree(x(m, 4, 0, 3), v(m, "n", 1, 2)));
        c("tree(IntVar[],IntVar,int)", m -> m.tree(x(m, 4, 1, 4), v(m, "n", 1, 2), 1));
        s("conditional(Variable[],Function,Supplier) [unsupported by design]", m -> {
            IntVar[] x = x(m, 2, 0, 2);
            m.conditional(x, vs -> vs[0].isInstantiated(), () -> new Constraint[]{m.arithm(x[0], "!=", x[1])});
        });

        // ---------------------------------------------------------------- decompositions
        s("allDifferentExceptDec(IntVar[],int[])", m -> m.allDifferentExceptDec(x(m, 3, 0, 2), ints(0)));
        s("allDifferentExceptDecWithGCC(IntVar[],int[])", m -> m.allDifferentExceptDecWithGCC(x(m, 3, 0, 2), ints(0)));
        s("amongDec(IntVar,IntVar[],IntVar[])", m -> m.amongDec(v(m, "n", 0, 3), x(m, 3, 0, 2), new IntVar[]{m.intVar(1), m.intVar(2)}));
        s("argmaxDec(IntVar,int,IntVar[])", m -> m.argmaxDec(v(m, "z", 0, 2), 0, x(m, 3, 0, 2)));
        s("argminDec(IntVar,int,IntVar[])", m -> m.argminDec(v(m, "z", 0, 2), 0, x(m, 3, 0, 2)));
        s("binPackingDec(IntVar[],int[],IntVar[],int)", m -> m.binPackingDec(x(m, 3, 0, 1), ints(1, 2, 2), m.intVarArray("l", 2, 0, 4), 0));
        s("circuitDec(IntVar[],int)", m -> m.circuitDec(x(m, 4, 0, 3), 0));
        s("costFlow(int[],int[],int[],int[],IntVar[],IntVar,int)", m -> m.costFlow(ints(0, 1, 0), ints(1, 2, 2), ints(1, 0, -1), ints(1, 1, 3), m.intVarArray("f", 3, 0, 1), v(m, "c", 0, 5), 0));
        s("cumulativeDec(IntVar[],IntVar[],IntVar[],IntVar)", m -> m.cumulativeDec(x(m, 2, 0, 2), m.intVarArray("d", 2, 1, 2), m.intVarArray("h", 2, 1, 1), v(m, "c", 1, 1)));
        s("cumulativeDec(Task[],IntVar[],IntVar)", m -> m.cumulativeDec(tasks(m, 2), m.intVarArray("h", 2, 1, 1), v(m, "c", 1, 1)));
        s("cumulativeTimeDec(IntVar[],int[],int[],int)", m -> m.cumulativeTimeDec(x(m, 2, 0, 2), ints(1, 2), ints(1, 1), 1));
        s("disjunctiveDec(Task[],boolean)", m -> m.disjunctiveDec(tasks(m, 2), true));
        s("element(IntVar,IntVar[][],IntVar,int,IntVar,int)", m -> m.element(v(m, "v", 0, 2), m.intVarMatrix("t", 2, 2, 0, 2), v(m, "r", 0, 1), 0, v(m, "c", 1, 2), 1));
        s("element(IntVar,int[][],IntVar,int,IntVar,int)", m -> m.element(v(m, "v", 0, 5), new int[][]{{1, 2}, {3, 4}}, v(m, "r", 0, 1), 0, v(m, "c", 0, 1), 0));
        s("globalCardinalityDec(IntVar[],IntVar[],IntVar[],boolean)", m -> m.globalCardinalityDec(x(m, 3, 0, 2), new IntVar[]{m.intVar(0), m.intVar(1)}, m.intVarArray("o", 2, 0, 3), false));
        s("ifThenElseDec(BoolVar[],IntVar[],IntVar)", m -> m.ifThenElseDec(b(m, 2), x(m, 2, 0, 2), v(m, "y", 0, 2)));
        s("ifThenElseDec(BoolVar[],int[],IntVar)", m -> m.ifThenElseDec(b(m, 2), ints(1, 2), v(m, "y", 0, 2)));
        s("intValuePrecedeChainDec(IntVar[],int,int)", m -> m.intValuePrecedeChainDec(x(m, 4, 0, 2), 1, 2));
        s("intValuePrecedeChainDec(IntVar[],int[])", m -> m.intValuePrecedeChainDec(x(m, 4, 0, 2), ints(0, 1, 2)));
        s("product(BoolVar[][],BoolVar[][],BoolVar[][])", m -> m.product(m.boolVarMatrix("a", 2, 2), m.boolVarMatrix("b", 2, 2), m.boolVarMatrix("c", 2, 2)));
        s("product(IntVar[][],IntVar[][],IntVar[][])", m -> m.product(m.intVarMatrix("a", 2, 2, 0, 1), m.intVarMatrix("b", 2, 2, 0, 1), m.intVarMatrix("c", 2, 2, 0, 2)));
        s("regularDec(IntVar[],IAutomaton)", m -> m.regularDec(x(m, 3, 0, 2), automaton()));

        // ---------------------------------------------------------------- reifications
        s("ifOnlyIf(Constraint,Constraint)", m -> {
            IntVar[] x = x(m, 2, 0, 2);
            m.ifOnlyIf(m.arithm(x[0], "=", 1), m.arithm(x[1], ">", 0));
        });
        s("ifThen(BoolVar,Constraint)", m -> m.ifThen(m.boolVar("b"), m.arithm(v(m, "x", 0, 2), "=", 1)));
        s("ifThen(Constraint,Constraint)", m -> {
            IntVar[] x = x(m, 2, 0, 2);
            m.ifThen(m.arithm(x[0], "=", 1), m.arithm(x[1], ">", 0));
        });
        s("ifThenElse(BoolVar,Constraint,Constraint)", m -> {
            IntVar x = v(m, "x", 0, 2);
            m.ifThenElse(m.boolVar("b"), m.arithm(x, "=", 1), m.arithm(x, "=", 2));
        });
        s("ifThenElse(Constraint,Constraint,Constraint)", m -> {
            IntVar[] x = x(m, 2, 0, 2);
            m.ifThenElse(m.arithm(x[0], "=", 1), m.arithm(x[1], ">", 0), m.arithm(x[1], "=", 0));
        });
        s("impXrelC(IntVar,String,int,BoolVar)", m -> m.impXrelC(v(m, "x", 0, 3), "<", 2, m.boolVar("b")));
        s("impXrelYC(IntVar,String,IntVar,int,BoolVar)", m -> m.impXrelYC(v(m, "x", 0, 3), "<=", v(m, "y", 0, 3), 1, m.boolVar("b")));
        s("reifXrelC(IntVar,String,int,BoolVar)", m -> m.reifXrelC(v(m, "x", 0, 3), ">=", 1, m.boolVar("b")));
        s("reifXrelYC(IntVar,String,IntVar,int,BoolVar)", m -> m.reifXrelYC(v(m, "x", 0, 3), "=", v(m, "y", 0, 3), 1, m.boolVar("b")));
        s("reification(BoolVar,Constraint)", m -> m.reification(m.boolVar("b"), m.arithm(v(m, "x", 0, 3), "=", 1)));
        s("reifyXeqC(IntVar,int,BoolVar)", m -> m.reifyXeqC(v(m, "x", 0, 3), 1, m.boolVar("b")));
        s("reifyXeqY(IntVar,IntVar,BoolVar)", m -> m.reifyXeqY(v(m, "x", 0, 3), v(m, "y", 0, 3), m.boolVar("b")));
        s("reifyXeqYC(IntVar,IntVar,int,BoolVar)", m -> m.reifyXeqYC(v(m, "x", 0, 3), v(m, "y", 0, 3), 1, m.boolVar("b")));
        s("reifyXgeY(IntVar,IntVar,BoolVar)", m -> m.reifyXgeY(v(m, "x", 0, 3), v(m, "y", 0, 3), m.boolVar("b")));
        s("reifyXgtC(IntVar,int,BoolVar)", m -> m.reifyXgtC(v(m, "x", 0, 3), 1, m.boolVar("b")));
        s("reifyXgtY(IntVar,IntVar,BoolVar)", m -> m.reifyXgtY(v(m, "x", 0, 3), v(m, "y", 0, 3), m.boolVar("b")));
        s("reifyXgtYC(IntVar,IntVar,int,BoolVar)", m -> m.reifyXgtYC(v(m, "x", 0, 3), v(m, "y", 0, 3), 1, m.boolVar("b")));
        s("reifyXinS(IntVar,IntIterableRangeSet,BoolVar)", m -> m.reifyXinS(v(m, "x", 0, 5), new IntIterableRangeSet(ints(1, 4)), m.boolVar("b")));
        s("reifyXleY(IntVar,IntVar,BoolVar)", m -> m.reifyXleY(v(m, "x", 0, 3), v(m, "y", 0, 3), m.boolVar("b")));
        s("reifyXltC(IntVar,int,BoolVar)", m -> m.reifyXltC(v(m, "x", 0, 3), 2, m.boolVar("b")));
        s("reifyXltY(IntVar,IntVar,BoolVar)", m -> m.reifyXltY(v(m, "x", 0, 3), v(m, "y", 0, 3), m.boolVar("b")));
        s("reifyXltYC(IntVar,IntVar,int,BoolVar)", m -> m.reifyXltYC(v(m, "x", 0, 3), v(m, "y", 0, 3), 1, m.boolVar("b")));
        s("reifyXneC(IntVar,int,BoolVar)", m -> m.reifyXneC(v(m, "x", 0, 3), 1, m.boolVar("b")));
        s("reifyXneY(IntVar,IntVar,BoolVar)", m -> m.reifyXneY(v(m, "x", 0, 3), v(m, "y", 0, 3), m.boolVar("b")));
        s("reifyXneYC(IntVar,IntVar,int,BoolVar)", m -> m.reifyXneYC(v(m, "x", 0, 3), v(m, "y", 0, 3), 1, m.boolVar("b")));
        s("reifyXnotinS(IntVar,IntIterableRangeSet,BoolVar)", m -> m.reifyXnotinS(v(m, "x", 0, 5), new IntIterableRangeSet(ints(1, 4)), m.boolVar("b")));

        // ---------------------------------------------------------------- result variables
        s("argmax(String,IntVar[])", m -> m.arithm(m.argmax("a", x(m, 3, 0, 2)), "!=", 1).post());
        s("argmin(String,IntVar[])", m -> m.arithm(m.argmin("a", x(m, 3, 0, 2)), "!=", 1).post());
        s("count(String,IntVar,IntVar[])", m -> m.arithm(m.count("c", v(m, "v", 0, 2), x(m, 3, 0, 2)), ">", 1).post());
        s("count(String,int,IntVar[])", m -> m.arithm(m.count("c", 1, x(m, 3, 0, 2)), ">", 1).post());
        s("element(String,IntVar[],IntVar,int)", m -> m.arithm(m.element("e", x(m, 3, 0, 2), v(m, "i", 1, 3), 1), "!=", 1).post());
        s("element(String,int[],IntVar)", m -> m.arithm(m.element("e", ints(3, 1, 4), v(m, "i", 0, 2)), "!=", 1).post());
        s("element(String,int[],IntVar,int)", m -> m.arithm(m.element("e", ints(3, 1, 4), v(m, "i", 1, 3), 1), "!=", 1).post());
        s("max(String,IntVar[])", m -> m.arithm(m.max("m", x(m, 3, 0, 2)), "!=", 1).post());
        s("min(String,IntVar[])", m -> m.arithm(m.min("m", x(m, 3, 0, 2)), "!=", 1).post());
        s("mod(String,IntVar,IntVar)", m -> m.arithm(m.mod("r", v(m, "x", 0, 6), v(m, "y", 1, 3)), "!=", 1).post());
        s("mod(String,IntVar,int)", m -> m.arithm(m.mod("r", v(m, "x", 0, 6), 3), "!=", 1).post());
        s("sum(String,IntVar[])", m -> m.arithm(m.sum("s", x(m, 3, 0, 2)), "=", 3).post());

        // ---------------------------------------------------------------- clauses
        s("addClause(int[]), lit(int), neg(int), satVar(Variable,Literalizer)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClause(m.lit(b[0].satVar()), m.neg(b[1].satVar()));
            m.addClause(m.lit(b[2].satVar()), m.lit(b[1].satVar()));
        });
        s("sat()", m -> {
            b(m, 2);
            m.sat();
        });
        s("addClauseFalse(BoolVar)", m -> m.addClauseFalse(b(m, 2)[0]));
        s("addClauseTrue(BoolVar)", m -> m.addClauseTrue(b(m, 2)[0]));
        s("addClauses(BoolVar[],BoolVar[])", m -> {
            BoolVar[] b = b(m, 3);
            m.addClauses(new BoolVar[]{b[0], b[1]}, new BoolVar[]{b[2]});
        });
        s("addClauses(LogOp)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClauses(LogOp.or(b[0], LogOp.and(b[1], LogOp.nor(b[2]))));
        });
        s("addClausesAtMostNMinusOne(BoolVar[])", m -> m.addClausesAtMostNMinusOne(b(m, 3)));
        s("addClausesAtMostOne(BoolVar[])", m -> m.addClausesAtMostOne(b(m, 3)));
        s("addClausesBoolAndArrayEqVar(BoolVar[],BoolVar)", m -> m.addClausesBoolAndArrayEqVar(b(m, 2), m.boolVar("r")));
        s("addClausesBoolAndArrayEqualFalse(BoolVar[])", m -> m.addClausesBoolAndArrayEqualFalse(b(m, 3)));
        s("addClausesBoolAndEqVar(BoolVar,BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClausesBoolAndEqVar(b[0], b[1], b[2]);
        });
        s("addClausesBoolEq(BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 2);
            m.addClausesBoolEq(b[0], b[1]);
        });
        s("addClausesBoolIsEqVar(BoolVar,BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClausesBoolIsEqVar(b[0], b[1], b[2]);
        });
        s("addClausesBoolIsLeVar(BoolVar,BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClausesBoolIsLeVar(b[0], b[1], b[2]);
        });
        s("addClausesBoolIsLtVar(BoolVar,BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClausesBoolIsLtVar(b[0], b[1], b[2]);
        });
        s("addClausesBoolIsNeqVar(BoolVar,BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClausesBoolIsNeqVar(b[0], b[1], b[2]);
        });
        s("addClausesBoolLe(BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 2);
            m.addClausesBoolLe(b[0], b[1]);
        });
        s("addClausesBoolLt(BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 2);
            m.addClausesBoolLt(b[0], b[1]);
        });
        s("addClausesBoolNot(BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 2);
            m.addClausesBoolNot(b[0], b[1]);
        });
        s("addClausesBoolOrArrayEqVar(BoolVar[],BoolVar)", m -> m.addClausesBoolOrArrayEqVar(b(m, 2), m.boolVar("r")));
        s("addClausesBoolOrArrayEqualTrue(BoolVar[])", m -> m.addClausesBoolOrArrayEqualTrue(b(m, 3)));
        s("addClausesBoolOrEqVar(BoolVar,BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClausesBoolOrEqVar(b[0], b[1], b[2]);
        });
        s("addClausesBoolXorEqVar(BoolVar,BoolVar,BoolVar)", m -> {
            BoolVar[] b = b(m, 3);
            m.addClausesBoolXorEqVar(b[0], b[1], b[2]);
        });
        s("addClausesMaxBoolArrayLessEqVar(BoolVar[],BoolVar)", m -> m.addClausesMaxBoolArrayLessEqVar(b(m, 2), m.boolVar("r")));
        s("addClausesSumBoolArrayGreaterEqVar(BoolVar[],BoolVar)", m -> m.addClausesSumBoolArrayGreaterEqVar(b(m, 2), m.boolVar("r")));
        s("addClausesSumBoolArrayLessEqKVar(BoolVar[],BoolVar)", m -> m.addClausesSumBoolArrayLessEqKVar(b(m, 3), m.boolVar("r")));
        s("addConstructiveDisjunction(Constraint[])", m -> {
            IntVar[] x = x(m, 2, 0, 3);
            m.addConstructiveDisjunction(m.arithm(x[0], "<", x[1]), m.arithm(x[0], "=", 3));
        });
        s("addElement(IntVar,int[],IntVar,int)", m -> m.addElement(v(m, "v", 0, 5), ints(3, 1, 4), v(m, "i", 0, 2), 0));
        s("addTable(IntVar[],HybridTuples)", m -> {
            HybridTuples h = new HybridTuples();
            h.add(HybridTuples.eq(1), HybridTuples.any());
            h.add(HybridTuples.ne(1), HybridTuples.col(0));
            m.addTable(x(m, 2, 0, 2), h);
        });
        s("addTable(IntVar[],Tuples)", m -> m.addTable(x(m, 2, 0, 2), tuples(true, ints(0, 1), ints(2, 2))));

        // ---------------------------------------------------------------- scheduling
        s("cumulative(IntVar[],int[],int[],int)", m -> m.cumulative(x(m, 3, 0, 3), ints(1, 2, 1), ints(1, 1, 2), 2).post());
        s("cumulative(List,List,IntVar)", m -> m.cumulative(Arrays.asList(tasks(m, 2)), Arrays.asList(m.intVar(1), m.intVar(1)), v(m, "c", 1, 2)).post());
        s("cumulative(Task[],IntVar[],IntVar)", m -> m.cumulative(tasks(m, 3), m.intVarArray("h", 3, 1, 2), v(m, "c", 2, 3)).post());
        s("cumulative(Task[],IntVar[],IntVar,boolean,boolean)", m -> m.cumulative(tasks(m, 3), m.intVarArray("h", 3, 1, 2), v(m, "c", 2, 3), true, false).post());
        s("setTimes(Task[])", m -> {
            Task[] t = tasks(m, 2);
            cum(m, t);
            m.setTimes(t);
        });
        s("smallest(Task[]), smallest(Task[],ArbitrationRule), before(Task,Task,ArbitrationRule)", m -> {
            Task[] t = tasks(m, 2);
            cum(m, t);
            m.smallest(t);
            m.smallest(t, ISchedulingFactory.ArbitrationRule.MIN_ECT);
            m.before(t[0], t[1], ISchedulingFactory.ArbitrationRule.MIN_EST);
        });
    }

    //***********************************************************************************
    // CHECKS
    //***********************************************************************************

    /**
     * The modes in which the cases are checked: some factories build different constraints (or variables) with
     * the SAT solver or with LCG.
     */
    private enum Mode {
        DEFAULT, SAT, LCG;

        Settings settings() {
            switch (this) {
                case SAT:
                    return SettingsBuilder.init().setEnableSAT(true).build();
                case LCG:
                    return SettingsBuilder.init().setLCG(true).build();
                default:
                    return SettingsBuilder.init().build();
            }
        }
    }

    @DataProvider
    public Object[][] cases() {
        List<Object[]> res = new ArrayList<>();
        for (Mode mode : Mode.values()) {
            for (Case c : CASES) {
                res.add(new Object[]{mode, c});
            }
        }
        return res.toArray(new Object[0][]);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "cases")
    public void testCase(Mode mode, Case c) {
        Settings settings = mode.settings();
        // what the solver itself does not support (in this mode) is out of the scope of the duplication
        Model direct = new Model("m", settings);
        String stats;
        try {
            c.body().accept(direct);
            stats = SpecTestUtils.solve(direct, 50_000);
        } catch (Throwable e) {
            throw new SkipException("not supported by the solver in mode " + mode + ": " + e);
        }
        direct = new Model("m", settings);
        c.body().accept(direct);
        RecordingModel recorded = new RecordingModel("m", settings);
        c.body().accept(recorded);
        if (c.name().contains("[unsupported by design]")) {
            SpecException e = Assert.expectThrows(SpecException.class, recorded::snapshot);
            Assert.assertTrue(e.getMessage().contains("lambda"), e.getMessage());
            return;
        }
        ModelSpec spec = recorded.snapshot();
        Model replayed = spec.instantiate().model();
        String expected = SpecTestUtils.structure(direct);
        Assert.assertEquals(SpecTestUtils.structure(recorded), expected, "recording changes the model");
        Assert.assertEquals(SpecTestUtils.structure(replayed), expected, "the replayed model differs");
        Assert.assertEquals(SpecTestUtils.solve(recorded, 50_000), stats, "the recorded model searches differently");
        Assert.assertEquals(SpecTestUtils.solve(replayed, 50_000), stats, "the replayed model searches differently");
    }

    /**
     * @return the factory methods dealing only with integer and boolean variables
     */
    private static Set<String> scope() {
        Set<String> scope = new TreeSet<>();
        for (Method m : IModel.class.getMethods()) {
            if (!m.isDefault() || Modifier.isStatic(m.getModifiers()) || m.isSynthetic() || RecordingModel.EXCLUDED.contains(m.getName())) {
                continue;
            }
            String types = Arrays.stream(m.getGenericParameterTypes()).map(Type::getTypeName)
                    .collect(Collectors.joining(" ")) + " " + m.getGenericReturnType().getTypeName();
            boolean real = Arrays.stream(m.getParameterTypes()).anyMatch(p -> p == double.class || p == double[].class)
                    && !m.getName().equals("multiCostRegular");
            String sig = signature(m.getName(), m.getParameterTypes());
            if (OUT_OF_SCOPE.stream().noneMatch(types::contains) && !real && !NOT_IMPLEMENTED.contains(sig)) {
                scope.add(sig);
            }
        }
        return scope;
    }

    private static String signature(String name, Class<?>[] params) {
        return name + Arrays.stream(params).map(Class::getSimpleName).collect(Collectors.joining(",", "(", ")"));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testEveryIntFactoryMethodIsCovered() {
        Set<String> called = new TreeSet<>();
        for (Mode mode : Mode.values()) {
            for (Case c : CASES) {
                record(mode, c, called);
            }
        }
        List<String> missing = scope().stream().filter(s -> !called.contains(s)).collect(Collectors.toList());
        Assert.assertTrue(missing.isEmpty(), missing.size() + " factory methods are not covered: " + missing);
    }

    /**
     * Run a case on a recording model and collect the factory methods called at top level.
     */
    private static void record(Mode mode, Case c, Set<String> called) {
        {
            RecordingModel m = new RecordingModel("m", mode.settings());
            try {
                c.body().accept(m);
            } catch (Throwable ignored) {
                // not supported by the solver in this mode
                return;
            }
            // the calls are recorded, whether journalizable or not
            m.getRecorder().unsupported().forEach(u -> called.add(u.operation().replace(" ", "")));
            try {
                for (Step s : m.snapshot().steps()) {
                    if (s instanceof Step.Call) {
                        called.add(((Step.Call) s).method().toString().replace(" ", ""));
                    }
                }
            } catch (SpecException ignored) {
                // unsupported by design
            }
        }
    }
}
