/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.hybrid.HybridTuples;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.function.UnaryOperator;

/**
 * Sharing policy of the constant arguments of a {@link ModelSpec}: the single place which decides what is shared by
 * the instances of a spec.
 * <ul>
 *     <li>immutable values (boxed primitives, strings, enums) are shared;</li>
 *     <li>heavy read-only data ({@link Tuples}, {@link HybridTuples}, {@link MultivaluedDecisionDiagram}) are frozen on
 *     capture, then shared;</li>
 *     <li>objects without instance field (e.g., non-capturing lambdas, stateless singletons such as
 *     {@code Condition.EXCEPT_0}) cannot capture anything: they are shared;</li>
 *     <li>arrays (of any dimension, whose elements are supported) and {@link IntIterableRangeSet} are copied on capture
 *     and on each instantiation;</li>
 *     <li>automata ({@link FiniteAutomaton} and subclasses) are not thread-safe: copied on capture and on each
 *     instantiation;</li>
 *     <li>records whose components are supported: shared if all their components are, otherwise rebuilt with
 *     their canonical constructor from copied components.</li>
 * </ul>
 * Any other type is rejected, in particular capturing lambdas (they may capture objects of the recorded model).
 *
 * @author Charles Prud'homme
 */
final class Values {

    private Values() {
    }

    /**
     * Capture a constant when building a spec, detaching it from the caller.
     *
     * @param value a constant
     * @return the value to store in the spec
     * @throws SpecException if the type of <i>value</i> is not supported
     */
    static Object capture(Object value) {
        if (value instanceof Tuples t) {
            return t.freeze();
        }
        if (value instanceof HybridTuples t) {
            return t.freeze();
        }
        if (value instanceof MultivaluedDecisionDiagram mdd) {
            return mdd.freeze();
        }
        return isShared(value) ? value : copy(value, Values::capture);
    }

    /**
     * @param value a captured constant
     * @return the value to pass to a model under instantiation
     */
    static Object materialize(Object value) {
        return isShared(value) ? value : copy(value, Values::materialize);
    }

    /**
     * @return <i>true</i> if <i>value</i> can be shared by models solved concurrently
     */
    static boolean isShared(Object value) {
        return value == null
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof String
                || value instanceof Enum
                || (value instanceof Tuples t && t.isFrozen())
                || (value instanceof HybridTuples t && t.isFrozen())
                || (value instanceof MultivaluedDecisionDiagram mdd && mdd.isFrozen())
                || Specs.isStateless(value.getClass())
                || (value instanceof Record r && allShared(r));
    }

    private static boolean allShared(Record r) {
        try {
            for (RecordComponent rc : r.getClass().getRecordComponents()) {
                if (!isShared(component(r, rc))) {
                    return false;
                }
            }
            return true;
        } catch (SpecException e) {
            // a record which cannot be read is not shared: copying it reports the problem
            return false;
        }
    }

    private static Object component(Record r, RecordComponent rc) {
        try {
            rc.getAccessor().setAccessible(true);
            return rc.getAccessor().invoke(r);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new SpecException("Cannot read the component " + rc.getName() + " of " + r.getClass().getName()
                    + " (is its package open to org.chocosolver.solver?)", e);
        }
    }

    /**
     * @param elements copies the elements of an array or the components of a record
     */
    private static Object copy(Object value, UnaryOperator<Object> elements) {
        Class<?> c = value.getClass();
        if (c.isArray()) {
            int n = Array.getLength(value);
            Object dst = Array.newInstance(c.getComponentType(), n);
            if (c.getComponentType().isPrimitive()) {
                System.arraycopy(value, 0, dst, 0, n);
            } else {
                Object[] src = (Object[]) value;
                Object[] d = (Object[]) dst;
                for (int i = 0; i < n; i++) {
                    d[i] = elements.apply(src[i]);
                }
            }
            return dst;
        }
        if (value instanceof IntIterableRangeSet set) {
            return set.duplicate();
        }
        if (value instanceof FiniteAutomaton fa) {
            // reading an automaton may lazily modify its internal (dk.brics) state
            synchronized (fa) {
                try {
                    return fa.clone();
                } catch (CloneNotSupportedException e) {
                    throw new SpecException("Cannot copy automaton", e);
                }
            }
        }
        if (value instanceof Record r) {
            RecordComponent[] rcs = c.getRecordComponents();
            Object[] args = new Object[rcs.length];
            Class<?>[] types = new Class<?>[rcs.length];
            for (int i = 0; i < rcs.length; i++) {
                args[i] = elements.apply(component(r, rcs[i]));
                types[i] = rcs[i].getType();
            }
            try {
                Constructor<?> canonical = c.getDeclaredConstructor(types);
                canonical.setAccessible(true);
                return canonical.newInstance(args);
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new SpecException("Cannot copy the record " + c.getName(), e);
            }
        }
        if (c.isSynthetic() || c.isHidden()) {
            throw new SpecException("Unsupported capturing lambda " + c.getName()
                    + ": it may capture objects of the recorded model; use a non-capturing lambda");
        }
        throw new SpecException("Unsupported constant of type " + c.getName());
    }
}
