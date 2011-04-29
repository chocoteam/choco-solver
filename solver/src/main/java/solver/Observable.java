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

package solver;

import solver.exception.ContradictionException;

/**
 * This class represents an observable object, observed by some <code>T</code> objects
 * and reacting on <code>E</code> objects.
 * <br/><br/>
 * As defined in <code>java.util.Observable</code> javadocs:<br/>
 * "The order in which notifications will be delivered is unspecified.
 * The default implementation provided in the Observable class will notify Observers in the order
 * in which they registered interest, but subclasses may change this order, use no guaranteed order,
 * deliver notifications on separate threads, or may guarantee that their subclass follows this order, as they choose.
 * <br/><br/>
 * Note that this notification mechanism is has nothing to do with threads and is completely separate
 * from the wait and notify mechanism of class Object"
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @since 0.01
 */
public interface Observable<T,E> {

    /**
     * Adds an observers to the set of observers for this object,
     * provided that it is not the same as some observer already in the set.
     * @param observer an observer to add
     */
    public void addObserver(T observer);

    /**
     * Deletes an observers from the set of observers for this object.
     * @param observer the observer to delete
     */
    public void deleteObserver(T observer);

    /**
     * If this <code>Observable</code> object has changed, then notify all of its observers.<br/>
     * Each observer has its update method.
     * @param e event on this object
     * @param o object which leads to the modification of this object
     * @throws ContradictionException
     */
    public void notifyObservers(E e, T o) throws ContradictionException;

}
