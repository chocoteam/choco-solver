/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.backtrackable;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * @author Alexandre LEBRUN
 */
public class BipartiteSetTest extends BitSetTest {

    @Override
    public ISet create(int offset) {
        return SetFactory.makeStoredSet(SetType.BIPARTITESET, offset, model);
    }

    @Override
    public ISet create() {
        return SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
    }
}
