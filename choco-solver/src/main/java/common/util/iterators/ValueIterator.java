/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package common.util.iterators;

/**
 * An interface to declare values iterator.
 * <p/>
 * A value iterator can be iterated in 2 ways: bottom-up (from lower bound to upper bound) <br/>
 * and top-down (from upper bound to lower bound).<br/>
 * To iterate in bottom-up way, first call bottomUpInit(), then hasNext() and next().<br/>
 * To iterate in bottom-up way, first call topDownInit(), then hasPrevious() and previous().<br/>
 * <br/>
 * Once a way is selected, using the wrong methods can lead to unexpected behaviour.
 * <p/>
 * <pre>
 * ValueIterator vit = ...;
 * vit.bottomUpInit();
 * while(vit.hasNext()){
 *    int v = vit.next();
 *    // operate on value v here
 * }</pre>
 * OR
 * <pre>
 * ValueIterator vit = ...;
 * vit.topDownInit();
 * while(vit.hasPrevious()){
 *    int v = vit.previous();
 *    // operate on value v here
 * }</pre>
 *
 * @author Charles Prud'homme
 * @since 05/10/11
 */
public interface ValueIterator {

    void bottomUpInit();

    void topDownInit();

    /**
     * Returns <tt>true</tt> if the iteration has more values. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return valid value.)
     *
     * @return <tt>true</tt> if the getIterator has more values.
     */
    abstract boolean hasNext();

    /**
     * Returns <tt>true</tt> if the iteration has more ranges. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return a valid value.)
     *
     * @return <tt>true</tt> if the getIterator has more values.
     */
    abstract boolean hasPrevious();

    /**
     * Compute and return the next value.
     *
     * @return the next element in the iteration.
     */
    abstract int next();

    /**
     * Compute and return the next value.
     *
     * @return the previous element in the iteration.
     */
    abstract int previous();

}
