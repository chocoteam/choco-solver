/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.delta;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

/**
 * Delta monitor dedicated to set views over set variables.
 *
 * @author Dimitri Justeau-Allaire
 * @since 20/08/2021
 */
public abstract class SetViewOnSetsDeltaMonitor implements ISetDeltaMonitor {

    protected ISetDeltaMonitor[] deltaMonitors;
    protected ISet[] addedValues;
    protected ISet[] removedValues;

    public SetViewOnSetsDeltaMonitor(ISetDeltaMonitor... deltaMonitors) {
        this.deltaMonitors = deltaMonitors;
        addedValues = new ISet[deltaMonitors.length];
        removedValues = new ISet[deltaMonitors.length];
        for (int i = 0; i < deltaMonitors.length; i++) {
            addedValues[i] = SetFactory.makeSmallBipartiteSet();
            removedValues[i] = SetFactory.makeSmallBipartiteSet();
        }
    }

    protected void fillValues() throws ContradictionException {
        for (int i = 0; i < deltaMonitors.length; i++) {
            int finalI = i;
            addedValues[i].clear();
            removedValues[i].clear();
            deltaMonitors[i].forEach(e -> addedValues[finalI].add(e), SetEventType.ADD_TO_KER);
            deltaMonitors[i].forEach(e -> removedValues[finalI].add(e), SetEventType.REMOVE_FROM_ENVELOPE);
        }
    }

    @Override
    public void startMonitoring() {
        for (int i = 0; i < deltaMonitors.length; i++) {
            deltaMonitors[i].startMonitoring();
        }
    }
}
