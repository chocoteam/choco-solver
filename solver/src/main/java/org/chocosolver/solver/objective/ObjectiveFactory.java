/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * Factory to create (mono-)objective managers.
 *
 * @author Arnaud Malapert
 */
public final class ObjectiveFactory {


    private ObjectiveFactory() {
        super();
    }

    /**
     * Define a manager for satisfaction problems.
     *
     * @return a singleton object
     */
    public static IObjectiveManager<Variable> SAT() {
        return SATManager.getInstance();
    }

    /**
     * Define the variable to optimize (maximize or minimize)
     * By default, the manager uses {@link IObjectiveManager#setStrictDynamicCut()} to avoid exploring worse solutions.
     *
     * @param objective variable to optimize
     * @param policy    {{@link ResolutionPolicy#MINIMIZE}/{@link ResolutionPolicy#MAXIMIZE}
     * @return the objective manager
     * @throws IllegalArgumentException if the policy is {@link ResolutionPolicy#SATISFACTION}.
     */
    public static IObjectiveManager<IntVar> makeObjectiveManager(IntVar objective, ResolutionPolicy policy) {
        IObjectiveManager<IntVar> objman;
        switch (policy) {
            case MINIMIZE:
                objman = new MinIntObjManager(objective);
                break;
            case MAXIMIZE:
                objman = new MaxIntObjManager(objective);
                break;
            default:
                throw new IllegalArgumentException("cant build integer objective manager :" + policy);
        }
        objman.setStrictDynamicCut();
        return objman;
    }

    /**
     * Define the variable to optimize (maximize or minimize)
     * By default, the manager uses {@link IObjectiveManager#setStrictDynamicCut()} to avoid exploring worse solutions.
     *
     * @param objective variable to optimize
     * @param policy    {{@link ResolutionPolicy#MINIMIZE}/{@link ResolutionPolicy#MAXIMIZE}
     * @return the objective manager
     * @throws IllegalArgumentException if the policy is {@link ResolutionPolicy#SATISFACTION}.
     */
    public static IObjectiveManager<RealVar> makeObjectiveManager(RealVar objective, ResolutionPolicy policy, double precision) {
        IObjectiveManager<RealVar> objman;
        switch (policy) {
            case MINIMIZE:
                objman = new MinRealObjManager(objective, precision);
                break;
            case MAXIMIZE:
                objman = new MaxRealObjManager(objective, precision);
                break;
            default:
                throw new IllegalArgumentException("cant build real objective manager :" + policy);
        }
        objman.setStrictDynamicCut();
        return objman;
    }

    /**
     * @param object to copy
     * @return copy built by a copy constructor if one exists, otherwise the parameter.
     */
    @SuppressWarnings("unchecked")
    public static <V> V copy(V object) {
        try {
            Class c = object.getClass();
            // Use the "copy constructor":
            Constructor ct = c.getConstructor(c);
            return (V) ct.newInstance(object);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // fails silently
        }
        return object;
    }
}

/**
 * A class for CSP (in opposition to COP) which matches {@link IObjectiveManager} requisites.
 */
final class SATManager implements IObjectiveManager<Variable> {

    private static final long serialVersionUID = 2115489336441115889L;
    private final static SATManager INSTANCE = new SATManager();

    public static SATManager getInstance() {
        return INSTANCE;
    }

    private SATManager() {}

    /**
     * readResolve method to preserve singleton property
     */
    private Object readResolve() {
        // Return the one true INSTANCE and let the garbage collector
        // take care of the INSTANCE impersonator.
        return INSTANCE;
    }

    @Override
    public ResolutionPolicy getPolicy() {
        return ResolutionPolicy.SATISFACTION;
    }

    @Override
    public boolean isOptimization() {
        return false;
    }

    @Override
    public Number getBestLB() {
        throw new UnsupportedOperationException("There is no objective bounds in satisfaction problems");
    }

    @Override
    public Number getBestUB() {
        throw new UnsupportedOperationException("There is no objective bounds in satisfaction problems");
    }

    @Override
    public boolean updateBestLB(Number lb) {
        throw new UnsupportedOperationException("There is no objective bounds in satisfaction problems");
    }

    @Override
    public boolean updateBestUB(Number ub) {
        throw new UnsupportedOperationException("There is no objective bounds in satisfaction problems");
    }

    @Override
    public Number getBestSolutionValue() {
        throw new UnsupportedOperationException("There is no objective variable in satisfaction problems");
    }

    @Override
    public Variable getObjective() {
        return null;
    }

    @Override
    public boolean updateBestSolution(Number n) {
        throw new UnsupportedOperationException("not a mono-objective optimization problem");
    }

    @Override
    public boolean updateBestSolution() {
        // nothing to do
        return false;
    }

    @Override
    public void setWalkingDynamicCut() {
        // nothing to do
    }

    @Override
    public void setStrictDynamicCut() {
        // nothing to do
    }

    @Override
    public void setCutComputer(Function<Number, Number> cutComputer) {
        // nothing to do
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        // nothing to do
    }

    @Override
    public String toString() {
        return "SAT";
    }

}