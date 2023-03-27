/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.hybrid;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to define hybrid tuples, that is, tuples containing expressions of the following forms:
 * <ul>
 *     <li>any()</li>
 *     <li>eq(2),</li>
 *     <li>gt(col(0), 1))</li>
 *     <li>...</li>
 * </ul>
 * <br/>
 * This kind of tuples makes possible to expression extension constraint in a very compact way.
 * Instead of only listing all possible combinations (or forbidden ones), one can also
 * define relationships between a variables and a value or relationships between a variable and other variables.
 * <br/>
 * For instance, declaring that 3 variables must be equal can be defined as:
 * <pre>
 * {@code
 *
 * HybridTuples tuples = new HybridTuples();
 * tuples.add(any(), col(0), col(0));
 * model.table(new IntVar[]{x, y, z}, tuples).post();
 * }
 * </pre>
 *
 * @author Charles Prud'homme
 * @since 13/02/2023
 */
public class HybridTuples {

    /**
     * List of hybrid tuples declared
     */
    protected final List<ISupportable[]> hybridTuples;
    /**
     * For sanity check only
     */
    private int arity;

    /**
     * Create an empty structure that stores hybrid tuples
     */
    public HybridTuples() {
        this.hybridTuples = new ArrayList<>();
    }

    /**
     * Add a hybrid tuple to this storage.
     * <br/>
     * A hybrid tuple is an expression on column/variable that makes possible to define
     * basic yet expressive expression like {@code gt(col(0).add(1))}.
     *
     * @param tuple the hybrid tuple as a set of expressions
     * @throws SolverException if the tuple does not the match the arity of previously declared ones.
     */
    public void add(ISupportable... tuple) {
        if (hybridTuples.size() == 0) {
            arity = tuple.length;
        } else if (arity != tuple.length) {
            throw new SolverException("The given tuple does not match the arity: " + arity);
        }
        this.hybridTuples.add(convert(tuple));
    }

    private ISupportable[] convert(ISupportable[] tuple) {
        ISupportable[] t = tuple.clone();
        for (int i = 0; i < tuple.length; i++) {
            if (tuple[i] instanceof UnCol) {
                UnCol col = (UnCol) tuple[i];
                ISupportable h;
                switch (col.op) {
                    case EQ:
                        h = new NaEqXYC(i, col.anInt, col.inc);
                        break;
                    case NQ:
                        h = new NaNqXYC(i, col.anInt, col.inc);
                        break;
                    case LE:
                        h = new NaLqXYC(i, col.anInt, col.inc);
                        break;
                    case GE:
                        h = new NaGqXYC(i, col.anInt, col.inc);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                t[i] = h;
                t[col.anInt] = Many.merge(t[col.anInt], h);
            }// else do nothing
        }
        return t;
    }

    /**
     * Add many hybrid tuples at once
     *
     * @param tuples hybrid tuples
     * @throws SolverException if one tuple does not the match the arity of previously declared ones.
     */
    public void add(ISupportable[]... tuples) {
        for (ISupportable[] tuple : tuples) {
            add(tuple);
        }
    }

    /**
     * @return the current tuples as an array of expressions.
     */
    public ISupportable[][] toArray() {
        return hybridTuples.toArray(new ISupportable[0][0]);
    }

    /**
     * @return the arity of the tuples, that is the number of variables it requires
     */
    public int arity() {
        return hybridTuples.get(0).length;
    }

    /**
     * @return the number of tuples declared in this collection
     */
    public int nbTuples() {
        return hybridTuples.size();
    }

    //////////////////////// DSL ////////////////////////

    /**
     * @return an expression that indicates that no restriction exists on this column/variable
     */
    public static ISupportable any() {
        return new UnAny();
    }

    /**
     * @param idx index of a column/variable position. The first column/variable has the index 0.
     * @return refer to a specific (different) column/variable
     * @implNote As is, this expression is equivalent to <code>eq(col(idx))</code>
     */
    public static UnCol col(int idx) {
        return new UnCol(idx, 0, Operator.EQ);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is equal to the value <code>val</code>
     */
    public static ISupportable eq(int val) {
        return new UnEqXC(val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is different from
     * the value <code>val</code>
     */
    public static ISupportable ne(int val) {
        return new UnNqXC(val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is greater or
     * equal to the value <code>val</code>
     */
    public static ISupportable ge(int val) {
        return new UnGqXC(val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is strictly greater than
     * the value <code>val</code>
     */
    public static ISupportable gt(int val) {
        return new UnGqXC(val + 1);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is less than or
     * equal to the value <code>val</code>
     */
    public static ISupportable le(int val) {
        return new UnLqXC(val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is strictly less than
     * the value <code>val</code>
     */
    public static ISupportable lt(int val) {
        return new UnLqXC(val - 1);
    }

    /**
     * @param vals set of values
     * @return an expression that ensures that the column/variable intersects the set <code>vals</code>
     */
    public static ISupportable in(int... vals) {
        IntIterableRangeSet set = new IntIterableRangeSet(vals);
        return new UnXInS(set);
    }

    /**
     * @param vals set of values
     * @return an expression that ensures that the column/variable does not intersect the set <code>vals</code>
     */
    public static ISupportable nin(int... vals) {
        IntIterableRangeSet set = new IntIterableRangeSet(vals);
        set.flip();
        return new UnXInS(set);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @param inc value to be added or subtracted to col
     * @return an expression that ensures that the column/variable is equal to <code>exp + inc</code>
     */
    public static ISupportable eq(UnCol col, int inc) {
        UnCol copy = col.copy();
        copy.op(Operator.EQ);
        copy.add(inc);
        return copy;
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is not equal to <code>exp</code>
     */
    public static ISupportable ne(UnCol col) {
        return ne(col, 0);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @param inc value to be added or subtracted to col
     * @return an expression that ensures that the column/variable is not equal to <code>exp + inc</code>
     */
    public static ISupportable ne(UnCol col, int inc) {
        UnCol copy = col.copy();
        copy.op(Operator.NQ);
        copy.add(inc);
        return copy;
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is greater than
     * or equal to <code>col</code>
     */
    public static ISupportable ge(UnCol col) {
        return ge(col, 0);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is greater than
     * or equal to <code>col</code>
     */
    public static ISupportable ge(UnCol col, int inc) {
        UnCol copy = col.copy();
        copy.op(Operator.GE);
        copy.add(inc);
        return copy;
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is less than
     * or equal to <code>exp</code>
     */
    public static ISupportable le(UnCol col) {
        return le(col, 0);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is less than
     * or equal to <code>exp</code>
     */
    public static ISupportable le(UnCol col, int inc) {
        UnCol copy = col.copy();
        copy.op(Operator.LE);
        copy.add(inc);
        return copy;
    }

    /**
     * Expressions to be used in {@link HybridTuples}.
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
        boolean satisfiable(PropHybridTable.StrHVar[] vs, int i);

        /**
         * For a given STR variable, at position <code>i</code> in <code>vs</code>,
         * this method gets all values supported by this expression.
         *
         * @param vs STR variables, declared on the table constraint
         * @param i  index of the variable in {@code vs}
         */
        void support(PropHybridTable.StrHVar[] vs, int i);


    }

    /**
     * For unary expressions, that is, expressions that involves only one variable.
     */
    static abstract class Unary implements ISupportable {
        /**
         * An int value, to be used as index, increment, ...
         */
        final int anInt;

        Unary(int anInt) {
            this.anInt = anInt;
        }

        @Override
        public final boolean satisfiable(PropHybridTable.StrHVar[] vs, int i) {
            return satisfiable(vs[i]);
        }

        @Override
        public final void support(PropHybridTable.StrHVar[] vs, int i) {
            support(vs[i]);
        }

        /**
         * @param v a STR variable
         * @return <i>true</i> if this expression can be satisfied
         * for the current domain of the variable,
         * that is at least one value is consistent with the expression.
         * Return <code>false</code> otherwise.
         */
        public abstract boolean satisfiable(PropHybridTable.StrHVar v);

        /**
         * For a given STR variable <i>v</i>,
         * this method gets all values supported by this expression.
         *
         * @param v a STR variable
         */
        public abstract void support(PropHybridTable.StrHVar v);
    }

    /**
     * The Kleene star ('*') expression: all values are supported.
     */
    static class UnAny extends Unary {

        UnAny() {
            super(0);
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar v) {
            return true;
        }

        @Override
        public void support(PropHybridTable.StrHVar v) {
            v.supportAll();
        }
    }

    /**
     * Expression to refer to another column of the tuple.
     * Such an expression only exists to ease declaration of tuples.
     * It is not intended to be used  to filter
     */
    public static class UnCol extends Unary {

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
        public boolean satisfiable(PropHybridTable.StrHVar v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void support(PropHybridTable.StrHVar v) {
            throw new UnsupportedOperationException();
        }

        protected UnCol copy() {
            return new UnCol(this.anInt, this.inc, this.op);
        }
    }

    /**
     * 'X = c' expression
     */
    static class UnEqXC extends Unary {

        UnEqXC(int cste) {
            super(cste);
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar v) {
            return v.var.contains(anInt);
        }

        @Override
        public void support(PropHybridTable.StrHVar v) {
            v.support(anInt);
        }
    }

    /**
     * 'X != c' expression
     */
    static class UnNqXC extends Unary {

        UnNqXC(int cste) {
            super(cste);
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar v) {
            return !v.var.isInstantiatedTo(anInt);
        }

        @Override
        public void support(PropHybridTable.StrHVar v) {
            int ub = v.var.getUB();
            for (int val = v.var.getLB(); val <= ub; val = v.var.nextValue(val)) {
                if (val != anInt) {
                    v.support(val);
                }
            }
        }
    }

    /**
     * 'X <= c' expression
     */
    static class UnLqXC extends Unary {

        UnLqXC(int cste) {
            super(cste);
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar v) {
            return v.var.getLB() <= anInt;
        }

        @Override
        public void support(PropHybridTable.StrHVar v) {
            int ub = v.var.previousValue(anInt + 1);
            for (int val = v.var.getLB(); val <= ub; val = v.var.nextValue(val)) {
                v.support(val);
            }
        }
    }

    /**
     * 'X >= c' expression
     */
    static class UnGqXC extends Unary {

        UnGqXC(int cste) {
            super(cste);
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar v) {
            return v.var.getUB() >= anInt;
        }

        @Override
        public void support(PropHybridTable.StrHVar v) {
            int ub = v.var.getUB();
            for (int val = v.var.nextValue(anInt - 1); val <= ub; val = v.var.nextValue(val)) {
                v.support(val);
            }
        }
    }

    /**
     * 'X in S' expression
     */
    static class UnXInS extends Unary {

        IntIterableRangeSet set;

        UnXInS(IntIterableRangeSet set) {
            super(0);
            this.set = set;
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar v) {
            boolean intersect = false;
            if (v.var.getDomainSize() < set.size()) {
                int ub = v.var.getUB();
                for (int i = v.var.getLB(); i <= ub && !intersect; i = v.var.nextValue(i)) {
                    intersect = set.contains(i);
                }
            } else {
                int max = set.max();
                for (int i = set.min(); i <= max && !intersect; i = set.nextValue(i)) {
                    intersect = v.var.contains(i);
                }
            }
            return intersect;
        }

        @Override
        public void support(PropHybridTable.StrHVar v) {
            int ub = v.var.getUB();
            for (int i = v.var.getLB(); i <= ub; i = v.var.nextValue(i)) {
                if (set.contains(i)) {
                    v.support(i);
                }
            }
        }
    }

    /**
     * For n-ary expression, that is, expressions involving two or more variables
     */
    static abstract class Nary implements ISupportable {
        final int cste;

        Nary(int cste) {
            this.cste = cste;
        }

    }

    /**
     * 'X = Y + c' expression
     */
    static class NaEqXYC extends Nary {

        int[] is;

        /**
         * Create an expression like: col(i0) = col(i1) + a
         *
         * @param i0 index of the first variable, wrt the table
         * @param i1 index of the second variable, wrt the table
         * @param a  a constant
         */
        NaEqXYC(int i0, int i1, int a) {
            super(a);
            this.is = new int[]{i0, i1};
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar[] vs, int idx) {
            int j = is[1], c = cste;
            if (idx == is[1]) {
                j = is[0];
                c = -cste;
            }
            int sm = idx, la = j;
            // look for the smallest domain to iterate on
            if (vs[idx].var.getDomainSize() > vs[j].var.getDomainSize()) {
                sm = j;
                la = idx;
                c *= -1;
            }
            // then look for the first supported value
            for (int l = vs[sm].var.getLB(); l <= vs[sm].var.getUB(); l = vs[sm].var.nextValue(l)) {
                if (vs[la].var.contains(l + c)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void support(PropHybridTable.StrHVar[] vs, int idx) {
            int j = is[1], c = cste;
            if (idx == is[1]) {
                j = is[0];
                c = -cste;
            }
            int sm = idx, la = j, de = 0;
            // look for the smallest domain to iterate on
            if (vs[idx].var.getDomainSize() > vs[j].var.getDomainSize()) {
                sm = j;
                la = idx;
                c *= -1;
                de = c;
            }
            // then look for the first supported value
            for (int l = vs[sm].var.getLB(); l <= vs[sm].var.getUB(); l = vs[sm].var.nextValue(l)) {
                if (vs[la].var.contains(l + c)) {
                    vs[idx].support(l + de);
                }
            }
        }
    }

    /**
     * 'X != Y + c' expression
     */
    static class NaNqXYC extends Nary {

        int[] is;

        /**
         * Create an expression like: col(i0) != col(i1) + a
         *
         * @param i0 index of the first variable, wrt the table
         * @param i1 index of the second variable, wrt the table
         * @param a  a constant
         */
        NaNqXYC(int i0, int i1, int a) {
            super(a);
            this.is = new int[]{i0, i1};
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar[] vs, int idx) {
            int j = is[1], c = cste;
            if (idx == is[1]) {
                j = is[0];
                c = -cste;
            }
            if (vs[j].var.isInstantiated()) {
                return !vs[idx].var.isInstantiatedTo(vs[j].var.getValue() + c);
            }
            return true;
        }

        @Override
        public void support(PropHybridTable.StrHVar[] vs, int idx) {
            int j = is[1], c = cste;
            if (idx == is[1]) {
                j = is[0];
                c = -cste;
            }
            if (vs[j].var.isInstantiated()) {
                int vj = vs[j].var.getValue() + c;
                int ub = vs[idx].var.getUB();
                for (int val = vs[idx].var.getLB(); val <= ub; val = vs[idx].var.nextValue(val)) {
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
    static class NaGqXYC extends Nary {

        int[] is;

        /**
         * Create an expression like: col(i0) >= col(i1) + a
         *
         * @param i0 index of the first variable, wrt the table
         * @param i1 index of the second variable, wrt the table
         * @param a  a constant
         */
        NaGqXYC(int i0, int i1, int a) {
            super(a);
            this.is = new int[]{i0, i1};
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar[] vs, int idx) {
            int j;
            if (idx == is[0]) {
                j = is[1];
                return vs[idx].var.getUB() >= vs[j].var.getLB() + cste;
            } else {
                j = is[0];
                return vs[j].var.getUB() >= vs[idx].var.getLB() + cste;
            }

        }

        @Override
        public void support(PropHybridTable.StrHVar[] vs, int idx) {
            int j;
            if (idx == is[0]) {
                j = is[1];
                int lb = vs[j].var.getLB() + cste;
                int ub = vs[idx].var.getUB();
                for (int a = vs[idx].var.nextValue(lb - 1); a <= ub; a = vs[idx].var.nextValue(a)) {
                    vs[idx].support(a);
                }
            } else {
                j = is[0];
                int ub = Math.min(vs[idx].var.getUB(), vs[j].var.getUB() - cste);
                for (int a = vs[idx].var.getLB(); a <= ub; a = vs[idx].var.nextValue(a)) {
                    vs[idx].support(a);
                }
            }
        }
    }

    /**
     * 'X <= Y + c' expression
     */
    static class NaLqXYC extends Nary {

        int[] is;

        /**
         * Create an expression like: col(i0) <= col(i1) + a
         *
         * @param i0 index of the first variable, wrt the table
         * @param i1 index of the second variable, wrt the table
         * @param a  a constant
         */
        NaLqXYC(int i0, int i1, int a) {
            super(a);
            this.is = new int[]{i0, i1};
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar[] vs, int idx) {
            int j;
            if (idx == is[0]) {
                j = is[1];
                return vs[idx].var.getLB() <= vs[j].var.getUB() + cste;
            } else {
                j = is[0];
                return vs[j].var.getLB() <= vs[idx].var.getUB() + cste;
            }

        }

        @Override
        public void support(PropHybridTable.StrHVar[] vs, int idx) {
            int j;
            if (idx == is[0]) {
                j = is[1];
                int ub = vs[j].var.getUB() + cste;
                for (int a = vs[idx].var.getLB(); a <= ub; a = vs[idx].var.nextValue(a)) {
                    vs[idx].support(a);
                }
            } else {
                j = is[0];
                int lb = Math.max(vs[idx].var.getLB(), vs[j].var.getLB() - cste);
                int ub = vs[idx].var.getUB();
                for (int a = vs[idx].var.nextValue(lb - 1); a <= ub; a = vs[idx].var.nextValue(a)) {
                    vs[idx].support(a);
                }
            }
        }
    }

    /**
     * Conjunction of {@link ISupportable}s
     */
    static class Many implements ISupportable {

        final List<ISupportable> exps;

        Many() {
            this.exps = new ArrayList<>();
        }

        public void push(ISupportable e) {
            this.exps.add(e);
        }

        @Override
        public boolean satisfiable(PropHybridTable.StrHVar[] vs, int i) {
            boolean isSat = false;
            for (int j = 0; j < exps.size() && !isSat; j++) {
                isSat = exps.get(j).satisfiable(vs, i);
            }
            return isSat;
        }

        @Override
        public void support(PropHybridTable.StrHVar[] vs, int i) {
            for (int j = 0; j < exps.size() && vs[i].cnt > 0; j++) {
                exps.get(j).support(vs, i);
            }
        }

        /**
         * @param h1 an expression
         * @param h2 an expression
         * @return a merge expression
         * @implSpec if hi is {@link #any()}, then ignore it, the other relation is stronger in terms of filtering.
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
