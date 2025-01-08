/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.hybrid;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Expressions to be used in {@link HybridTuples}.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/04/2023
 */
public interface ISupportable {
    /**
     * @param vs STR variables, declared on the table constraint
     * @param i  index of the variable in {@code vs}
     * @return <i>true</i> if this expression can be satisfied
     * for the current domain of the variable at position <code>i</code>,
     * that is at least one value is consistent with the expression.
     * Return <code>false</code> otherwise.
     */
    boolean satisfiable(ASupport[] vs, int i);

    /**
     * For a given STR variable, at position <code>i</code> in <code>vs</code>,
     * this method gets all values supported by this expression.
     *
     * @param vs STR variables, declared on the table constraint
     * @param i  index of the variable in {@code vs}
     */
    void support(ASupport[] vs, int i);

    /**
     * For unary expressions, that is, expressions that involves only one variable.
     */
    abstract class Unary implements ISupportable {
        /**
         * An int value, to be used as index, increment, ...
         */
        final int anInt;

        Unary(int anInt) {
            this.anInt = anInt;
        }

        @Override
        public final boolean satisfiable(ASupport[] vs, int i) {
            return satisfiable(vs[i]);
        }

        @Override
        public final void support(ASupport[] vs, int i) {
            support(vs[i]);
        }

        /**
         * @param v a STR variable
         * @return <i>true</i> if this expression can be satisfied
         * for the current domain of the variable,
         * that is at least one value is consistent with the expression.
         * Return <code>false</code> otherwise.
         */
        public abstract boolean satisfiable(ASupport v);

        /**
         * For a given STR variable <i>v</i>,
         * this method gets all values supported by this expression.
         *
         * @param v a STR variable
         */
        public abstract void support(ASupport v);
    }

    /**
     * The Kleene star ('*') expression: all values are supported.
     */
    class UnAny extends Unary {

        UnAny() {
            super(0);
        }

        @Override
        public boolean satisfiable(ASupport v) {
            return true;
        }

        @Override
        public void support(ASupport v) {
            v.supportAll();
        }
    }

    /**
     * Expression to refer to another column of the tuple.
     * Such an expression only exists to ease declaration of tuples.
     * It is not intended to be used  to filter
     */
    class UnCol extends Unary {

        /**
         * Optional value to add or remove from 'col(idx)'
         */
        int inc;
        /**
         * Operator, for internal purpose only
         */
        Operator op;

        UnCol(int idx, int inc, Operator operator) {
            super(idx);
            this.inc = inc;
            this.op = operator;
        }

        void op(Operator op) {
            this.op = op;
        }

        void add(int delta) {
            this.inc += delta;
        }

        @Override
        public boolean satisfiable(ASupport v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void support(ASupport v) {
            throw new UnsupportedOperationException();
        }

        protected UnCol copy() {
            return new UnCol(this.anInt, this.inc, this.op);
        }
    }

    /**
     * 'X = c' expression
     */
    class UnEqXC extends Unary {

        UnEqXC(int cste) {
            super(cste);
        }

        @Override
        public boolean satisfiable(ASupport v) {
            return v.getVar().contains(anInt);
        }

        @Override
        public void support(ASupport v) {
            v.support(anInt);
        }
    }

    /**
     * 'X != c' expression
     */
    class UnNqXC extends Unary {

        UnNqXC(int cste) {
            super(cste);
        }

        @Override
        public boolean satisfiable(ASupport v) {
            return !v.getVar().isInstantiatedTo(anInt);
        }

        @Override
        public void support(ASupport v) {
            int ub = v.getVar().getUB();
            for (int val = v.getVar().getLB(); val <= ub; val = v.getVar().nextValue(val)) {
                if (val != anInt) {
                    v.support(val);
                }
            }
        }
    }

    /**
     * 'X <= c' expression
     */
    class UnLqXC extends Unary {

        UnLqXC(int cste) {
            super(cste);
        }

        @Override
        public boolean satisfiable(ASupport v) {
            return v.getVar().getLB() <= anInt;
        }

        @Override
        public void support(ASupport v) {
            int ub = v.getVar().previousValue(anInt + 1);
            for (int val = v.getVar().getLB(); val <= ub; val = v.getVar().nextValue(val)) {
                v.support(val);
            }
        }
    }

    /**
     * 'X >= c' expression
     */
    class UnGqXC extends Unary {

        UnGqXC(int cste) {
            super(cste);
        }

        @Override
        public boolean satisfiable(ASupport v) {
            return v.getVar().getUB() >= anInt;
        }

        @Override
        public void support(ASupport v) {
            int ub = v.getVar().getUB();
            for (int val = v.getVar().nextValue(anInt - 1); val <= ub; val = v.getVar().nextValue(val)) {
                v.support(val);
            }
        }
    }

    /**
     * 'X in S' expression
     */
    class UnXInS extends Unary {

        IntIterableRangeSet set;

        UnXInS(IntIterableRangeSet set) {
            super(0);
            this.set = set;
        }

        @Override
        public boolean satisfiable(ASupport v) {
            boolean intersect = false;
            if (v.getVar().getDomainSize() < set.size()) {
                int ub = v.getVar().getUB();
                for (int i = v.getVar().getLB(); i <= ub && !intersect; i = v.getVar().nextValue(i)) {
                    intersect = set.contains(i);
                }
            } else {
                int max = set.max();
                for (int i = set.min(); i <= max && !intersect; i = set.nextValue(i)) {
                    intersect = v.getVar().contains(i);
                }
            }
            return intersect;
        }

        @Override
        public void support(ASupport v) {
            int ub = v.getVar().getUB();
            for (int i = v.getVar().getLB(); i <= ub; i = v.getVar().nextValue(i)) {
                if (set.contains(i)) {
                    v.support(i);
                }
            }
        }
    }

    /**
     * For n-ary expression, that is, expressions involving two or more variables
     */
    abstract class Nary implements ISupportable {
        final int cste;
        final int[] is;

        Nary(int cste, int[] indices) {
            this.cste = cste;
            this.is = indices;
        }

    }

    /**
     * 'X = Y + c' expression
     */
    class NaEqXYC extends Nary {

        /**
         * Create an expression like: col(i0) = col(i1) + a
         *
         * @param i0 index of the first variable, wrt the table
         * @param i1 index of the second variable, wrt the table
         * @param a  a constant
         */
        NaEqXYC(int i0, int i1, int a) {
            super(a, new int[]{i0, i1});
        }

        @Override
        public boolean satisfiable(ASupport[] vs, int idx) {
            int j = is[1], c = cste;
            if (idx == is[1]) {
                j = is[0];
                c = -cste;
            }
            int sm = idx, la = j;
            // look for the smallest domain to iterate on
            if (vs[idx].getVar().getDomainSize() > vs[j].getVar().getDomainSize()) {
                sm = j;
                la = idx;
                c *= -1;
            }
            // then look for the first supported value
            for (int l = vs[sm].getVar().getLB(); l <= vs[sm].getVar().getUB(); l = vs[sm].getVar().nextValue(l)) {
                if (vs[la].getVar().contains(l + c)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void support(ASupport[] vs, int idx) {
            int j = is[1], c = cste;
            if (idx == is[1]) {
                j = is[0];
                c = -cste;
            }
            int sm = idx, la = j, de = 0;
            // look for the smallest domain to iterate on
            if (vs[idx].getVar().getDomainSize() > vs[j].getVar().getDomainSize()) {
                sm = j;
                la = idx;
                c *= -1;
                de = c;
            }
            // then look for the first supported value
            for (int l = vs[sm].getVar().getLB(); l <= vs[sm].getVar().getUB(); l = vs[sm].getVar().nextValue(l)) {
                if (vs[la].getVar().contains(l + c)) {
                    vs[idx].support(l + de);
                }
            }
        }
    }

    /**
     * 'X != Y + c' expression
     */
    class NaNqXYC extends Nary {

        /**
         * Create an expression like: col(i0) != col(i1) + a
         *
         * @param i0 index of the first variable, wrt the table
         * @param i1 index of the second variable, wrt the table
         * @param a  a constant
         */
        NaNqXYC(int i0, int i1, int a) {
            super(a, new int[]{i0, i1});
        }

        @Override
        public boolean satisfiable(ASupport[] vs, int idx) {
            int j = is[1], c = cste;
            if (idx == is[1]) {
                j = is[0];
                c = -cste;
            }
            if (vs[j].getVar().isInstantiated()) {
                return !vs[idx].getVar().isInstantiatedTo(vs[j].getVar().getValue() + c);
            }
            return true;
        }

        @Override
        public void support(ASupport[] vs, int idx) {
            int j = is[1], c = cste;
            if (idx == is[1]) {
                j = is[0];
                c = -cste;
            }
            if (vs[j].getVar().isInstantiated()) {
                int vj = vs[j].getVar().getValue() + c;
                int ub = vs[idx].getVar().getUB();
                for (int val = vs[idx].getVar().getLB(); val <= ub; val = vs[idx].getVar().nextValue(val)) {
                    if (val != vj) {
                        vs[idx].support(val);
                    }
                }
            } else {
                vs[idx].supportAll();
            }
        }
    }

    /**
     * 'X >= Y + c' expression
     */
    class NaGqXYC extends Nary {

        /**
         * Create an expression like: col(i0) >= col(i1) + a
         *
         * @param i0 index of the first variable, wrt the table
         * @param i1 index of the second variable, wrt the table
         * @param a  a constant
         */
        NaGqXYC(int i0, int i1, int a) {
            super(a, new int[]{i0, i1});
        }

        @Override
        public boolean satisfiable(ASupport[] vs, int idx) {
            int j;
            if (idx == is[0]) {
                j = is[1];
                return vs[idx].getVar().getUB() >= vs[j].getVar().getLB() + cste;
            } else {
                j = is[0];
                return vs[j].getVar().getUB() >= vs[idx].getVar().getLB() + cste;
            }

        }

        @Override
        public void support(ASupport[] vs, int idx) {
            int j;
            if (idx == is[0]) {
                j = is[1];
                int lb = vs[j].getVar().getLB() + cste;
                int ub = vs[idx].getVar().getUB();
                for (int a = vs[idx].getVar().nextValue(lb - 1); a <= ub; a = vs[idx].getVar().nextValue(a)) {
                    vs[idx].support(a);
                }
            } else {
                j = is[0];
                int ub = Math.min(vs[idx].getVar().getUB(), vs[j].getVar().getUB() - cste);
                for (int a = vs[idx].getVar().getLB(); a <= ub; a = vs[idx].getVar().nextValue(a)) {
                    vs[idx].support(a);
                }
            }
        }
    }

    /**
     * 'X <= Y + c' expression
     */
    class NaLqXYC extends Nary {
        /**
         * Create an expression like: col(i0) <= col(i1) + a
         *
         * @param i0 index of the first variable, wrt the table
         * @param i1 index of the second variable, wrt the table
         * @param a  a constant
         */
        NaLqXYC(int i0, int i1, int a) {
            super(a, new int[]{i0, i1});
        }

        @Override
        public boolean satisfiable(ASupport[] vs, int idx) {
            int j;
            if (idx == is[0]) {
                j = is[1];
                return vs[idx].getVar().getLB() <= vs[j].getVar().getUB() + cste;
            } else {
                j = is[0];
                return vs[j].getVar().getLB() <= vs[idx].getVar().getUB() + cste;
            }

        }

        @Override
        public void support(ASupport[] vs, int idx) {
            int j;
            if (idx == is[0]) {
                j = is[1];
                int ub = vs[j].getVar().getUB() + cste;
                for (int a = vs[idx].getVar().getLB(); a <= ub; a = vs[idx].getVar().nextValue(a)) {
                    vs[idx].support(a);
                }
            } else {
                j = is[0];
                int lb = Math.max(vs[idx].getVar().getLB(), vs[j].getVar().getLB() - cste);
                int ub = vs[idx].getVar().getUB();
                for (int a = vs[idx].getVar().nextValue(lb - 1); a <= ub; a = vs[idx].getVar().nextValue(a)) {
                    vs[idx].support(a);
                }
            }
        }
    }

    /**
     * Conjunction of {@link ISupportable}s
     */
    class Many implements ISupportable {

        final List<ISupportable> exps;
        ASupport.AndSupport andSup = new ASupport.AndSupport();

        ASupport[] copy = null;

        Many() {
            this.exps = new ArrayList<>();
        }

        public void push(ISupportable e) {
            this.exps.add(e);
        }

        @Override
        public boolean satisfiable(ASupport[] vs, int i) {
            if (copy == null) {
                copy = new ASupport[vs.length];
                System.arraycopy(vs, 0, copy, 0, vs.length);
            }
            andSup.setVar(vs[i].getVar());
            ASupport old = vs[i];
            copy[i] = andSup;
            for (int j = 0; j < exps.size(); j++) {
                exps.get(j).support(copy, i);
                andSup.filter();
            }
            copy[i] = old;
            return andSup.and.cardinality() > 0;
        }

        @Override
        public void support(ASupport[] vs, int i) {
            andSup.transferTo(vs[i]);
        }

        /**
         * @param h1 an expression
         * @param h2 an expression
         * @return a merge expression
         * @implSpec if hi is {@link HybridTuples#any()}, then ignore it, the other relation is stronger in terms of filtering.
         */
        public static Many merge(ISupportable h1, ISupportable h2) {
            Many m;
            if (h1 instanceof Many) {
                m = (Many) h1;
                if (h2 instanceof Many) {
                    Many m2 = (Many) h2;
                    for (int i = 0; i < m2.exps.size(); i++) {
                        m.push(m2.exps.get(i));
                    }
                } else if (!(h2 instanceof UnAny)) {
                    m.push(h2);
                }
            } else if (h2 instanceof Many) {
                m = (Many) h2;
                if (!(h1 instanceof UnAny)) {
                    m.push(h2);
                }
            } else {
                m = new Many();
                if (!(h1 instanceof UnAny)) {
                    m.push(h1);
                }
                if (!(h2 instanceof UnAny)) {
                    m.push(h2);
                }
            }
            return m;
        }
    }
}
