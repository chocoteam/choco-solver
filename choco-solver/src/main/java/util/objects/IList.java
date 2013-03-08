/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package util.objects;

import util.Indexable;

import java.io.Serializable;

/**
 * A IList is a container of elements.
 * An element in this list has particular behavior: it can change of state (from active to passive),
 * this container must consider this information.
 * Restoring the previous state of elements must be done upon backtracking.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/02/11
 */
public interface IList<V, E extends Indexable> extends Serializable {

    /**
     * Add a new <code>element</code>
     *
     * @param element        to add
     * @param dynamic        is it dynamice
     * @param activeSilently is it activated
     */
    void add(E element, boolean dynamic, boolean activeSilently);

    /**
     * Activate a element
     *
     * @param element the modified element
     */
    void setActive(E element);

    /**
     * Desactivate a element
     *
     * @param element the modified element
     */
    void setPassive(E element);

    /**
     * Permanently delete <code>element</code>
     *
     * @param element to delete
     */
    void remove(E element);

    /**
     * Returns the total number of element contained.
     *
     * @return the total number of element contained.
     */
    int size();

    /**
     * Returns the number of active elements.
     *
     * @return the number of active elements.
     */
    int cardinality();

    E get(int i);
}
