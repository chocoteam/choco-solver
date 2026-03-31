/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.iterators;

/**
 * An disposable iterator for a range sequence.
 * <p/>
 *
 * @author Charles Prud'homme
 * @since 05/10/11
 */
public abstract class DisposableRangeIterator extends Disposable implements RangeIterator {

    public void bottomUpInit() {
        super.init();
    }

    public void topDownInit() {
        super.init();
    }

}
