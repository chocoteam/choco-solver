package org.chocosolver.solver.expression.discrete.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.expression.discrete.relational.UnCReExpression;
import org.chocosolver.solver.variables.SetVar;

/**
 * Interface for set variable (*SetVar*) expressions.
 * This interface enables the construction of relational and arithmetic expressions
 * over sets, allowing operations such as union, intersection, and membership checks.
 */

public interface SetExpression {
    /**
     * Returns the union of this set with another set.
     *
     * @param y the other set expression.
     * @return a new expression representing the union.
     */
    default SetExpression union(SetExpression y) {
        return new BiArSetExpression(SetOperator.UNION, this, y);
    }

    /**
     * Returns the intersection of this set with another set.
     *
     * @param y the other set expression.
     * @return a new expression representing the intersection.
     */
    default SetExpression intersection(SetExpression y) {
        return new BiArSetExpression(SetOperator.INTERSECTION, this, y);
    }
    /**
     * Checks if this set is a subset of another set.
     *
     * @param y the other set expression.
     * @return a boolean expression representing the subset relationship.
     */
    default ReExpression subSet(SetExpression y) {
        return new BiReSetExpression(SetOperator.SUBSET, this, y);
    }

    /**
     * Checks if two sets are equal.
     *
     * @param y the other set expression.
     * @return a boolean expression representing equality.
     */
    default ReExpression eq(SetExpression y) {
        return new BiReSetExpression(SetOperator.EQ, this, y);
    }

    /**
     * Checks if two sets are different.
     *
     * @param y the other set expression.
     * @return a boolean expression representing inequality.
     */
    default ReExpression ne(SetExpression y) {
        return new BiReSetExpression(SetOperator.NE, this, y);
    }

    /**
     * Checks if this set contains another set.
     *
     * @param y the other set expression.
     * @return a boolean expression representing the containment relation.
     */
    default ReExpression contains(SetExpression y) {
        return new BiReSetExpression(SetOperator.CONTAINS, this, y);
    }
    /**
     * Checks if this set does not contain another set.
     *
     * @param y the other set expression.
     * @return a boolean expression representing the non-membership relation.
     */
    default ReExpression notContains(SetExpression y) {
        return new BiReSetExpression(SetOperator.NC, this, y);
    }

    /**
     * Checks if this set is a subset of a given set of integers.
     *
     * @param y the integer values to check.
     * @return a boolean expression representing the subset relationship.
     */
    default ReExpression subSet(int... y) {
        return new UnReSetExpression(SetOperator.SUBSET, this, y);
    }

    /**
     * Checks if this set is equal to a given set of integers.
     *
     * @param y the integer values to compare.
     * @return a boolean expression representing equality.
     */
    default ReExpression eq(int... y) {
        return new UnReSetExpression(SetOperator.EQ, this, y);
    }

    /**
     * Checks if this set does not contain a given set of integers.
     *
     * @param y the integer values to check.
     * @return a boolean expression representing non-membership.
     */
    default ReExpression notContains(int... y) {
        return new UnReSetExpression(SetOperator.NC, this, y);
    }

    /**
     * Checks if this set contains a given set of integers.
     *
     * @param y the integer values to check.
     * @return a boolean expression representing membership.
     */
    default ReExpression contains(int... y) {
        return new UnReSetExpression(SetOperator.CONTAINS, this, y);
    }

    /**
     * Checks if this set is different from a given set of integers.
     *
     * @param y the integer values to compare.
     * @return a boolean expression representing inequality.
     */
    default ReExpression ne(int... y) {
        return new UnReSetExpression(SetOperator.NE, this, y);
    }

    /**
     * Returns a constraint ensuring that the set is not empty.
     *
     * @return a constraint enforcing the non-emptiness of the set.
     */
    default Constraint notEmpty() {
        SetVar set = this.getSetVar();
        return getModel().notEmpty(set);
    }

    /**
     * Constrains the cardinality of the set to be equal to a specific value.
     *
     * @param valueCard the desired cardinality value.
     * @return a unary relational expression enforcing the set cardinality.
     */
    default UnCReExpression setCard(int valueCard) {
        SetVar set = this.getSetVar();
        return set.getCard().eq(valueCard);
    }

    /**
     * Returns the set variable associated with this expression.
     *
     * @return the corresponding set variable.
     */
    SetVar getSetVar();

    /**
     * Returns the model associated with this expression.
     *
     * @return the model of the expression.
     * @throws UnsupportedOperationException if this method is not supported.
     */

    default Model getModel() {
        throw new UnsupportedOperationException("getModel() is not supported in this context");
    }

}