/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.sat.SatSolver;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.sat.PropNogoods;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.decision.SetDecision;

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
    private ArrayDeque<Decision> decisions;

    /**
     * The (unique) no-good store
     */
    private final PropNogoods png;

    /**
     * A constraint for the specific Nogood store designed to store ONLY positive decisions.
     * Beware :
     * - Must be posted as a constraint AND plugged as a monitor as well
     * - Cannot be reified
     * - Only works for integer variables
     * - Only works if branching decisions are assignments (neither domain split nor value removal)
     *
     * @param model solver to observe
     */
    public NogoodFromRestarts(Model model) {
        png = model.getNogoodStore().getPropNogoods();
        decisions = new ArrayDeque<>(16);
    }

    @Override
    public void beforeRestart() {
        extractNogoodFromPath();
    }

    @SuppressWarnings("unchecked")
    private void extractNogoodFromPath() {
        int d = (int) png.getModel().getSolver().getNodeCount();
        png.getModel().getSolver().getDecisionPath().transferInto(decisions, false);
        Decision decision;
        int[] lits = new int[d];
        int i = 0;
        while (!decisions.isEmpty()) {
            decision = decisions.pollFirst();
            if (decision instanceof IntDecision) {
                IntDecision id = (IntDecision) decision;
                if (id.getDecOp() == DecisionOperatorFactory.makeIntEq()) {
                    if (id.hasNext() || id.getArity() == 1) {
                        lits[i++] = SatSolver.negated(png.Literal(id.getDecisionVariable(), id.getDecisionValue(), true));
                    } else {
                        if (i == 0) {
                            // value can be removed permanently from var!
                            png.addLearnt(SatSolver.negated(png.Literal(id.getDecisionVariable(), id.getDecisionValue(), true)));
                        } else {
                            lits[i] = SatSolver.negated(png.Literal(id.getDecisionVariable(), id.getDecisionValue(), true));
                            png.addLearnt(Arrays.copyOf(lits, i + 1));
                        }
                    }
                } else if (id.getDecOp() == DecisionOperatorFactory.makeIntNeq()) {
                    if (id.hasNext()) {
                        lits[i++] = png.Literal(id.getDecisionVariable(), id.getDecisionValue(), true);
                    } else {
                        if (i == 0) {
                            // value can be removed permanently from var!
                            png.addLearnt(png.Literal(id.getDecisionVariable(), id.getDecisionValue(), true));
                        } else {
                            lits[i] = png.Literal(id.getDecisionVariable(), id.getDecisionValue(), true);
                            png.addLearnt(Arrays.copyOf(lits, i + 1));
                        }
                    }
                } else if (id.getDecOp() == DecisionOperatorFactory.makeIntSplit()) {
                    if (id.hasNext() || id.getArity() == 1) {
                        lits[i++] = SatSolver.negated(png.Literal(id.getDecisionVariable(), id.getDecisionValue(), false));
                    } else {
                        if (i == 0) {
                            // value can be removed permanently from var!
                            png.addLearnt(SatSolver.negated(png.Literal(id.getDecisionVariable(), id.getDecisionValue(), false)));
                        } else {
                            lits[i] = SatSolver.negated(png.Literal(id.getDecisionVariable(), id.getDecisionValue(), false));
                            png.addLearnt(Arrays.copyOf(lits, i + 1));
                        }
                    }
                } else if (id.getDecOp() == DecisionOperatorFactory.makeIntReverseSplit()) {
                    if (id.hasNext()) {
                        lits[i++] = png.Literal(id.getDecisionVariable(), id.getDecisionValue(), false);
                    } else {
                        if (i == 0) {
                            // value can be removed permanently from var!
                            png.addLearnt(png.Literal(id.getDecisionVariable(), id.getDecisionValue(), false));
                        } else {
                            lits[i] = png.Literal(id.getDecisionVariable(), id.getDecisionValue(), false);
                            png.addLearnt(Arrays.copyOf(lits, i + 1));
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("NogoodStoreFromRestarts cannot deal with such operator: " + ((IntDecision) decision).getDecOp());
                }
            } else if (decision instanceof SetDecision) {
                SetDecision sd = (SetDecision) decision;
                if (sd.getDecOp() == DecisionOperatorFactory.makeSetForce()) {
                    if (sd.hasNext() || sd.getArity() == 1) {
                        lits[i++] = SatSolver.negated(png.Literal(sd.getDecisionVariable(), sd.getDecisionValue(), true));
                    } else {
                        if (i == 0) {
                            // value can be removed permanently from var!
                            png.addLearnt(SatSolver.negated(png.Literal(sd.getDecisionVariable(), sd.getDecisionValue(), true)));
                        } else {
                            lits[i] = SatSolver.negated(png.Literal(sd.getDecisionVariable(), sd.getDecisionValue(), true));
                            png.addLearnt(Arrays.copyOf(lits, i + 1));
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("NogoodStoreFromRestarts cannot deal with such operator: " + ((SetDecision) decision).getDecOp());
                }
            } else {
                throw new UnsupportedOperationException("NogoodStoreFromRestarts can only deal with IntDecision and SetDecision.");
            }
        }
    }
}
