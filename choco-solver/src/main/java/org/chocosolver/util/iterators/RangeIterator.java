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
package org.chocosolver.util.iterators;

/**
 * An interface to declare range iterator.
 * <p/>
 * A range iterator can be iterated in 2 ways: bottom-up (from lower bound to upper bound) <br/>
 * and top-down (from upper bound to lower bound).<br/>
 * To iterate in bottom-up way, first call bottomUpInit(), then hasNext() and next().<br/>
 * To iterate in bottom-up way, first call topDownInit(), then hasPrevious() and previous().<br/>
 * <br/>
 * Once a way is selected, using the wrong methods can lead to unexpected behaviour.
 * <p/>
 * <pre>
 * RangeIterator rit = ...;
 * rit.bottomUpInit();
 * while (rit.hasNext()) {
 *     int from = rit.min();
 *     int to = rit.max();
 *     // operate on range [from,to] here
 *     rit.next();
 * }</pre>
 *
 * OR
 *
 * <pre>
 * DisposableRangeIterator rit = ...;
 * rit.topDownInit();
 * while (rit.hasPrevious()) {
 *     int from = rit.min();
 *     int to = rit.max();
 *     // operate on range [from,to] here
 *     rit.previous();
 * }</pre>
 *
 * <br/>
 * Based on <br/>"Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/10/11
 */
public interface RangeIterator {

    void bottomUpInit();

    void topDownInit();

    /**
     * Returns <tt>true</tt> if the iteration has more ranges. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return a valid range.)
     *
     * @return <tt>true</tt> if the getIterator has more ranges.
     */
    abstract boolean hasNext();

    /**
     * Returns <tt>true</tt> if the iteration has more ranges. (In other
     * words, returns <tt>true</tt> if <tt>previous</tt> would return a valid range.)
     *
     * @return <tt>true</tt> if the getIterator has more ranges.
     */
    abstract boolean hasPrevious();

    /**
     * Compute the next range.
     */
    abstract void next();

    /**
     * Compute the previous range.
     */
    abstract void previous();

    /**
     * Return the lower bound of the current range (inclusive)
     *
     * @return lower bound of the current range
     */
    abstract int min();

    /**
     * Return the upper bound of the current range (inclusive)
     *
     * @return upper bound of the current range
     */
    abstract int max();
}
