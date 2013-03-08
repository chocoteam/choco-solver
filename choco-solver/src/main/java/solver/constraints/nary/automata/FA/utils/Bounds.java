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

package solver.constraints.nary.automata.FA.utils;

import solver.constraints.nary.automata.penalty.IPenaltyFunction;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 23, 2010
 * Time: 11:12:10 AM
 */
public class Bounds {

    public MinMax min, max;

    public class MinMax {
        public int value = Integer.MIN_VALUE;
        public int prefered = Integer.MIN_VALUE;
        public IPenaltyFunction penalty = null;


    }

    private Bounds(int minValue, int minPrefered, IPenaltyFunction minPenaltyFunction,
                   int maxValue, int maxPrefered, IPenaltyFunction maxPenaltyFunction) {
        min = new MinMax();
        max = new MinMax();

        min.value = minValue;
        min.prefered = minPrefered;
        min.penalty = minPenaltyFunction;

        max.value = maxValue;
        max.prefered = maxPrefered;
        max.penalty = maxPenaltyFunction;

    }

    public static Bounds makeBounds(int minValue, int minPrefered, IPenaltyFunction minPenaltyFunction,
                                    int maxValue, int maxPrefered, IPenaltyFunction maxPenaltyFunction) {
        return new Bounds(minValue, minPrefered, minPenaltyFunction, maxValue, maxPrefered, maxPenaltyFunction);
    }

    public static Bounds makeMinBounds(int minValue, int minPrefered, IPenaltyFunction minPenaltyFunction) {
        return new Bounds(minValue, minPrefered, minPenaltyFunction, Integer.MIN_VALUE, Integer.MIN_VALUE, null);
    }

    public static Bounds makeMaxBounds(int maxValue, int maxPrefered, IPenaltyFunction maxPenaltyFunction) {
        return new Bounds(Integer.MIN_VALUE, Integer.MIN_VALUE, null, maxValue, maxPrefered, maxPenaltyFunction);
    }


    public static void main(String[] args) {
        Bounds a = new Bounds(0, 0, null, 0, 0, null);

        Bounds b = new Bounds(9, 9, null, 9, 9, null);


        System.out.println(b.min.prefered);
        System.out.println(a.min.prefered);


    }


}
