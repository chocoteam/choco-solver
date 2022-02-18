/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.events;

/**
 * An enum defining the set variable event types:
 * <ul>
 * <li><code>ADD_TO_KER</code>: value enforcing event,</li>
 * <li><code>REMOVE_FROM_ENVELOPE</code>: value removal event,</li>
 * </ul>
 * <p/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public enum SetEventType implements IEventType {

	VOID(0),
	ADD_TO_KER(1),
	REMOVE_FROM_ENVELOPE(2);

	private final int mask;

	SetEventType(int mask) {
		this.mask = mask;
	}

	@Override
	public int getMask() {
		return mask;
	}

	//******************************************************************************************************************
	//******************************************************************************************************************

	public static int all() {
		return ADD_TO_KER.mask+REMOVE_FROM_ENVELOPE.mask;
	}

	public static boolean isKerAddition(int mask) {
		return (mask & ADD_TO_KER.mask) != 0;
	}

	public static boolean isEnvRemoval(int mask) {
		return (mask & REMOVE_FROM_ENVELOPE.mask) != 0;
	}
}
