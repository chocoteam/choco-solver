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

package solver.variables.graph;

/**Class representing the neighborhood (a set of nodes) of a node
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 fŽvr. 2011
 */
public interface INeighbors {

    /**Add element to the neighborhood
     * Does not guaranty there is no duplications
     * @param element
     */
    void add(int element);

    /**Remove the first occurence of element from the neighborhood
     * @param element
     * @return true iff element was in the neighborhood and has been removed
     */
    boolean remove(int element);

    /**Test the existence of element in the neighborhood
     * @param element
     * @return true iff the neighborhood contains element
     */
    boolean contain(int element);

    /**
     * @return true iff the neighborhood is empty
     */
    boolean isEmpty();

    /**
     * @return the number of elements in the neighborhood
     */
    int neighborhoodSize();

    /**
     * Remove all elements from the neighborhood
     */
    void clear();
    
    /**
     * @return the first element of the neighborhood, -1 empty set
     */
    int getFirstElement();

	/**enables to iterate over the neighborhood
	 * 
	 * should be used as follow :
	 * 
	 * for(int i=getFirstElement(); i>=0; i = getNextElement()){
	 * 		...
	 * }
	 * 
	 * The use of getFirstElement() is necessary to ensure a complete iteration
	 * 
	 * @return the next element of the neighborhood
	 */
	int getNextElement();
}
