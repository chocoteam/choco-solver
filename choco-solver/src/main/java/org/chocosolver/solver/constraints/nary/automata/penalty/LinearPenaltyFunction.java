/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.nary.automata.penalty;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: May 3, 2010
 * Time: 5:49:24 PM
 */
public class LinearPenaltyFunction extends AbstractPenaltyFunction {

    /**
     * minimum bound
     */
    private int min;

    /**
     * soft minimum bound (= min if not soft).
     */
    private int minPref;

    /**
     * unit violation cost of the soft minimum bound (= 0 if not soft).
     */
    private int minPenalty;

    /**
     * maximum value
     */
    private int max;

    /**
     * soft maximum value (= max if not soft).
     */
    private int maxPref;

    /**
     * unit violation cost of the soft maximum value (= 0 if not soft).
     */
    private int maxPenalty;


    public LinearPenaltyFunction(int min, int minPref, int minPenalty, int max, int maxPref, int maxPenalty) {
        this.min = min;
        this.max = max;
        this.minPref = minPref;
        this.maxPref = maxPref;
        this.minPenalty = minPenalty;
        this.maxPenalty = maxPenalty;

    }


    @Override
    public int penalty(int value) {
        if (value < minPref) {
            if (value >= min) {
                return (minPref - value) * minPenalty;
            } else {
                return Integer.MAX_VALUE;
            }
        } else if (value > maxPref) {
            if (value <= max) {
                return (value - maxPref) * maxPenalty;
            } else {
                return Integer.MAX_VALUE;
            }

        } else {
            return 0;
        }
    }
}
