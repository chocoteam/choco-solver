/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 21/06/2018.
 */
public class ShrinkableList<T> extends ArrayList<T> {

    public ShrinkableList(int initialCapacity) {
        super(initialCapacity);
    }

    public ShrinkableList() {
        super();
    }

    public ShrinkableList(Collection<? extends T> c) {
        super(c);
    }

    @Override
    public void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }
}
