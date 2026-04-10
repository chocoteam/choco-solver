/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.nonbacktrackable;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

/**
 * @author Alexandre LEBRUN
 */
public class BipartiteSetTest extends SetTest {

    @Override
    public ISet create(int offset) {
        return SetFactory.makeBipartiteSet(offset);
    }
}
