/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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

    public final static SATManager getInstance() {
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
    public void updateBestLB(Number lb) {
        throw new UnsupportedOperationException("There is no objective bounds in satisfaction problems");
    }

    @Override
    public void updateBestUB(Number ub) {
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
    public void updateBestSolution(Number n) {
        throw new UnsupportedOperationException("not a mono-objective optimization problem");
    }

    @Override
    public void updateBestSolution() {
        // nothing to do
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
