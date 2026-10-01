/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * An argument of a {@link Step.Call}.
 * Objects of the model (variables, constraints, tasks) are referred to by identifier, never by name.
 * <p>
 * Arguments are immutable: public accessors return copies, the package reads the arrays directly.
 *
 * @author Charles Prud'homme
 */
public sealed interface Arg permits Arg.Ref, Arg.RefArray, Arg.RefMatrix, Arg.RefList, Arg.Const, Arg.Logic,
        Arg.Literal {

    /**
     * Apply <i>action</i> on each identifier this argument refers to.
     */
    default void forEachRef(IntConsumer action) {
    }

    /**
     * A reference to one object of the model.
     */
    record Ref(int id) implements Arg {
        public Ref {
            if (id < 0) throw new SpecException("Negative identifier: " + id);
        }

        @Override
        public void forEachRef(IntConsumer action) {
            action.accept(id);
        }
    }

    /**
     * An array of objects of the model, e.g. an {@code IntVar[]}.
     *
     * @param componentType type of the elements of the array to build, e.g. {@code IntVar.class}
     * @param ids           identifiers of the elements
     */
    record RefArray(Class<?> componentType, int[] ids) implements Arg {
        public RefArray {
            Objects.requireNonNull(componentType);
            ids = ids.clone();
        }

        @Override
        public int[] ids() {
            return ids.clone();
        }

        int[] rawIds() {
            return ids;
        }

        @Override
        public void forEachRef(IntConsumer action) {
            for (int id : ids) action.accept(id);
        }

        @Override
        public String toString() {
            return componentType.getSimpleName() + Arrays.toString(ids);
        }
    }

    /**
     * A matrix of objects of the model, e.g. an {@code IntVar[][]}.
     *
     * @param componentType type of the elements of the matrix to build, e.g. {@code IntVar.class}
     * @param ids           identifiers of the elements, row by row
     */
    record RefMatrix(Class<?> componentType, int[][] ids) implements Arg {
        public RefMatrix {
            Objects.requireNonNull(componentType);
            ids = (int[][]) Values.capture(ids);
        }

        @Override
        public int[][] ids() {
            return (int[][]) Values.materialize(ids);
        }

        int[][] rawIds() {
            return ids;
        }

        @Override
        public void forEachRef(IntConsumer action) {
            for (int[] row : ids) for (int id : row) action.accept(id);
        }

        @Override
        public String toString() {
            return componentType.getSimpleName() + Arrays.deepToString(ids);
        }
    }

    /**
     * A {@link java.util.List} of objects of the model.
     */
    record RefList(int[] ids) implements Arg {
        public RefList {
            ids = ids.clone();
        }

        @Override
        public int[] ids() {
            return ids.clone();
        }

        int[] rawIds() {
            return ids;
        }

        @Override
        public void forEachRef(IntConsumer action) {
            for (int id : ids) action.accept(id);
        }

        @Override
        public String toString() {
            return "List" + Arrays.toString(ids);
        }
    }

    /**
     * A constant, captured according to the sharing policy of {@link Values}.
     */
    record Const(Object value) implements Arg {
        public Const {
            value = Values.capture(value);
        }

        /**
         * @return the value, shared or copied according to the sharing policy of {@link Values}
         */
        @Override
        public Object value() {
            return Values.materialize(value);
        }

        /**
         * @return the captured value, as stored in the spec (not to be modified)
         */
        Object rawValue() {
            return value;
        }

        @Override
        public String toString() {
            if (value instanceof int[] a) return Arrays.toString(a);
            if (value instanceof Object[] a) return Arrays.deepToString(a);
            return String.valueOf(value);
        }
    }

    /**
     * A logical tree ({@link LogOp}) over boolean variables.
     *
     * @param and      <i>true</i> for a conjunction, <i>false</i> for a disjunction
     * @param negated  <i>true</i> for the negation of the conjunction (resp. disjunction)
     * @param children either references to {@link BoolVar} or nested {@link Logic}
     */
    record Logic(boolean and, boolean negated, Arg[] children) implements Arg {
        public Logic {
            children = children.clone();
            for (Arg c : children) {
                if (!(c instanceof Ref) && !(c instanceof Logic)) {
                    throw new SpecException("A logical tree only accepts references and logical trees, not " + c);
                }
            }
        }

        @Override
        public Arg[] children() {
            return children.clone();
        }

        Arg[] rawChildren() {
            return children;
        }

        @Override
        public void forEachRef(IntConsumer action) {
            for (Arg c : children) c.forEachRef(action);
        }

        @Override
        public String toString() {
            return (negated ? "n" : "") + (and ? "and" : "or") + Arrays.toString(children);
        }
    }

    /**
     * A {@link org.chocosolver.sat.Literalizer}, which binds a variable to a literal of the SAT solver.
     * Literalizers are mutable (the SAT solver numbers them): a new one is built for each instance.
     *
     * @param kind  the kind of literalizer
     * @param var   identifier of the variable
     * @param value the value (for {@link Kind#INT_EQ} and {@link Kind#INT_LE} only)
     */
    record Literal(Kind kind, int var, int value) implements Arg {

        /**
         * The kinds of literalizers: {@code BoolLit}, {@code IntEqLit} and {@code IntLeLit}.
         */
        public enum Kind {
            BOOL, INT_EQ, INT_LE
        }

        public Literal {
            Objects.requireNonNull(kind);
            if (var < 0) throw new SpecException("Negative identifier: " + var);
        }

        @Override
        public void forEachRef(IntConsumer action) {
            action.accept(var);
        }
    }

    static Logic and(Arg... children) {
        return new Logic(true, false, children);
    }

    static Logic or(Arg... children) {
        return new Logic(false, false, children);
    }
}
