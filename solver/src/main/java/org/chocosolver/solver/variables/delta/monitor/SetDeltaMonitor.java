/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta.monitor;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.loop.TimeStampedObject;
import org.chocosolver.solver.variables.delta.ISetDelta;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetDeltaMonitor extends TimeStampedObject implements ISetDeltaMonitor {

    private final ISetDelta delta;
    private final int[] first;
    private final int[] last;
    private final ICause propagator;

    public SetDeltaMonitor(ISetDelta delta, ICause propagator) {
        super(delta.getEnvironment());
        this.delta = delta;
        this.first = new int[2];
        this.last = new int[2];
        this.propagator = propagator;
    }

    @Override
    public void startMonitoring() {
        delta.lazyClear();    // fix 27/07/12
        resetStamp();
        for (int i = 0; i < 2; i++) {
            this.first[i] = this.last[i] = delta.getSize(i);
        }
    }

    private void freeze() {
        if (getTimeStamp() == -1) {
            throw new SolverException("Delta Monitor created in this is not activated. " +
                    "This should be the last instruction of p.propagate(int) " +
                    "by calling `monitor.startMonitoring()`");
        }
        if (needReset()) {
            delta.lazyClear();
            for (int i = 0; i < 2; i++) {
                this.first[i] = 0;
            }
            resetStamp();
        }
        if (getTimeStamp() != ((TimeStampedObject) delta).getTimeStamp()) {
            throw new SolverException("Delta and monitor are not synchronized. " +
                    "\ndeltamonitor.freeze() is called " +
                    "but no value has been removed since the last call.");
        }
        for (int i = 0; i < 2; i++) {
            this.last[i] = delta.getSize(i);
        }
    }

    @Override
    public void forEach(IntProcedure proc, SetEventType evt) throws ContradictionException {
        freeze();
        int x;
        if (evt == SetEventType.ADD_TO_KER) {
            x = ISetDelta.LB;
        } else if (evt == SetEventType.REMOVE_FROM_ENVELOPE) {
            x = ISetDelta.UB;
        } else {
            throw new UnsupportedOperationException("The event in parameter should be ADD_TO_KER or REMOVE_FROM_ENVELOPE");
        }
        while (first[x] < last[x]) {
            if (delta.getCause(first[x], x) != propagator) {
                proc.execute(delta.get(first[x], x));
            }
            first[x]++;
        }
    }
}
