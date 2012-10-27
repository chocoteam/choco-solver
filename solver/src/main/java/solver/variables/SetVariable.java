/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables;

import choco.kernel.memory.IEnvironment;
import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.delta.*;
import solver.variables.delta.monitor.SetDeltaMonitor;
import solver.variables.setDataStructures.ISet;
import solver.variables.setDataStructures.SetFactory;
import solver.variables.setDataStructures.SetType;
import solver.variables.view.IView;


public abstract class SetVariable extends AbstractVariable<SetDelta, SetDeltaMonitor, IView, SetVar> implements SetVar {

    //////////////////////////////// GRAPH PART /////////////////////////////////////////
	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected ISet envelop, kernel;
	protected IEnvironment environment;
	protected SetDelta delta;
	///////////// Attributes related to Variable ////////////
	protected boolean reactOnModification;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public SetVariable(String name, Solver solver) {
		this(name,0,SetType.LINKED_LIST,SetType.LINKED_LIST,solver);
	}

	public SetVariable(String name, int maximalSize, SetType envType, SetType kerType, Solver solver) {
		super(name, solver);
		solver.associates(this);
		this.environment = solver.getEnvironment();
		envelop = SetFactory.makeStoredSet(envType, maximalSize, environment);
		kernel = SetFactory.makeStoredSet(kerType,maximalSize,environment);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean instantiated() {
		return envelop.getSize() == kernel.getSize();
	}

	@Override
    public boolean addToKernel(int value, ICause cause) throws ContradictionException {
		if(!envelop.contain(value)){
			contradiction(cause,null,"");
		}
		if(!kernel.contain(value)){
			kernel.add(value);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeFromEnveloppe(int value, ICause cause) throws ContradictionException {
		if(kernel.contain(value)){
			contradiction(cause,null,"");
		}
		if(envelop.contain(value)){
			envelop.remove(value);
			return true;
		}
		return false;
	}

	@Override
	public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
		boolean changed = !instantiated();
		for(int i:value){
			addToKernel(i,cause);
		}
		if(kernel.getSize()!=value.length){
			contradiction(cause,null,"");
		}
		if(envelop.getSize()!=value.length){
			for(int i=envelop.getFirstElement();i>=0;i=envelop.getNextElement()){
				if(!kernel.contain(i)){
					envelop.remove(i);
				}
			}
		}
		return changed;
	}

	@Override
	public boolean contains(int v) {
		return envelop.contain(v);
	}

	@Override
	public int[] getValue() {
		int[] lb = new int[kernel.getSize()];
		int k = 0;
		for(int i=kernel.getFirstElement();i>=0;i=kernel.getNextElement()){
			lb[k++] = i;
		}
		return lb;
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	public ISet getKernel() {
		return kernel;
	}

	public ISet getEnvelop() {
		return envelop;
	}

	//***********************************************************************************
	// VARIABLE STUFF
	//***********************************************************************************

	@Override
	public Explanation explain(VariableState what) {
		throw new UnsupportedOperationException("GraphVar does not (yet) implement method explain(...)");
	}

	@Override
	public Explanation explain(VariableState what, int val) {
		throw new UnsupportedOperationException("GraphVar does not (yet) implement method explain(...)");
	}

	@Override
	public SetDelta getDelta() {
		return delta;
	}

	@Override
	public int getTypeAndKind() {
		return VAR + SET;
	}

	@Override
	public String toString() {
		return getName();
	}

    @Override
    public void createDelta() {
        if (!reactOnModification) {
			reactOnModification = true;
			delta = new SetDelta(solver.getSearchLoop());
		}
    }

    @Override
    public SetDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new SetDeltaMonitor(delta, propagator);
    }

	public void notifyPropagators(EventType event, @NotNull ICause cause) throws ContradictionException {
        notifyMonitors(event, cause);
        if ((modificationEvents & event.mask) != 0) {
            //records.forEach(afterModification.set(this, event, cause));
            solver.getEngine().onVariableUpdate(this, event, cause);
        }
        notifyViews(event, cause);
    }

    public void notifyMonitors(EventType event, @NotNull ICause cause) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event, cause);
        }
	}

	@Override
	public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
		solver.getEngine().fails(cause, this, message);
	}
}
