/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.search.loop.TimeStampedObject;

/**
 * A class to store the removed intervals of an integer variable.
 * <p/>
 * It defines methods to <code>add</code> a value, <code>clear</code> the structure
 * and execute a <code>Procedure</code> for each value stored.
 */
public final class IntervalDelta extends TimeStampedObject implements IIntervalDelta {
    private static final int SIZE = 32;

    private int[] from;
    private int[] to;
    private ICause[] causes;
    private int last;

    public IntervalDelta(IEnvironment environment) {
		super(environment);
        from = new int[SIZE];
        to = new int[SIZE];
        causes = new ICause[SIZE];
    }

    private void ensureCapacity() {
        if (last >= from.length) {
            int[] tmp = new int[last * 3 / 2 + 1];
            System.arraycopy(from, 0, tmp, 0, last);
            from = tmp;
            tmp = new int[last * 3 / 2 + 1];
            System.arraycopy(to, 0, tmp, 0, last);
            to = tmp;
            ICause[] tmpc = new ICause[last * 3 / 2 + 1];
            System.arraycopy(causes, 0, tmpc, 0, last);
            causes = tmpc;
        }
    }

	@Override
    public void lazyClear() {
        if (needReset()) {
			last = 0;
			resetStamp();
        }
    }

    @Override
    public void add(int lb, int ub, ICause cause) {
		lazyClear();
        ensureCapacity();
        causes[last] = cause;
        from[last] = lb;
        to[last++] = ub;
    }

    @Override
    public int getLB(int idx) {
        return from[idx];
    }

    @Override
    public int getUB(int idx) {
        return to[idx];
    }

    @Override
    public ICause getCause(int idx) {
        return causes[idx];
    }

    @Override
    public int size() {
        return last;
    }
}
