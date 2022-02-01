/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
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
