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
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * Class to monitor the objective function and avoid exploring "worse" solutions
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class ObjectiveManager<V extends Variable, N extends Number> implements ICause {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    final protected ResolutionPolicy policy;
    final protected boolean strict;
    final protected V objective;

    final private boolean intOrReal;
    final private double precision;

    protected N bestProvedLB, bestProvedUB; // best bounds found so far

    // creates an objective manager for satisfaction problems
    public static ObjectiveManager SAT() {
        return new ObjectiveManager(null, ResolutionPolicy.SATISFACTION, false);
    }

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    private ObjectiveManager(V objective, ResolutionPolicy policy, double precision, boolean strict, boolean intOrReal) {
        this.policy = policy;
        this.strict = strict;
        this.objective = objective;
        this.precision = precision;
        this.intOrReal = intOrReal;
        if (isOptimization()) {
            this.bestProvedLB = getObjLB();
            this.bestProvedUB = getObjUB();
        }
    }

    /**
     * Creates an optimization manager for an integer objective function (represented by an IntVar)
     * Enables to cut "worse" solutions
     *
     * @param objective variable (represent the value of a solution)
     * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
     * @param strict    Forces to compute strictly better solutions.
     *                  Enables to enumerate better or equal solutions when set to false.
     */
    @SuppressWarnings("unchecked")
    public ObjectiveManager(IntVar objective, ResolutionPolicy policy, boolean strict) {
        this((V) objective, policy, 0, strict, true);
    }

    /**
     * Creates an optimization manager for a continuous objective function (represented by a RealVar)
     * Enables to cut "worse" solutions
     *
     * @param objective variable (represent the value of a solution)
     * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
     * @param precision precision parameter defining the minimum objective improvement between two solutions
     *                  (avoids wasting time enumerating a huge set of equivalent solutions)
     * @param strict    Forces to compute strictly better solutions.
     *                  Enables to enumerate better or equal solutions when set to false.
     */
    @SuppressWarnings("unchecked")
    public ObjectiveManager(RealVar objective, ResolutionPolicy policy, double precision, boolean strict) {
        this((V) objective, policy, precision, strict, false);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * @return true iff the problem is an optimization problem
     */
    public boolean isOptimization() {
        return policy != ResolutionPolicy.SATISFACTION;
    }

    /**
     * Updates the lower (or upper) bound of the objective variable, considering its best know value.
     *
     * @param decision decision to apply
     * @throws org.chocosolver.solver.exception.ContradictionException if this application leads to a contradiction  @param decision
     */
    public void apply(Decision decision) throws ContradictionException {
        decision.apply();
    }

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

    protected int getNbDecimals() {
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
                if(bestProvedUB == null || newVal.doubleValue() < bestProvedUB.doubleValue()) {
                    bestProvedUB = newVal;
                }
            } else {
                if(bestProvedLB == null || newVal.doubleValue() > bestProvedLB.doubleValue()) {
                    bestProvedLB = newVal;
                }
            }
        }
    }

    /**
     * Prevent the model from computing worse quality solutions
     *
     * @throws org.chocosolver.solver.exception.ContradictionException
     */
    public void postDynamicCut() throws ContradictionException {
        if (isOptimization()) {
            if (intOrReal) {
                int offset = 0;
                if (objective.getModel().getMeasures().getSolutionCount() > 0 && strict) {
                    offset = 1;
                }
                IntVar io = (IntVar) objective;
                if (policy == ResolutionPolicy.MINIMIZE) {
                    io.updateBounds(bestProvedLB.intValue(), bestProvedUB.intValue() - offset, this);
                } else {
                    io.updateBounds(bestProvedLB.intValue() + offset, bestProvedUB.intValue(), this);
                }
            } else {
                double offset = 0;
                if (objective.getModel().getMeasures().getSolutionCount() > 0 && strict) {
                    offset = precision;
                }
                RealVar io = (RealVar) objective;
                if (policy == ResolutionPolicy.MINIMIZE) {
                    io.updateBounds(bestProvedLB.doubleValue(), bestProvedUB.doubleValue() - offset, this);
                } else {
                    io.updateBounds(bestProvedLB.doubleValue() + offset, bestProvedUB.doubleValue(), this);
                }
            }
        }
    }

    /**
     * @return the best solution value found so far (returns the initial bound if no solution has been found yet)
     */
    public N getBestSolutionValue() {
        if (policy == ResolutionPolicy.MINIMIZE) {
            return bestProvedUB;
        }
        if (policy == ResolutionPolicy.MAXIMIZE) {
            return bestProvedLB;
        }
        throw new UnsupportedOperationException("There is no objective variable in satisfaction problems");
    }

    /**
     * States that lb is a global lower bound on the problem
     *
     * @param lb lower bound
     */
    public void updateBestLB(N lb) {
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
    public void updateBestUB(N ub) {
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

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    /**
     * @return the ResolutionPolicy of the problem
     */
    public ResolutionPolicy getPolicy() {
        return policy;
    }

    /**
     * @return the objective variable
     */
    public V getObjective() {
        return objective;
    }

    /**
     * @return the best lower bound computed so far
     */
    public N getBestLB() {
        return bestProvedLB;
    }

    /**
     * @return the best upper bound computed so far
     */
    public N getBestUB() {
        return bestProvedUB;
    }
}
