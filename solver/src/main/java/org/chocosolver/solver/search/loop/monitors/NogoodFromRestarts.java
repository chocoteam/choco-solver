/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.sat.MiniSat;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.sat.NogoodStealer;
import org.chocosolver.solver.constraints.nary.sat.PropSat;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.decision.SetDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * A constraint for the specific Nogood store designed to store ONLY positive decisions.
 * <p>
 * Related to "Nogood Recording from Restarts", C. Lecoutre et al.
 * <br/>
 * Beware :
 * - Must be plugged as a monitor
 * - Only works for integer variables
 * - Only works if branching decisions are assignments (no domain split nor value removal)
 *
 * @author Charles Prud'homme
 * @since 20/06/13
 */
public class NogoodFromRestarts implements IMonitorRestart {

    /**
     * Stores the decision path before
     */
    @SuppressWarnings("rawtypes")
    private final ArrayDeque<Decision> decisions;

    /**
     * The (unique) no-good store
     */
    private final PropSat png;

    /**
     * {@link NogoodStealer} that helps sharing no-goods among models.
     */
    private final NogoodStealer nogoodStealer;

    /**
     * A constraint for the specific Nogood store designed to store IntVar and SetVar-based decisions.
     * @param model solver to observe
     */
    public NogoodFromRestarts(Model model) {
        this(model, NogoodStealer.NONE);
    }

    /**
     * A constraint for the specific Nogood store designed to store IntVar and SetVar-based decisions.
     * @param model solver to observe
     * @param stealer when nogoods can be shared among (equivalent) models
     */
    public NogoodFromRestarts(Model model, NogoodStealer stealer) {
        this.png = model.getMinisat().getPropSat();
        this.decisions = new ArrayDeque<>(16);
        this.nogoodStealer = stealer;
        this.nogoodStealer.add(model);
    }

    @Override
    public void beforeRestart() {
        extractNogoodFromPath(png.getModel().getSolver().getDecisionPath());
        nogoodStealer.nogoodStealing(png.getModel(), this);
    }

    @SuppressWarnings("unchecked")
    public void extractNogoodFromPath(DecisionPath decisionPath) {
        assert decisions.isEmpty();
        decisionPath.transferInto(decisions, false);
        int d = decisions.size();
        Decision<Variable> decision;
        int[] lits = new int[d];
        int i = 0;
        while (!decisions.isEmpty()) {
            decision = decisions.pollFirst();
            int lit = asLit(decision);
            if (decision.hasNext() || decision.getArity() == 1) {
                lits[i++] = lit;
            } else {
                if (i == 0) {
                    // value can be removed permanently from var!
                    png.addLearnt(lit);
                } else {
                    lits[i] = lit;
                    png.addLearnt(Arrays.copyOf(lits, i + 1));
                }
            }
        }
    }

    /**
     * Transform this decision into a literal to be used in {@link PropSat}.
     *
     * @param decision a decision
     * @return the literal corresponding to this decision
     */
    private <V extends Variable> int asLit(Decision<V> decision) {
        if (decision instanceof IntDecision) {
            IntDecision id = (IntDecision) decision;
            return asLit(
                nogoodStealer.getById(id.getDecisionVariable(), png.getModel()), 
                id.getDecOp(),
                id.getDecisionValue()
            );
        } else if (decision instanceof SetDecision) {
            SetDecision id = (SetDecision) decision;
            return asLit(
                nogoodStealer.getById(id.getDecisionVariable(), png.getModel()), 
                id.getDecOp(),
                id.getDecisionValue()
            );
        } else {
            throw new UnsupportedOperationException("Cannot deal with such decision: " + decision);
        }
    }

    private int asLit(IntVar var, DecisionOperator<IntVar> op, int val) {
        int l;
        if (DecisionOperatorFactory.makeIntEq().equals(op)) {
            l = MiniSat.makeLiteral(png.makeIntEq(var, val), false);
        } else if (DecisionOperatorFactory.makeIntNeq().equals(op)) {
            l = MiniSat.makeLiteral(png.makeIntEq(var, val), true);
        } else if (DecisionOperatorFactory.makeIntSplit().equals(op)) {
            l = MiniSat.makeLiteral(png.makeIntLe(var, val), false);
        } else if (DecisionOperatorFactory.makeIntReverseSplit().equals(op)) {
            l = MiniSat.makeLiteral(png.makeIntLe(var, val), true);
        } else {
            throw new UnsupportedOperationException("Cannot deal with such operator: " + op);
        }
        return l;
    }

    private int asLit(SetVar var, DecisionOperator<SetVar> op, int val) {
        int l;
        if (DecisionOperatorFactory.makeSetForce().equals(op)) {
            l = MiniSat.makeLiteral(png.makeSetIn(var, val), false);
        } else if (DecisionOperatorFactory.makeSetRemove().equals(op)) {
            l = MiniSat.makeLiteral(png.makeSetIn(var, val), true);
        } else {
            throw new UnsupportedOperationException("Cannot deal with such operator: " + op);
        }
        return l;
    }
}
