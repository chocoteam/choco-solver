/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.PropNogoods;
import org.chocosolver.solver.constraints.nary.cnf.SatSolver;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.decision.fast.FastDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.queues.CircularQueue;

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

    CircularQueue<Decision<IntVar>> decisions;
    final PropNogoods png;

    /**
     * A constraint for the specific Nogood store designed to store ONLY positive decisions.
     * Beware :
     * - Must be posted as a constraint AND plugged as a monitor as well
     * - Cannot be reified
     * - Only works for integer variables
     * - Only works if branching decisions are assignments (neither domain split nor value removal)
     *
     * @param solver solver to observe
     */
    public NogoodFromRestarts(Solver solver) {
        png = solver.getNogoodStore().getPropNogoods();
        decisions = new CircularQueue<>(16);
    }

    @Override
    public void beforeRestart() {
        extractNogoodFromPath();
    }

    @Override
    public void afterRestart() {
    }

    private void extractNogoodFromPath() {
        int d = png.getSolver().getSearchLoop().getCurrentDepth();
        Decision<IntVar> decision = png.getSolver().getSearchLoop().getLastDecision();
        while (decision != RootDecision.ROOT) {
            decisions.addLast(decision);
            decision = decision.getPrevious();
        }
        int[] lits = new int[d];
        int i = 0;
        while (!decisions.isEmpty()) {
            decision = decisions.pollLast();
            assert decision instanceof FastDecision : "NogoodStoreFromRestarts is only valid for integer variables (hence FastDecision)";
            assert decision.toString().contains(DecisionOperator.int_eq.toString()) : "NogoodStoreFromRestarts is only valid for assignment decisions";
            if (decision.hasNext()) {
                lits[i++] = SatSolver.negated(png.Literal(decision.getDecisionVariable(), (Integer) decision.getDecisionValue()));
            } else {
                if (i == 0) {
                    // value can be removed permanently from var!
                    png.addLearnt(SatSolver.negated(png.Literal(decision.getDecisionVariable(), (Integer) decision.getDecisionValue())));
                } else {
                    lits[i] = SatSolver.negated(png.Literal(decision.getDecisionVariable(), (Integer) decision.getDecisionValue()));
                    png.addLearnt(Arrays.copyOf(lits, i + 1));
                }
            }
        }
    }
}
