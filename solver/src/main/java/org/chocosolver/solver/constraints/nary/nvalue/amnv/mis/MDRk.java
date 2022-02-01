/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.mis;

import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

import java.util.Random;

/**
 * Min Degree + Random k heuristic
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class MDRk extends MD {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected int k, iter;
    protected Random rd;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates an instance of the Min Degree + Random k heuristic to compute independent sets on graph
     *
     * @param graph the grah
     * @param k     number of random iterations
     */
    public MDRk(UndirectedGraph graph, int k) {
        super(graph);
        this.k = k;
        this.rd = new Random(0);
    }

    /**
     * Creates an instance of the Min Degree + Random k heuristic to compute independent sets on graph
     *
     * @param graph the graph
     */
    public MDRk(UndirectedGraph graph) {
        this(graph, Rk.defaultKValue);
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
        if (iter == 1) {
            super.computeMIS();
        } else {
            computeMISRk();
        }
    }

    protected void computeMISRk() {
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
            ISetIterator nei = graph.getNeighborsOf(idx).iterator();
            while (nei.hasNext()){
                out.set(nei.nextInt());
            }
        }
    }

    @Override
    public boolean hasNextMIS() {
        return iter < k;
    }
}
