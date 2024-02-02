/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
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
import java.util.Arrays;
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
        //noinspection unchecked
        List<ISupportable>[] t = new ArrayList[tuple.length];
        for (int i = 0; i < tuple.length; i++) {
            t[i] = new ArrayList<>();
        }
        for (int i = 0; i < tuple.length; i++) {
            t[i].add(tuple[i]);
            if (tuple[i] instanceof ISupportable.UnCol) {
                ISupportable.UnCol col = (ISupportable.UnCol) tuple[i];
                ISupportable h;
                switch (col.op) {
                    case EQ:
                        h = new ISupportable.NaEqXYC(i, col.anInt, col.inc);
                        break;
                    case NQ:
                        h = new ISupportable.NaNqXYC(i, col.anInt, col.inc);
                        break;
                    case LE:
                        h = new ISupportable.NaLqXYC(i, col.anInt, col.inc);
                        break;
                    case GE:
                        h = new ISupportable.NaGqXYC(i, col.anInt, col.inc);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
                t[i].remove(t[i].size() - 1);
                t[i].add(h);
                t[col.anInt].add(h);
            }// else do nothing
        }
        for (int i = 0; i < t.length; i++) {
            if (t[i].size() > 1) {
                ISupportable.Many m = ISupportable.Many.merge(t[i].get(0), t[i].get(1));
                for (int j = 2; j < t[i].size(); j++) {
                    m = ISupportable.Many.merge(m, t[i].get(j));
                }
                t[i].clear();
                t[i].add(m);
            }
        }
        return Arrays.stream(t).map(l -> l.get(0)).toArray(ISupportable[]::new);
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
        return new ISupportable.UnAny();
    }

    /**
     * @param idx index of a column/variable position. The first column/variable has the index 0.
     * @return refer to a specific (different) column/variable
     * @implNote As is, this expression is equivalent to <code>eq(col(idx))</code>
     */
    public static ISupportable.UnCol col(int idx) {
        return new ISupportable.UnCol(idx, 0, Operator.EQ);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is equal to the value <code>val</code>
     */
    public static ISupportable eq(int val) {
        return new ISupportable.UnEqXC(val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is different from
     * the value <code>val</code>
     */
    public static ISupportable ne(int val) {
        return new ISupportable.UnNqXC(val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is greater or
     * equal to the value <code>val</code>
     */
    public static ISupportable ge(int val) {
        return new ISupportable.UnGqXC(val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is strictly greater than
     * the value <code>val</code>
     */
    public static ISupportable gt(int val) {
        return new ISupportable.UnGqXC(val + 1);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is less than or
     * equal to the value <code>val</code>
     */
    public static ISupportable le(int val) {
        return new ISupportable.UnLqXC(val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is strictly less than
     * the value <code>val</code>
     */
    public static ISupportable lt(int val) {
        return new ISupportable.UnLqXC(val - 1);
    }

    /**
     * @param values set of values
     * @return an expression that ensures that the column/variable intersects the set <code>values</code>
     */
    public static ISupportable in(int... values) {
        IntIterableRangeSet set = new IntIterableRangeSet(values);
        return new ISupportable.UnXInS(set);
    }

    /**
     * @param values set of values
     * @return an expression that ensures that the column/variable does not intersect the set <code>values</code>
     */
    public static ISupportable nin(int... values) {
        IntIterableRangeSet set = new IntIterableRangeSet(values);
        set.flip();
        return new ISupportable.UnXInS(set);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @param inc value to be added or subtracted to col
     * @return an expression that ensures that the column/variable is equal to <code>col + inc</code>
     */
    public static ISupportable eq(ISupportable.UnCol col, int inc) {
        ISupportable.UnCol copy = col.copy();
        copy.op(Operator.EQ);
        copy.add(inc);
        return copy;
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is not equal to <code>col</code>
     */
    public static ISupportable ne(ISupportable.UnCol col) {
        return ne(col, 0);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @param inc value to be added or subtracted to col
     * @return an expression that ensures that the column/variable is not equal to <code>col + inc</code>
     */
    public static ISupportable ne(ISupportable.UnCol col, int inc) {
        ISupportable.UnCol copy = col.copy();
        copy.op(Operator.NQ);
        copy.add(inc);
        return copy;
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is greater than
     * or equal to <code>col + inc</code>
     */
    public static ISupportable ge(ISupportable.UnCol col) {
        return ge(col, 0);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is greater than
     * or equal to <code>col</code>
     */
    public static ISupportable ge(ISupportable.UnCol col, int inc) {
        ISupportable.UnCol copy = col.copy();
        copy.op(Operator.GE);
        copy.add(inc);
        return copy;
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is greater than <code>col</code>
     */
    public static ISupportable gt(ISupportable.UnCol col) {
        return ge(col, 1);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is greater than <code>col + inc</code>
     */
    public static ISupportable gt(ISupportable.UnCol col, int inc) {
        return ge(col, inc + 1);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is less than
     * or equal to <code>col</code>
     */
    public static ISupportable le(ISupportable.UnCol col) {
        return le(col, 0);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is less than
     * or equal to <code>col + inc</code>
     */
    public static ISupportable le(ISupportable.UnCol col, int inc) {
        ISupportable.UnCol copy = col.copy();
        copy.op(Operator.LE);
        copy.add(inc);
        return copy;
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is less than <code>col</code>
     */
    public static ISupportable lt(ISupportable.UnCol col) {
        return le(col, -1);
    }

    /**
     * @param col a reference to a column arithmetical expression
     * @return an expression that ensures that the column/variable is less than <code>col + inc</code>
     */
    public static ISupportable lt(ISupportable.UnCol col, int inc) {
        return le(col, inc - 1);
    }
}
