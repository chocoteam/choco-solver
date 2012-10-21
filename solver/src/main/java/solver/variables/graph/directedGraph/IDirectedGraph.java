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

package solver.variables.graph.directedGraph;

import solver.variables.graph.IGraph;
import solver.variables.graph.ISet;

/**
 * @author Jean-Guillaume Fages
 * 
 * Provides services to manipulate directed graphs
 */
public interface IDirectedGraph extends IGraph {

    /**remove arc (from,to) from the graph
     * @param from
     * @param to
     * @return true iff arc (from,to) was in the graph
     */
    boolean removeArc(int from, int to);
    
    /**add arc (from,to) to the graph
     * @param from
     * @param to
     * @return true iff arc (from,to) was not already in the graph
     */
    boolean addArc(int from, int to);

    /**Get the successors of node x in the graph
     * @param x
     * @return successors of x in the graph 
     */
    ISet getSuccessorsOf(int x);
    
    /**Get the predecessors of node x in the graph
     * @param x
     * @return predecessors of x in the graph 
     */
    ISet getPredecessorsOf(int x);
    
    /**Test whether arc (x,y) exists or not in the graph
     * @param x
     * @param y
     * @return true iff arc (x,y) exists in the graph
     */
    boolean arcExists(int x, int y);

}
