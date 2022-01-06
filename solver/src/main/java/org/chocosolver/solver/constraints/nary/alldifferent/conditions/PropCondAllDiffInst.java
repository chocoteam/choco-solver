/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffInst;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Propagator for ConditionnalAllDifferent that only reacts on instantiation
 *
 * @author Jean-Guillaume Fages
 */
public class PropCondAllDiffInst extends PropAllDiffInst {

	private final Condition condition;
	private final boolean mode;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * ConditionnalAllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
	 *
	 * IF mode
	 * 	for all X in vars, condition(X) => X != Y, for all Y in vars
	 * ELSE
	 * 	for all X,Y in vars, condition(X) AND condition(Y) => X != Y
     *
     * @param variables array of integer variables
	 * @param c a condition to define the subset of variables subject to the AllDiff cstr
	 * @param mode defines how to apply filtering
     */
    public PropCondAllDiffInst(IntVar[] variables, Condition c, boolean mode) {
        super(variables);
		this.condition = c;
		this.mode = mode;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

	protected void fixpoint() throws ContradictionException {
		while (toCheck.size() > 0) {
			int vidx = toCheck.pop();
			if(condition.holdOnVar(vars[vidx])){
				int val = vars[vidx].getValue();
				for (int i = 0; i < n; i++) {
					if (i != vidx && (mode || condition.holdOnVar(vars[i]))) {
						if (vars[i].removeValue(val, this)) {
							if (vars[i].isInstantiated()) {
								toCheck.push(i);
							}
						}
					}
				}
			}
		}
	}

    @Override
    public ESat isEntailed() {
		int nbInst = 0;
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].isInstantiated()) {
				nbInst++;
				if(condition.holdOnVar(vars[i])){
					for (int j = i + 1; j < vars.length; j++) {
						if(condition.holdOnVar(vars[j]))
							if (vars[j].isInstantiated() && vars[i].getValue() == vars[j].getValue()) {
								return ESat.FALSE;
							}
					}
				}
			}
		}
		if (nbInst == vars.length) {
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
    }
}
