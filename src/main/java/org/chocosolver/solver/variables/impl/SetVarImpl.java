/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.impl;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.set.PropCardinality;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.SetDelta;
import org.chocosolver.solver.variables.delta.monitor.SetDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
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

    protected final ISet lb, ub, lbReadOnly, ubReadOnly;
    protected SetDelta delta;
    protected boolean reactOnModification;
    protected IntVar cardinality = null;

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
			if(!ub.contain(i)){
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
			if(!ub.contain(i)){
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
        return ub.getSize() == lb.getSize();
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
		if(cardinality==null){
			int ubc =  ub.getSize(), lbc = lb.getSize();
			if(ubc==lbc) cardinality = model.intVar(ubc);
			else{
				cardinality = model.intVar(name+".card", lbc, ubc);
				new Constraint("SetCard", new PropCardinality(this, cardinality)).post();
			}
		}
		return cardinality;
	}
	
	@Override
	public void setCard(IntVar card) {
		if(cardinality==null){
			cardinality=card;
			new Constraint("SetCard", new PropCardinality(this, card)).post();
		} else {
			model.arithm(cardinality, "=", card).post();
		}
	}

	@Override
    public boolean force(int element, ICause cause) throws ContradictionException {
        assert cause != null;
        if (!ub.contain(element)) {
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
        if (lb.contain(element)) {
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
        if (lb.getSize() != value.length) {
            contradiction(cause, "");
        }
        if (ub.getSize() != value.length) {
            for (int i : getUB()) {
                if (!getLB().contain(i)) {
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

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        assert cause != null;
        model.getSolver().getEngine().fails(cause, this, message);
    }
}
