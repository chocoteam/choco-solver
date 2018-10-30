/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.HashSet;

/**
 * <br/>
 * CPRU r544: remove default implementation
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public interface BoolVar extends IntVar, ILogical, ReExpression {

    ESat getBooleanValue();

    boolean setToTrue(ICause cause) throws ContradictionException;

    boolean setToFalse(ICause cause) throws ContradictionException;

    BoolVar not();

	boolean hasNot();

    void _setNot(BoolVar not);

    default void explain(ExplanationForSignedClause clause,
                         ValueSortedMap<IntVar> front,
                         Implications ig,
                         int p) {
        IntVar pivot = ig.getIntVarAt(p);
        int val = 1 - this.getValue();
        IntIterableRangeSet set0 = clause.getComplementSet(this);
        set0.retainBetween(val, val);
        clause.addLiteral(this, set0, false);
        IntIterableRangeSet set1 = clause.getComplementSet(pivot);
        set1.retainBetween(val, val);
        clause.addLiteral(pivot, set1, true);

    }

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
}
