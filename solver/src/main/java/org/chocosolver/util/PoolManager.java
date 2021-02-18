/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util;


import java.util.ArrayDeque;
import java.util.Deque;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/02/11
 */
public class PoolManager<E>  {

    private Deque<E> elements;

    public PoolManager() {
        this(16);
    }

    public PoolManager(int initialSize) {
        elements = new ArrayDeque<>(initialSize);
    }

    public E getE() {
        if (elements.isEmpty()) {
            return null;
        } else {
            return elements.remove();
        }
    }

    public void returnE(E element) {
        elements.add(element);
    }
}
