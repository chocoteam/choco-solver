/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.propagation.PropagationEngine;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/11/2022
 */
final class BipartiteList implements IBipartiteList {
    /**
     * The current capacity
     */
    private int capacity;
    /**
     * The position of the first element (inclusive)
     */
    int first;
    /**
     * The position of the last element (exclusive)
     */
    int last;
    /**
     * The number of passive propagators, starting from first
     */
    final IStateInt splitter;

    /**
     * List of propagators
     */
    Propagator<?>[] propagators;

    /**
     * Store the index of each propagator.
     */
    int[] pindices;

    BipartiteList(IEnvironment environment) {
        this.splitter = environment.makeInt(0);
        this.first = this.last = 0;
        this.capacity = 1;
        this.propagators = new Propagator[capacity];
        this.pindices = new int[capacity];
    }

    @Override
    public int getFirst() {
        return first;
    }

    @Override
    public int getLast() {
        return last;
    }

    /**
     * @implSpec starting from first
     */
    @Override
    public int getSplitter() {
        return splitter.get();
    }

    @Override
    public Propagator<?> get(int i) {
        return propagators[i];
    }

    /**
     * @implSpec Add a propagator <i>p</i> at the end of {@link #propagators}
     * and set at the same position in {@link #pindices} the position
     * of the variable in <i>p</i>.
     */
    @Override
    public int add(Propagator<?> propagator, int idxInVar) {
        if (first > 0 && splitter.get() == 0) {
            shiftTail();
        }
        if (last == capacity - 1) {
            capacity = ArrayUtils.newBoundedSize(capacity, capacity * 2);
            propagators = Arrays.copyOf(propagators, capacity);
            pindices = Arrays.copyOf(pindices, capacity);
        }
        propagators[last] = propagator;
        pindices[last++] = idxInVar;
        return last - 1;
    }

    /**
     * @implSpec this consider the status (active or not) of a propagator for correct removal.
     */
    public void remove(Propagator<?> propagator, int idxInProp, final AbstractVariable var) {
        int p = propagator.getVIndice(idxInProp);
        assert p > -1;
        assert propagators[p] == propagator : "Try to unlink from " + var.getName() + ":\n" + propagator + "but found:\n" + propagators[p];
        assert propagators[p].getVar(idxInProp) == var;
        // Dynamic addition of a propagator may be not considered yet, so the assertion is not correct
        if (p < splitter.get()) {
            // swap the propagator to remove with the first one
            propagator.setVIndices(idxInProp, -1);
            propagators[p] = propagators[first];
            pindices[p] = pindices[first];
            propagators[p].setVIndices(pindices[p], p);
            propagators[first] = null;
            pindices[first] = 0;
            first++;
        } else {
            // swap the propagator to remove with the last one
            last--;
            if (p < last) {
                propagators[p] = propagators[last];
                pindices[p] = pindices[last];
                propagators[p].setVIndices(pindices[p], p);
            }
            propagators[last] = null;
            pindices[last] = 0;
            propagator.setVIndices(idxInProp, -1);
        }
    }

    public void swap(Propagator<?> propagator, int idxInProp, final AbstractVariable var) {
        int p = propagator.getVIndice(idxInProp);
        assert p != -1;
        assert propagators[p] == propagator : "Try to swap from " + var.getName() + ":\n" + propagator + "but found: " + propagators[p];
        assert propagators[p].getVar(idxInProp) == var;
        int pos = splitter.add(1) - 1;
        if (first > 0) {
            if (pos == 0) {
                shiftTail();
                //then recompute the position of this
                p = propagator.getVIndice(idxInProp);
            } else {
                // s = Math.min(s, first);
                throw new UnsupportedOperationException();
            }
        }
        if (pos < p) {
            propagators[p] = propagators[pos];
            propagators[pos] = propagator;
            int pi = pindices[p];
            pindices[p] = pindices[pos];
            pindices[pos] = pi;
            propagators[p].setVIndices(pindices[p], p);
            propagators[pos].setVIndices(pindices[pos], pos);
            assert propagators[pos] == propagator;
        }
    }

    public void schedule(ICause cause, PropagationEngine engine, int mask) {
        int s = splitter.get();
        if (first > 0) {
            if (s == 0) {
                shiftTail();
            } else {
                throw new UnsupportedOperationException();
            }
        }
        for (int p = s; p < last; p++) {
            Propagator<?> prop = propagators[p];
            if (prop.isActive() && cause != prop) {
                engine.schedule(prop, pindices[p], mask);
            }
        }
    }

    private void shiftTail() {
        for (int i = 0; i < last - first; i++) {
            propagators[i] = propagators[i + first];
            pindices[i] = pindices[i + first];
            propagators[i].setVIndices(pindices[i], i);
        }
        for (int i = last - first; i < last; i++) {
            propagators[i] = null;
            pindices[i] = 0;
        }
        last -= first;
        first = 0;
    }

    public Stream<Propagator<?>> stream() {
        int s = splitter.get();
        if (first > 0) {
            if (s == 0) {
                shiftTail();
            }
        }
        Spliterator<Propagator<?>> it = new Spliterator<Propagator<?>>() {
            int i = s;

            @Override
            public boolean tryAdvance(Consumer<? super Propagator<?>> action) {
                if (i < last) {
                    action.accept(propagators[i++]);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Spliterator<Propagator<?>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return last - first;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.CONCURRENT;
            }

        };
        return StreamSupport.stream(it, false);
    }
}
