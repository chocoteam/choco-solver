/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.dynamic;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

/**
 * Dynamic filter set over another set.
 * The class is abstract, any extension only need to implement the `filter` method.
 * Membership to the original set is already tested in the `contains` method.
 * @author Dimitri Justeau-Allaire
 * @since 09/03/2021
 */
public abstract class SetDynamicFilterOnSet extends SetDynamicFilter {

    protected ISet observed;
    protected ISetIterator observedIterator;

    public SetDynamicFilterOnSet(ISet observed) {
        this.observed = observed;
        this.observedIterator = observed.newIterator();
    }

    @Override
    protected SetDynamicFilterIterator createIterator() {
        return new SetDynamicFilterIterator() {
            @Override
            protected void resetPointers() {
                observedIterator.reset();
            }
            @Override
            protected void findNext() {
                next = null;
                while (!hasNext()) {
                    if (!observedIterator.hasNext()) {
                        return;
                    }
                    int observedNext = observedIterator.nextInt();
                    if (contains(observedNext)) {
                        next = observedNext;
                    }
                }
            }
        };
    }

    /**
     * Predicate on elements, if true the element can be a member of this set.
     * It is actually a member if it is also a member of the observed set.
     * @param element the element to test
     * @return true if the element can be a member of this set
     */
    protected abstract boolean filter(int element);

    @Override
    public boolean contains(int element) {
        return observed.contains(element) && filter(element);
    }
}
