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

import java.util.Collections;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

/**
 * Selects the first integer in the envelope and not in the kernel
 *
 * @author Jean-Guillaum Fages, Charles Prud'homme
 * @since 17/03/2014
 */
public class SetDomainMax implements SetValueSelector {

    @Override
    public int selectValue(SetVar s) {
        Spliterator<Integer> str = Spliterators.spliterator(s.getUB().iterator(), s.getUB().size(),
                ORDERED & DISTINCT & SORTED & SIZED);
        Optional<Integer> v = StreamSupport.stream(str, false)
                .sorted(Collections.reverseOrder())
                .filter(i -> !s.getLB().contains(i))
                .findFirst();
        if (v.isPresent()) {
            return v.get();
        }
        throw new UnsupportedOperationException(s + " is already instantiated. Cannot compute a decision on it");
    }
}
