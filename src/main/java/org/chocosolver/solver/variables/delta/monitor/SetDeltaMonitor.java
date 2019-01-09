/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta.monitor;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
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

    protected final ISetDelta delta;

    protected int[] first, last; // references, in variable delta value to propagate, to un propagated values
    protected int[] frozenFirst, frozenLast; // same as previous while the recorder is frozen, to allow "concurrent modifications"
    protected ICause propagator;

    public SetDeltaMonitor(ISetDelta delta, ICause propagator) {
		super(delta.getEnvironment());
        this.delta = delta;
        this.first = new int[2];
        this.last = new int[2];
        this.frozenFirst = new int[2];
        this.frozenLast = new int[2];
        this.propagator = propagator;
    }

    @Override
    public void freeze() {
		if (needReset()) {
            delta.lazyClear();
			for (int i = 0; i < 2; i++) {
				this.first[i] = last[i] = 0;
			}
			resetStamp();
		}
        assert this.getTimeStamp() == ((TimeStampedObject)delta).getTimeStamp()
                        :"Delta and monitor desynchronized. deltamonitor.freeze() is called " +
                        "but no value has been removed since the last call.";
        for (int i = 0; i < 2; i++) {
            this.frozenFirst[i] = first[i]; // freeze indices
            this.first[i] = this.frozenLast[i] = last[i] = delta.getSize(i);
        }
    }

    @Override
    public void unfreeze() {
        delta.lazyClear();    // fix 27/07/12
        resetStamp();
        for (int i = 0; i < 2; i++) {
            this.first[i] = last[i] = delta.getSize(i);
        }
    }

    @Override
    public void forEach(IntProcedure proc, SetEventType evt) throws ContradictionException {
        int x;
        if (evt == SetEventType.ADD_TO_KER) {
            x = ISetDelta.LB;
        } else if (evt == SetEventType.REMOVE_FROM_ENVELOPE) {
            x = ISetDelta.UB;
        } else {
            throw new UnsupportedOperationException("The event in parameter should be ADD_TO_KER or REMOVE_FROM_ENVELOPE");
        }
        for (int i = frozenFirst[x]; i < frozenLast[x]; i++) {
            if (delta.getCause(i, x) != propagator) {
                proc.execute(delta.get(i, x));
            }
        }
    }
}
