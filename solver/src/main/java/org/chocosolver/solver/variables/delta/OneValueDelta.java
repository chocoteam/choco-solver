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
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class OneValueDelta extends TimeStampedObject implements IEnumDelta {

    private int value;
    private ICause cause;
    private boolean set;

    public OneValueDelta(IEnvironment environment) {
        super(environment);
    }

	@Override
    public void lazyClear() {
        if (needReset()) {
            set = false;
            resetStamp();
        }
    }

    @Override
    public void add(int value, ICause cause) {
		lazyClear();
        this.value = value;
        this.cause = cause;
        set = true;
    }

    @Override
    public int get(int idx) {
        if (idx < 1) {
            return value;
        } else {
            throw new IndexOutOfBoundsException("OneValueDelta#get(): size must be checked before!");
        }
    }

    @Override
    public ICause getCause(int idx) {
        if (idx < 1) {
            return cause;
        } else {
            throw new IndexOutOfBoundsException("OneValueDelta#get(): size must be checked before!");
        }
    }

    @Override
    public int size() {
        return set ? 1 : 0;
    }
}
