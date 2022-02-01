/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.sat.Literalizer;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.util.ESat;

import java.util.HashSet;

/**
 * <br/>
 * CPRU r544: remove default implementation
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public interface BoolVar extends IntVar, ILogical, ReExpression {

    int kFALSE = 0;
    int kTRUE = 1;
    int kUNDEF = 2;

    ESat getBooleanValue();

    boolean setToTrue(ICause cause) throws ContradictionException;

    boolean setToFalse(ICause cause) throws ContradictionException;

    BoolVar not();

	boolean hasNot();

    void _setNot(BoolVar not);

    @Override
    default IntVar intVar() {
        return boolVar();
    }

    @Override
    default BoolVar boolVar(){
        return this;
    }

    @Override
    default void extractVar(HashSet<IntVar> variables){
        variables.add(this);
    }

    /**
     * Creates, or returns if already existing, the SAT variable twin of this.
     * @return the SAT variable of this
     */
    default int satVar() {
        return this.getModel().satVar(this, new Literalizer.BoolLit(this));
    }
}
