/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.expression.discrete.relational.UnCReExpression;
import org.chocosolver.solver.variables.SetVar;

import java.util.Arrays;

/**
 * Interface for set variable (*SetVar*) arithmetical expressions.
 * This interface enables the construction of relational and arithmetic expressions
 * over sets, allowing operations such as union, intersection, and membership checks.
 *
 * @author Gabriel Augusto David
 * @author Charles Prud'homme
 */

public interface ArSetExpression {

    /**
     * Return the associated model
     *
     * @return a Model object
     */
    Model getModel();

    /**
     * Returns the set variable associated with this expression.
     *
     * @return the corresponding set variable.
     */
    SetVar setVar();

    /**
     * Returns the union of this set with another set.
     *
     * @param y the other set expression.
     * @return a new expression representing the union.
     */
    default ArSetExpression union(ArSetExpression y) {
        return new BiArSetExpression(SetOperator.UNION, this, y);
    }

    /**
     * Returns the intersection of this set with another set.
     *
     * @param y the other set expression.
     * @return a new expression representing the intersection.
     */
    default ArSetExpression intersection(ArSetExpression y) {
        return new BiArSetExpression(SetOperator.INTERSECTION, this, y);
    }

    /**
     * Checks if this set is a subset of another set.
     *
     * @param y the other set expression.
     * @return a boolean expression representing the subset relationship.
     */
    default ReExpression subSet(ArSetExpression y) {
        return new BiReSetExpression(SetOperator.SUBSET, this, y);
    }

    /**
     * Checks if two sets are equal.
     *
     * @param y the other set expression.
     * @return a boolean expression representing equality.
     */
    default ReExpression eq(ArSetExpression y) {
        return new BiReSetExpression(SetOperator.EQ, this, y);
    }

    /**
     * Checks if two sets are different.
     *
     * @param y the other set expression.
     * @return a boolean expression representing inequality.
     */
    default ReExpression ne(ArSetExpression y) {
        return new BiReSetExpression(SetOperator.NE, this, y);
    }

    /**
     * Checks if this set contains another set.
     *
     * @param y the other set expression.
     * @return a boolean expression representing the containment relation.
     */
    default ReExpression contains(ArSetExpression y) {
        return new BiReSetExpression(SetOperator.CONTAINS, this, y);
    }

    /**
     * Checks if this set does not contain another set.
     *
     * @param y the other set expression.
     * @return a boolean expression representing the non-membership relation.
     */
    default ReExpression notContains(ArSetExpression y) {
        return new BiReSetExpression(SetOperator.NOT_CONTAINS, this, y);
    }

    /**
     * Checks if this set is a subset of a given set of integers.
     *
     * @param y the integer values to check.
     * @return a boolean expression representing the subset relationship.
     */
    default ReExpression subSet(int... y) {
        return new BiReSetExpression(SetOperator.SUBSET, this, this.getModel().setVar(Arrays.stream(y).toArray()));
    }

    /**
     * Checks if this set is equal to a given set of integers.
     *
     * @param y the integer values to compare.
     * @return a boolean expression representing equality.
     */
    default ReExpression eq(int... y) {
        return new BiReSetExpression(SetOperator.EQ, this, this.getModel().setVar(Arrays.stream(y).toArray()));
    }

    /**
     * Checks if this set does not contain a given set of integers.
     *
     * @param y the integer values to check.
     * @return a boolean expression representing non-membership.
     */
    default ReExpression notContains(int... y) {
        return new BiReSetExpression(SetOperator.NOT_CONTAINS, this, this.getModel().setVar(Arrays.stream(y).toArray()));
    }

    /**
     * Checks if this set contains a given set of integers.
     *
     * @param y the integer values to check.
     * @return a boolean expression representing membership.
     */
    default ReExpression contains(int... y) {
        return new BiReSetExpression(SetOperator.CONTAINS, this, this.getModel().setVar(Arrays.stream(y).toArray()));
    }

    /**
     * Checks if this set is different from a given set of integers.
     *
     * @param y the integer values to compare.
     * @return a boolean expression representing inequality.
     */
    default ReExpression ne(int... y) {
        return new BiReSetExpression(SetOperator.NE, this, this.getModel().setVar(Arrays.stream(y).toArray()));
    }

    /**
     * Returns a constraint ensuring that the set is not empty.
     *
     * @return a constraint enforcing the non-emptiness of the set.
     */
    default ReExpression notEmpty() {
        return new BiReSetExpression(SetOperator.NOT_EMPTY, this, this.getModel().setVar());
    }

    /**
     * Constrains the cardinality of the set to be equal to a specific value.
     *
     * @param valueCard the desired cardinality value.
     * @return a unary relational expression enforcing the set cardinality.
     */
    default UnCReExpression setCard(int valueCard) {
        SetVar set = this.setVar();
        return set.getCard().eq(valueCard);
    }

}