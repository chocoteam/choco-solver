/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
    private int first, last;
    private final ICause propagator;

    public EnumDeltaMonitor(IEnumDelta delta, ICause propagator) {
        super(delta.getEnvironment());
        this.delta = delta;
        this.first = 0;
        this.last = 0;
        this.propagator = propagator;
    }

    private void freeze() {
        if (needReset()) {
            delta.lazyClear();
            this.first = 0;
            this.last = 0;
            resetStamp();
        }
        this.first = this.last;
        this.last = delta.size();
    }

    @Override
    public void forEachRemVal(SafeIntProcedure proc) {
        freeze();
        while (first < last) {
            if (propagator == Cause.Null || propagator != delta.getCause(first)) {
                proc.execute(delta.get(first));
            }
            first++;
        }
    }

    @Override
    public void forEachRemVal(IntProcedure proc) throws ContradictionException {
        freeze();
        while (first < last) {
            if (propagator == Cause.Null || propagator != delta.getCause(first)) {
                proc.execute(delta.get(first));
            }
            first++;
        }
    }

    @Override
    public String toString() {
        return String.format("(%d,%d) :: %d", first, last, delta.size());
    }

    @Override
    public int sizeApproximation() {
        return last - first;
    }
}
