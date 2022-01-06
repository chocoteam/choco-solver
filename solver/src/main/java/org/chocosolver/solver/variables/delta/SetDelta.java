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
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetDelta extends TimeStampedObject implements ISetDelta {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

	private final IEnumDelta[] delta;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public SetDelta(IEnvironment environment) {
        super(environment);
        delta = new IEnumDelta[2];
        delta[0] = new EnumDelta(environment);
        delta[1] = new EnumDelta(environment);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

	@Override
    public int getSize(int kerOrEnv) {
        return delta[kerOrEnv].size();
    }

	@Override
    public void add(int element, int kerOrEnv, ICause cause) {
		lazyClear();
        delta[kerOrEnv].add(element, cause);
    }

	@Override
    public void lazyClear() {
        if (needReset()) {
			delta[0].lazyClear();
			delta[1].lazyClear();
			resetStamp();
        }
    }

	@Override
    public int get(int index, int kerOrEnv) {
        return delta[kerOrEnv].get(index);
    }

	@Override
    public ICause getCause(int index, int kerOrEnv) {
        return delta[kerOrEnv].getCause(index);
    }
}
