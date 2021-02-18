/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import gnu.trove.set.hash.TIntHashSet;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.set.PropCardinality;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.SetDelta;
import org.chocosolver.solver.variables.delta.monitor.SetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.impl.scheduler.SetEvtScheduler;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.Set_ReadOnly;

/**
 * Set variable to represent a set of integers, i.e. a value is a set
 *
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetVarImpl extends AbstractVariable implements SetVar {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final ISet lb, ub, lbReadOnly, ubReadOnly;
    private SetDelta delta;
    private boolean reactOnModification;
    private IntVar cardinality = null;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

	/**
	 * Creates a Set variable
	 *
	 * @param name		name of the variable
	 * @param ker		initial kernel domain
	 * @param kerType	data structure of the kernel
	 * @param env		initial envelope domain
	 * @param envType	data structure of the envelope
	 * @param model	solver of the variable.
	 */
	public SetVarImpl(String name, int[] ker, SetType kerType, int[] env, SetType envType, Model model) {
		super(name, model);
		ker = new TIntHashSet(ker).toArray();
		env = new TIntHashSet(env).toArray();
		int offSet = env.length>0?env[0]:0;
		for(int i:env){
			offSet = Math.min(offSet,i);
		}
		lb = SetFactory.makeStoredSet(kerType, offSet, model);
		ub = SetFactory.makeStoredSet(envType, offSet, model);
		lbReadOnly = new Set_ReadOnly(lb);
		ubReadOnly = new Set_ReadOnly(ub);
		for(int i:env){
			ub.add(i);
		}
		for(int i:ker){
			lb.add(i);
			if(!ub.contains(i)){
				throw new UnsupportedOperationException("Invalid SetVar domain definition : "
						+i+" is in the LB but not in the UB.");
			}
		}
	}

	/**
	 * Creates a set variable, of domain <code>[lb, ub]</code>
	 * Beware : Use this constructor with caution (domain is directly accessible)
	 * lb and ub should be created properly (e.g. lb subset of ub) and should not be modified externally
	 *
	 * Both lb and ub should be backtrackable sets (stored sets): use SetFactory.makeStoredSet to build them
	 *
	 * @param name		name of the variable
	 * @param lb		lower bound of the set variable (mandatory elements)
	 * @param ub		upper bound of the set variable (potential elements)
	 * @param model	solver of the variable.
	 */
	public SetVarImpl(String name, ISet lb, ISet ub, Model model) {
		super(name, model);
		this.lb = lb;
		this.ub = ub;
		lbReadOnly = new Set_ReadOnly(lb);
		ubReadOnly = new Set_ReadOnly(ub);
		for(int i:lb){
			if(!ub.contains(i)){
				throw new UnsupportedOperationException("Invalid SetVar domain definition : "
						+i+" is in the LB but not in the UB.");
			}
		}
	}

	/**
	 * Creates a fixed Set variable, equal to <code>value</code>
	 * Beware : Use this constructor with caution (domain is directly accessible)
	 * value should be created properly and should not be modified afterward
	 *
	 * @param name		name of the variable
	 * @param value		value of the set variable
	 * @param model	solver of the variable.
	 */
	public SetVarImpl(String name, ISet value, Model model) {
		super(name, model);
		lb = value;
		ub = lb;
		lbReadOnly = new Set_ReadOnly(lb);
		ubReadOnly = new Set_ReadOnly(ub);
	}

	/**
	 * Creates a fixed Set variable, equal to <code>value</code>
	 *
	 * @param name		name of the variable
	 * @param value		value of the set variable
	 * @param model	solver of the variable.
	 */
	public SetVarImpl(String name, int[] value, Model model) {
		super(name, model);
		lb = SetFactory.makeConstantSet(new TIntHashSet(value).toArray());
		ub = lb;
		lbReadOnly = new Set_ReadOnly(lb);
		ubReadOnly = new Set_ReadOnly(ub);
	}

	//***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean isInstantiated() {
        return ub.size() == lb.size();
    }

	@Override
	public ISet getLB() {
		return lbReadOnly;
	}

	@Override
	public ISet getUB() {
		return ubReadOnly;
	}
	
	@Override
	public IntVar getCard() {
		if(!hasCard()){
			int ubc =  ub.size(), lbc = lb.size();
			if(ubc==lbc) cardinality = model.intVar(ubc);
			else{
				cardinality = model.intVar(name+".card", lbc, ubc);
				new Constraint(ConstraintsName.SETCARD, new PropCardinality(this, cardinality)).post();
			}
		}
		return cardinality;
	}
	
	@Override
	public boolean hasCard() {
            return cardinality != null;
	}
        
	@Override
	public void setCard(IntVar card) {
		if(!hasCard()){
			cardinality=card;
			new Constraint(ConstraintsName.SETCARD, new PropCardinality(this, card)).post();
		} else {
			model.arithm(cardinality, "=", card).post();
		}
	}

	@Override
    public boolean force(int element, ICause cause) throws ContradictionException {
        assert cause != null;
        if (!ub.contains(element)) {
            contradiction(cause, "");
            return true;
        }
        if (lb.add(element)) {
			if (reactOnModification) {
				delta.add(element, SetDelta.LB, cause);
			}
			SetEventType e = SetEventType.ADD_TO_KER;
			notifyPropagators(e, cause);
			return true;
		}
		return false;
    }

    @Override
    public boolean remove(int element, ICause cause) throws ContradictionException {
        assert cause != null;
        if (lb.contains(element)) {
            contradiction(cause, "");
            return true;
        }
		if (ub.remove(element)) {
			if (reactOnModification) {
				delta.add(element, SetDelta.UB, cause);
			}
			SetEventType e = SetEventType.REMOVE_FROM_ENVELOPE;
			notifyPropagators(e, cause);
			return true;
		}
        return false;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        boolean changed = !isInstantiated();
        for (int i : value) {
            force(i, cause);
        }
        if (lb.size() != value.length) {
            contradiction(cause, "");
        }
        if (ub.size() != value.length) {
            for (int i : getUB()) {
                if (!getLB().contains(i)) {
                    remove(i, cause);
                }
            }
        }
        return changed;
    }

    @Override
    public SetDelta getDelta() {
        return delta;
    }

    @Override
    public int getTypeAndKind() {
        return VAR | SET;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new SetEvtScheduler();
    }

    @Override
    public String toString() {
		if(isInstantiated()){
			return getName()+" = "+getLB().toString();
		}else {
			return getName()+" = ["+getLB()+", "+getUB()+"]";
		}
    }

    @Override
    public void createDelta() {
        if (!reactOnModification) {
            reactOnModification = true;
            delta = new SetDelta(model.getEnvironment());
        }
    }

    @Override
    public SetDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new SetDeltaMonitor(delta, propagator);
    }
}
