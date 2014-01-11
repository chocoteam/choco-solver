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
package solver.constraints.nary.min_max;

import solver.Solver;
import solver.constraints.IntConstraint;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * VAL = MIN(VARS)
 * <br/>
 * CPRU: not tested yet
 *
 * @author Charles Prud'homme
 * @since 26/07/12
 */
public class Minimum extends IntConstraint {

	public Minimum(IntVar val, IntVar[] vars, Solver solver) {
		super(ArrayUtils.append(new IntVar[]{val}, vars), solver);
		setPropagators(new PropMin(vars, val));
		boolean enu = val.hasEnumeratedDomain();
		for(int i=0; i<vars.length && !enu; i++){
			enu = vars[i].hasEnumeratedDomain();
		}
		if(enu){
			addPropagators(new PropMin(vars,val));
		}
	}

	@Override
	public ESat isSatisfied(int[] tuple) {
		int m = tuple[1];
		for (int i = 2; i < tuple.length; i++) {
			if (m > tuple[i]) {
				m = tuple[i];
			}
		}
		return ESat.eval(tuple[0] == m);
	}
}
