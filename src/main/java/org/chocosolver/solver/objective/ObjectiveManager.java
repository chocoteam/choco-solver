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
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;

import java.util.function.Function;

/**
 * Class to monitor the objective function and avoid exploring "worse" solutions
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since Oct. 2012
 * @
 * @param <V> type of objective variable
 * @param <N> related to the type of objective variable:
 *           if {@link #objective} is an {@link IntVar}, then this is an {@link Integer},
 *           if {@link #objective} is a {@link RealVar}, then this is a {@link Double},
 */
public class ObjectiveManager<V extends Variable, N extends Number> extends BoundsManager<N> implements ICause {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************


    /** The variable to optimize **/
    final protected V objective;
    /** set to <tt>true<tt/> if {@link #objective} is an integer variable, to <tt>false<tt/> if it is a real variable **/
    final private boolean intOrReal;
    /** if {@link #objective} is a real variable, define the precision to consider it as instantiated **/
    final private double precision;
    /** Define how the cut should be update when posting the cut **/
    private Function<N, N> mCutComputer;


    /**
     * Create an objective manager for satisfaction problem, ie when no objective is defined
     * @return a no-objective manager (singleton)
     */
    public static ObjectiveManager SAT() {
        return new ObjectiveManager<>(null, ResolutionPolicy.SATISFACTION, null);
    }

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create an objective manager
     * @param objective variable to optimize
     * @param policy resolution policy
     * @param precision if {@link #objective} is a real variable, define the precision to consider it as instantiated
     * @param intOrReal set to <tt>true<tt/> if {@link #objective} is an integer variable,
     *                  to <tt>false<tt/> if it is a real variable
     */
    @SuppressWarnings("unchecked")
    private ObjectiveManager(V objective, ResolutionPolicy policy, double precision, boolean intOrReal,
                             Function<N,N> cutComputer) {
        super(policy);
        this.policy = policy;
        this.objective = objective;
        this.precision = precision;
        this.intOrReal = intOrReal;
        this.mCutComputer = cutComputer;
        if (isOptimization()) {
            if(intOrReal){
                this.bestProvedLB = (N)(Integer)(getObjLB().intValue() - 1);
                this.bestProvedUB = (N)(Integer)(getObjUB().intValue() + 1);
            }else{
                this.bestProvedLB = (N)(Double)(getObjLB().doubleValue() - precision);
                this.bestProvedUB = (N)(Double)(getObjUB().doubleValue() + precision);
            }
        }
    }

    /**
     * Creates an optimization manager for an integer objective function (represented by an IntVar)
     * Enables to cut "worse" solutions
     *  @param objective variable (represent the value of a solution)
     * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
     * @param cutComputer define how the cut should be updated dynamically
     */
    @SuppressWarnings("unchecked")
    public ObjectiveManager(IntVar objective, ResolutionPolicy policy, Function<Integer,Integer> cutComputer) {
        this((V) objective, policy, 0, true, (Function<N, N>) cutComputer);
    }

    /**
     * Creates an optimization manager for a continuous objective function (represented by a RealVar)
     * Enables to cut "worse" solutions
     *  @param objective variable (represent the value of a solution)
     * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
     * @param precision precision parameter defining the minimum objective improvement between two solutions
     *                  (avoids wasting time enumerating a huge set of equivalent solutions)
     * @param cutComputer define how the cut should be updated dynamically
     */
    @SuppressWarnings("unchecked")
    public ObjectiveManager(RealVar objective, ResolutionPolicy policy, double precision, Function<N,N> cutComputer) {
        this((V) objective, policy, precision, false, cutComputer);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return isOptimization() && ruleStore.addBoundsRule((IntVar) objective);
    }

    @Override
    public String toString() {
        String st;
        switch (policy) {
            case SATISFACTION:
                return "SAT";
            case MINIMIZE:
                st = "Minimize";
                break;
            case MAXIMIZE:
                st = "Maximize";
                break;
            default:
                throw new UnsupportedOperationException("no objective manager");
        }
        if (intOrReal) {
            return String.format(st + " %s = %d", this.objective.getName(), getBestSolutionValue());
        } else {
            return String.format(st + " %s = %." + getNbDecimals() + "f", this.objective.getName(), getBestSolutionValue());
        }
    }

    private int getNbDecimals() {
        int dec = 0;
        double p = precision;
        while ((int) p <= 0 && dec <= 12) {
            dec++;
            p *= 10;
        }
        return dec;
    }

    /**
     * Informs the manager that a new solution has been found
     */
    public void update() {
        if (isOptimization()) {
            assert objective.isInstantiated();
            N newVal = getObjUB();
            if (policy == ResolutionPolicy.MINIMIZE) {
                updateBestUB(newVal);
            } else {
                updateBestLB(newVal);
            }
        }
    }

    /**
     * Prevent the model from computing worse quality solutions
     *
     * @throws org.chocosolver.solver.exception.ContradictionException if posting this cut fails
     */
    public void postDynamicCut() throws ContradictionException {
        if (isOptimization()) {
            if (intOrReal) {
                IntVar io = (IntVar) objective;
                if (policy == ResolutionPolicy.MINIMIZE) {
                    io.updateBounds(bestProvedLB.intValue(), mCutComputer.apply(bestProvedUB).intValue(), this);
                } else {
                    io.updateBounds(mCutComputer.apply(bestProvedLB).intValue(), bestProvedUB.intValue(), this);
                }
            } else {
                RealVar io = (RealVar) objective;
                if (policy == ResolutionPolicy.MINIMIZE) {
                    io.updateBounds(bestProvedLB.doubleValue(), mCutComputer.apply(bestProvedUB).doubleValue(), this);
                } else {
                    io.updateBounds(mCutComputer.apply(bestProvedLB).doubleValue(), bestProvedUB.doubleValue(), this);
                }
            }
        }
    }


    /**
     * States that lb is a global lower bound on the problem
     *
     * @param lb lower bound
     */
    public synchronized void updateBestLB(N lb) {
        if (bestProvedLB == null) {
            // this may happen with multi-thread resolution
            // when one thread find a model before one other is being launched
            bestProvedLB = lb;
        }
        if (lb.doubleValue() > bestProvedLB.doubleValue()) {
            bestProvedLB = lb;
        }
    }

    /**
     * States that ub is a global upper bound on the problem
     *
     * @param ub upper bound
     */
    public synchronized void updateBestUB(N ub) {
        if (bestProvedUB == null) {
            // this may happen with multi-thread resolution
            // when one thread find a model before one other is being launched
            bestProvedUB = ub;
        }
        if (ub.doubleValue() < bestProvedUB.doubleValue()) {
            bestProvedUB = ub;
        }
    }

    @SuppressWarnings("unchecked")
    private N getObjLB() {
        assert isOptimization();
        if (intOrReal) {
            Integer lb = ((IntVar) objective).getLB();
            if (bestProvedLB != null && bestProvedLB.intValue() > lb) {
                lb = bestProvedLB.intValue();
            }
            return (N) lb;
        } else {
            Double lb = ((RealVar) objective).getLB();
            if (bestProvedLB != null && bestProvedLB.doubleValue() > lb) {
                lb = bestProvedLB.doubleValue();
            }
            return (N) lb;
        }
    }

    @SuppressWarnings("unchecked")
    private N getObjUB() {
        assert isOptimization();
        if (intOrReal) {
            Integer ub = ((IntVar) objective).getUB();
            if (bestProvedUB != null && bestProvedUB.intValue() < ub) {
                ub = bestProvedUB.intValue();
            }
            return (N) ub;
        } else {
            Double ub = ((RealVar) objective).getUB();
            if (bestProvedUB != null && bestProvedUB.doubleValue() < ub) {
                ub = bestProvedUB.doubleValue();
            }
            return (N) ub;
        }
    }

    /**
     * Change the way cuts are managed.
     *
     * @param aCutComputer a cut computer
     */
    public void setCutComputer(Function<N,N> aCutComputer){
        this.mCutComputer = aCutComputer;
    }

    /**
     * Define a strict cut computer where in the next solution to find should be strictly greater than (resp. less)
     * the best solution found so far when maximizing (resp. minimizing) a problem.
     * @param policy resolution policy used
     * @return the new cut to consider
     */
    public static Function<Integer,Integer> strictIntVarCutComputer(ResolutionPolicy policy){
        return n -> policy == ResolutionPolicy.MAXIMIZE ? (Integer) (n + 1) : (Integer) (n - 1);
    }

    /**
     * Define a <i>walking</i> cut computer where in the next solution to find should be greater than (resp. less than)
     * or equal to the best solution found so far when maximizing (resp. minimizing) a problem.
     * @return the new cut to consider
     */
    public static Function<Integer,Integer> walkingIntVarCutComputer(){
        return n -> n;
    }

    /**
     * Define a strict cut computer where in the next solution to find should be strictly greater (resp. lesser) than
     * the best solution found so far when maximizing (resp. minimizing) a problem.
     * @param policy resolution policy used
     * @param precision precision to consider a RealVar as instantiated
     * @return the new cut to consider
     */
    public static Function<Double,Double> strictRealVarCutComputer(ResolutionPolicy policy, double precision){
        return n -> policy == ResolutionPolicy.MAXIMIZE ? (Double) (n + precision) : (Double) (n - precision);
    }

    /**
     * Define a <i>walking</i> cut computer where in the next solution to find should be greater than (resp. less than)
     * or equal to the best solution found so far when maximizing (resp. minimizing) a problem.
     * @return the new cut to consider
     */
    public static Function<Double,Double> walkingRealVarCutComputer(){
        return n -> n;
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    /**
     * @return the objective variable
     */
    public V getObjective() {
        return objective;
    }
}
