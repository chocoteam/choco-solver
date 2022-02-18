/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop;

import org.chocosolver.memory.IEnvironment;

/**
 * Class for factorizing code of time stamped objects
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 24/04/2014
 */
public abstract class TimeStampedObject {

	private int timestamp = -1;
	private final IEnvironment environment;

	public TimeStampedObject(IEnvironment environment) {
		this.environment = environment;
	}

	/** @return the environment */
	public final IEnvironment getEnvironment() {
		return environment;
	}

	/** @return the current time stamp of the object */
	public int getTimeStamp(){
		return timestamp;
	}

	/** @return true iff the current time stamp of the object is different from the time stamp of the environment */
	public final boolean needReset() {
		return timestamp != environment.getTimeStamp();
	}

	/** sets the current time stamp of the object to the time stamp of the environment */
	public final void resetStamp() {
		timestamp = environment.getTimeStamp();
	}
}
