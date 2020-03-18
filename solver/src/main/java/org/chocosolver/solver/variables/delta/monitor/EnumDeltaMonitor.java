/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.SafeIntProcedure;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class EnumDeltaMonitor extends TimeStampedObject implements IIntDeltaMonitor {

    private final IEnumDelta delta;
    private int first, frozenFirst, frozenLast;
    private final ICause propagator;

    public EnumDeltaMonitor(IEnumDelta delta, ICause propagator) {
		super(delta.getEnvironment());
        this.delta = delta;
        this.first = 0;
        this.frozenFirst = 0;
        this.frozenLast = 0;
        this.propagator = propagator;
    }

    @Override
    public void freeze() {
		if (needReset()) {
            delta.lazyClear();
			this.first = 0;
			resetStamp();
		}
        this.frozenFirst = first; // freeze indices
        this.frozenLast = delta.size();
    }

    @Override
    public void unfreeze() {
        //propagator is idempotent
        delta.lazyClear();    // fix 27/07/12
        resetStamp();
        this.first = delta.size();
    }

    @Override
    public void forEachRemVal(SafeIntProcedure proc) {
		for (int i = frozenFirst; i < frozenLast; i++) {
			if (propagator == Cause.Null || propagator != delta.getCause(i)) {
				proc.execute(delta.get(i));
			}
		}
    }

    @Override
    public void forEachRemVal(IntProcedure proc) throws ContradictionException {
		for (int i = frozenFirst; i < frozenLast; i++) {
			if (propagator == Cause.Null || propagator != delta.getCause(i)) {
				proc.execute(delta.get(i));
			}
		}
    }

    @Override
    public String toString() {
        return String.format("(%d,last) => (%d,%d) :: %d", first, frozenFirst, frozenLast, delta.size());
    }

	@Override
	public int sizeApproximation(){
		return frozenLast-frozenFirst;
	}
}
