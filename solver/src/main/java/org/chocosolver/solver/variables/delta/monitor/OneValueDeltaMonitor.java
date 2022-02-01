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
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.SafeIntProcedure;

/**
 * A monitor for OneValueDelta
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class OneValueDeltaMonitor extends TimeStampedObject implements IIntDeltaMonitor {

    protected final IEnumDelta delta;
    protected boolean used;
    protected ICause propagator;

    public OneValueDeltaMonitor(IEnumDelta delta, ICause propagator) {
        super(delta.getEnvironment());
        this.delta = delta;
        this.used = false;
        this.propagator = propagator;
    }

    @Override
    public void startMonitoring() {
        delta.lazyClear();    // fix 27/07/12
        resetStamp();
        this.used = false;
    }

    private void freeze() {
        if (getTimeStamp() == -1) {
            throw new SolverException("Delta Monitor created in this is not activated. " +
                    "This should be the last instruction of p.propagate(int) " +
                    "by calling `monitor.startMonitoring()`");
        }
        if (needReset()) {
            delta.lazyClear();
            used = false;
            resetStamp();
        }
        if (getTimeStamp() != ((TimeStampedObject) delta).getTimeStamp()) {
            throw new SolverException("Delta and monitor are not synchronized. " +
                    "\ndeltamonitor.freeze() is called " +
                    "but no value has been removed since the last call.");
        }
        used = delta.size() == 1;
    }

    @Override
    public void forEachRemVal(SafeIntProcedure proc) {
        freeze();
        if (used && propagator != delta.getCause(0)) {
            proc.execute(delta.get(0));
        }
        used = false;
    }

    @Override
    public void forEachRemVal(IntProcedure proc) throws ContradictionException {
        freeze();
        if (used && propagator != delta.getCause(0)) {
            proc.execute(delta.get(0));
        }
        used = false;
    }

    @Override
    public int sizeApproximation() {
        return used && propagator != delta.getCause(0) ? 1 : 0;
    }
}
