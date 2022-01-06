/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
public class DisposableRangeBoundIterator extends DisposableRangeIterator {

    private int from;
    private int to;
    private boolean _next = true;

    private final IntVar var;

    public DisposableRangeBoundIterator(IntVar var) {
        this.var = var;
    }

    @Override
    public void bottomUpInit() {
        super.bottomUpInit();
        _next = true;
        from = var.getLB();
        to = var.getUB();
    }

    @Override
    public void topDownInit() {
        super.topDownInit();
        _next = true;
        from = var.getLB();
        to = var.getUB();
    }

    @Override
    public boolean hasNext() {
        return _next;
    }

    @Override
    public boolean hasPrevious() {
        return _next;
    }

    @Override
    public void next() {
        _next = false;
    }

    @Override
    public void previous() {
        _next = false;
    }

    @Override
    public int min() {
        return from;
    }

    @Override
    public int max() {
        return to;
    }
}
