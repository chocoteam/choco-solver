/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.constraints.nary.nValue.amnv.mis;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.BitSet;
import java.util.Random;

/**
 * Random heuristic
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class Rk implements F {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    public static int DEFAULT_K = 30;

    protected UndirectedGraph graph;
    protected int n, k, iter;
    protected BitSet out, inMIS;
    protected Random rd;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates an instance of the Random heuristic to compute independent sets on graph
     *
     * @param graph on which IS have to be computed
     * @param k     number of iterations (i.e. number of expected IS per propagation)
     */
    public Rk(UndirectedGraph graph, int k) {
        this.graph = graph;
        this.k = k;
        n = graph.getNbMaxNodes();
        out = new BitSet(n);
        inMIS = new BitSet(n);
        rd = new Random(0);
    }

    /**
     * Creates an instance of the Random heuristic to compute independent sets on graph
     * uses the default setting DEFAULT_K=30
     *
     * @param graph on which IS have to be computed
     */
    public Rk(UndirectedGraph graph) {
        this(graph, DEFAULT_K);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void prepare() {
        iter = 0;
    }

    @Override
    public void computeMIS() {
        iter++;
        out.clear();
        inMIS.clear();
        while (out.cardinality() < n) {
            int nb = rd.nextInt(n - out.cardinality());
            int idx = out.nextClearBit(0);
            for (int i = idx; i >= 0 && i < n && nb >= 0; i = out.nextClearBit(i + 1)) {
                idx = i;
                nb--;
            }
            inMIS.set(idx);
            out.set(idx);
            ISet nei = graph.getNeighOf(idx);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                out.set(j);
            }
        }
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    @Override
    public BitSet getMIS() {
        return inMIS;
    }

    @Override
    public boolean hasNextMIS() {
        return iter < k;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            graph.duplicate(solver, identitymap);
            UndirectedGraph g = (UndirectedGraph) identitymap.get(graph);
            identitymap.put(this, new Rk(g, k));
        }
    }
}
