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

    /**
     * Prepare iteration from smallest value to highest value (using {@link #hasNext()} / {@link #next()})
     * <pre>
     * ValueIterator vit = ...;
     * vit.bottomUpInit();
     * while(vit.hasNext()){
     *    int v = vit.next();
     *    // operate on value v here
     * }</pre>
     * OR
     * <pre>
     */
    void bottomUpInit();

    /**
     * Prepare iteration from highest value to smallest value (using {@link #hasPrevious()} / {@link #previous()})
     * <pre>
     * ValueIterator vit = ...;
     * vit.topDownInit();
     * while(vit.hasPrevious()){
     *    int v = vit.previous();
     *    // operate on value v here
     * }</pre>
     */
    void topDownInit();

    /**
     * Returns <tt>true</tt> if the iteration has more values. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return valid value.)
     * <pre>
     * ValueIterator vit = ...;
     * vit.bottomUpInit();
     * while(vit.hasNext()){
     *    int v = vit.next();
     *    // operate on value v here
     * }</pre>
     * OR
     * <pre>
     *
     * @beware incompatible with {@link #hasPrevious()}
     *
     * @return <tt>true</tt> if the getIterator has more values.
     */
    boolean hasNext();

    /**
     * Returns <tt>true</tt> if the iteration has more ranges. (In other
     * words, returns <tt>true</tt> if <tt>previous</tt> would return a valid value.)
     * <pre>
     * ValueIterator vit = ...;
     * vit.topDownInit();
     * while(vit.hasPrevious()){
     *    int v = vit.previous();
     *    // operate on value v here
     * }</pre>
     *
     * @beware incompatible with {@link #hasNext()}
     *
     * @return <tt>true</tt> if the getIterator has more values.
     */
    boolean hasPrevious();

    /**
     * Compute and return the next value.
     * <pre>
     * ValueIterator vit = ...;
     * vit.bottomUpInit();
     * while(vit.hasNext()){
     *    int v = vit.next();
     *    // operate on value v here
     * }</pre>
     * OR
     * <pre>
     *
     * @beware incompatible with {@link #previous()}
     *
     * @return the next element in the iteration.
     */
    int next();

    /**
     * Compute and return the previous value.
     * <pre>
     * ValueIterator vit = ...;
     * vit.topDownInit();
     * while(vit.hasPrevious()){
     *    int v = vit.previous();
     *    // operate on value v here
     * }</pre>
     *
     * @beware incompatible with {@link #next()}
     *
     * @return the previous element in the iteration.
     */
    int previous();
}
