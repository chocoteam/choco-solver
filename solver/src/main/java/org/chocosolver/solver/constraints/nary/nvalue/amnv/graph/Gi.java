/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue.amnv.graph;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

/**
 * Intersection Graph
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class Gi extends G {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected IntVar[] X;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates the intersection graph of X
     *
     * @param X integer variable
     */
    public Gi(IntVar[] X) {
        super(X[0].getModel(), X.length);
        this.X = X;
    }

    //***********************************************************************************
    // ALGORITHMS
    //***********************************************************************************

    public void build() {
        int n = getNbMaxNodes();
        for (int i = 0; i < n; i++) {
            getNeighOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            for (int i2 = i + 1; i2 < n; i2++) {
                if (intersect(i, i2)) {
                    addEdge(i, i2);
                }
            }
        }
    }

    public void update() {
        int n = getNbMaxNodes();
        for (int i = 0; i < n; i++) {
            update(i);
        }
    }

    public void update(int i) {
        ISetIterator nei = getNeighOf(i).iterator();
        while (nei.hasNext()) {
            int j = nei.nextInt();
            if (!intersect(i, j)) {
                removeEdge(i, j);
            }
        }
    }

    protected boolean intersect(int i, int j) {
        IntVar x = X[i];
        IntVar y = X[j];
        if (x.getLB() > y.getUB() || y.getLB() > x.getUB()) {
            return false;
        }
        int ub = x.getUB();
        for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
            if (y.contains(val)) {
                return true;
            }
        }
        return false;
    }

}
