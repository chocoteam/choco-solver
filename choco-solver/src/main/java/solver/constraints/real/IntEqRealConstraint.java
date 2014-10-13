/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package solver.constraints.real;

import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.Variable;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * Channeling constraint between integers and reals, to avoid views
 *
 * @author	Jean-Guillaume Fages
 * @since	07/04/2014
 */
public class IntEqRealConstraint extends Constraint {

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Channeling between integer variables intVars and real variables realVars.
	 * Thus, for any i in [0,intVars.length-1], |intVars[i]-realVars[i]|< epsilon.
	 * intVars.length must be equal to realVars.length.
	 *
	 * @param intVars	integer variables
	 * @param realVars	real variables
	 * @param epsilon	precision parameter
	 */
	public IntEqRealConstraint(final IntVar[] intVars, final RealVar[] realVars, final double epsilon) {
		super("IntEqReal", new Propagator<Variable>(ArrayUtils.append(intVars,realVars)) {
			int n = intVars.length;
			@Override
			public void propagate(int evtmask) throws ContradictionException {
				assert n==realVars.length;
				for(int i=0;i<n;i++) {
					IntVar intVar = intVars[i];
					RealVar realVar = realVars[i];
					realVar.updateBounds((double) intVar.getLB() - epsilon, (double) intVar.getUB() + epsilon, aCause);
					intVar.updateLowerBound((int) Math.ceil(realVar.getLB() - epsilon), aCause);
					intVar.updateUpperBound((int) Math.floor(realVar.getUB() + epsilon), aCause);
					if (intVar.hasEnumeratedDomain()) {
						realVar.updateBounds((double) intVar.getLB() - epsilon, (double) intVar.getUB() + epsilon, aCause);
					}
				}
			}
			@Override
			public ESat isEntailed() {
				assert intVars.length==realVars.length;
				boolean allInst = true;
				for(int i=0;i<n;i++) {
					IntVar intVar = intVars[i];
					RealVar realVar = realVars[i];
					if ((realVar.getLB() < (double) intVar.getLB() - epsilon) || (realVar.getUB() > (double) intVar.getUB() + epsilon)) {
						return ESat.FALSE;
					}
					if (!(intVar.isInstantiated() && realVar.isInstantiated())) {
						allInst = false;
					}
				}
				return allInst?ESat.TRUE:ESat.UNDEFINED;
			}
		});
	}

	/**
	 * Channeling between an integer variable intVar and a real variable realVar.
	 * Thus, |intVar-realVar|< epsilon.
	 *
	 * @param intVar	integer variable
	 * @param realVar	real variable
	 * @param epsilon	precision parameter
	 */
	public IntEqRealConstraint(final IntVar intVar, final RealVar realVar, final double epsilon) {
		this(new IntVar[]{intVar},new RealVar[]{realVar},epsilon);
	}
}
