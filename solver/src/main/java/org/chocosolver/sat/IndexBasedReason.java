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

/**
 * An index-based reason, which relies on an index to retrieve the literals of the reason.
 * <br/>
 * This representation is useful to recycle reasons and limit calls to GC.
 * <br/>
 * @author Charles Prud'homme
 */
public final class IndexBasedReason extends Reason {

    private int index;
    private IReasonManager.IndexBasedManager manager;

    /**
     * Set up the reason with the given index and manager.
     * @param idx the index of the reason
     * @param manager the manager to retrieve the literals of the reason
     */
    public void setUp(int idx, IReasonManager.IndexBasedManager manager) {
        this.manager = manager;
        this.index = idx;
    }

    /**
     * @return the index of the reason
     */
    public int getIndex() {
        return this.index;
    }

    @Override
    Clause getConflict() {
        ReasonClause expl = manager.getSharedIndexBasedClause();
        expl.setIndex(this);
        return expl;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("lits: ");
        st.append(manager.getVal(index + 1));
        for (int i = 2; i <= manager.getVal(index); i++) {
            st.append(" ∨ ").append(manager.getVal(index + i));
        }
        return st.toString();
    }

    /**
     * A clause that retrieves its literals from the manager using the index of the reason.
     */
    public static final class ReasonClause extends Clause {

        private IndexBasedReason reason;

        void setIndex(IndexBasedReason reason) {
            this.reason = reason;
        }

        @Override
        public int size() {
            return reason.manager.getVal(reason.index);
        }

        @Override
        public double getActivity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setActivity(double activity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLBD() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLBD(int lbd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int _g(int i) {
            return reason.manager.getVal(reason.index + 1 + i);
        }

        @Override
        public void _s(int pos, int l) {
            reason.manager.setVal(reason.index + 1 + pos, l);
        }

        @Override
        public String toString(MiniSat sat) {
            return "@" + reason.index + "--" + super.toString(sat);
        }
    }
}
