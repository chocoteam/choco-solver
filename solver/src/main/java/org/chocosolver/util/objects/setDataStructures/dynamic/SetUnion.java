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
 * Set representing the union of a set of sets.
 * Constructed incrementally when observed sets are modified.
 * This set is read-only.
 *
 * @author Dimitri Justeau-Allaire
 * @since 29/03/2021
 */
public class SetUnion extends AbstractSet {

    public ISet[] sets;
    protected ISet values;

    /**
     * Constructor for unstored SetUnion. Internal storage is RANGESET by default.
     */
    public SetUnion(ISet... sets) {
        this(SetType.RANGESET, 0, sets);
    }

    /**
     * Constructor for unstored SetUnion with manual specification of set type for internal storage.
     */
    public SetUnion(SetType setType, int offset, ISet... sets) {
        this.sets = sets;
        this.values = SetFactory.makeSet(setType, offset);
        init();
    }

    /**
     * Constructor for stored SetUnion. Internal storage is RANGESET by default.
     */
    public SetUnion(Model model, ISet... sets) {
        this(model, SetType.RANGESET, 0, sets);
    }

    /**
     * Constructor for stored SetUnion with manual specification of set type for internal storage.
     */
    public SetUnion(Model model, SetType setType, int offset, ISet... sets) {
        this.sets = sets;
        this.values = SetFactory.makeStoredSet(setType, offset, model);
        init();
    }

    private void init() {
        for (int i = 0; i < sets.length; i++) {
            this.sets[i].registerObserver(this, i);
            for (int v : this.sets[i]) {
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
        boolean remove = true;
        for (int i = 0; i < sets.length; i++) {
            if (i != idx && sets[i].contains(element)) {
                remove = false;
                break;
            }
        }
        if (remove) {
            values.remove(element);
            notifyObservingElementRemoved(element);
        }
    }

    @Override
    public void notifyElementAdded(int element, int idx) {
        if (values.add(element)) {
            notifyObservingElementAdded(element);
        }
    }

    @Override
    public void notifyCleared(int idx) {
        for (int element : values) {
            boolean remove = true;
            for (ISet set : sets) {
                if (set.contains(element)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                values.remove(element);
                notifyObservingElementRemoved(element);
            }
        }
    }
}
