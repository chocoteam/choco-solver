/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta.monitor;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.TimeStampedObject;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.IIntervalDelta;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.SafeIntProcedure;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class IntervalDeltaMonitor extends TimeStampedObject implements IIntDeltaMonitor {

    protected final IIntervalDelta delta;
    protected int first, last, frozenFirst, frozenLast;
    protected ICause propagator;

    public IntervalDeltaMonitor(IIntervalDelta delta, ICause propagator) {
		super(delta.getEnvironment());
        this.delta = delta;
        this.first = 0;
        this.last = 0;
        this.frozenFirst = 0;
        this.frozenLast = 0;
        this.propagator = propagator;
    }

    @Override
    public void freeze() {
		if (needReset()) {
            delta.lazyClear();
			this.first = this.last = 0;
			resetStamp();
		}
        assert this.getTimeStamp() == ((TimeStampedObject)delta).getTimeStamp()
                        :"Delta and monitor desynchronized. deltamonitor.freeze() is called " +
                        "but no value has been removed since the last call.";
        this.frozenFirst = first; // freeze indices
        this.frozenLast = last = delta.size();
    }

    @Override
    public void unfreeze() {
        //propagator is idempotent
        delta.lazyClear();    // fix 27/07/12
        resetStamp();
        this.first = this.last = delta.size();
    }

    @Override
    public void forEachRemVal(SafeIntProcedure proc) {
		for (int i = frozenFirst; i < frozenLast; i++) {
			if (propagator == Cause.Null || propagator != delta.getCause(i)) {
				int lb = delta.getLB(i);
				int ub = delta.getUB(i);
				for (; lb <= ub; lb++) {
					proc.execute(lb);
				}
			}
        }
    }

    @Override
    public void forEachRemVal(IntProcedure proc) throws ContradictionException {
		for (int i = frozenFirst; i < frozenLast; i++) {
			if (propagator == Cause.Null || propagator != delta.getCause(i)) {
				int lb = delta.getLB(i);
				int ub = delta.getUB(i);
				for (; lb <= ub; lb++) {
					proc.execute(lb);
				}
			}
		}
    }

    @Override
    public String toString() {
        return String.format("(%d,%d) => (%d,%d) :: %d", first, last, frozenFirst, frozenLast, delta.size());
    }

	@Override
	public int sizeApproximation(){
		return frozenLast-frozenFirst;
	}
}
