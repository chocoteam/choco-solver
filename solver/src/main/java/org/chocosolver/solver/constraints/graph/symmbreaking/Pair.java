/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.symmbreaking;

/**
 * @author Моклев Вячеслав
 */
public class Pair<T, V> {
    private final T a;
    private final V b;

    public Pair(T a, V b) {
        this.a = a;
        this.b = b;
    }

    public T getA() {
        return a;
    }

    public V getB() {
        return b;
    }
}
