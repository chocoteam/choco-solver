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

package solver.search.strategy.strategy.set;

import solver.variables.SetVar;

/**
 * Heuristic to select a SetVar to branch on
 * @author Jean-Guillaume Fages
 * @since 6/10/13
 */
public interface SetVarSelector {

	/**
	 * Selects a non-instantiatied SetVar of vars, to branch on it
	 * @param vars an array of SetVar
	 * @return A non-instantiated SetVar to branch on, or null otherwise
	 */
	public SetVar selectVar(SetVar[] vars);

	/**
	 * Eventually perform some computation before the search process starts
	 */
	public void init();

	/**
	 * Selects the first unfixed variable
	 */
	public class FirstVar implements SetVarSelector{
		@Override
		public SetVar selectVar(SetVar[] vars) {
			for (SetVar s : vars) {
				if(!s.instantiated()){
					return s;
				}
			}
			return null;
		}
		@Override
		public void init(){}
	}

	/**
	 * Selects the variables minimising envelopeSize-kernelSize
	 * (quite similar to minDomain, or first-fail)
	 */
	public class MinDelta implements SetVarSelector{
		@Override
		public SetVar selectVar(SetVar[] vars) {
			int delta = Integer.MAX_VALUE;
			SetVar next = null;
			for (SetVar s : vars) {
				int d = s.getEnvelopeSize()-s.getKernelSize();
				if(d>0 && d<delta){
					delta = d;
					next = s;
				}
			}
			return next;
		}
		@Override
		public void init(){}
	}

	/**
	 * Selects the variables maximising envelopeSize-kernelSize
	 */
	public class MaxDelta implements SetVarSelector{
		@Override
		public SetVar selectVar(SetVar[] vars) {
			int delta = 0;
			SetVar next = null;
			for (SetVar s : vars) {
				int d = s.getEnvelopeSize()-s.getKernelSize();
				if(d>delta){
					delta = d;
					next = s;
				}
			}
			return next;
		}
		@Override
		public void init(){}
	}
}
