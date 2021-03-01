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

public class GraphDelta extends TimeStampedObject implements IDelta {

	//NR NE AR AE : NodeRemoved NodeEnforced ArcRemoved ArcEnforced
	public final static int NR = 0;
	public final static int NE = 1;
	public final static int AR_TAIL = 2;
	public final static int AR_HEAD = 3;
	public final static int AE_TAIL = 4;
	public final static int AE_HEAD = 5;
	public final static int NB = 6;

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private IEnumDelta[] deltaOfType;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public GraphDelta(IEnvironment environment) {
		super(environment);
		deltaOfType = new IEnumDelta[NB];
		for (int i = 0; i < NB; i++) {
			deltaOfType[i] = new EnumDelta(environment);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public int getSize(int i) {
		return deltaOfType[i].size();
	}

	public void add(int element, int type, ICause cause) {
		lazyClear();
		deltaOfType[type].add(element, cause);
	}

	public void lazyClear() {
		if (needReset()) {
			for (int i = 0; i < NB; i++) {
				deltaOfType[i].lazyClear();
			}
			resetStamp();
		}
	}

	public int get(int index, int type) {
		return deltaOfType[type].get(index);
	}

	public ICause getCause(int index, int type) {
		return deltaOfType[type].getCause(index);
	}
}
