/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
 * A class to store the removed value of an integer variable.
 * <p/>
 * It defines methods to <code>add</code> a value, <code>clear</code> the structure
 * and execute a <code>Procedure</code> for each value stored.
 */
public final class EnumDelta extends TimeStampedObject implements IEnumDelta {
    private static final int SIZE = 32;

    private int[] rem;
    private ICause[] causes;
    private int last;

    public EnumDelta(IEnvironment environment) {
		super(environment);
        rem = new int[SIZE];
        causes = new ICause[SIZE];
    }

    private void ensureCapacity() {
        if (last >= rem.length) {
            int[] tmp = new int[last * 3 / 2 + 1];
            ICause[] tmpc = new ICause[last * 3 / 2 + 1];
            System.arraycopy(rem, 0, tmp, 0, last);
            System.arraycopy(causes, 0, tmpc, 0, last);
            rem = tmp;
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

    /**
     * Adds a new value to the delta
     *
     * @param value value to add
     * @param cause of the removal
     */
    @Override
    public void add(int value, ICause cause) {
		lazyClear();
        ensureCapacity();
        causes[last] = cause;
        rem[last++] = value;
    }

    @Override
    public int get(int idx) {
        return rem[idx];
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
