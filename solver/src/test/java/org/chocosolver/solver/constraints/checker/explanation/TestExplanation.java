/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.explanation;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.constraints.binary.*;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.nary.PropIntValuePrecedeChain;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffAC;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffBC;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffInst;
import org.chocosolver.solver.constraints.nary.channeling.PropEnumDomainChanneling;
import org.chocosolver.solver.constraints.nary.channeling.PropInverseChannelAC;
import org.chocosolver.solver.constraints.nary.channeling.PropInverseChannelBC;
import org.chocosolver.solver.constraints.nary.element.PropElementV_fast;
import org.chocosolver.solver.constraints.nary.lex.PropLex;
import org.chocosolver.solver.constraints.nary.min_max.PropMax;
import org.chocosolver.solver.constraints.nary.min_max.PropMin;
import org.chocosolver.solver.constraints.nary.sum.*;
import org.chocosolver.solver.constraints.reification.PropXeqYHalfReif;
import org.chocosolver.solver.constraints.reification.PropXinSHalfReif;
import org.chocosolver.solver.constraints.reification.PropXleYHalfReif;
import org.chocosolver.solver.constraints.reification.PropXneYHalfReif;
import org.chocosolver.solver.constraints.ternary.*;
import org.chocosolver.solver.constraints.unary.*;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.search.loop.learn.LazyClauseGeneration;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.trace.IMessage;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.VariableUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.LongStream;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/2023
 */
//@Ignore
public class TestExplanation {

    @DataProvider(name = "propagators")
    public Object[][] getPropagators() {
        Object[][] objs = new Object[][]{
                //unary
                {PropEqualXC.class, new Class[]{IntVar.class, int.class}, new Object[]{null, null}},
                {PropGreaterOrEqualXC.class, new Class[]{IntVar.class, int.class}, new Object[]{null, null}},
                {PropLessOrEqualXC.class, new Class[]{IntVar.class, int.class}, new Object[]{null, null}},
                {PropMember.class, new Class[]{IntVar.class, IntIterableRangeSet.class, boolean.class}, new Object[]{null, null, false}},
                {PropNotEqualXC.class, new Class[]{IntVar.class, int.class}, new Object[]{null, null}},
                {PropNotMember.class, new Class[]{IntVar.class, IntIterableRangeSet.class, boolean.class}, new Object[]{null, null, false}},
                //
                {PropAbsolute.class, new Class[]{IntVar.class, IntVar.class}, new Object[]{null}},
                {PropEqualX_Y.class, new Class[]{IntVar.class, IntVar.class}, new Object[]{null}},
                {PropEqualX_YC.class, new Class[]{IntVar[].class, int.class}, new Object[]{2, null}},
                {PropEqualXY_C.class, new Class[]{IntVar[].class, int.class}, new Object[]{2, null}},
                {PropGreaterOrEqualX_Y.class, new Class[]{IntVar[].class}, new Object[]{2}},
                {PropGreaterOrEqualX_YC.class, new Class[]{IntVar[].class, int.class}, new Object[]{2, null}},
                {PropGreaterOrEqualXY_C.class, new Class[]{IntVar[].class, int.class}, new Object[]{2, null}},
                {PropLessOrEqualXY_C.class, new Class[]{IntVar[].class, int.class}, new Object[]{2, null}},
                {PropNotEqualX_Y.class, new Class[]{IntVar.class, IntVar.class}, new Object[]{null}},
                {PropNotEqualX_YC.class, new Class[]{IntVar[].class, int.class}, new Object[]{2, null}},
                {PropNotEqualXY_C.class, new Class[]{IntVar[].class, int.class}, new Object[]{2, null}},
                {PropScale.class, new Class[]{IntVar.class, int.class, IntVar.class}, new Object[]{null, 2, null}},
                {PropScale.class, new Class[]{IntVar.class, int.class, IntVar.class}, new Object[]{null, 3, null}},
                {PropTimesNaive.class, new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null}},
                {PropDivXYZ.class, new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null}},
//                 nary
                {PropSum.class, new Class[]{IntVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 1, "=", 0}},
                {PropSum.class, new Class[]{IntVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 1, ">=", 0}},
                {PropSum.class, new Class[]{IntVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 1, "<=", 0}},
                {PropSum.class, new Class[]{IntVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 1, "!=", 0}},
                {PropSumBool.class, new Class[]{BoolVar[].class, int.class, Operator.class, IntVar.class, int.class}, new Object[]{4, 2, "=", null, 0}},
                {PropSumBool.class, new Class[]{BoolVar[].class, int.class, Operator.class, IntVar.class, int.class}, new Object[]{4, 2, ">=", null, 0}},
                {PropSumBool.class, new Class[]{BoolVar[].class, int.class, Operator.class, IntVar.class, int.class}, new Object[]{4, 2, "<=", null, 0}},
                {PropSumBool.class, new Class[]{BoolVar[].class, int.class, Operator.class, IntVar.class, int.class}, new Object[]{4, 2, "!=", null, 0}},
                {PropSumBoolIncr.class, new Class[]{BoolVar[].class, int.class, Operator.class, IntVar.class, int.class}, new Object[]{4, 2, "=", null, 0}},
                {PropSumBoolIncr.class, new Class[]{BoolVar[].class, int.class, Operator.class, IntVar.class, int.class}, new Object[]{4, 2, ">=", null, 0}},
                {PropSumBoolIncr.class, new Class[]{BoolVar[].class, int.class, Operator.class, IntVar.class, int.class}, new Object[]{4, 2, "<=", null, 0}},
                {PropSumBoolIncr.class, new Class[]{BoolVar[].class, int.class, Operator.class, IntVar.class, int.class}, new Object[]{4, 2, "!=", null, 0}},
                {PropSumFullBool.class, new Class[]{BoolVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 2, "=", 2}},
                {PropSumFullBool.class, new Class[]{BoolVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 2, ">=", 0}},
                {PropSumFullBool.class, new Class[]{BoolVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 2, "<=", 0}},
                {PropSumFullBool.class, new Class[]{BoolVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 2, "!=", 0}},
                {PropSumFullBoolIncr.class, new Class[]{BoolVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 2, "=", 2}},
                {PropSumFullBoolIncr.class, new Class[]{BoolVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 2, ">=", 0}},
                {PropSumFullBoolIncr.class, new Class[]{BoolVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 2, "<=", 0}},
                {PropSumFullBoolIncr.class, new Class[]{BoolVar[].class, int.class, Operator.class, int.class}, new Object[]{4, 2, "!=", 0}},
                {PropScalar.class, new Class[]{IntVar[].class, int[].class, int.class, Operator.class, int.class}, new Object[]{4, new int[]{3, 2, -2, -3}, 2, "=", 0}},
                {PropScalar.class, new Class[]{IntVar[].class, int[].class, int.class, Operator.class, int.class}, new Object[]{4, new int[]{3, 2, -2, -3}, 2, ">=", 0}},
                {PropScalar.class, new Class[]{IntVar[].class, int[].class, int.class, Operator.class, int.class}, new Object[]{4, new int[]{3, 2, -2, -3}, 2, "<=", 0}},
                {PropScalar.class, new Class[]{IntVar[].class, int[].class, int.class, Operator.class, int.class}, new Object[]{4, new int[]{3, 2, -2, -3}, 2, "!=", 0}},
                {PropMaxBC.class, new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null}},
                {PropMinBC.class, new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null}},
                {PropMax.class, new Class[]{IntVar[].class, IntVar.class}, new Object[]{4, null}},
                {PropMin.class, new Class[]{IntVar[].class, IntVar.class}, new Object[]{4, null}},
                {PropXleYHalfReif.class, new Class[]{IntVar.class, IntVar.class, BoolVar.class}, new Object[]{null, null, null}},
                {PropXeqYHalfReif.class, new Class[]{IntVar.class, IntVar.class, BoolVar.class}, new Object[]{null, null, null}},
                {PropXneYHalfReif.class, new Class[]{IntVar.class, IntVar.class, BoolVar.class}, new Object[]{null, null, null}},
                {PropXinSHalfReif.class, new Class[]{IntVar.class, IntIterableRangeSet.class, BoolVar.class}, new Object[]{null, null, null}},
                {PropXplusYeqZ.class, new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null, null, null}},
                {PropAllDiffInst.class, new Class[]{IntVar[].class}, new Object[]{6}},
                {PropAllDiffBC.class, new Class[]{IntVar[].class}, new Object[]{6}},
                {PropAllDiffAC.class, new Class[]{IntVar[].class, boolean.class}, new Object[]{6, false}},
                {PropLex.class, new Class[]{IntVar[].class, IntVar[].class, boolean.class}, new Object[]{5, 5, null}},
                {PropElementV_fast.class, new Class[]{IntVar.class, IntVar[].class, IntVar.class}, new Object[]{null, 6, null}},
                {PropEnumDomainChanneling.class, new Class[]{BoolVar[].class, IntVar.class, int.class}, new Object[]{5, null, null}},
                {PropInverseChannelBC.class, new Class[]{IntVar[].class, IntVar[].class, int.class, int.class}, new Object[]{6, 6, 1, 1}},
                {PropInverseChannelAC.class, new Class[]{IntVar[].class, IntVar[].class, int.class, int.class}, new Object[]{6, 6, 1, 1}},
                {PropIntValuePrecedeChain.class, new Class[]{IntVar[].class, int.class, int.class}, new Object[]{6, 1, 3}},
                //{PropCardinality.class, new Class[]{SetVar.class, IntVar.class}, new Object[]{null, null}}
        };
        return Providers.merge(objs, getSeeds());
    }

    @DataProvider(name = "constraints")
    public Object[][] getConstraints() {
        Object[][] objs = new Object[][]{
                {"allDifferent", new Class[]{IntVar[].class}, new Object[]{6}},
                {"allEqual", new Class[]{IntVar[].class}, new Object[]{6}},
                {"allEqual", new Class[]{IntVar[].class}, new Object[]{6}},
                {"among", new Class[]{IntVar.class, IntVar[].class, int[].class}, new Object[]{null, 6, 6}},
                {"and", new Class[]{BoolVar[].class}, new Object[]{6}},
                {"argmax", new Class[]{IntVar.class, int.class, IntVar[].class}, new Object[]{null, null, 6}},
                {"argmin", new Class[]{IntVar.class, int.class, IntVar[].class}, new Object[]{null, null, 6}},
                {"atMostNValues", new Class[]{IntVar[].class, IntVar.class, boolean.class}, new Object[]{3, null, null}},
                {"atLeastNValues", new Class[]{IntVar[].class, IntVar.class, boolean.class}, new Object[]{6, null, null}},
                {"binPacking", new Class[]{IntVar[].class, int[].class, IntVar[].class, int.class}, new Object[]{6, 6, 4, null}},
                {"bitsIntChanneling", new Class[]{BoolVar[].class, IntVar.class}, new Object[]{6, null}},
                {"boolsIntChanneling", new Class[]{BoolVar[].class, IntVar.class, int.class}, new Object[]{6, null, null}},
                {"circuit", new Class[]{IntVar[].class}, new Object[]{6}},
                {"circuit", new Class[]{IntVar[].class, int.class}, new Object[]{6, null}},
                {"count", new Class[]{IntVar.class, IntVar[].class, IntVar.class}, new Object[]{null, 6, null}},
                {"count", new Class[]{int.class, IntVar[].class, IntVar.class}, new Object[]{null, 6, null}},
                {"decreasing", new Class[]{IntVar[].class, int.class}, new Object[]{6, null}},
                {"div", new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null, null, null}},
                {"increasing", new Class[]{IntVar[].class, int.class}, new Object[]{6, null}},
                {"inverseChanneling", new Class[]{IntVar[].class, IntVar[].class, int.class, int.class}, new Object[]{6, 6, null, null}},
                {"inverseChanneling", new Class[]{IntVar[].class, IntVar[].class}, new Object[]{6, 6}},
                {"lexLess", new Class[]{IntVar[].class, IntVar[].class}, new Object[]{6, 6}},
                {"lexLessEq", new Class[]{IntVar[].class, IntVar[].class}, new Object[]{6, 6}},
                {"max", new Class[]{IntVar.class, IntVar[].class}, new Object[]{null, 6}},
                {"max", new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null, null, null}},
                {"max", new Class[]{BoolVar.class, BoolVar[].class}, new Object[]{null, 6}},
                {"min", new Class[]{IntVar.class, IntVar[].class}, new Object[]{null, 6}},
                {"min", new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null, null, null}},
                {"min", new Class[]{BoolVar.class, BoolVar[].class}, new Object[]{null, 6}},
                {"notAllEqual", new Class[]{IntVar[].class}, new Object[]{6}},
                {"nValues", new Class[]{IntVar[].class, IntVar.class}, new Object[]{6, null}},
                {"or", new Class[]{BoolVar[].class}, new Object[]{6}},
                {"path", new Class[]{IntVar[].class, IntVar.class, IntVar.class}, new Object[]{6, null, null}},
                {"path", new Class[]{IntVar[].class, IntVar.class, IntVar.class, int.class}, new Object[]{6, null, null, null}},
                {"subPath", new Class[]{IntVar[].class, IntVar.class, IntVar.class, int.class, IntVar.class}, new Object[]{6, null, null, null, null}},
                {"subCircuit", new Class[]{IntVar[].class, int.class, IntVar.class}, new Object[]{6, null, null}},
                {"times", new Class[]{IntVar.class, IntVar.class, int.class}, new Object[]{null, null, null}},
                {"times", new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null, null, null}},
                {"times", new Class[]{IntVar.class, int.class, IntVar.class}, new Object[]{null, null, null}},

                //not supported {"allDifferentExcept0", new Class[]{IntVar[].class}, new Object[]{6}},
                //not supported {"cumulative", new Class[]{IntVar[].class, int[].class, int[].class, int.class}, new Object[]{6, 6, 6, null}},
                //not supported {"diffN", new Class[]{IntVar[].class, IntVar[].class, IntVar[].class, IntVar[].class, boolean.class}, new Object[]{4, 4, 4, 4, null}},
                //not supported {"knapsack", new Class[]{IntVar[].class, IntVar.class, IntVar.class, int[].class, int[].class}, new Object[]{4, null, null, 4, 4}},
                // ignored {"clausesIntChanneling", new Class[]{IntVar.class, BoolVar[].class, BoolVar[].class}, new Object[]{null, 6, 6}},
                // ignored {"intValuePrecedeChain", new Class[]{IntVar[].class, int[].class}, new Object[]{6, 6}},
                //ignored: decomposed and generation not straightforward  {"globalCardinality", new Class[]{IntVar[].class, int[].class, IntVar[].class, boolean.class}, new Object[]{6, 6, 6, null}},
                //not supported {"sort", new Class[]{IntVar[].class, IntVar[].class}, new Object[]{6, 6}},
                //not supported {"square", new Class[]{IntVar.class, IntVar.class}, new Object[]{null, null}},
                //not supported {"tree", new Class[]{IntVar[].class, IntVar.class}, new Object[]{6, null}},
                //not supported {"tree", new Class[]{IntVar[].class, IntVar.class, int.class}, new Object[]{6, null, null}},
        };
        return Providers.merge(objs, getSeeds());
    }

    @DataProvider(name = "intconstraints")
    public Object[][] getIntConstraints() {
        List<Object[]> objs = new ArrayList<>();
        mm:
        for (Method m : IIntConstraintFactory.class.getMethods()) {
            if (!m.getReturnType().isAssignableFrom(Constraint.class)) continue;
            // check parameters, should be among IntVar, IntVar[], int or int[]
            Class<?>[] parameterTypes = m.getParameterTypes();
            if (parameterTypes.length == 0) continue;
            Object[] info = new Object[parameterTypes.length];
            String[] classes = new String[parameterTypes.length];
            String[] objects = new String[parameterTypes.length];
            int i = 0;
            for (Class<?> parameterType : parameterTypes) {
                if (BoolVar.class.isAssignableFrom(parameterType)) {
                    info[i] = null;
                    classes[i] = "BoolVar.class";
                    objects[i] = null;
                } else if (BoolVar[].class.isAssignableFrom(parameterType)) {
                    info[i] = 5;
                    classes[i] = "BoolVar[].class";
                    objects[i] = String.valueOf(6);
                } else if (IntVar.class.isAssignableFrom(parameterType)) {
                    info[i] = null;
                    classes[i] = "IntVar.class";
                    objects[i] = null;
                } else if (IntVar[].class.isAssignableFrom(parameterType)) {
                    info[i] = 5;
                    classes[i] = "IntVar[].class";
                    objects[i] = String.valueOf(6);
                } else if (int.class.isAssignableFrom(parameterType)) {
                    info[i] = null;
                    classes[i] = "int.class";
                    objects[i] = null;
                } else if (int[].class.isAssignableFrom(parameterType)) {
                    info[i] = 5;
                    classes[i] = "int[].class";
                    objects[i] = String.valueOf(6);
                } else if (boolean.class.isAssignableFrom(parameterType)) {
                    info[i] = null;
                    classes[i] = "boolean.class";
                    objects[i] = null;
//                    bool = true;
                } else {
                    //System.out.println("skip -> "+m.getName()+" due to "+ parameterType.getName());
                    continue mm;
                }
                i++;
            }
            //if(!bool) continue;
            objs.add(new Object[]{m.getName(), parameterTypes, info});
            System.out.println("{\"" + m.getName() + "\", " +
                    "new Class[]{" + Arrays.toString(classes) + "}, " +
                    "new Object[]{" + Arrays.toString(objects).replace("[", "").replace("]", "") + "}},");
        }
        Object[][] robjs = new Object[objs.size()][];
        for (int i = 0; i < objs.size(); i++) {
            robjs[i] = objs.get(i);
        }
        return Providers.merge(robjs, getSeeds());
    }

    @DataProvider(name = "seed")
    public Object[][] getSeeds() {
        return LongStream.range(0, 400).mapToObj(l -> new Object[]{l}).toArray(Object[][]::new);
    }

    private static final Function<Model, IMessage> intValues = (m) ->
            () -> Arrays.stream(m.retrieveIntVars(true))
                    .filter(v -> !v.isAConstant())
                    .map(v -> v.getValue() + " ")
                    .reduce("", String::concat);

    @Test(groups = "lcg", timeOut = 60000, dataProvider = "propagators")
    public void testPropagator(Class<? extends Propagator<IntVar>> prop, Class<?>[] parameterTypes, Object[] info,
                               long seed) {
        boolean isExplained = false;
        Annotation[] annotations = prop.getAnnotations();
        for (Annotation annotation : annotations) {
            if (Objects.equals(annotation.annotationType(), Explained.class)) {
                isExplained = true;
            }
        }
        Assert.assertTrue(isExplained);
        mainLooop((l, s) -> buildModel(prop, parameterTypes, info, l, s), seed, Scalar::create);
    }

    @Test(groups = "lcg", timeOut = 60000, dataProvider = "constraints")
    public void testFromFactory(String constraint, Class<?>[] parameterTypes, Object[] info, long seed) {
        mainLooop((l, s) -> buildModel(constraint, parameterTypes, info, l, s), seed, Scalar::create);
    }

    @Test(groups = "1s", dataProvider = "seed")
    public void testReifyInS(long seed) {
        mainLooop((l, s) -> {
            Model model = new Model(Settings.init().setLCG(l));
            IntVar x = model.intVar("x", 1, 7);
            IntIterableRangeSet set = new IntIterableRangeSet();
            set.addBetween(2, 5);
            BoolVar r = model.boolVar("r");
            model.reifyXinS(x, set, r);
            model.addHook("variables", new ArrayList<Variable>() {{
                add(x);
                add(r);
            }});
            model.getSolver().setSearch(Search.randomSearch(new IntVar[]{x, r}, seed));
            return model;
        }, seed, Table::create);
    }

    @SuppressWarnings("unchecked")
    public void mainLooop(BiFunction<Boolean, Long, Model> modeler, long seed, BiFunction<IntVar[],
            Long, Cut> cutsup) {
        boolean PRINT = false;
        int LIMIT = 100;
        Model ref = modeler.apply(false, seed);
        List<Variable> variables = (List<Variable>) ref.getHook("variables");
        if (variables.stream().allMatch(Variable::isAConstant)) {
            return;
        }
        IntVar[] rvars = variables.stream().filter(VariableUtils::isInt).map(Variable::asIntVar).toArray(IntVar[]::new);
        if (PRINT) System.out.println(ref);
        List<Cut> cuts = new ArrayList<>();
        //BiFunction<IntVar[], Long, Cut> sup = cut::create; //Table::table;
        do {
            ref.getSolver().reset();
            Cut cut = cutsup.apply(rvars, seed + cuts.size());
            cuts.add(cut);
            cut.post(ref, rvars);
            ref.getSolver().limitSolution(LIMIT);
            ref.getSolver().findAllSolutions();
        } while (ref.getSolver().getSearchState() == SearchState.STOPPED
                || ref.getSolver().getFailCount() == 0);
        if (PRINT) {
            System.out.println(ref);
            ref.getSolver().showDecisions();
            ref.getSolver().showSolutions(intValues.apply(ref));
            ref.getSolver().showShortStatistics();
        }
        ref.getSolver().reset();
        ref.getSolver().findAllSolutions();
//        ref.getSolver().printShortStatistics();
        if (PRINT) System.out.println("#Solutions : " + ref.getSolver().getSolutionCount());
        if (ref.getSolver().getFailCount() > 0) {
            Model test = modeler.apply(true, seed);
            variables = (List<Variable>) test.getHook("variables");
            IntVar[] tvars = variables.stream().filter(VariableUtils::isInt).map(Variable::asIntVar).toArray(IntVar[]::new);
            for (Cut cut : cuts) {
                cut.post(test, tvars);
            }
            if (PRINT) {
                LazyClauseGeneration.VERBOSE = true;
                System.out.println(test);
                test.getSolver().showDecisions();
                test.getSolver().showSolutions(intValues.apply(test));
                test.getSolver().showShortStatistics();
            }
            //test.getSolver().showStatisticsDuringResolution(2000);
            test.getSolver().findAllSolutions();
//            test.getSolver().printShortStatistics();
            Assert.assertEquals(test.getSolver().getSolutionCount(), ref.getSolver().getSolutionCount(), String.format("Failed with seed %d", seed));
        }
    }

    private Model buildModel(Class<? extends Propagator<?>> prop, Class<?>[] parameterTypes, Object[] info,
                             boolean lcg, long seed) {

        try {
            Model model = new Model(prop.getSimpleName(), Settings.init().setLCG(lcg));
            Constructor<?> constructor = prop.getConstructor(parameterTypes);
            Object[] parameters = new Object[parameterTypes.length];
            List<Variable> variables = new ArrayList<>();
            buildInputParameters(parameterTypes, info, variables, parameters, model, new Random(seed));
            model.addHook("variables", variables);
            Propagator<?> propagator = (Propagator<?>) constructor.newInstance(parameters);
            new Constraint(prop.getSimpleName(), propagator).post();
            model.getSolver().setSearch(new FullyRandom(variables.toArray(new Variable[0]), seed));
            return model;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Model buildModel(String method, Class<?>[] parameterTypes, Object[] info, boolean lcg, long seed) {

        try {
            Model model = new Model(method, Settings.init().setLCG(lcg));
            Method m = Model.class.getMethod(method, parameterTypes);
            Object[] parameters = new Object[parameterTypes.length];
            List<Variable> variables = new ArrayList<>();
            buildInputParameters(parameterTypes, info, variables, parameters, model, new Random(seed));
            model.addHook("variables", variables);
            Constraint c = (Constraint) m.invoke(model, parameters);
            c.post();
            model.getSolver().setSearch(new FullyRandom(variables.toArray(new Variable[0]), seed));
            return model;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildInputParameters(Class<?>[] parameterTypes, Object[]
            info, List<Variable> variables, Object[] parameters, Model model, Random rnd) {
        int i = 0;
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType.isArray()) {
                if (BoolVar[][].class == parameterType) {
                    int[] dim = (int[]) info[i];
                    BoolVar[][] _variables = new BoolVar[dim[0]][dim[1]];
                    for (int j = 0; j < _variables.length; j++) {
                        for (int k = 0; k < _variables[j].length; k++) {
                            _variables[j][k] = DomainBuilder.makeBoolVar(model, rnd, variables.size());
                            variables.add(_variables[j][k]);
                        }
                    }
                    parameters[i] = _variables;
                } else if (IntVar[][].class == parameterType) {
                    int[] dim = (int[]) info[i];
                    IntVar[][] _variables = new IntVar[dim[0]][dim[1]];
                    for (int j = 0; j < _variables.length; j++) {
                        for (int k = 0; k < _variables[j].length; k++) {
                            _variables[j][k] = DomainBuilder.makeIntVar(model, rnd, variables.size());
                            variables.add(_variables[j][k]);
                        }
                    }
                    parameters[i] = _variables;
                } else if (BoolVar[].class == parameterType) {
                    BoolVar[] _variables = new BoolVar[(int) info[i]];
                    for (int j = 0; j < _variables.length; j++) {
                        _variables[j] = DomainBuilder.makeBoolVar(model, rnd, variables.size());
                        variables.add(_variables[j]);
                    }
                    parameters[i] = _variables;
                } else if (IntVar[].class == parameterType) {
                    IntVar[] _variables = new IntVar[(int) info[i]];
                    for (int j = 0; j < _variables.length; j++) {
                        _variables[j] = DomainBuilder.makeIntVar(model, rnd, variables.size());
                        variables.add(_variables[j]);
                    }
                    parameters[i] = _variables;
                } else if (int[].class == parameterType) {
                    if (info[i] instanceof Integer) {
                        int[] _constants = new int[(int) info[i]];
                        for (int j = 0; j < _constants.length; j++) {
                            _constants[j] = DomainBuilder.makeInt(rnd);
                        }
                        parameters[i] = _constants;
                    } else if (info[i] instanceof int[]) {
                        parameters[i] = info[i];
                    }
                }
            } else {
                if (int.class == parameterType) {
                    if (info[i] == null) {
                        parameters[i] = DomainBuilder.makeInt(rnd);
                    } else {
                        parameters[i] = info[i];
                    }
                } else if (BoolVar.class == parameterType) {
                    BoolVar _variable = DomainBuilder.makeBoolVar(model, rnd, variables.size());
                    variables.add(_variable);
                    parameters[i] = _variable;
                } else if (IntVar.class == parameterType) {
                    IntVar _variable = DomainBuilder.makeIntVar(model, rnd, variables.size());
                    variables.add(_variable);
                    parameters[i] = _variable;
                } else if (Operator.class == parameterType) {
                    parameters[i] = Operator.get((String) info[i]);
                } else if (IntIterableRangeSet.class == parameterType) {
                    parameters[i] = new IntIterableRangeSet(DomainBuilder.makeInts(rnd));
                } else if (boolean.class == parameterType) {
                    if (info[i] == null) {
                        parameters[i] = rnd.nextBoolean();
                    } else {
                        parameters[i] = info[i];
                    }
                }
            }
            i++;
        }
    }

    public interface Cut {
        void post(Model model, IntVar[] vars);
    }

    private static class Scalar implements Cut {
        final int[] coeffs;
        final int b;
        final String op;

        public Scalar(int[] coeffs, int b, String op) {
            this.coeffs = coeffs;
            this.b = b;
            this.op = op;
        }

        public void post(Model model, IntVar[] vars) {
            model.scalar(vars, coeffs, op, b).post();
        }

        public static Scalar create(IntVar[] vars, long seed) {
            Random r = new Random(seed);
            int n = vars.length;
            int[] coeffs = new int[n];
            int lb = 0;
            int ub = 0;
            for (int i = 0; i < n; i++) {
                coeffs[i] = -4 + r.nextInt(9);
                lb += coeffs[i] * (coeffs[i] >= 0 ? vars[i].getLB() : vars[i].getUB());
                ub += coeffs[i] * (coeffs[i] >= 0 ? vars[i].getUB() : vars[i].getLB());
            }
            int b = lb + r.nextInt(ub - lb + 1);
            String op = r.nextBoolean() ? ">=" : "<=";
            return new Scalar(coeffs, b, op);
        }
    }

    private static class Table implements Cut {

        final Tuples tuples;

        private Table(Tuples tuples) {
            this.tuples = tuples;
        }

        @Override
        public void post(Model model, IntVar[] vars) {
            model.table(vars, tuples).post();
        }

        public static Table create(IntVar[] vars, long seed) {
            Random r = new Random(seed);
            Tuples tuples = TuplesFactory.randomTuples(0.5, r, vars);
            return new Table(tuples);
        }

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Test(groups = "lcg", timeOut = 60_000, dataProvider = "trueOrFalse", dataProviderClass = Providers.class)
    public void testTable2(boolean lcg) {
        // Test bug l.724 in ISatFactory
        // What if the model detects inconsistency during declaration?
        Model model = new Model(Settings.init().setLCG(lcg));
        IntVar x = model.intVar("x", 3, 4);
        IntVar y = model.intVar("y", 3, 4);
        Tuples t1 = new Tuples(true);
        t1.add(4, 4);
        model.table(new IntVar[]{x, y}, t1).post();
        Tuples t2 = new Tuples(true);
        t2.add(4, 3);
        model.table(new IntVar[]{x, y}, t2).post();
        Assert.assertFalse(model.getSolver().solve());
    }

    @Test(groups = "lcg", timeOut = 60_000, dataProvider = "trueOrFalse", dataProviderClass = Providers.class)
    public void testDummy1(boolean lcg) {
        Model model = new Model(Settings.init().setLCG(lcg));
        IntVar x = model.intVar("x", 3, 4);
        model.member(x, 3, 4).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertFalse(model.getSolver().solve());
    }

    @Test(groups = "lcg", timeOut = 60_000, dataProvider = "trueOrFalse", dataProviderClass = Providers.class)
    public void testDummy3(boolean lcg) {
        Model model = new Model(Settings.init().setLCG(lcg));
        IntVar x = model.intVar("x", 3, 4);
        model.member(x, 3, 4).post();
        model.setObjective(true, x);
        model.getSolver().setSearch(Search.inputOrderLBSearch(x));
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertFalse(model.getSolver().solve());
    }

    @Test(groups = "lcg", timeOut = 60_000, dataProvider = "trueOrFalse", dataProviderClass = Providers.class)
    public void testNotAllEquals1(boolean lcg) {
        Model model = new Model(Settings.init().setLCG(lcg));
        IntVar x = model.intVar("x", 3, 4);
        IntVar y = model.intVar("y", 3, 4);
        model.notAllEqual(x, y).post();
        //model.getSolver().showSolutions(x, y);
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertFalse(model.getSolver().solve());
    }

    @Test(groups = "lcg", timeOut = 60_000, dataProvider = "trueOrFalse", dataProviderClass = Providers.class)
    public void testAtMostNValues(boolean lcg) {
        Model model = new Model(Settings.init().setLCG(lcg));
        IntVar[] x = model.intVarArray("x", 3, 2, 4);
        IntVar z = model.intVar("z", 1, 2);
        model.atMostNValues(x, z, true).post();
//        model.getSolver().showSolutions(x);
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 24);
    }
}
