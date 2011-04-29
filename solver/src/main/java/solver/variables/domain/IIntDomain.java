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

package solver.variables.domain;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import solver.variables.domain.delta.IntDelta;

import java.io.Serializable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28 juil. 2010
 */
public interface IIntDomain extends Serializable{

    /**
     * Returns the lower bound of <code>this</code>.
     * @return lower bound
     */
    int getLB();

    /**
     * Returns the upper bound of <code>this</code>.
     * @return upper bound
     */
    int getUB();

    /**
     * Returns the number of values in <code>this</code>.
     * @return number of values
     */
    int getSize();

    /**
     * Checks the emptyness of <code>this</code>, ie if there is at least one value remaining
     * @return true if the domain is empty, false otherwise
     */
    boolean empty();

    /**
     * Checsk if <code>this</code>. is reduced to a singleton
     * @return true if the domain is reduced to a singleton, false otherwise
     */
    boolean instantiated();

    /**
     * Checks if <code>aValue</code> is contained in <code>this</code>.
     * @param aValue value to check
     * @return true if the value is inside <code>this</code>, false otherwise
     */
    boolean contains(int aValue);

    /**
     * Checks if there is a aValue after <code>aValue</code> in <code>this</code>.
     * If <code>aValue</code> is greater than the upper bound, it returns false,
     * true otherwise
     * @param aValue departure aValue (excluded)
     * @return a boolean
     */
    boolean hasNextValue(int aValue);

    /**
     * Returns the value after <code>aValue</code> in <code>this</code>.
     * If <code>aValue</code> is lesser than the lower bound, it returns the lower bound,
     * if <code>aValue</code> is the upper bound, return Integer.MAX_VALUE
     * @param aValue departure value (excluded)
     * @return the value after aValue in the domain
     */
    int nextValue(int aValue);

    /**
     * Checks if there is a value before <code>aValue</code> in <code>this</code>.
     * If <code>aValue</code> is less than the lower bound, it returns false,
     * true otherwise
     * @param aValue departure value (excluded)
     * @return a boolean
     */
    boolean hasPreviousValue(int aValue);

    /**
     * Returns the value before <code>aValue</code> in <code>this</code>.
     * If <code>aValue</code> is greater than the upper bound, it returns the upper bound,
     * if <code>aValue</code> is the lower bound, return Integer.MIN_VALUE
     * @param aValue departure value (excluded)
     * @return the value before aValue in the domain
     */
    int previousValue(int aValue);

    /**
     * Gets an iterator over the values of <code>this</code>, in increasing order
     * @return a disposable iterator (call to #dispose() is mandatory)
     */
    DisposableIntIterator getIterator();

    /**
     * Restricts <code>this</code> to a singleton: <code>aValue</code>
     * @param aValue instantiation value
     * @return true if the restriction has been applied (ie <code>this</code> was not yet instantiated to <code>aValue</code>), false otherwise
     */
    boolean restrict(int aValue);

    /**
     * Restricts <code>this</code> to a singleton: <code>aValue</code>, and store the removed values in the delta
     * @param aValue instantiation value
     * @return true at least one value has been removed (ie <code>this</code> was not yet instantiated to <code>aValue</code>), false otherwise
     */
    boolean restrictAndUpdateDelta(int aValue);

    /**
     * Updates the lower bound of <code>this</code>, by removing values before <code>aValue</code>
     * @param aValue the new lower bound
     * @return true at least one value has been removed (ie the previous lower bound was smaller), false otherwise
     */
    boolean updateLowerBound(int aValue);

    /**
     * Updates the lower bound of <code>this</code>, by removing values before <code>aValue</code>,
     * and store the removed values in the delta
     * @param aValue the new lower bound
     * @return true at least one value has been removed (ie the previous lower bound was smaller), false otherwise
     */
    boolean updateLowerBoundAndDelta(int aValue);

    /**
     * Updates the upper bound of <code>this</code>, by removing values after <code>aValue</code>
     * @param aValue the new upper bound
     * @return true at least one value has been removed (ie the previous upper bound was greater), false otherwise
     */
    boolean updateUpperBound(int aValue);

    /**
     * Updates the upper bound of <code>this</code>, by removing values after <code>aValue</code>,
     * and store the removed values in the delta.
     * @param aValue the new upper bound
     * @return true at least one value has been removed (ie the previous upper bound was greater), false otherwise
     */
    boolean updateUpperBoundAndDelta(int aValue);

    /**
     * removes <code>aValue</code> from <code>this</code>
     * @param aValue value to remove
     * @return true if <code>aValue</code> has been removed (ie, <code>aValue</code> was contained in <code>this</code>),
     * false otherwise
     */
    boolean remove(int aValue);

    /**
     * removes <code>aValue</code> from <code>this</code>, and store it in the delta
     * @param aValue value to remove
     * @return true if <code>aValue</code> has been removed (ie, <code>aValue</code> was contained in <code>this</code>),
     * false otherwise
     */
    boolean removeAndUpdateDelta(int aValue);

    /**
     * Checks wether <code>this</code> is an enumerated domain, ie each value is physically stored
     * @return true if <code>this</code> is enumerated, false otherwise
     */
    boolean isEnumerated();

    /**
     * Returns the current delta, ie the set of removed value during the actual propagation loop.
     * @return the delta
     */
    IntDelta getDelta();

    void recordRemoveValues();

}
