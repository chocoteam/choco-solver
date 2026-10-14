/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 25/05/12
 * Time: 15:39
 */

package org.chocosolver.solver.objective;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;

import static org.chocosolver.solver.objective.OptimizationPolicy.DICHOTOMIC;

/**
 * Class that defines a branching strategy over the objective variable
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class ObjectiveStrategy extends AbstractStrategy<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int globalLB, globalUB;
    private final IntVar obj;
    private long nbSols;
    private final Model model;
    private boolean firstCall;
    private DecisionOperator<IntVar> decOperator;
    private final OptimizationPolicy optPolicy;
    private final double fraction;
    private final boolean largerHalfFirst;
    public final DecisionOperator<IntVar> decUB = new BottomUpDecisionOperator();
    public final DecisionOperator<IntVar> incLB = new TopDownDecisionOperator();
    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Defines a branching strategy over the objective variable
     * BEWARE: only activated after a first solution
     * <p>
     * With DICHOTOMIC, the domain [LB, UB] of the objective is split at
     * {@code LB + fraction * (UB - LB)}: 0.5 halves the domain, a greater value takes a split
     * point closer to the upper bound, a lower value one closer to the lower bound.
     * When the split point is not an integer, it is rounded so that minimizing an objective
     * with {@code fraction} and maximizing its opposite with {@code 1 - fraction} try mirrored
     * cuts. The direction of the rounding is given by {@code largerHalfFirst}:
     * <ul>
     * <li>{@code false}: the branch tried first keeps the smaller part of the domain
     * (rounded down when minimizing, up when maximizing), the cut is more aggressive;</li>
     * <li>{@code true}: the branch tried first keeps the larger part of the domain
     * (rounded up when minimizing, down when maximizing), the cut is more cautious
     * but its refutation is stronger.</li>
     * </ul>
     *
     * @param objective       variable
     * @param policy          BOTTOM_UP, TOP_TOWN or DICHOTOMIC
     * @param fraction        with DICHOTOMIC, position of the split point in the domain,
     *                        in [0, 1]; ignored otherwise
     * @param largerHalfFirst with DICHOTOMIC, set to <i>true</i> to try first the larger part
     *                        of the domain, <i>false</i> to try first the smaller one
     */
    public ObjectiveStrategy(IntVar objective, OptimizationPolicy policy, double fraction, boolean largerHalfFirst) {
        super(objective);
        if (Double.isNaN(fraction) || fraction < 0. || fraction > 1.) {
            throw new IllegalArgumentException("fraction should be in [0, 1], not " + fraction);
        }
        this.fraction = fraction;
        this.largerHalfFirst = largerHalfFirst;
        this.obj = objective;
        this.model = obj.getModel();
        this.firstCall = true;
        this.optPolicy = policy;
        model.getSolver().setRestartOnSolutions();
    }

    private DecisionOperator<IntVar> getOperator(OptimizationPolicy optPolicy, ResolutionPolicy resoPolicy) {
        switch (optPolicy) {
            case BOTTOM_UP:
                return decUB;
            case TOP_DOWN:
                return incLB;
            case DICHOTOMIC:
                switch (resoPolicy) {
                    case MINIMIZE:
                        return decUB;
                    case MAXIMIZE:
                        return incLB;
                    default:
                        throw new UnsupportedOperationException("ObjectiveStrategy is not for " + resoPolicy + " ResolutionPolicy");
                }
            default:
                throw new UnsupportedOperationException("unknown OptimizationPolicy " + optPolicy);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public boolean init() {
        decOperator = getOperator(optPolicy, model.getSolver().getObjectiveManager().getPolicy());
        return true;
    }

    @Override
    public void remove() {

    }

    @Override
    public Decision<IntVar> getDecision() {
        if (model.getSolver().getSolutionCount() == 0
                || (nbSols == model.getSolver().getSolutionCount() && optPolicy == DICHOTOMIC)) {
            return null;
        }
        if (obj.isInstantiated()) {
            return null;
        }
        if (firstCall) {
            firstCall = false;
            globalLB = obj.getLB();
            globalUB = obj.getUB();
        }
        nbSols = model.getSolver().getSolutionCount();
        globalLB = Math.max(globalLB, obj.getLB());//check
        globalUB = Math.min(globalUB, obj.getUB());//check
        //        ObjectiveManager man = model.getResolver().getObjectiveManager();
        //        man.updateLB(globalLB);
        //        man.updateUB(globalUB);
        if (globalLB > globalUB) {
            return null;
        }

        if (model.getSettings().warnUser()) {
            model.getSolver().log().bold().println("- objective in [" + globalLB + ", " + globalUB + "]");
        }
        int target = getTarget();
        if (decOperator == incLB ? target <= obj.getLB() : target >= obj.getUB()) {
            // the decision would not reduce the domain
            return null;
        }
        IntDecision dec = decisionPath.makeIntDecision(obj, decOperator, target);
        if (model.getSettings().warnUser()) {
            model.getSolver().log().bold().println("- trying " + obj + " " + (decOperator == decUB ? "<=" : ">=") + " " + target);
        }
        return dec;
    }

    private int getTarget() {
        switch (optPolicy) {
            case BOTTOM_UP:
                return globalLB;
            case TOP_DOWN:
                return globalUB;
            default:
                double split = globalLB + fraction * ((double) globalUB - globalLB);
                // rounded towards the bound to improve, unless the larger half is tried first
                boolean roundUp = (decOperator == incLB) != largerHalfFirst;
                return (int) (roundUp ? Math.ceil(split) : Math.floor(split));
        }
    }

    public class BottomUpDecisionOperator implements DecisionOperator<IntVar> {
        //FIXME can not serialize decision 

        @Override
        public boolean apply(IntVar var, int value, ICause cause) throws ContradictionException {
            return var.updateUpperBound(value, cause);
        }

        @Override
        public boolean unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            globalLB = value + 1;
            //            model.getResolver().getObjectiveManager().updateLB(globalLB);
            return var.updateLowerBound(globalLB, cause);
        }

        @Override
        public String toString() {
            return " objective " + optPolicy + ", decreases the upper bound first";
        }

        @Override
        public DecisionOperator<IntVar> opposite() {
            return incLB;
        }
    }

    public class TopDownDecisionOperator implements DecisionOperator<IntVar> {
        //FIXME can not serialize decision 
        @Override
        public boolean apply(IntVar var, int value, ICause cause) throws ContradictionException {
            return var.updateLowerBound(value, cause);
        }

        @Override
        public boolean unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            globalUB = value - 1;
            //            model.getResolver().getObjectiveManager().updateUB(globalUB);
            return var.updateUpperBound(globalUB, cause);
        }

        @Override
        public String toString() {
            return " objective " + optPolicy + ", increases the lower bound first";
        }

        @Override
        public DecisionOperator<IntVar> opposite() {
            return decUB;
        }
    }
}
