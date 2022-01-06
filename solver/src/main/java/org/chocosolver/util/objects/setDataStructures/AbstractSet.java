/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */

/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures;

/**
 * Abstract class for sets, implementing the basic functionalities for observable sets.
 *
 * @author Dimitri Justeau-Allaire
 * @since 30/03/2021
 */
public abstract class AbstractSet implements ISet {

    ISet[] observing;
    int[] idxInObserving;
    int obsIdx;

    public AbstractSet() {
        this.observing = new ISet[2];
        this.idxInObserving = new int[2];
        this.obsIdx = 0;
    }

    @Override
    public void registerObserver(ISet set, int idx) {
        if (obsIdx == observing.length) {
            ISet[] tmp = observing;
            int[] tmpIdx = idxInObserving;
            observing = new ISet[tmp.length * 3 / 2 + 1];
            idxInObserving = new int[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, observing, 0, obsIdx);
            System.arraycopy(tmpIdx, 0, idxInObserving, 0, obsIdx);
        }
        observing[obsIdx] = set;
        idxInObserving[obsIdx] = idx;
        obsIdx++;
    }

    protected void notifyObservingElementAdded(int element) {
        for (int i = 0; i < obsIdx; i++) {
            observing[i].notifyElementAdded(element, idxInObserving[i]);
        }
    }

    protected void notifyObservingElementRemoved(int element) {
        for (int i = 0; i < obsIdx; i++) {
            observing[i].notifyElementRemoved(element, idxInObserving[i]);
        }
    }

    protected void notifyObservingCleared() {
        for (int i = 0; i < obsIdx; i++) {
            observing[i].notifyCleared(idxInObserving[i]);
        }
    }

    protected void notifyObservingAddedBetween(int from, int to) {
        for (int i = 0; i < obsIdx; i++) {
            for (int e = from; e <= to; e++) {
                observing[i].notifyElementAdded(e, idxInObserving[i]);
            }
        }
    }

    protected void notifyObservingRemovedBetween(int from, int to) {
        for (int i = 0; i < obsIdx; i++) {
            for (int e = from; e <= to; e++) {
                observing[i].notifyElementRemoved(e, idxInObserving[i]);
            }
        }
    }

    protected void notifyObservingRetainedBetween(int from, int to) {
        for (int i = 0; i < obsIdx; i++) {
            observing[i].notifyCleared(idxInObserving[i]);
            for (int e = from; e <= to; e++) {
                observing[i].notifyElementAdded(e, idxInObserving[i]);
            }
        }
    }

    protected void notifyObservingFullUpdate() {
        if (obsIdx ==  0) {
            return;
        }
        int[] values = this.toArray();
        for (int i = 0; i < obsIdx; i++) {
            observing[i].notifyCleared(idxInObserving[i]);
            for (int e : values) {
                observing[i].notifyElementAdded(e, idxInObserving[i]);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("{");
        ISetIterator iter = newIterator();
        while (iter.hasNext()) {
            st.append(iter.nextInt()).append(", ");
        }
        st.append("}");
        return st.toString().replace(", }","}");
    }
}
