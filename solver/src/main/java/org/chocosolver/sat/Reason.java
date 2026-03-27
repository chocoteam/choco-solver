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
 * A class to explain a modification.
 * A reason is always associated with one or more literals.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/09/2023
 */
public abstract class Reason {

    /**
     * An undefined reason.
     * This reason is static and should not be modified.
     */
    static final Clause UNDEF = new ArrayClause(new int[]{0});
    /**
     * A thread-local clause to explain a modification with one literal.
     * This clause is static and can be reused in the same thread.
     */
    private final static ThreadLocal<Clause> short_expl_2 = ThreadLocal.withInitial(() -> new ArrayClause(new int[]{0, 0}));
    /**
     * A thread-local clause to explain a propagation with two literals.
     * This clause is static and can be reused in the same thread.
     */
    private final static ThreadLocal<Clause> short_expl_3 = ThreadLocal.withInitial(() -> new ArrayClause(new int[]{0, 0, 0}));

    /**
     * Create an undefined reason.
     *
     * @return an undefined static reason
     * @implSpec In practice, this reason is static and thus should not be modified.
     */
    public static Clause undef() {
        return UNDEF;
    }

    /**
     * Extract the conflict clause from the reason.
     *
     * @return a clause
     */
    abstract Clause getConflict();

    /**
     * A reason with a single literal
     */
    final static class Reason1 extends Reason {
        int d1;

        Reason1 set(int d1) {
            this.d1 = d1;
            return this;
        }

        @Override
        public Clause getConflict() {
            Clause c = short_expl_2.get();
            c._s(1, d1);
            return c;
        }

        @Override
        public String toString() {
            return "lits: 0 ∨ " + d1;
        }
    }

    /**
     * A reason with two literals
     */
    final static class Reason2 extends Reason {
        int d1;
        int d2;

        Reason2 set(int d1, int d2) {
            this.d1 = d1;
            this.d2 = d2;
            return this;
        }

        @Override
        public Clause getConflict() {
            Clause c = short_expl_3.get();
            c._s(1, d1);
            c._s(2, d2);
            return c;
        }

        @Override
        public String toString() {
            return "lits: 0 ∨ " + d1 + " ∨ " + d2;
        }
    }


}
