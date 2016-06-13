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
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

/**
 * Channeling constraint between integers and reals, to avoid views
 *
 * @author Jean-Guillaume Fages
 * @since 07/04/2014
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
     * @param intVars  integer variables
     * @param realVars real variables
     * @param epsilon  precision parameter
     */
    public IntEqRealConstraint(IntVar[] intVars, RealVar[] realVars, double epsilon) {
        super("IntEqReal", new PropIntEqReal(intVars, realVars, epsilon));
    }

    /**
     * Channeling between an integer variable intVar and a real variable realVar.
     * Thus, |intVar-realVar|< epsilon.
     *
     * @param intVar  integer variable
     * @param realVar real variable
     * @param epsilon precision parameter
     */
    public IntEqRealConstraint(final IntVar intVar, final RealVar realVar, final double epsilon) {
        this(new IntVar[]{intVar}, new RealVar[]{realVar}, epsilon);
    }

    private static class PropIntEqReal extends Propagator<Variable> {

        private int n;
        private IntVar[] intVars;
        private RealVar[] realVars;
        private double epsilon;

        public PropIntEqReal(IntVar[] intVars, RealVar[] realVars, double epsilon) {
            super(ArrayUtils.append(intVars, realVars), PropagatorPriority.LINEAR, false);
            this.n = intVars.length;
            this.intVars = intVars;
            this.realVars = realVars;
            this.epsilon = epsilon;
            assert n == realVars.length;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            for (int i = 0; i < n; i++) {
                IntVar intVar = intVars[i];
                RealVar realVar = realVars[i];
                realVar.updateBounds((double) intVar.getLB() - epsilon, (double) intVar.getUB() + epsilon, this);
                intVar.updateBounds((int) ceil(realVar.getLB() - epsilon), (int) floor(realVar.getUB() + epsilon), this);
                if (intVar.hasEnumeratedDomain()) {
                    realVar.updateBounds((double) intVar.getLB() - epsilon, (double) intVar.getUB() + epsilon, this);
                }
            }
        }

        @Override
        public ESat isEntailed() {
            assert intVars.length == realVars.length;
            boolean allInst = true;
            for (int i = 0; i < n; i++) {
                IntVar intVar = intVars[i];
                RealVar realVar = realVars[i];
                if ((realVar.getLB() < (double) intVar.getLB() - epsilon) || (realVar.getUB() > (double) intVar.getUB() + epsilon)) {
                    return ESat.FALSE;
                }
                if (!(intVar.isInstantiated() && realVar.isInstantiated())) {
                    allInst = false;
                }
            }
            return allInst ? ESat.TRUE : ESat.UNDEFINED;
        }

    }
}
