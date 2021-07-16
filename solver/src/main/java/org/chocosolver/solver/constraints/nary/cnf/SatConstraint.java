/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cnf;

import org.chocosolver.solver.constraints.nary.sat.PropSat;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 nov. 2010
 */
public class SatConstraint extends Constraint {

	private final PropSat miniSat;

	public SatConstraint(Model model) {
		super(ConstraintsName.SATCONSTRAINT,new PropSat(model));
		miniSat = (PropSat) propagators[0];
	}

	@Override
	public ESat isSatisfied() {
		ESat so = ESat.UNDEFINED;
		for (int i = 0; i < propagators.length; i++) {
			so = propagators[i].isEntailed();
			if (!so.equals(ESat.TRUE)) {
				return so;
			}
		}
		return so;
	}

	public PropSat getPropSat() {
		return miniSat;
	}
}
