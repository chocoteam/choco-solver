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

package common.util.objects.setDataStructures;

import java.io.Serializable;

/**
 * Class representing a set (of nodes)
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 9 feb. 2011
 */
public interface ISet extends Serializable {

    /**
     * Add element to the set
     * Does not guaranty there is no duplications
     *
     * @param element
     * @return true iff element was not in the set and has been added
     */
    boolean add(int element);

    /**
     * Remove the first occurence of element from the set
     *
     * @param element
     * @return true iff element was in the set and has been removed
     */
    boolean remove(int element);

    /**
     * Test the existence of element in the set
     *
     * @param element
     * @return true iff the set contains element
     */
    boolean contain(int element);

    /**
     * @return true iff the set is empty
     */
    boolean isEmpty();

    /**
     * @return the number of elements in the set
     */
    int getSize();

    /**
     * Remove all elements from the set
     */
    void clear();

    /**
     * @return the first element of the set, -1 empty set
     */
    int getFirstElement();

    /**
     * enables to iterate over the set
     * <p/>
     * should be used as follow :
     * <p/>
     * for(int i=getFirstElement(); i>=0; i = getNextElement()){
     * ...
     * }
     * <p/>
     * The use of getFirstElement() is necessary to ensure a complete iteration
     * <p/>
     * WARNING cannot encapsulate two for loops (copy the set for that)
     *
     * @return the next element of the set
     */
    int getNextElement();
}
