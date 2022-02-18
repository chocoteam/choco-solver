/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.backtrackable;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.objects.setDataStructures.nonbacktrackable.SetTest;

/**
 * @author Jean-Guillaume FAGES
 */
public class SortedBitSetTest extends SetTest {

    @Override
    public ISet create(int offset) {
        IntIterableBitSet set = new IntIterableBitSet();
        set.setOffset(offset);
        return set;
    }
}
