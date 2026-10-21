/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * A step of a {@link ModelSpec}. Steps are replayed in order.
 * <p>
 * A step may <i>define</i> identifiers (its outputs) and <i>use</i> identifiers defined by previous steps.
 * An identifier may be defined more than once, provided that the replay binds it to the same object each time
 * (e.g., a cached constant).
 * <p>
 * Steps are immutable: public accessors return copies, the package reads the arrays directly.
 *
 * @author Charles Prud'homme
 */
public sealed interface Step permits Step.Call, Step.Post, Step.Reify, Step.Link, Step.Unpost, Step.NewTask,
        Step.Custom, Step.Hook, Step.Seed, Step.Objective, Step.Group {

    /**
     * Apply <i>action</i> on each identifier this step uses.
     */
    default void forEachUse(IntConsumer action) {
    }

    /**
     * Apply <i>action</i> on each identifier this step defines.
     */
    default void forEachDef(IntConsumer action) {
    }

    /**
     * A call to a factory method of the model.
     *
     * @param out    identifiers of the outputs: empty if the result is ignored, one identifier for an object,
     *               one identifier per element for an array (row by row for a matrix), -1 to skip an element
     * @param method the factory method
     * @param args   the arguments, one per parameter of <i>method</i>
     */
    record Call(int[] out, FactoryMethod method, Arg[] args) implements Step {
        public Call {
            Objects.requireNonNull(method);
            out = out.clone();
            args = args.clone();
            if (args.length != method.parameterCount()) {
                throw new SpecException(method + " expects " + method.parameterCount()
                        + " arguments, got " + args.length);
            }
            if (out.length > 0 && method.returnType() == void.class) {
                throw new SpecException(method + " returns nothing");
            }
        }

        @Override
        public int[] out() {
            return out.clone();
        }

        @Override
        public Arg[] args() {
            return args.clone();
        }

        int[] rawOut() {
            return out;
        }

        Arg[] rawArgs() {
            return args;
        }

        @Override
        public void forEachUse(IntConsumer action) {
            for (Arg a : args) a.forEachRef(action);
        }

        @Override
        public void forEachDef(IntConsumer action) {
            for (int o : out) if (o >= 0) action.accept(o);
        }

        @Override
        public String toString() {
            return Arrays.toString(out) + " = " + method.name() + Arrays.toString(args);
        }
    }

    /**
     * Post constraints, as {@link org.chocosolver.solver.Model#post(Constraint...)}.
     */
    record Post(int[] cstrs) implements Step {
        public Post {
            cstrs = cstrs.clone();
        }

        @Override
        public int[] cstrs() {
            return cstrs.clone();
        }

        int[] rawCstrs() {
            return cstrs;
        }

        @Override
        public void forEachUse(IntConsumer action) {
            for (int c : cstrs) action.accept(c);
        }

        @Override
        public String toString() {
            return "post" + Arrays.toString(cstrs);
        }
    }

    /**
     * Reify a constraint, as {@link Constraint#reify()}, and bind the resulting boolean variable to <i>out</i>.
     */
    record Reify(int cstr, int out) implements Step {
        @Override
        public void forEachUse(IntConsumer action) {
            action.accept(cstr);
        }

        @Override
        public void forEachDef(IntConsumer action) {
            action.accept(out);
        }
    }

    /**
     * Link a constraint to a boolean variable: {@link Constraint#reifyWith}, {@link Constraint#implies} or
     * {@link Constraint#impliedBy}.
     */
    record Link(Kind kind, int cstr, int bool) implements Step {

        /**
         * The kinds of links between a constraint and a boolean variable.
         */
        public enum Kind {
            REIFY_WITH, IMPLIES, IMPLIED_BY
        }

        public Link {
            Objects.requireNonNull(kind);
        }

        @Override
        public void forEachUse(IntConsumer action) {
            action.accept(cstr);
            action.accept(bool);
        }
    }

    /**
     * Unpost constraints, as {@link org.chocosolver.solver.Model#unpost(Constraint...)}.
     */
    record Unpost(int[] cstrs) implements Step {
        public Unpost {
            cstrs = cstrs.clone();
        }

        @Override
        public int[] cstrs() {
            return cstrs.clone();
        }

        int[] rawCstrs() {
            return cstrs;
        }

        @Override
        public void forEachUse(IntConsumer action) {
            for (int c : cstrs) action.accept(c);
        }

        @Override
        public String toString() {
            return "unpost" + Arrays.toString(cstrs);
        }
    }

    /**
     * Create a {@link Task} (or an {@link OptionalTask} when <i>performed</i> is not -1) with its canonical
     * constructor, from existing variables.
     *
     * @param out       identifier of the task
     * @param start     identifier of the start variable
     * @param duration  identifier of the duration variable
     * @param end       identifier of the end variable
     * @param performed identifier of the boolean variable of an optional task, -1 for a mandatory task
     */
    record NewTask(int out, int start, int duration, int end, int performed) implements Step {

        public NewTask(int out, int start, int duration, int end) {
            this(out, start, duration, end, -1);
        }

        @Override
        public void forEachUse(IntConsumer action) {
            action.accept(start);
            action.accept(duration);
            action.accept(end);
            if (performed >= 0) action.accept(performed);
        }

        @Override
        public void forEachDef(IntConsumer action) {
            action.accept(out);
        }
    }

    /**
     * A custom constraint, built by <i>factory</i> on the model under instantiation, for specs built by hand
     * ({@link SpecBuilder#custom}) or extended by a variant ({@link Variant#extend}); a recorded model journalizes
     * its custom constraints as calls to {@code IConstraintFactory#custom}.
     * The factory must only use objects obtained from the {@link Resolver}.
     * The constraint is neither posted nor reified: use subsequent steps.
     *
     * @param out     identifier of the constraint, -1 to ignore it
     * @param name    name of the custom constraint (for error messages)
     * @param factory builds the constraint; must be stateless as it may be called concurrently
     */
    record Custom(int out, String name, Function<Resolver, Constraint> factory) implements Step {
        public Custom {
            Objects.requireNonNull(name);
            Objects.requireNonNull(factory);
        }

        @Override
        public void forEachDef(IntConsumer action) {
            if (out >= 0) action.accept(out);
        }
    }

    /**
     * Add a hook to the model, as {@link org.chocosolver.solver.Model#addHook(String, Object)}, or remove it
     * when <i>value</i> is a null constant; remove all the hooks, as
     * {@link org.chocosolver.solver.Model#removeAllHooks()}, when <i>name</i> is null.
     */
    record Hook(String name, Arg value) implements Step {
        public Hook {
            Objects.requireNonNull(value);
            if (name == null && !(value instanceof Arg.Const c && c.value() == null)) {
                throw new IllegalArgumentException("removing all the hooks takes no value");
            }
        }

        @Override
        public void forEachUse(IntConsumer action) {
            value.forEachRef(action);
        }
    }

    /**
     * Set the seed of the model, as {@link org.chocosolver.solver.Model#setSeed(long)}.
     */
    record Seed(long seed) implements Step {
    }

    /**
     * Set the objective of the model, as {@link org.chocosolver.solver.Model#setObjective(boolean,
     * org.chocosolver.solver.variables.Variable)}, or clear it when <i>var</i> is -1.
     */
    record Objective(boolean maximize, int var) implements Step {
        @Override
        public void forEachUse(IntConsumer action) {
            if (var >= 0) action.accept(var);
        }
    }

    /**
     * Declare a group of variables, as
     * {@link org.chocosolver.solver.Model#addGroup(org.chocosolver.solver.variables.Group)}.
     * Groups are used by some search strategies to select the decision variables.
     */
    record Group(String name, Arg.RefArray vars) implements Step {
        public Group {
            Objects.requireNonNull(name);
            Objects.requireNonNull(vars);
        }

        @Override
        public void forEachUse(IntConsumer action) {
            vars.forEachRef(action);
        }
    }
}
