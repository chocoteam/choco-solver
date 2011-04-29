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
package solver.search.strategy.enumerations.sorters;

/**
 * Generic way to define a Random object comparator.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/01/11
 */
public class Random<E> extends AbstractSorter<E> {

    final java.util.Random rand;

    public Random() {
        this.rand = new java.util.Random();
    }

    public Random(long seed) {
        this.rand = new java.util.Random(seed);
    }

    public Random(java.util.Random rand) {
        this.rand = rand;
    }

    @Override
    public int sort(E[] elements, int from, int to) {
        /*E swap;
        for (int i = from; i < to; i++) {
            int j = i + 1 + rand.nextInt(to - i);
            swap = elements[i];
            elements[i] = elements[j];
            elements[j] = swap;
        }
        //BEWARE: could return to, return from or as below
        return from + rand.nextInt(to - from + 1);*/
        swap(elements, from, from + rand.nextInt(to - from + 1));
        return from;
    }

    @Override
    public int minima(E[] elements, int from, int to) {
        return sort(elements, from, to);
    }

    @Override
    public int compare(E o1, E o2) {
        return rand.nextBoolean() ? 1 : -1;
    }
}
