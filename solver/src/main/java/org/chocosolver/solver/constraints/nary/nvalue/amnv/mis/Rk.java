/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.mis;

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
