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
package org.chocosolver.util.objects.graphs;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.io.Serializable;

/**
 * @author Jean-Guillaume Fages, Xavier Lorca
 *         <p/>
 *         Provide an interface for the graph manipulation
 */
public interface IGraph extends Serializable {


    /**
     * @return the collection of nodes present in the graph
     */
    ISet getNodes();

    /**
     * Adds node x to the node set of the graph
     *
     * @param x a node index
     * @return true iff x was not already present in the graph
     */
    boolean addNode(int x);

    /**
     * Remove node x from the graph
     *
     * @param x a node index
     * @return true iff x was present in the graph
     */
    boolean removeNode(int x);

    /**
     * The maximum number of nodes in the graph
	 * Vertices of the graph belong to [0,getNbMaxNodes()-1]
	 * This quantity is fixed at the creation of the graph
     *
     * @return the maximum number of nodes of the graph
     */
    int getNbMaxNodes();

    /**
     * Get the type of data structures used in the graph
	 *
     * @return the type of data structures used in the graph
     */
    SetType getType();


    /**
     * Get either x's successors or neighbors.
     * <p/>
     * This method enables to capitalize some code but should be called with care
     *
     * @param x a node index
     * @return x's successors if <code>this</code> is directed
     *         x's neighbors otherwise
     */
    ISet getSuccOrNeighOf(int x);

    /**
     * Get either x's predecessors or neighbors.
     * <p/>
     * This method enables to capitalize some code but should be called with care
     *
     * @param x a node index
     * @return x's predecessors if <code>this</code> is directed
     *         x's neighbors otherwise
     */
    ISet getPredOrNeighOf(int x);

    /**
     * If <code>this </code> is directed
     * returns true if and only if arc (x,y) exists
     * Else, if <code>this</code> is undirected
     * returns true if and only if edge (x,y) exists
     * <p/>
     * This method enables to capitalize some code but should be called with care
     *
     * @param x a node index
     * @param y a node index
     */
    boolean isArcOrEdge(int x, int y);

    /**
     * @return true if and only if <code>this</code> is a directed graph
     */
    boolean isDirected();

    void duplicate(Solver solver, THashMap<Object, Object> identitymap);
}
