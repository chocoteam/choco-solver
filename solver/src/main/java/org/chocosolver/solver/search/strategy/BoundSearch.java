/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

/**
 * BEWARE: ONLY FOR INTEGERS (lets the default search work for other variable types)
 *
 * Search heuristic combined with a constraint performing strong consistency on the next decision variable
 * and branching on the value with the best objective bound (for optimization) and branches on the lower bound for SAT problems.
 *
 * @author Jean-Guillaume FAGES
 */
public class BoundSearch extends AbstractStrategy<IntVar> {

    private final Model model;
    private final DecisionPath decisionPath;
    private AbstractStrategy<IntVar> definedSearch; // int search into which this is plugged
    private TIntIntHashMap vb = new TIntIntHashMap(); // value-bound map
    private IntVar variable; // current variable, on which this branches
    private Random rd = new Random(0); // only to alternate between LB and UB for bounded domain filtering
    @SuppressWarnings("WeakerAccess")
    public int MAX_DOM_SIZE = 100; // maximum size of an enumerated domain to apply strong consistency on it

    @SuppressWarnings("WeakerAccess")
    public BoundSearch(AbstractStrategy<IntVar> mainSearch) {
        super(mainSearch.getVariables());
        model = vars[0].getModel();
        definedSearch = mainSearch;
        decisionPath = model.getSolver().getDecisionPath();
    }

    @Override
    public boolean init() {
        return definedSearch.init();
    }

    @Override
    public void remove() {
        this.definedSearch.remove();
    }

    @Override
    public Decision<IntVar> getDecision() {
        if (variable == null || variable.isInstantiated()) {
            Decision<IntVar> d = definedSearch.getDecision();
            vb.clear();
            if (d == null) return null;
            if ((d.getDecisionVariable().getTypeAndKind() & IntVar.INT) != 0) variable = d.getDecisionVariable();
            else {
                return d;
            }
        }
        if (variable.getDomainSize() < MAX_DOM_SIZE) {
            if (variable.hasEnumeratedDomain()) {
                vb.clear();
                for (int v = variable.getLB(); v <= variable.getUB(); v = variable.nextValue(v)) {
                    int bound = bound(v);
                    if (bound == Integer.MAX_VALUE) {
                        return removeVal(v);
                        // stops at first infeasible value because there is no meta-decision objects
                        // could be improved (remove all infeasible values) by using a move instead of a search heuristic
                    } else {
                        vb.put(v, bound);
                    }
                }
                return decisionPath.makeIntDecision(variable, DecisionOperatorFactory.makeIntEq(), getBestVal());
            } else {
                vb.clear();
                int lbB = bound(variable.getLB());
                int ubB = bound(variable.getUB());
                if (lbB == Integer.MAX_VALUE && ubB == Integer.MAX_VALUE) {
                    return removeVal(rd.nextBoolean() ? variable.getLB() : variable.getUB());
                } else {
                    return decisionPath.makeIntDecision(variable, DecisionOperatorFactory.makeIntEq(),
                            lbB <= ubB ? variable.getLB() : variable.getUB());
                }
            }
        } else {
            vb.clear();
            return definedSearch.getDecision();
        }
    }

    private IntDecision removeVal(int val) {
        IntDecision d = decisionPath.makeIntDecision(variable, DecisionOperatorFactory.makeIntNeq(), val);
        d.setRefutable(false);
        return d;
    }

    private int bound(int val) {
        int cost;
        model.getEnvironment().worldPush();
        try {
            variable.instantiateTo(val, Cause.Null);
            model.getSolver().getEngine().propagate();
            ResolutionPolicy rp = model.getSolver().getObjectiveManager().getPolicy();
            if (rp == ResolutionPolicy.SATISFACTION) {
                cost = 1;
            } else if (rp == ResolutionPolicy.MINIMIZE) {
                cost = ((IntVar) model.getObjective()).getLB();
            } else {
                cost = -((IntVar) model.getObjective()).getUB();
            }
        } catch (ContradictionException cex) {
            cost = Integer.MAX_VALUE;
        }
        model.getSolver().getEngine().flush();
        model.getEnvironment().worldPop();
        return cost;
    }

    private int getBestVal() {
        int coef = 1;
        if (variable.hasEnumeratedDomain()) {
            int bestCost = Integer.MAX_VALUE;
            int bestV = variable.getUB();
            for (int v = variable.getLB(); v <= variable.getUB(); v = variable.nextValue(v)) {
                int c = vb.get(v);
                if (c < bestCost * coef) {
                    bestCost = c;
                    bestV = v;
                }
            }
            return bestV;
        } else {
            return bound(variable.getLB()) < bound(variable.getUB()) * coef ? variable.getLB() : variable.getUB();
        }
    }
}
