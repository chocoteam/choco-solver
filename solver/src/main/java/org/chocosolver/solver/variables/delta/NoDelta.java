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
import org.chocosolver.solver.exception.SolverException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 10/02/11
 */
public enum NoDelta implements IEnumDelta, IIntervalDelta, ISetDelta {
    singleton;

    @Override
    public void add(int value, ICause cause) {}

    @Override
    public void lazyClear() {}

    @Override
    public IEnvironment getEnvironment() {
        throw new SolverException("NoDelta#getEnvironment(): forbidden call!");
    }

    @Override
    public void add(int lb, int ub, ICause cause) {}

    @Override
    public int getLB(int idx) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("NoDelta#getLB(): forbidden call, size must be checked before!");
    }

    @Override
    public int getUB(int idx) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("NoDelta#getUB(): forbidden call, size must be checked before!");
    }

    @Override
    public int get(int idx) {
        throw new IndexOutOfBoundsException("NoDelta#get(): forbidden call, size must be checked before!");
    }

    @Override
    public ICause getCause(int idx) {
        throw new IndexOutOfBoundsException("NoDelta#getCause(): forbidden call, size must be checked before!");
    }

    @Override
    public int size() {
        return 0;
    }

	@Override
	public int getSize(int kerOrEnv) {
		return 0;
	}

	@Override
	public int get(int index, int kerOrEnv) {
		throw new IndexOutOfBoundsException("NoDelta#get(): forbidden call, size must be checked before!");
	}

	@Override
	public ICause getCause(int index, int kerOrEnv) {
		throw new IndexOutOfBoundsException("NoDelta#getCause(): forbidden call, size must be checked before!");
	}
}
