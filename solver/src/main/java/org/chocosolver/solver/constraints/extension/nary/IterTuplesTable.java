/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;

/**
 *
 **/
class IterTuplesTable extends TuplesList {

    /**
     * number of variables
     */
    private final int nbVar;
    /**
     * lower bound of each variable
     */
    private final int[] lowerbounds;

    /**
     * upper bound of each variable
     */
    private final int[] ranges;

    /**
     * table[i][j] gives the table of supports as an int[] for value j of variable i
     */
    private final int[][][] table;

    public IterTuplesTable(Tuples tuples, IntVar[] vars) {
        super(tuples, vars);
        nbVar = vars.length;
        lowerbounds = new int[nbVar];
        ranges = new int[nbVar];
        for (int i = 0; i < nbVar; i++) {
            lowerbounds[i] = vars[i].getLB();
            ranges[i] = vars[i].getUB() - lowerbounds[i] + 1;
        }

        table = new int[nbVar][][];
        for (int i = 0; i < nbVar; i++) {
            table[i] = new int[ranges[i]][];
            int[] nbsups = getNbSupportFor(i);
            for (int j = 0; j < nbsups.length; j++) {
                table[i][j] = new int[nbsups[j]];
            }
        }
        buildInitialListOfSupports();
    }

    // required for duplicate method, should not be called by default
    private IterTuplesTable(int[][] tuplesIndexes, int n, int[] lowerbounds, int[] ranges, int[][][] table) {
        super(tuplesIndexes);
        this.nbVar = n;
        this.lowerbounds = lowerbounds;
        this.ranges = ranges;
        this.table = table;
    }

    /**
     * return the number of tuples supporting each value of variable i
     *
     * @param i a variable
     * @return nb supports
     */
    public int[] getNbSupportFor(int i) {
        int[] nbsup = new int[ranges[i]];
        int nt = tuplesIndexes.length;
        for (int j = 0; j < nt; j++) {
            int[] tuple = tuplesIndexes[j];
            nbsup[tuple[i] - lowerbounds[i]]++;
        }
        return nbsup;
    }

    public void buildInitialListOfSupports() {
        int cpt = 0;
        int[][] level = new int[nbVar][];
        for (int i = 0; i < nbVar; i++) {
            level[i] = new int[ranges[i]];
        }
        int nt = tuplesIndexes.length;
        for (int j = 0; j < nt; j++) {
            int[] tuple = tuplesIndexes[j];
            for (int i = 0; i < tuple.length; i++) {
                int value = tuple[i] - lowerbounds[i];
                table[i][value][level[i][value]] = cpt;
                level[i][value]++;
            }
            cpt++;
        }
    }

    /**
     * for fast access
     *
     * @return table
     */
    public int[][][] getTableLists() {
        return table;
    }

    /**
     * This relation do not take advantage of the knowledge of the
     * previous support ! so start from scratch
     *
     * @param val is the value assuming the offset has already been
     *            removed
     */
    public int seekNextTuple(int oldidx, int var, int val) {
        int nidx = oldidx + 1;
        if (nidx < table[var][val].length) {
            return table[var][val][nidx];
        } else {
            return -1;
        }
    }

    /**
     * return the number of supports of the pair (var, val) assuming the
     * offset has already been removed
     */
    public int getNbSupport(int var, int val) {
        return table[var][val].length;
    }

    public int getRelationOffset(int var) {
        return lowerbounds[var];
    }

    public boolean checkTuple(int[] tuple) {
        throw new SolverException("checkTuple should not be used on an IterRelation");
    }
}
