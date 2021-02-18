/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;



/**
 * A simple class that provides a method to check if a given
 * tuple is valid i.e. if it is ok regarding the current domain
 * of the variables
 */
public class ValidityChecker implements IntComparator {

    //variables sorted from the minimum domain to the max
    protected IntVar[] vars;
    public int[] sortedidx;
    protected int arity;
    protected ArraySort sorter;

    public ValidityChecker(int ari, IntVar[] vars) {
        arity = ari;
        this.vars = new IntVar[arity];
        sortedidx = new int[arity];
        sorter = new ArraySort(arity, false, true);
        for (int i = 0; i < vars.length; i++) {
            this.vars[i] = vars[i];
            sortedidx[i] = i;
        }
    }

    public final int getPosition(int idx) {
        return idx;
    }

    /**
     * Sort the variable to speedup the check
     */
    public void sortvars() {
        for (int i = 0; i < arity; i++) {
            sortedidx[i] = i;
        }
        sorter.sort(sortedidx, arity, this);
        boolean correct = true;
        for(int i = 0; i < vars.length-1; i++){
            correct &= vars[sortedidx[i]].getDomainSize() <= vars[sortedidx[i + 1]].getDomainSize();
        }
        assert correct : "wrong sort";
    }

    // Is tuple valide ?
    public boolean isValid(int[] tuple) {
        for (int i = 0; i < arity; i++)
            if (!vars[sortedidx[i]].contains(tuple[sortedidx[i]])) return false;
        return true;
    }

    @Override
    public int compare(int i1, int i2) {
        return vars[i1].getDomainSize() - vars[i2].getDomainSize();
    }
}
