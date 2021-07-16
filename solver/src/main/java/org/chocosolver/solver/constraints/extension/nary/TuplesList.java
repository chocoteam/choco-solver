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

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A simple way of storing the tuples as a list. This doesn't allow
 * consistency check (TuplesTable is made for that)
 * or iteration over supports of each value (IterTuplesTable is made for that)
 * This simple way of storing supports only allow fast iteration over the all
 * set of tuples and is used by STR gac scheme.
 */
public class TuplesList extends LargeRelation {

    // each tuple (a int[]) has its own index
    protected final int[][] tuplesIndexes;

    protected static final Comparator<int[]> TCOMP = (o1, o2) -> {
        int n = o1.length;
        int i = 0;
        while (i < n && o1[i] == o2[i]) i++;
        if (i == n) return 0;
        if (o1[i] < o2[i]) return -1;
        return 1;
    };

    // required for duplicate method, should not be called by default
    TuplesList(int[][] tuplesIndexes) {
        this.tuplesIndexes = tuplesIndexes;
        Arrays.sort(tuplesIndexes, TCOMP);
    }

    public TuplesList(Tuples tuples, IntVar[] vars) {
        int nb = tuples.nbTuples();
        int[][] _tuplesIndexes = new int[nb][];
        int k = 0;
        for (int i = 0; i < nb; i++) {
            int[] tuple = tuples.get(i);
            if (valid(tuple, vars)) {
                _tuplesIndexes[k++] = tuple;
            }
        }
        tuplesIndexes = new int[k][];
        System.arraycopy(_tuplesIndexes, 0, tuplesIndexes, 0, k);
        Arrays.sort(tuplesIndexes, TCOMP);

    }

    public int[] getTuple(int support) {
        return tuplesIndexes[support];
    }

    public int[][] getTupleTable() {
        return tuplesIndexes;
    }

    public boolean checkTuple(int[] tuple) {
        throw new SolverException("TuplesList is an unusual large relation...");
    }

    public boolean isConsistent(int[] tuple) {
        return Arrays.binarySearch(tuplesIndexes, tuple, TCOMP) >= 0;
    }

    @Override
    public Tuples convert() {
        Tuples tuples = new Tuples(true);
        for(int[] t: tuplesIndexes){
            tuples.add(t);
        }
        return tuples;
    }
}
