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

import solver.variables.IntVar;

import java.io.Serializable;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 23 avr. 2010br/>
 * Since : Galak 0.1<br/>
 */
public abstract class AbstractCoeffPolicy implements Serializable {

    final IntVar[] vars;

    final int cste;

    final int nbPosVars;

    final int[] coeffs;


    public static AbstractCoeffPolicy build(final IntVar[] vars, final int[] coeffs, final int nbPosVars, final int cste){
        if(AbstractCoeffPolicy.isIntSum(coeffs, nbPosVars)){
            return ForSum.get(vars, coeffs, nbPosVars, cste);
        }else{
            return ForScalar.get(vars, coeffs, nbPosVars, cste);
        }
    }

    private static boolean isIntSum(int[] sortedCoeffs, int nbPositiveCoeffs) {
		for (int i = 0; i < nbPositiveCoeffs; i++) {
			if( sortedCoeffs[i] != 1){
                return false;
            }
		}
		for (int i = nbPositiveCoeffs; i < sortedCoeffs.length; i++) {
			if(sortedCoeffs[i] != -1){
                return false;
            }
		}
		return true;
	}

    AbstractCoeffPolicy(final IntVar[] vars, final int[] coeffs, final int nbPosVars, final int cste) {
        this.vars = vars;
        this.coeffs = coeffs;
        this.nbPosVars = nbPosVars;
        this.cste = cste;
    }

    public abstract int getInfNV(final int i, final int mylb);

    public abstract int getSupNV(final int i, final int myub);

    public abstract int getInfPV(final int i, final int myub);

    public abstract int getSupPV(final int i, final int mylb);

    /**
     * Computes an upper bound estimate of a linear combination of variables.
     *
     * @return the new upper bound value
     */
    public abstract int computeUpperBound();

    /**
     * Computes a lower bound estimate of a linear combination of variables.
     *
     * @return the new lower bound value
     */
    public abstract int computeLowerBound();
}
