/**
*  Copyright (c) 2010, Ecole des Mines de Nantes
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

package solver.constraints.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.PropAllDiffAC;
import solver.constraints.propagators.nary.PropInverseChanneling;
import solver.variables.IntVar;

/**
 * X[i + o] = j <=> Y[j + o] = i
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
public class InverseChanneling extends IntConstraint<IntVar> {

    protected IntVar[] X, Y;

    protected final int Ox, Oy;

    protected final int nbX, nbY;


    public InverseChanneling(IntVar[] X, IntVar[] Y, Solver solver) {
        this(X, Y, solver, _DEFAULT_THRESHOLD);
    }

    public InverseChanneling(IntVar[] X, IntVar[] Y, Solver solver, PropagatorPriority threshold) {
        super(ArrayUtils.append(X, Y), solver, threshold);
        this.X = X.clone();
        this.Y = Y.clone();
        nbX = X.length;
        nbY = Y.length;
        int _oX = Integer.MAX_VALUE;
        for (int i = 0; i < nbX; i++) {
            if (_oX > X[i].getLB()) {
                _oX = X[i].getLB();
            }
        }
        Ox = -_oX;

        int _oY = Integer.MAX_VALUE;
        for (int i = 0; i < nbY; i++) {
            if (_oY > Y[i].getLB()) {
                _oY = Y[i].getLB();
            }
        }
        Oy = -_oY;

        setPropagators(
                new PropAllDiffAC(this.X, solver.getEnvironment(), this),
                new PropAllDiffAC(this.Y, solver.getEnvironment(), this),
                new PropInverseChanneling(this.X, this.Y, Ox, Oy, solver.getEnvironment(), this));
    }

    /**
     * Checks if the constraint is satisfied when all variables are instantiated.
     *
     * @param tuple an complete instantiation
     * @return true iff a solution
     */
    @Override
    public ESat isSatisfied(int[] tuple) {
        for (int i = 0; i < nbX; i++) {
            int j = tuple[i] + Ox;
            if (tuple[j] != (i - Oy)) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }

    @Override
    public ESat isSatisfied() {
        for (int i = 0; i < nbX; i++) {
            // X[i] = j' && j = j' + Ox[i] => Y[j] = i' && i' = i - Oy[j]
            if (X[i].instantiated()) {
                int j = X[i].getValue() + Ox;
                if(j < 0 || j > nbY){
                    return ESat.FALSE;
                }else
                if (Y[j].instantiated()){
                    if(Y[j].getValue() != (i - Oy)) {
                        return ESat.FALSE;
                    }
                }else{
                    return ESat.UNDEFINED;
                }
            } else {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InverseChanneling({");
        for (int i = 0; i < nbX; i++) {
            if (i > 0) sb.append(", ");
            sb.append(X[i]);
        }
        sb.append("}, {");
        for (int i = 0; i < nbY; i++) {
            if (i > 0) sb.append(", ");
            sb.append(Y[i]);
        }
        sb.append("})");
        return sb.toString();
    }

}
