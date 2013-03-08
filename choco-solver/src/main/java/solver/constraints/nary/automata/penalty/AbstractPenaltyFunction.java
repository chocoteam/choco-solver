/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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

package solver.constraints.nary.automata.penalty;

import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 30, 2010
 * Time: 1:57:07 PM
 */
public abstract class AbstractPenaltyFunction implements IPenaltyFunction {
    @Override
    public abstract int penalty(int value);

    @Override
    public double minGHat(double lambda, IntVar var) {

        double ghat = Double.POSITIVE_INFINITY;
        int ub = var.getUB();
        for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
            ghat = Math.min(ghat, penalty(i) - lambda * i);
        }
        return ghat;
    }

    @Override
    public double maxGHat(double lambda, IntVar var) {
        double ghat = Double.NEGATIVE_INFINITY;
        int ub = var.getUB();
        for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
            ghat = Math.max(ghat, penalty(i) - lambda * i);
        }
        return ghat;
    }
}
