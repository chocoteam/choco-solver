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
package org.chocosolver.solver.constraints.nary.nValue.amnv.graph;

import org.chocosolver.solver.constraints.nary.nValue.amnv.differences.AutoDiffDetection;
import org.chocosolver.solver.constraints.nary.nValue.amnv.differences.D;
import org.chocosolver.solver.variables.IntVar;

/**
 * Constrained intersection graph
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class Gci extends Gi {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private D D;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates the constrained intersection graph of X and D
     *
     * @param X integer variables
     * @param D set of difference constraints
     */
    public Gci(IntVar[] X, D D) {
        super(X);
        this.D = D;
    }

    /**
     * Creates the constrained intersection graph of X and D
     * by automatically detecting disequalities and allDifferent constraints.
     *
     * @param X integer variables
     */
    public Gci(IntVar[] X) {
        this(X, new AutoDiffDetection(X));
    }

    //***********************************************************************************
    // ALGORITHMS
    //***********************************************************************************

    protected boolean intersect(int i, int j) {
        return !D.mustBeDifferent(i, j) && super.intersect(i, j);
    }

}
