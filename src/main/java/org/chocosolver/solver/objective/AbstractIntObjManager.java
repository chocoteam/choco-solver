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
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 *
 */
abstract class AbstractIntObjManager extends AbstractObjManager<IntVar> {

    private static final long serialVersionUID = 5539060355541720114L;

    public AbstractIntObjManager(AbstractObjManager<IntVar> objman) {
        super(objman);
    }

    public AbstractIntObjManager(IntVar objective, ResolutionPolicy policy, Number precision) {
        super(objective, policy, precision);
        bestProvedLB = objective.getLB() - 1;
        bestProvedUB = objective.getUB() + 1;
    }

    @Override
    public synchronized void updateBestLB(Number lb) {
        if (bestProvedLB.intValue() < lb.intValue()) {
            bestProvedLB = lb;
        }
    }

    @Override
    public synchronized void updateBestUB(Number ub) {
        if (bestProvedUB.intValue() > ub.intValue()) {
            bestProvedUB = ub;
        }
    }

    @Override
    public void updateBestSolution() {
        assert objective.isInstantiated();
        updateBestSolution(objective.getValue());
    }

    @Override
    public void setStrictDynamicCut() {
        cutComputer = (Number n) -> n.intValue() + precision.intValue();
    }

    @Override
    public void resetBestBounds() {
        bestProvedLB = objective.getLB() - 1;
        bestProvedUB = objective.getUB() + 1;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return isOptimization() && ruleStore.addBoundsRule(objective);
    }

    @Override
    public String toString() {
        return String.format("%s %s = %d", policy, objective == null ? "?" : this.objective.getName(), getBestSolutionValue().intValue());
    }

}

class MinIntObjManager extends AbstractIntObjManager {

    private static final long serialVersionUID = 6963161492115613388L;

    @SuppressWarnings("unused") // use for copy by introspection
    public MinIntObjManager(AbstractObjManager<IntVar> objman) {
        super(objman);
    }

    public MinIntObjManager(IntVar objective) {
        super(objective, ResolutionPolicy.MINIMIZE, -1);
    }

    @Override
    public void updateBestSolution(Number n) {
        updateBestUB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(bestProvedLB.intValue(), cutComputer.apply(bestProvedUB).intValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedUB;
    }

}

class MaxIntObjManager extends AbstractIntObjManager {

    private static final long serialVersionUID = -245398442954059838L;

    @SuppressWarnings("unused") // use for copy by introspection
    public MaxIntObjManager(AbstractObjManager<IntVar> objman) {
        super(objman);
    }

    public MaxIntObjManager(IntVar objective) {
        super(objective, ResolutionPolicy.MAXIMIZE, 1);
    }

    @Override
    public void updateBestSolution(Number n) {
        updateBestLB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
        objective.updateBounds(cutComputer.apply(bestProvedLB).intValue(), bestProvedUB.intValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
        return bestProvedLB;
    }
}
