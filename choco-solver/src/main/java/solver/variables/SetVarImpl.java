/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables;

import memory.IEnvironment;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.delta.SetDelta;
import solver.variables.delta.monitor.SetDeltaMonitor;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;
import util.tools.StringUtils;

import java.util.BitSet;

/**
 * Set variable to represent a set of integers in the range [0,n-1]
 *
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetVarImpl extends AbstractVariable implements SetVar {

    //////////////////////////////// GRAPH PART /////////////////////////////////////////
    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected ISet envelope, kernel;
    protected IEnvironment environment;
    protected SetDelta delta;
	protected int min, max;
    ///////////// Attributes related to Variable ////////////
    protected boolean reactOnModification;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

	/**
	 * Creates a Set variable
	 *
	 * @param name		name of the variable
	 * @param env		initial envelope domain
	 * @param envType	data structure of the envelope
	 * @param ker		initial kernel domain
	 * @param kerType	data structure of the kernel
	 * @param solver	solver of the variable.
	 */
	protected SetVarImpl(String name, int[] env, SetType envType, int[] ker, SetType kerType, Solver solver) {
		super(name, solver);
		solver.associates(this);
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for(int i:env){
			if(i==Integer.MIN_VALUE || i==Integer.MAX_VALUE){
				throw new UnsupportedOperationException("too large (infinite) integers within the set variable. " +
						"Integer.MIN_VALUE and i==Integer.MAX_VALUE are not handled.");
			}
			min = Math.min(min,i);
			max = Math.max(max,i);
		}
		check(env,ker,max,min);
		this.environment = solver.getEnvironment();
		envelope = SetFactory.makeStoredSet(envType, max-min+1, environment);
		kernel = SetFactory.makeStoredSet(kerType, max-min+1, environment);
		for(int i:env){
			envelope.add(i-min);
		}
		for(int i:ker){
			kernel.add(i-min);
		}
		this.min = min;
		this.max = max;
	}

	/**
	 * Creates a Set variable
	 *
	 * @param name		name of the variable
	 * @param min		first envelope value
	 * @param max		last envelope value
	 * @param solver	solver of the variable.
	 */
	protected SetVarImpl(String name, int min, int max, Solver solver) {
		super(name, solver);
		solver.associates(this);
		this.environment = solver.getEnvironment();
		envelope = SetFactory.makeStoredSet(SetType.BITSET, max-min+1, environment);
		kernel = SetFactory.makeStoredSet(SetType.BITSET, max-min+1, environment);
		for(int i=min; i<=max; i++){
			envelope.add(i-min);
		}
		this.min = min;
		this.max = max;
	}

	private static void check(int[] env, int[] ker, int max, int min) {
		BitSet b = new BitSet(max-min);
		for(int i:env){
			if(b.get(i-min)){
				throw new UnsupportedOperationException("Invalid envelope definition. "+i+" is added twice.");
			}b.set(i-min);
		}
		for(int i:ker){
			if(!b.get(i-min)){
				throw new UnsupportedOperationException("Invalid envelope/kernel definition. "
						+i+" is in the kernel but not in the envelope.");
			}
		}
	}

	//***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean instantiated() {
        return envelope.getSize() == kernel.getSize();
    }

	@Override
    public boolean addToKernel(int element, ICause cause) throws ContradictionException {
        assert cause != null;
        if (element<min || element>max || !envelope.contain(element-min)) {
            contradiction(cause, null, "");
            return true;
        }
        if (kernel.contain(element-min)) {
            return false;
        }
        kernel.add(element-min);
        if (reactOnModification) {
            delta.add(element, SetDelta.KERNEL, cause);
        }
        EventType e = EventType.ADD_TO_KER;
        notifyPropagators(e, cause);
        return true;
    }

    @Override
    public boolean removeFromEnvelope(int element, ICause cause) throws ContradictionException {
        assert cause != null;
		if(element<min || element>max)return false;
        if (kernel.contain(element-min)) {
            contradiction(cause, EventType.REMOVE_FROM_ENVELOPE, "");
            return true;
        }
        if (!envelope.remove(element-min)) {
            return false;
        }
        if (reactOnModification) {
            delta.add(element, SetDelta.ENVELOP, cause);
        }
        EventType e = EventType.REMOVE_FROM_ENVELOPE;
        notifyPropagators(e, cause);
        return true;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        boolean changed = !instantiated();
        for (int i : value) {
            addToKernel(i, cause);
        }
        if (kernel.getSize() != value.length) {
            contradiction(cause, null, "");
        }
        if (envelope.getSize() != value.length) {
            for (int i = getEnvelopeFirst(); i != END; i = getEnvelopeNext()) {
                if (!kernelContains(i)) {
                    removeFromEnvelope(i, cause);
                }
            }
        }
        return changed;
    }

    @Override
    public int[] getValue() {
        int[] lb = new int[kernel.getSize()];
        int k = 0;
        for (int i = kernel.getFirstElement(); i >= 0; i = kernel.getNextElement()) {
            lb[k++] = i+min;
        }
        return lb;
    }

    //***********************************************************************************
    // ITERATIONS
    //***********************************************************************************

	@Override
	public int getKernelFirst() {
		int i = kernel.getFirstElement();
		return (i==-1)?END:i+min;
	}

	@Override
	public int getKernelNext() {
		int i = kernel.getNextElement();
		return (i==-1)?END:i+min;
	}

	@Override
	public int getKernelSize(){
		return kernel.getSize();
	}

	@Override
	public boolean kernelContains(int i){
		if(i<min || i>max)return false;
		return kernel.contain(i-min);
	}

	@Override
	public int getEnvelopeFirst() {
		int i = envelope.getFirstElement();
		return (i==-1)?END:i+min;
	}

	@Override
	public int getEnvelopeNext() {
		int i = envelope.getNextElement();
		return (i==-1)?END:i+min;
	}

	@Override
	public int getEnvelopeSize(){
		return envelope.getSize();
	}

	@Override
	public boolean envelopeContains(int i){
		if(i<min || i>max)return false;
		return envelope.contain(i-min);
	}

	//***********************************************************************************
    // VARIABLE STUFF
    //***********************************************************************************

    @Override
    public void explain(VariableState what, Explanation to) {
        throw new UnsupportedOperationException("SetVar does not (yet) implement method explain(...)");
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        throw new UnsupportedOperationException("SetVar does not (yet) implement method explain(...)");
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
    public SetVar duplicate() {
		int[] env = new int[getEnvelopeSize()];
		int idx=0;
		for(int i=getEnvelopeFirst();i!=END;i=getEnvelopeNext()){
			env[idx++] = i;
		}
		int[] ker = new int[getKernelSize()];
		idx=0;
		for(int i=getKernelFirst();i!=END;i=getKernelNext()){
			ker[idx++] = i;
		}
		return new SetVarImpl(StringUtils.randomName(this.name),env,envelope.getSetType(),ker,kernel.getSetType(),solver);
    }

    @Override
    public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(" Envelope : {");
		int s = envelope.getSize();
		for(int i=envelope.getFirstElement();i>=0;i=envelope.getNextElement()){
			sb.append(i+min);
			s--;
			if(s>0){
				sb.append(",");
			}
		}
		sb.append("} Kernel : {");
		s = kernel.getSize();
		for(int i=kernel.getFirstElement();i>=0;i=kernel.getNextElement()){
			sb.append(i+min);
			s--;
			if(s>0){
				sb.append(",");
			}
		}
		sb.append("}");
        return sb.toString();
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

	@Override
    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        notifyMonitors(event);
        if ((modificationEvents & event.mask) != 0) {
            //records.forEach(afterModification.set(this, event, cause));
            solver.getEngine().onVariableUpdate(this, event, cause);
        }
        notifyViews(event, cause);
    }

	@Override
    public void notifyMonitors(EventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        assert cause != null;
        solver.getEngine().fails(cause, this, message);
    }
}
