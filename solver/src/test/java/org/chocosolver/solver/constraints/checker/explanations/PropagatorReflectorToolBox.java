/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.explanations;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 31/05/2017.
 */
public class PropagatorReflectorToolBox {

    /**
     * Represents an operation that accepts three input arguments and returns no
     * result.  This is the three-arity specialization of {@link Consumer}.
     * Unlike most other functional interfaces, {@code TriConsumer} is expected
     * to operate via side-effects.
     *
     * <p>This is a <a href="package-summary.html">functional interface</a>
     * whose functional method is {@link #accept(Object, Object, Object)}.
     *
     * @param <T> the type of the first argument to the operation
     * @param <U> the type of the second argument to the operation
     * @param <V> the type of the third argument to the operation
     *
     * @see Consumer
     * @since 1.8
     */
    public interface TriConsumer<T, U, V>{
        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         * @param v the third input argument
         */
        void accept(T t, U u, V v);
    }

    /**
     *
     * @param DEBUG
     * @param prop
     * @param parameterTypes
     * @param info
     * @param iterations the number of loops to achieve for the test
     * @param cons the operation to perform on the propagator, its variables and the initial domains
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static void mainLoop(boolean DEBUG, Class<? extends Propagator> prop, Class[] parameterTypes,
                       Object[] info, int iterations, TriConsumer<Propagator, List<IntVar>,List<IntIterableRangeSet>> cons)
            throws NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        long seed  = 0;//System.currentTimeMillis();
        if(DEBUG)System.out.printf("Start with seed: %d\n", seed);
        Random rnd = new Random();
        Constructor constructor = prop.getConstructor(parameterTypes);
        Object[] parameters = new Object[parameterTypes.length];
        for (long ite = 0; ite < iterations; ite++) {
            //System.out.printf("Iteration %d\n", ite);
            Model model = new Model();
            rnd.setSeed(seed + ite);
            List<IntVar> variables = new ArrayList<>();
            buildInputParameters(parameterTypes, info, variables, parameters, model, rnd);
            List<IntIterableRangeSet> domains = extractDomains(variables);
            Propagator propagator = (Propagator) constructor.newInstance(parameters);
            cons.accept(propagator, variables, domains);
        }
    }


    @SuppressWarnings("Duplicates")
    private static void buildInputParameters(Class[] parameterTypes, Object[] info, List<IntVar> variables, Object[] parameters, Model model, Random rnd) {
        int i = 0;
        for (Class parameterType : parameterTypes) {
            if (parameterType.isArray()) {
                if (BoolVar[].class == parameterType) {
                    BoolVar[] _variables = new BoolVar[(int) info[i]];
                    for (int j = 0; j < _variables.length; j++) {
                        _variables[j] = DomainBuilder.makeBoolVar(model, rnd, variables.size());
                        variables.add(_variables[j]);
                    }
                    parameters[i] = _variables;
                }else
                if (IntVar[].class == parameterType) {
                    IntVar[] _variables = new IntVar[(int) info[i]];
                    for (int j = 0; j < _variables.length; j++) {
                        _variables[j] = DomainBuilder.makeIntVar(model, rnd, variables.size());
                        variables.add(_variables[j]);
                    }
                    parameters[i] = _variables;
                }
            } else {
                if (int.class == parameterType) {
                    if(info[i] == null) {
                        parameters[i] = DomainBuilder.makeInt(rnd);
                    }else{
                        parameters[i] = info[i];
                    }
                }else if (BoolVar.class == parameterType) {
                    BoolVar _variable = DomainBuilder.makeBoolVar(model, rnd, variables.size());
                    variables.add(_variable);
                    parameters[i] = _variable;
                }else if (IntVar.class == parameterType) {
                    IntVar _variable = DomainBuilder.makeIntVar(model, rnd, variables.size());
                    variables.add(_variable);
                    parameters[i] = _variable;
                }else if(Operator.class == parameterType){
                    parameters[i] = Operator.get((String) info[i]);
                }
            }
            i++;
        }
    }

    private static List<IntIterableRangeSet> extractDomains(List<IntVar> variables) {
        List<IntIterableRangeSet> domains = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            domains.add(new IntIterableRangeSet(variables.get(i)));
        }
        return domains;
    }

    public static BitSet[] extractStatus1(List<IntVar> variables, List<int[]> domains){
        BitSet[] status = new BitSet[domains.size()];
        for (int d = 0; d < variables.size(); d++) {
            status[d] = status(variables.get(d), domains.get(d));
        }
        return status;
    }

    private static BitSet status(IntVar variable, int[] domain) {
        BitSet status = new BitSet(domain.length);
        for (int i = 0; i < domain.length; i++) {
            status.set(i, variable.contains(domain[i]));
        }
        return status;
    }

    public static BitSet[] extractStatus2(List<IntVar> variables, List<IntIterableRangeSet> domains){
        BitSet[] status = new BitSet[domains.size()];
        for (int d = 0; d < variables.size(); d++) {
            status[d] = status(variables.get(d), domains.get(d));
        }
        return status;
    }

    private static BitSet status(IntVar variable, IntIterableRangeSet domain) {
        BitSet status = new BitSet();
        int o = domain.min();
        for (int val : domain) {
            status.set(val - o, variable.contains(val));
        }
        return status;
    }

}
