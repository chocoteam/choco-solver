/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
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
 * Clause -- a simple class for representing a clause
 * <br/>
 *
 * @author Charles Prud'homme, Laurent Perron
 * @since 12/07/13
 */
public class Clause extends Reason {
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
     * A unique id
     */
    private final int id;

    /**
     * Create a clause with a set of literals
     *
     * @param ps     literals
     * @param learnt indicate if the clause is learnt
     */
    public Clause(int[] ps, boolean learnt) {
        super(0);
        if (ps.length <= 3) { // 3 is the max. size of short_expl_3
            literals_ = ps.clone();
        } else {
            literals_ = Clause.reduceOs(ps);
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
        if(j < ps.length) {
            ps = Arrays.copyOf(ps, j);
        }
        return ps;
    }

    /**
     * Create a clause with a set of literals, not learnt
     *
     * @param ps literals
     */
    Clause(int[] ps) {
        this(ps, false);
    }

    /**
     * Create a clause with a set of literals
     *
     * @param ps     literals
     * @param learnt indicate if the clause is learnt
     */
    public Clause(TIntList ps, boolean learnt) {
        super(0);
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
    Clause(TIntList ps) {
        this(ps, false);
    }

    /**
     * @return an undefined clause
     */
    public static Clause undef() {
        return UNDEF;
    }

    @Override
    Clause getConflict() {
        return this;
    }

    /**
     * @return the number of literals composing the clause
     */
    public int size() {
        return literals_.length;
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
    void _s(int pos, int l) {
        literals_[pos] = l;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("T").append(Thread.currentThread().getId());
        st.append("~#").append(id).append(" Size:").append(literals_.length).append(" - ");
        if (literals_.length > 0) {
            st.append(literals_[0]);
        }
        for (int i = 1; i < literals_.length; i++) {
            st.append(" ∨ ").append(literals_[i]);
        }
        return st.toString();
    }

    public String toString(MiniSat sat) {
        StringBuilder st = new StringBuilder();
        st.append("#").append(id).append(" Size:").append(literals_.length).append(" - ");
        if (literals_.length > 0) {
            st.append(sat.printLit(literals_[0]));
        }
        for (int i = 1; i < literals_.length; i++) {
            st.append(" ∨ ").append(sat.printLit(literals_[i]));
        }
        return st.toString();
    }
}
