/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;

import java.util.Arrays;
import java.util.IdentityHashMap;

/**
 * The mapping from identifiers to the objects of a model, shared by {@link Recorder} and {@link Replayer}.
 * The reverse mapping ({@link #idOf(Object)}) is built on demand.
 *
 * @author Charles Prud'homme
 */
abstract class IdTable implements Resolver {

    private final Model model;
    private Object[] objects = new Object[64];
    private int capacity;
    private IdentityHashMap<Object, Integer> reverse;

    IdTable(Model model) {
        this.model = model;
    }

    /**
     * Bind <i>id</i> to <i>o</i>.
     *
     * @return the object previously bound to <i>id</i>, or null
     */
    final Object put(int id, Object o) {
        if (id >= objects.length) {
            objects = Arrays.copyOf(objects, Math.max(id + 1, objects.length * 2));
        }
        Object prev = objects[id];
        objects[id] = o;
        capacity = Math.max(capacity, id + 1);
        reverse = null;
        return prev;
    }

    @Override
    public final Model model() {
        return model;
    }

    @Override
    public final Object get(int id) {
        if (!isBound(id)) {
            throw new SpecException("Unbound identifier " + id);
        }
        return objects[id];
    }

    @Override
    public final int capacity() {
        return capacity;
    }

    @Override
    public final boolean isBound(int id) {
        return id >= 0 && id < capacity && objects[id] != null;
    }

    @Override
    public int idOf(Object o) {
        if (reverse == null) {
            reverse = new IdentityHashMap<>();
            for (int id = 0; id < capacity; id++) {
                if (objects[id] != null) {
                    reverse.putIfAbsent(objects[id], id);
                }
            }
        }
        return reverse.getOrDefault(o, -1);
    }
}
