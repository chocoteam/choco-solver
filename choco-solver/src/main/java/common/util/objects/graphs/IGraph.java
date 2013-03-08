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

package common.util.objects.graphs;

import common.util.objects.setDataStructures.ISet;
import common.util.objects.setDataStructures.SetType;

import java.io.Serializable;

/**
 * @author Jean-Guillaume Fages, Xavier Lorca
 *         <p/>
 *         Provide an interface for the graph manipulation
 */
public interface IGraph extends Serializable {


    /**
     * @return the collection of active nodes
     */
    ISet getActiveNodes();

    /**
     * Activate node x
     *
     * @param x a node index
     * @return true iff x was not already activated
     */
    boolean activateNode(int x);

    /**
     * Desactivate node x
     *
     * @param x a node index
     * @return true iff x was activated
     */
    boolean desactivateNode(int x);

    /**
     * The number of nodes of the graph
     *
     * @return the number of nodes of the graph
     */
    int getNbNodes();

    /**
     * Get the type of the graph
     *
     * @return the type of the graph SPARSE or DENSE
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
    ISet getSuccsOrNeigh(int x);

    /**
     * Get either x's predecessors or neighbors.
     * <p/>
     * This method enables to capitalize some code but should be called with care
     *
     * @param x a node index
     * @return x's predecessors if <code>this</code> is directed
     *         x's neighbors otherwise
     */
    ISet getPredsOrNeigh(int x);

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
}
