/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.events;

/**
 * An enum defining the propagator event types:
 * <ul>
 * <li><code>FULL_PROPAGATION</code>: Propagation from scratch (as in initial propagation),</li>
 * <li><code>CUSTOM_PROPAGATION</code>: custom propagation triggered by the developer (partially incremental propagation)</li>
 * </ul>
 * <p/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public enum PropagatorEventType implements IEventType {

	VOID(0),
	CUSTOM_PROPAGATION(1),
	FULL_PROPAGATION(2);

	private final int mask;

	PropagatorEventType(int mask) {
		this.mask = mask;
	}

	@Override
	public int getMask() {
		return mask;
	}

	//******************************************************************************************************************
	//******************************************************************************************************************

	public static boolean isFullPropagation(int mask) {
		return (mask & FULL_PROPAGATION.mask) != 0;
	}

	public static boolean isCustomPropagation(int mask) {
		return (mask & CUSTOM_PROPAGATION.mask) != 0;
	}
}
