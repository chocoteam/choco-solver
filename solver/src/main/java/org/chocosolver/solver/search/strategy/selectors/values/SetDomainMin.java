/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

/**
 * Selects the first integer in the envelope and not in the kernel
 *
 * @author Jean-Guillaum Fages, Charles Prud'homme
 * @since 17/03/2014
 */
public class SetDomainMin implements SetValueSelector {

    @Override
    public int selectValue(SetVar s) {
        ISetIterator iter = s.getUB().iterator();
        while (iter.hasNext()) {
            int i = iter.nextInt();
            if (!s.getLB().contains(i)) {
                return i;
            }
        }
        throw new UnsupportedOperationException(s + " is already instantiated. Cannot compute a decision on it");
    }
}
