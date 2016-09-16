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
import org.chocosolver.solver.variables.RealVar;

abstract class AbstractRealObjManager extends AbstractObjManager<RealVar> {
    
    public AbstractRealObjManager(AbstractObjManager<RealVar> objman) {
	super(objman);
    }

    public AbstractRealObjManager(RealVar objective, ResolutionPolicy policy, Number precision) {
	super(objective, policy, precision);
	double prec = Math.abs(precision.doubleValue());
	bestProvedLB = Double.valueOf(objective.getLB() - prec);
	bestProvedUB = Double.valueOf(objective.getUB() + prec);
    }

    @Override
    public void updateBestLB(Number lb) {
	if (bestProvedLB.doubleValue() < lb.doubleValue()) {
	    bestProvedLB = lb;
	}
    }

    @Override
    public void updateBestUB(Number ub) {
	if (bestProvedUB.doubleValue() > ub.doubleValue()) {
	    bestProvedUB = ub;
	}
    }

    @Override
    public void updateBestSolution() {
	assert objective.isInstantiated();
	updateBestSolution(objective.getUB());
    }

    @Override
    public void setStrictDynamicCut() {
	cutComputer = (Number n) -> Double.valueOf(n.doubleValue() + precision.doubleValue());
    }

}

class MinRealObjManager extends AbstractRealObjManager {

    public MinRealObjManager(AbstractObjManager<RealVar> objman) {
	super(objman);
    }

    public MinRealObjManager(RealVar objective, double precision) {
	super(objective, ResolutionPolicy.MINIMIZE, -precision);
    }

    @Override
    public void updateBestSolution(Number n) {
	updateBestUB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
	objective.updateBounds(bestProvedLB.doubleValue(), cutComputer.apply(bestProvedUB).doubleValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
	return bestProvedUB;
    }

}

class MaxRealObjManager extends AbstractRealObjManager {

    public MaxRealObjManager(AbstractObjManager<RealVar> objman) {
	super(objman);
    }

    public MaxRealObjManager(RealVar objective, double precision) {
	super(objective, ResolutionPolicy.MAXIMIZE, precision);
    }

    @Override
    public void updateBestSolution(Number n) {
	updateBestLB(n);
    }

    @Override
    public void postDynamicCut() throws ContradictionException {
	objective.updateBounds(cutComputer.apply(bestProvedLB).doubleValue(), bestProvedUB.doubleValue(), this);
    }

    @Override
    public Number getBestSolutionValue() {
	return bestProvedLB;
    }
}
