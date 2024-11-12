package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.set.SetExpression;
import org.chocosolver.util.objects.setDataStructures.SetType;


/**
 * SetVarExpression - SetVar Implementation with Expression Support
 *
 * This class extends SetVar to allow arithmetic and relational operations to be performed
 * directly on set variables, facilitating composition of constraints and complex expressions
 * such as union, intersection, equality, and subset relations.
 *
 * Example:
 * setA.eq(1,2,3).post();
 * setA.union(setB).post();
 */

public class SetVarExpression extends SetVarImpl implements SetExpression {
    /**
     This class is a copy of SetVarImpl that implements the SetExpression functions.
     */

    public SetVarExpression(Model model, String name, int[] valuesLB, int[] valuesUB){
        super(name, valuesLB, SetType.BITSET, valuesUB, SetType.BITSET, model.ref());
    }

    public SetVarExpression(Model model, String name, int...domain){
        super(name, domain, model.ref());
    }
    @Override
    public SetVarImpl getSetVar(){
        return this;
    }
}