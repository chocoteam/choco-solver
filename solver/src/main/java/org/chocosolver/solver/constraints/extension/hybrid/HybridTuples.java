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

import org.chocosolver.solver.exception.SolverException;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.solver.constraints.extension.hybrid.HReExpression.Op.*;

/**
 * A class to define hybrid tuples, that is, tuples containing expressions of the following forms:
 * <ul>
 *     <li>any()</li>
 *     <li>eq(2),</li>
 *     <li>gt(col(0).add(1))</li>
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
    protected final List<HReExpression[]> htuples;
    /**
     * For sanity check only
     */
    private int arity;

    /**
     * Create an empty structure that stores hybrid tuples
     */
    public HybridTuples() {
        this.htuples = new ArrayList<>();
    }

    /**
     * Add a hybrid tuple to this storage.
     * <br/>
     * A hybrid tuple is an expression on column/variable that makes possible to define
     * basic yet expressive expression like {@code gt(col(0).add(1))}.
     * @param tuple the hybrid tuple as a set of expressions
     * @throws SolverException if the tuple does not the match the arity of previously declared ones.
     */
    public void add(HReExpression... tuple) {
        if (htuples.size() == 0) {
            arity = tuple.length;
        } else if (arity != tuple.length) {
            throw new SolverException("The given tuple does not match the arity: " + arity);
        }
        this.htuples.add(tuple);
    }

    /**
     * Add many hybrid tuples at once
     *
     * @param tuples hybrid tuples
     * @throws SolverException if one tuple does not the match the arity of previously declared ones.
     */
    public void add(HReExpression[]... tuples) {
        for (HReExpression[] tuple : tuples) {
            add(tuple);
        }
    }

    /**
     * @return the current tuples as an array of expressions.
     */
    public HReExpression[][] toArray() {
        return htuples.toArray(new HReExpression[0][0]);
    }

    //////////////////////// DSL ////////////////////////

    /**
     * @return an expression that indicates that no restriction exists on this column/variable
     */
    public static HReExpression any() {
        return new HReExpression.SimpleHReExpression(STAR, 0);
    }

    /**
     * @param idx index of a column/variable position. The first column/variable has the index 0.
     * @return refer to a specific (different) column/variable
     * @implNote As is, this expression is equivalent to <code>eq(col(idx))</code>
     */
    public static HReExpression.ColHReExpression col(int idx) {
        return new HReExpression.ColHReExpression(COL, idx);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is equal to the value <code>val</code>
     */
    public static HReExpression eq(int val) {
        return new HReExpression.SimpleHReExpression(EQ, val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is different from
     * the value <code>val</code>
     */
    public static HReExpression ne(int val) {
        return new HReExpression.SimpleHReExpression(NQ, val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is greater or
     * equal to the value <code>val</code>
     */
    public static HReExpression ge(int val) {
        return new HReExpression.SimpleHReExpression(GE, val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is strictly greater than
     * the value <code>val</code>
     */
    public static HReExpression gt(int val) {
        return new HReExpression.SimpleHReExpression(GE, val + 1);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is less than or
     * equal to the value <code>val</code>
     */
    public static HReExpression le(int val) {
        return new HReExpression.SimpleHReExpression(LE, val);
    }

    /**
     * @param val an integer
     * @return an expression that ensures that the column/variable is strictly less than
     * the value <code>val</code>
     */
    public static HReExpression lt(int val) {
        return new HReExpression.SimpleHReExpression(LE, val - 1);
    }

    /**
     * @param exp a hybrid arithmetical expression
     * @return an expression that ensures that the column/variable is equal to <code>exp</code>
     */
    public static HReExpression eq(HArExpression exp) {
        return new HReExpression.ComplexHReExpression(EQ, exp);
    }

    /**
     * @param exp a hybrid arithmetical expression
     * @return an expression that ensures that the column/variable is different from <code>exp</code>
     */
    public static HReExpression ne(HArExpression exp) {
        return new HReExpression.ComplexHReExpression(NQ, exp);
    }

    /**
     * @param exp a hybrid arithmetical expression
     * @return an expression that ensures that the column/variable is greater than
     * or equal to <code>exp</code>
     */
    public static HReExpression ge(HArExpression exp) {
        return new HReExpression.ComplexHReExpression(GE, exp);
    }

    /**
     * @param exp a hybrid arithmetical expression
     * @return an expression that ensures that the column/variable is less than
     * or equal to <code>exp</code>
     */
    public static HReExpression le(HArExpression exp) {
        return new HReExpression.ComplexHReExpression(LE, exp);
    }


}
