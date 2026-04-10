/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
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
 * @author Charles Prud'homme
 * @since 12/07/13
 */
public abstract class Clause extends Reason {

    /**
     * @return an undefined clause
     */
    public static Clause undef() {
        return UNDEF;
    }

    /**
     * @return the number of literals composing the clause
     */
    public abstract int size();

    public abstract double getActivity();

    public abstract void setActivity(double activity);

    /**
     * @return literals block distance of this clause.
     */
    public abstract int getLBD();

    /**
     * Set the literals block distance of this clause.
     *
     * @param lbd the value to set
     */
    public abstract void setLBD(int lbd);

    /**
     * @return <i>true</i> if the clause is learnt, <i>false</i> otherwise
     */
    public boolean learnt() {
        return false;
    }

    @Override
    Clause getConflict() {
        return this;
    }

    /**
     * Get the i-th literal of the clause
     *
     * @param i index of the literal
     * @return the i-th literal of the clause
     */
    public abstract int _g(int i);

    /**
     * Set the i-th literal of the clause
     *
     * @param pos position of the literal
     * @param l   literal
     */
    public abstract void _s(int pos, int l);

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("T").append(Thread.currentThread().getId());
        st/*.append("~#").append(id)*/.append(" Size:").append(size()).append(" - ");
        if (size() > 0) {
            st.append(_g(0));
        }
        for (int i = 1; i < size(); i++) {
            st.append(" ∨ ").append(_g(i));
        }
        return st.toString();
    }

    public String toString(MiniSat sat) {
        StringBuilder st = new StringBuilder();
        st.append("#")/*.append(id)*/.append(" Size:").append(size()).append(" - ");
        if (size() > 0) {
            st.append(sat.printLit(_g(0)));
        }
        for (int i = 1; i < size(); i++) {
            st.append(" ∨ ").append(sat.printLit(_g(i)));
        }
        return st.toString();
    }
}
