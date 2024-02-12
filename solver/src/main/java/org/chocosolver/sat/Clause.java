/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.TIntList;

/**
 * Clause -- a simple class for representing a clause
 * <br/>
 *
 * @author Charles Prud'homme, Laurent Perron
 * @since 12/07/13
 */
public class Clause extends Reason{
    static int counter = 0;
    //private static Clause UNDEF = new Clause(new int[0]);

    private final int[] literals_;
    private final boolean learnt;
    double activity;

    private final int id;

    public Clause(int[] ps, boolean learnt) {
        super(0);
        literals_ = ps.clone();
        this.learnt = learnt;
        this.id = counter++;
    }

    Clause(int[] ps) {
        this(ps, false);
    }

    public Clause(TIntList ps, boolean learnt) {
        super(0);
        literals_ = ps.toArray();
        this.learnt = learnt;
        this.id = counter++;
    }

    Clause(TIntList ps) {
        this(ps, false);
    }

    public static Clause undef() {
        return UNDEF;
    }

    @Override
    Clause getConflict() {
        return this;
    }

    public int size() {
        return literals_.length;
    }

    public boolean learnt() {
        return learnt;
    }


    public int _g(int i) {
        return literals_[i];
    }

    void _s(int pos, int l) {
        literals_[pos] = l;
    }

    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("#").append(id).append(" Size:").append(literals_.length).append(" - ");
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
