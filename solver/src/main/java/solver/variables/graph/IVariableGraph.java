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

import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.exception.ContradictionException;

/**An interface for graph variable manipulation in constraint programming
 * @author Jean-Guillaume Fages, Xavier Lorca
 *
 */
public interface IVariableGraph<E extends IGraph> {

    /**
     * Remove node x from the maximal partial subgraph
     *
     * @param x node's index
     * @param cause algorithm which is related to the removal
     * @return true iff the removal has an effect
     */
    boolean removeNode(int x, @NotNull ICause cause) throws ContradictionException;
    
    /**
     * Enforce the node x to belong to any partial subgraph
     *
     * @param x node's index
     * @param cause algorithm which is related to the modification
     * @return true iff the node is effectively added to the mandatory structure
     */
    boolean enforceNode(int x, @NotNull ICause cause) throws ContradictionException;

    /**
     * Remove node y from the neighborhood of node x from the maximal partial subgraph
     *
     * @param x node's index
     * @param y node's index
     * @param cause algorithm which is related to the removal
     * @return true iff the removal has an effect
     * @throws ContradictionException 
     */
    boolean removeArc(int x, int y, @NotNull ICause cause) throws ContradictionException;

    /**
     * Enforce the node y into the neighborhood of node x in any partial subgraph
     *
     * @param x node's index
     * @param y node's index
     * @param cause algorithm which is related to the removal
     * @return true iff the node y is effectively added in the neighborhooh of node x
     */
    boolean enforceArc(int x, int y, @NotNull ICause cause) throws ContradictionException;


    /**
     * Compute the order of the graph in its current state (ie the number of nodes that may belong to an instantiation)
     * @return the number of nodes that may belong to an instantiation
     */
    int getEnvelopOrder();

    /**
     * Compute the order of the graph in its final state (ie the minimum number of nodes that necessarily belong to any instantiation)
     * @return the minimum number of nodes that necessarily belong to any instantiation
     */
    int getKernelOrder();

    /**
     * @return the graph representing the domain of the variable graph
     */
    E getKernelGraph();
    
    /**
     * @return the graph representing the instantiated values (nodes and edges) of the variable graph
     */
    E getEnvelopGraph();
}
