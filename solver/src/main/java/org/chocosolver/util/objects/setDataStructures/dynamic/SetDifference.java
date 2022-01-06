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

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.*;

/**
 * Set representing the difference between two sets (setA \ setB).
 * Constructed incrementally when observed sets are modified.
 * This set is read-only.
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class SetDifference extends AbstractSet {

    public ISet setA;
    public ISet setB;
    protected ISet values;

    /**
     * Constructor for unstored SetDifference. If setA is not Dynamic, setA.getSetType() is used for internal
     * storage of values (as values cannot be inferior than setA's minimum possible element), else RANGESET is used
     * as it does not need an offset.
      * @param setA
     * @param setB
     */
    public SetDifference(ISet setA, ISet setB) {
        this(setA, setB,
                setA.getSetType() != SetType.DYNAMIC ? setA.getSetType() : SetType.RANGESET,
                setA instanceof ISet.WithOffset ? ((ISet.WithOffset)setA).getOffset() : 0);
    }

    /**
     * Constructor for unstored SetDifference with manual specification of set type for internal storage.
     * @param setA
     * @param setB
     */
    public SetDifference(ISet setA, ISet setB, SetType setType, int offset) {
        this.setA = setA;
        this.setB = setB;
        if (setType == SetType.FIXED_ARRAY || setType == SetType.FIXED_INTERVAL) {
            this.values = SetFactory.makeRangeSet();
        } else {
            this.values = SetFactory.makeSet(setType, offset);
        }
        init();
    }

    /**
     * Constructor for stored SetDifference. If setA is not Dynamic, setA.getSetType() is used for internal
     * storage of values (as values cannot be inferior than setA's minimum possible element), else RANGESET is used
     * as it does not need an offset.
     * @param setA
     * @param setB
     */
    public SetDifference(Model model, ISet setA, ISet setB) {
        this(model, setA, setB,
                setA.getSetType() != SetType.DYNAMIC ? setA.getSetType() : SetType.RANGESET,
                setA instanceof ISet.WithOffset ? ((ISet.WithOffset)setA).getOffset() : 0);
    }

    /**
     * Constructor for stored SetDifference with manual specification of set type for internal storage.
     * @param setA
     * @param setB
     */
    public SetDifference(Model model, ISet setA, ISet setB, SetType setType, int offset) {
        this.setA = setA;
        this.setB = setB;
        this.values = SetFactory.makeStoredSet(setType, offset, model);
        init();
    }

    private void init() {
        setA.registerObserver(this, 0);
        setB.registerObserver(this, 1);
        // init the difference
        for (int v : setA) {
            if (!setB.contains(v)) {
                values.add(v);
            }
        }
    }

    @Override
    public ISetIterator iterator() {
        return values.iterator();
    }

    @Override
    public ISetIterator newIterator() {
        return values.newIterator();
    }

    @Override
    public boolean add(int element) {
        throw new UnsupportedOperationException("this set is read-only");
    }

    @Override
    public boolean remove(int element) {
        throw new UnsupportedOperationException("this set is read-only");
    }


    @Override
    public boolean contains(int element) {
        return values.contains(element);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("this set is read-only");
    }

    @Override
    public int min() {
        return values.min();
    }

    @Override
    public int max() {
        return values.max();
    }

    @Override
    public SetType getSetType() {
        return SetType.DYNAMIC;
    }

    @Override
    public void notifyElementRemoved(int element, int idx) {
        if (idx == 0) {
            if (values.remove(element)) {
                notifyObservingElementRemoved(element);
            }
        }
        if (idx == 1 && setA.contains(element)) {
            if (values.add(element)) {
                notifyObservingElementAdded(element);
            }
        }
    }

    @Override
    public void notifyElementAdded(int element, int idx) {
        if (idx == 0 && !setB.contains(element)) {
            if (values.add(element)) {
                notifyObservingElementAdded(element);
            }
        }
        if (idx == 1 && setA.contains(element)) {
            if (values.remove(element)) {
                notifyObservingElementRemoved(element);
            }
        }
    }

    @Override
    public void notifyCleared(int idx) {
        if (idx == 0) {
            values.clear();
            notifyObservingCleared();
        }
        if (idx == 0) {
            for (int v : setA) {
                if (values.add(v)) {
                    notifyObservingElementAdded(v);
                }
            }
        }
    }
}
