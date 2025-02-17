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

import org.chocosolver.solver.ICause;

/**
 * A class to explain a propagation.
 * A reason is always associated with one or more literals.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/09/2023
 */
public abstract class Reason implements ICause {
    /**
     * An undefined reason.
     * This reason is static and should not be modified.
     */
    static final Clause UNDEF = new Clause(new int[]{0});
    /**
     * A thread-local clause to explain a propagation with one literal.
     * This clause is static and can be reused in the same thread.
     */
    private final static ThreadLocal<Clause> short_expl_2 = ThreadLocal.withInitial(() -> new Clause(new int[]{0, 0}));
    /**
     * A thread-local clause to explain a propagation with two literals.
     * This clause is static and can be reused in the same thread.
     */
    private final static ThreadLocal<Clause> short_expl_3 = ThreadLocal.withInitial(() -> new Clause(new int[]{0, 0, 0}));
    /**
     * The type of reason among {0, 2, 3}.
     * 2: a reason with one literal (and the asserting literal)
     * 3: a reason with two literals (and the asserting literal)
     * 0: a reason with more than two literals (and the asserting literal)
     */
    final int type;

    /**
     * Create a reason with a type.
     * @param type the type of reason
     */
    Reason(int type) {
        this.type = type;
    }

    /**
     * Create an undefined reason.
     *
     * @return an undefined static reason
     * @implSpec In practice, this reason is static and thus should not be modified.
     */
    public static Reason undef() {
        return UNDEF;
    }

    /**
     * Create a reason from a clause
     *
     * @param cl a clause
     * @return a reason
     */
    public static Reason r(Clause cl) {
        return cl;
    }

    /**
     * Create a reason from a single literal
     *
     * @param p a literal
     * @return a reason
     */
    public static Reason r(int p) {
        return new Reason1(p);
    }

    /**
     * Create a reason from two literals
     *
     * @param p a literal
     * @param q a literal
     * @return a reason
     */
    public static Reason r(int p, int q) {
        return new Reason2(p, q);
    }

    /**
     * Create a reason from one or more literals.
     * <p>If more than 2 literals are given, the literal at index 0 should be left empty for the asserting literal.
     * </p>
     *
     * @param ps other literals
     * @return a reason
     * @implSpec if length of ps is strictly greater than 2,
     * then the literal at index 0 should be left empty for the asserting literal
     */
    public static Reason r(int... ps) {
        if (ps.length == 1) {
            return new Reason1(ps[0]);
        } else if (ps.length == 2) {
            return new Reason2(ps[0], ps[1]);
        } else if (ps.length > 2) {
            assert ps[0] == 0 : "The first literal should be left empty for the asserting literal";
            return new Clause(ps);
        } else {
            return Reason.UNDEF;
        }
    }

    /**
     * Gather a reason with a new literal.
     *
     * @param r a reason
     * @param p a literal
     * @return a new reason with p added to the literals of r
     */
    public static Reason gather(Reason r, int p) {
        switch (r.type) {
            case 0: {
                Clause cl = (Clause) r;
                int[] ps = new int[cl.size() + 1];
                for (int i = 0; i < cl.size(); i++) {
                    ps[i] = cl._g(i);
                }
                ps[cl.size()] = p;
                return Reason.r(ps);
            }
            case 2:
                return Reason.r(((Reason1) r).d1, p);
            case 3: {
                int[] ps = new int[4];
                ps[0] = 0;// leave space for the asserting literal
                ps[1] = ((Reason2) r).d1;
                ps[2] = ((Reason2) r).d2;
                ps[3] = p;
                return Reason.r(ps);
            }
            default:
                return Reason.r(p);
        }
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
        final int d1;

        private Reason1(int d1) {
            super(2);
            this.d1 = d1;
        }

        @Override
        public Clause getConflict() {
            short_expl_2.get()._s(1, d1);
            return short_expl_2.get();
        }

        @Override
        public String toString() {
            return "lit:" + d1;
        }
    }

    /**
     * A reason with two literals
     */
    final static class Reason2 extends Reason {
        final int d1;
        final int d2;

        private Reason2(int d1, int d2) {
            super(3);
            this.d1 = d1;
            this.d2 = d2;
        }

        @Override
        public Clause getConflict() {
            short_expl_3.get()._s(1, d1);
            short_expl_3.get()._s(2, d2);
            return short_expl_3.get();
        }

        @Override
        public String toString() {
            return "lits:" + d1 + " ∨ " + d2;
        }
    }

}
