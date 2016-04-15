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
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.sat.PropNogoods;
import org.chocosolver.sat.SatSolver;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;

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
        Decision<IntVar> decision;
        int[] lits = new int[d];
        int i = 0;
        while (!decisions.isEmpty()) {
            decision = decisions.pollFirst();
            if (decision instanceof IntDecision) {
                IntDecision id = (IntDecision) decision;
                if (id.getDecOp() == DecisionOperator.int_eq) {
                    if (id.hasNext()) {
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
                } else if (id.getDecOp() == DecisionOperator.int_neq) {
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
                } else {
                    throw new UnsupportedOperationException("NogoodStoreFromRestarts cannot deal with such operator: " + ((IntDecision) decision).getDecOp());
                }
            } else {
                throw new UnsupportedOperationException("NogoodStoreFromRestarts can only deal with IntDecision.");
            }
        }
    }
}
