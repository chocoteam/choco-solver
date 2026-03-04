/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.TIntList;

import java.util.Arrays;

import static org.chocosolver.sat.MiniSat.clauseCounter;

/**
 * An implementation of a clause as an array of literals.
 * @author Charles Prud'homme
 */
public final class ArrayClause extends Clause {
    /**
     * The list of literals composing the clause
     */
    private final int[] literals_;
    /**
     * Indicate if the clause is learnt or not
     */
    private final boolean learnt;
    /**
     * Activity of the clause (related to frequency of conflict)
     */
    double activity;

    /**
     * Literals block distance:
     * "Given a clause C, and a partition of its literals into n subsets according to the current assignment,
     * s.t. literals are partitioned w.r.t their decision level. The LBD of C is exactly n."
     */
    private int lbd = Integer.MAX_VALUE;
    /**
     * A unique id
     */
    private final int id;

    /**
     * Create a clause with a set of literals
     *
     * @param ps     literals
     * @param learnt indicate if the clause is learnt
     */
    public ArrayClause(int[] ps, boolean learnt) {
        //super(0);
        if (ps.length <= 3) { // 3 is the max. size of short_expl_3
            literals_ = ps.clone();
        } else {
            literals_ = ps.clone();//org.chocosolver.sat.ArrayClause.reduceOs(ps);
        }
        this.learnt = learnt;
        this.id = clauseCounter.get();
        clauseCounter.set(id + 1);
    }

    /**
     * This method reduces the number of occurrences of literal 0 in the clause.
     * If no 0 is present or if one 0 is present, the clause is returned as is.
     * If more than one 0 is present, the clause is reduced to remove all but one 0.
     *
     * @param ps literals
     * @return the reduced clause
     */
    private static int[] reduceOs(int[] ps) {
        // move 0s to the end of the array
        // the first 0 will be kept, the others will be removed
        int j = 1;
        for (int i = 1; i < ps.length; i++) {
            if (ps[i] != 0) {
                ps[j++] = ps[i];
            }
        }
        if (j < ps.length) {
            ps = Arrays.copyOf(ps, j);
        }
        return ps;
    }

    /**
     * Create a clause with a set of literals, not learnt
     *
     * @param ps literals
     */
    ArrayClause(int[] ps) {
        this(ps, false);
    }

    /**
     * Create a clause with a set of literals
     *
     * @param ps     literals
     * @param learnt indicate if the clause is learnt
     */
    public ArrayClause(TIntList ps, boolean learnt) {
        //super(0);
        literals_ = ps.toArray();
        this.learnt = learnt;
        this.id = clauseCounter.get();
        clauseCounter.set(id + 1);
    }

    /**
     * Create a clause with a set of literals, not learnt
     *
     * @param ps literals
     */
    ArrayClause(TIntList ps) {
        this(ps, false);
    }
    /**
     * @return the number of literals composing the clause
     */
    public int size() {
        return literals_.length;
    }

    @Override
    public double getActivity() {
        return this.activity;
    }

    @Override
    public void setActivity(double activity) {
        this.activity = activity;
    }

    @Override
    public int getLBD() {
        return this.lbd;
    }

    @Override
    public void setLBD(int lbd) {
        this.lbd = lbd;
    }

    /**
     * @return <i>true</i> if the clause is learnt, <i>false</i> otherwise
     */
    public boolean learnt() {
        return learnt;
    }

    /**
     * Get the i-th literal of the clause
     *
     * @param i index of the literal
     * @return the i-th literal of the clause
     */
    public int _g(int i) {
        return literals_[i];
    }

    /**
     * Set the i-th literal of the clause
     *
     * @param pos position of the literal
     * @param l   literal
     */
    public void _s(int pos, int l) {
        literals_[pos] = l;
    }
}
