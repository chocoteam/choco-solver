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

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.*;

/**
 * Set representing the intersection of a set of sets.
 * Constructed incrementally when observed sets are modified.
 * This set is read-only.
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class SetIntersection extends AbstractSet {

    public ISet[] sets;
    protected ISet values;

    /**
     * Constructor for unstored SetIntersection. Internal storage is RANGESET by default.
     */
    public SetIntersection(ISet... sets) {
        this(SetType.RANGESET, 0, sets);
    }

    /**
     * Constructor for unstored SetIntersection with manual specification of set type for internal storage.
     */
    public SetIntersection(SetType setType, int offset, ISet... sets) {
        this.sets = sets;
        this.values = SetFactory.makeSet(setType, offset);
        init();
    }

    /**
     * Constructor for stored SetIntersection. Internal storage is RANGESET by default.
     */
    public SetIntersection(Model model, ISet... sets) {
        this(model, SetType.RANGESET, 0, sets);
    }

    /**
     * Constructor for stored SetIntersection with manual specification of set type for internal storage.
     */
    public SetIntersection(Model model, SetType setType, int offset, ISet... sets) {
        this.sets = sets;
        this.values = SetFactory.makeStoredSet(setType, offset, model);
        init();
    }

    private void init() {
        // init the intersection
        for (int i = 0; i < sets.length; i++) {
            this.sets[i].registerObserver(this, i);
            for (int v : this.sets[i]) {
                if (!values.contains(v)) {
                    boolean add = true;
                    for (ISet s : sets) {
                        if (!s.contains(v)) {
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        values.add(v);
                    }
                }
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
        if (values.remove(element)) {
            notifyObservingElementRemoved(element);
        }
    }

    @Override
    public void notifyElementAdded(int element, int idx) {
        boolean add = true;
        for (ISet s : sets) {
            if (!s.contains(element)) {
                add = false;
                break;
            }
        }
        if (add) {
            values.add(element);
            notifyObservingElementAdded(element);
        }
    }

    @Override
    public void notifyCleared(int idx) {
        values.clear();
        notifyObservingCleared();
    }
}
