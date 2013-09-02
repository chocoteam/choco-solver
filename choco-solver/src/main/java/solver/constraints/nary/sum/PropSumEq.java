/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.sum;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * A propagator for SUM(x_i) = y
 *
 * @author Jean-Guillaume Fages
 * @since 21/07/13
 */
public class PropSumEq extends Propagator<IntVar> {

    final int n; // number of variables

    protected static PropagatorPriority computePriority(int nbvars) {
        if (nbvars == 1) {
            return PropagatorPriority.UNARY;
        } else if (nbvars == 2) {
            return PropagatorPriority.BINARY;
        } else if (nbvars == 3) {
            return PropagatorPriority.TERNARY;
        } else {
            return PropagatorPriority.LINEAR;
        }
    }

    public PropSumEq(IntVar[] variables, IntVar sum) {
        super(ArrayUtils.append(variables, new IntVar[]{sum}), computePriority(variables.length), false);
        n = variables.length;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		boolean again;
		do{
			int min = 0;
			int max = 0;
			int ampMax = 0;
			for(int i=0;i<n;i++){
				min += vars[i].getLB();
				max += vars[i].getUB();
				ampMax = Math.max(vars[i].getUB()-vars[i].getLB(),ampMax);
			}
			vars[n].updateLowerBound(min,aCause);
			vars[n].updateUpperBound(max,aCause);
			int lb = vars[n].getLB();
			int ub = vars[n].getUB();
			again = false;
			if(min+ampMax>ub){
//				again = true;
				for(int i=0;i<n;i++){
                    again |= vars[i].updateUpperBound(ub-min+vars[i].getLB(),aCause);
				}
			}
			if(max-ampMax<lb){
//				again = true;
				for(int i=0;i<n;i++){
                    again |= vars[i].updateLowerBound(lb-max+vars[i].getUB(),aCause);
				}
			}
		} while (again);
	}

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public final ESat isEntailed() {
		int min = 0;
		int max = 0;
		for(int i=0;i<n;i++){
			min += vars[i].getLB();
			max += vars[i].getUB();
		}
		if(max<vars[n].getLB() || min>vars[n].getUB()){
			return ESat.FALSE;
		}
		if(min==max && vars[n].instantiated()){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        linComb.append(vars[0].getName());
        int i = 1;
        for (; i < n; i++) {
            linComb.append(" + ").append(vars[i].getName());
        }
        linComb.append(" = ");
        linComb.append(vars[n].getName());
        return linComb.toString();
    }
}
