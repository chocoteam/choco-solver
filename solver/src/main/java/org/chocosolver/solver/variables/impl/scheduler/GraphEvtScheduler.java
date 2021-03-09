/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.scheduler;

import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.iterators.EvtScheduler;

public class GraphEvtScheduler implements EvtScheduler<GraphEventType> {

	private boolean done = true;

	@Override
	public void init(int mask) {
		done = false;
	}

	@Override
	public int select(int mask) {
		return 0;
	}

	@Override
	public boolean hasNext() {
		return !done;
	}

	@Override
	public int next() {
		if (done) return 1;
		done = true;
		return 0;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
