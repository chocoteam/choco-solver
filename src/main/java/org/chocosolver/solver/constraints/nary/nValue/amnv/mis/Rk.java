/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.nary.nValue.amnv.mis;

import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

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

    /** number of random iterations **/
    public static int defaultKValue = 30;

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
        this(graph, defaultKValue);
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
            ISetIterator nei = graph.getNeighOf(idx).iterator();
            while (nei.hasNext()) {
                out.set(nei.nextInt());
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
}
