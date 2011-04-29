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

package solver.constraints.propagators.nary.intlincomb.policy;

import choco.kernel.common.util.tools.MathUtils;
import solver.variables.IntVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 23 avr. 2010<br/>
 * Since : Galak 0.1<br/>
 */
final class ForScalar extends AbstractCoeffPolicy {

    static AbstractCoeffPolicy get(final IntVar[] vars, final int[] coeffs, final int nbPosVars, final int cste) {
        return new ForScalar(vars, coeffs, nbPosVars, cste);
    }

    private ForScalar(final IntVar[] vars, final int[] coeffs, final int nbPosVars, final int cste) {
        super(vars, coeffs, nbPosVars, cste);
    }

    @Override
    public int getInfNV(final int i, final int mylb) {
        return MathUtils.divCeil(mylb, -coeffs[i]) + vars[i].getUB();
    }

    @Override
    public int getSupNV(final int i, final int myub) {
        return MathUtils.divFloor(myub, -coeffs[i]) + vars[i].getLB();
    }

    @Override
    public int getInfPV(final int i, final int myub) {
        return MathUtils.divCeil(-myub, coeffs[i]) + vars[i].getUB();
    }

    @Override
    public int getSupPV(final int i, final int mylb) {
        return MathUtils.divFloor(-mylb, coeffs[i]) + vars[i].getLB();
    }

    @Override
    public int computeLowerBound() {
        int lb = cste;
        for (int i = 0; i < nbPosVars; i++) {
            lb += coeffs[i] * vars[i].getLB();
        }
        for (int i = nbPosVars; i < vars.length; i++) {
            lb += coeffs[i] * vars[i].getUB();
        }
        return lb;
    }


    @Override
    public int computeUpperBound() {
        int ub = cste;
        for (int i = 0; i < nbPosVars; i++) {
            ub += coeffs[i] * vars[i].getUB();
        }
        for (int i = nbPosVars; i < vars.length; i++) {
            ub += coeffs[i] * vars[i].getLB();
        }
        return ub;
    }
}
