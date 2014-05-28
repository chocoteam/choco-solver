/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.nary.nogood;

import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.IMonitorRestart;
import solver.search.loop.monitors.IMonitorSolution;
import solver.variables.IntVar;

/**
 * Avoid exploring same solutions (useful with restart on solution)
 * Beware :
 * - Must be posted as a constraint AND plugged as a monitor as well
 * - Cannot be reified
 * - Only works for integer variables
 *
 * This can be used to remove similar/symmetric solutions
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 20/06/13
 */
public class NogoodStoreFromSolutions extends Constraint implements IMonitorSolution, IMonitorRestart {

	static final String MSG_NGOOD = "unit propagation failure (nogood from solution)";
	INogood solutionNoGood;
	final PropNogoodStore png;
	final protected IntVar[] decisionVars;

	/**
	 * Avoid exploring same solutions (useful with restart on solution)
	 * Beware :
	 * - Must be posted as a constraint AND plugged as a monitor as well
	 * - Cannot be reified
	 * - Only works for integer variables
	 *
	 * This can be used to remove similar/symmetric solutions
	 *
	 * @param vars all decision variables which define a solution (can be a subset of variables)
	 */
	public NogoodStoreFromSolutions(IntVar[] vars) {
		super("NogoodStoreFromSolutions",new PropNogoodStore(vars));
		decisionVars = vars;
		png = (PropNogoodStore) propagators[0];

	}

	@Override
	public void onSolution() {
		extractNogoodFromPath();
	}

	@Override
	public void beforeRestart() {}

	@Override
	public void afterRestart() {
		// initial propagation (would not be triggered otherwise)
		try {
			png.addNogood(solutionNoGood);
			png.unitPropagation();
			// forces to reach the fix-point of constraints
			png.getSolver().getEngine().propagate();
		} catch (ContradictionException e) {
			png.getSolver().getSearchLoop().interrupt(MSG_NGOOD);
		}
	}

	private void extractNogoodFromPath() {
		int n = decisionVars.length;
		if(n==1){
			solutionNoGood = new UnitNogood(decisionVars[0], decisionVars[0].getValue());
		}else{
			int[] values = new int[n];
			for(int i=0;i<n;i++){
				values[i] = decisionVars[i].getValue();
			}
			solutionNoGood = new Nogood(decisionVars, values);
		}
	}
}
