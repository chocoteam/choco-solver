/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.iterators;

import org.chocosolver.solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/10/11
 */
public class DisposableValueBoundIterator extends DisposableValueIterator {

    private int value;
    private int bound;

    private IntVar var;

    public DisposableValueBoundIterator(IntVar var) {
        this.var = var;
    }

    @Override
    public void bottomUpInit() {
        super.bottomUpInit();
        value = var.getLB();
        bound = var.getUB();
    }

    @Override
    public void topDownInit() {
        super.topDownInit();
        value = var.getUB();
        bound = var.getLB();
    }

    @Override
    public boolean hasNext() {
        return value <= bound;
    }

    @Override
    public boolean hasPrevious() {
        return value >= bound;
    }

    @Override
    public int next() {
        return value++;
    }

    @Override
    public int previous() {
        return value--;
    }
}
