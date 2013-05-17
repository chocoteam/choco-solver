/**
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

package solver.variables.fast;

import memory.IEnvironment;
import memory.IStateBitSet;
import memory.IStateInt;
import solver.Cause;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.explanations.antidom.AntiDomBitset;
import solver.explanations.antidom.AntiDomain;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.EnumDelta;
import solver.variables.delta.IEnumDelta;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import solver.variables.delta.monitor.EnumDeltaMonitor;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueIterator;
import util.tools.StringUtils;

/**
 * <br/>IntVar implementation for quite small domains bit with very distant values e.g. {-51900,42,235923}
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 14/05/2013
 */
public final class BitsetArrayIntVarImpl extends AbstractVariable<IEnumDelta, IntVar<IEnumDelta>> implements IntVar<IEnumDelta> {

	private static final long serialVersionUID = 1L;

	protected boolean reactOnRemoval = false;

	//  values
	private final int[] values;
	//  Bitset of indexes
	private final IStateBitSet indexes;
	// Lower bound of the current domain
	private final IStateInt LB;
	// Upper bound of the current domain
	private final IStateInt UB;
	// Size of the current domain
	private final IStateInt SIZE;
	//offset of the lower bound and the first value in the domain
	private final int LENGTH;

	private IEnumDelta delta = NoDelta.singleton;

	private DisposableValueIterator _viterator;
	private DisposableRangeIterator _riterator;

	//////////////////////////////////////////////////////////////////////////////////////

	public BitsetArrayIntVarImpl(String name, int[] sortedValues, Solver solver) {
		super(name, solver);
		solver.associates(this);
		IEnvironment env = solver.getEnvironment();
		this.LENGTH = sortedValues.length;
		this.values = sortedValues.clone();
		this.indexes = env.makeBitSet(LENGTH);
		this.indexes.set(0,LENGTH);
		this.LB = env.makeInt(0);
		this.UB = env.makeInt(LENGTH-1);
		this.SIZE = env.makeInt(LENGTH);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Removes <code>value</code>from the domain of <code>this</code>. The instruction comes from <code>propagator</code>.
	 * <ul>
	 * <li>If <code>value</code> is out of the domain, nothing is done and the return value is <code>false</code>,</li>
	 * <li>if removing <code>value</code> leads to a dead-end (domain wipe-out),
	 * a <code>ContradictionException</code> is thrown,</li>
	 * <li>otherwise, if removing <code>value</code> from the domain can be done safely,
	 * the event type is created (the original event can be promoted) and observers are notified
	 * and the return value is <code>true</code></li>
	 * </ul>
	 *
	 * @param value value to remove from the domain (int)
	 * @param cause removal releaser
	 * @return true if the value has been removed, false otherwise
	 * @throws solver.exception.ContradictionException
	 *          if the domain become empty due to this action
	 */
	public boolean removeValue(int value, ICause cause) throws ContradictionException {
		// BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
//        records.forEach(beforeModification.set(this, EventType.REMOVE, cause));
		assert cause != null;
		ICause antipromo = cause;
		if(value<values[LB.get()] || value>values[UB.get()]){
			return false;
		}
		int index = -1;
		for (int i=indexes.nextSetBit(LB.get()); i>=0 && values[i]<=value; i=indexes.nextSetBit(i + 1)) {
			if(values[i]==value){
				index = i;
				break;
			}
		}
		if(index!=-1){
			if (SIZE.get() == 1) {
				if (Configuration.PLUG_EXPLANATION){
					solver.getExplainer().removeValue(this, value, antipromo);
				}
				//            monitors.forEach(onContradiction.set(this, EventType.REMOVE, cause));
				this.contradiction(cause, EventType.REMOVE, MSG_REMOVE);
			}
			EventType e = EventType.REMOVE;
			this.indexes.clear(index);
			this.SIZE.add(-1);
			if (reactOnRemoval) {
				delta.add(value, cause);
			}
			if (value == getLB()) {
				LB.set(indexes.nextSetBit(LB.get()));
				e = EventType.INCLOW;
				if (cause.reactOnPromotion()) {
					cause = Cause.Null;
				}
			} else if (value == getUB()) {
				UB.set(indexes.prevSetBit(UB.get()));
				e = EventType.DECUPP;
				if (cause.reactOnPromotion()) {
					cause = Cause.Null;
				}
			}
			assert !indexes.isEmpty();
			if (this.instantiated()) {
				e = EventType.INSTANTIATE;
				if (cause.reactOnPromotion()) {
					cause = Cause.Null;
				}
			}
			this.notifyPropagators(e, cause);
			if (Configuration.PLUG_EXPLANATION){
				solver.getExplainer().removeValue(this, value, antipromo);
			}
			return true;
		}else{
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
		assert cause != null;
		if (from <= getLB())
			return updateLowerBound(to + 1, cause);
		else if (getUB() <= to)
			return updateUpperBound(from - 1, cause);
		else {     // TODO: really ugly .........
			boolean anyChange = false;
			for (int v = this.nextValue(from - 1); v <= to; v = nextValue(v)) {
				anyChange |= removeValue(v, cause);
			}
			return anyChange;
		}
	}

	/**
	 * Instantiates the domain of <code>this</code> to <code>value</code>. The instruction comes from <code>propagator</code>.
	 * <ul>
	 * <li>If the domain of <code>this</code> is already instantiated to <code>value</code>,
	 * nothing is done and the return value is <code>false</code>,</li>
	 * <li>If the domain of <code>this</code> is already instantiated to another value,
	 * then a <code>ContradictionException</code> is thrown,</li>
	 * <li>Otherwise, the domain of <code>this</code> is restricted to <code>value</code> and the observers are notified
	 * and the return value is <code>true</code>.</li>
	 * </ul>
	 *
	 * @param value instantiation value (int)
	 * @param cause instantiation releaser
	 * @return true if the instantiation is done, false otherwise
	 * @throws solver.exception.ContradictionException
	 *          if the domain become empty due to this action
	 */
	public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
		// BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
		assert cause != null;
		if (this.instantiated()) {
			int cvalue = this.getValue();
			if (value != cvalue) {
				if (Configuration.PLUG_EXPLANATION){
					solver.getExplainer().instantiateTo(this, value, cause, cvalue, cvalue);
				}
				this.contradiction(cause, EventType.INSTANTIATE, MSG_INST);
			}
			return false;
		} else {
			int index = -1;
			for (int i=indexes.nextSetBit(LB.get()); i>=0 && values[i]<=value; i=indexes.nextSetBit(i + 1)) {
				if(values[i]==value){
					index = i;
					break;
				}
			}
			if (index!=-1) {
				if (reactOnRemoval) {
					for (int i=indexes.nextSetBit(LB.get()); i>=0; i=indexes.nextSetBit(i + 1)) {
						if(i!=index){
							delta.add(values[i], cause);
						}
					}
				}
				int oldLB = 0;
				int oldUB = 0;
				if (Configuration.PLUG_EXPLANATION) {
					oldLB = getLB(); // call getter to avoid adding OFFSET..
					oldUB = getUB();
				}

				this.indexes.clear();
				this.indexes.set(index);
				this.LB.set(index);
				this.UB.set(index);
				this.SIZE.set(1);

				if (indexes.isEmpty()) {
					this.contradiction(cause, EventType.INSTANTIATE, MSG_EMPTY);
				}
				if (Configuration.PLUG_EXPLANATION){
					solver.getExplainer().instantiateTo(this, value, cause, oldLB, oldUB);
				}
				this.notifyPropagators(EventType.INSTANTIATE, cause);
				return true;
			} else {
				if (Configuration.PLUG_EXPLANATION){
					solver.getExplainer().instantiateTo(this, value, cause, getLB(), getUB());
				}
				this.contradiction(cause, EventType.INSTANTIATE, MSG_UNKNOWN);
				return false;
			}
		}
	}

	/**
	 * Updates the lower bound of the domain of <code>this</code> to <code>value</code>.
	 * The instruction comes from <code>propagator</code>.
	 * <ul>
	 * <li>If <code>value</code> is smaller than the lower bound of the domain, nothing is done and the return value is <code>false</code>,</li>
	 * <li>if updating the lower bound to <code>value</code> leads to a dead-end (domain wipe-out),
	 * a <code>ContradictionException</code> is thrown,</li>
	 * <li>otherwise, if updating the lower bound to <code>value</code> can be done safely,
	 * the event type is created (the original event can be promoted) and observers are notified
	 * and the return value is <code>true</code></li>
	 * </ul>
	 *
	 * @param value new lower bound (included)
	 * @param cause updating releaser
	 * @return true if the lower bound has been updated, false otherwise
	 * @throws solver.exception.ContradictionException
	 *          if the domain become empty due to this action
	 */
	public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
		assert cause != null;
		ICause antipromo = cause;
		int old = this.getLB();
		if (old < value) {
			int oub = this.getUB();
			if (oub < value) {
				if (Configuration.PLUG_EXPLANATION){
					solver.getExplainer().updateLowerBound(this, old, oub+1, antipromo);
				}
				this.contradiction(cause, EventType.INCLOW, MSG_LOW);
			} else {
				EventType e = EventType.INCLOW;
				int index;
				for (index=indexes.nextSetBit(LB.get()); index>=0 && values[index]<value; index=indexes.nextSetBit(index+1)) {}
				assert index>=0 && values[index]>=value;
				if (reactOnRemoval) {
					//BEWARE: this loop significantly decreases performances
					for(int i=LB.get();i>=0 && i<index; i=indexes.nextSetBit(i+1)){
						delta.add(values[i],cause);
					}
				}
				indexes.clear(LB.get(), index);
				LB.set(index);
				assert SIZE.get()>indexes.cardinality();
				SIZE.set(indexes.cardinality());
				if (instantiated()) {
					e = EventType.INSTANTIATE;
					if (cause.reactOnPromotion()) {
						cause = Cause.Null;
					}
				}
				this.notifyPropagators(e, cause);
				if (Configuration.PLUG_EXPLANATION){
					solver.getExplainer().updateLowerBound(this, old, value, antipromo);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates the upper bound of the domain of <code>this</code> to <code>value</code>.
	 * The instruction comes from <code>propagator</code>.
	 * <ul>
	 * <li>If <code>value</code> is greater than the upper bound of the domain, nothing is done and the return value is <code>false</code>,</li>
	 * <li>if updating the upper bound to <code>value</code> leads to a dead-end (domain wipe-out),
	 * a <code>ContradictionException</code> is thrown,</li>
	 * <li>otherwise, if updating the upper bound to <code>value</code> can be done safely,
	 * the event type is created (the original event can be promoted) and observers are notified
	 * and the return value is <code>true</code></li>
	 * </ul>
	 *
	 * @param value new upper bound (included)
	 * @param cause update releaser
	 * @return true if the upper bound has been updated, false otherwise
	 * @throws solver.exception.ContradictionException
	 *          if the domain become empty due to this action
	 */
	public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
		assert cause != null;
		ICause antipromo = cause;
		int old = this.getUB();
		if (old > value) {
			int olb = this.getLB();
			if (olb > value) {
				if (Configuration.PLUG_EXPLANATION){
					solver.getExplainer().updateUpperBound(this, old, olb-1, antipromo);
				}
				this.contradiction(cause, EventType.DECUPP, MSG_UPP);
			} else {
				EventType e = EventType.DECUPP;
				int index;
				for (index=indexes.prevSetBit(UB.get()); index>=0 && values[index]>value; index=indexes.prevSetBit(index-1)) {}
				assert index>=0 && values[index]<=value;
				if (reactOnRemoval) {
					//BEWARE: this loop significantly decreases performances
					for(int i=UB.get();i>index; i=indexes.prevSetBit(i-1)){
						delta.add(values[i],cause);
					}
				}
				indexes.clear(index+1, UB.get()+1);
				UB.set(index);
				assert SIZE.get()>indexes.cardinality();
				SIZE.set(indexes.cardinality());
				if (instantiated()) {
					e = EventType.INSTANTIATE;
					if (cause.reactOnPromotion()) {
						cause = Cause.Null;
					}
				}
				this.notifyPropagators(e, cause);
				if (Configuration.PLUG_EXPLANATION){
					solver.getExplainer().updateUpperBound(this, old, value, antipromo);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void wipeOut(ICause cause) throws ContradictionException {
		assert cause != null;
		removeInterval(this.getLB(), this.getUB(), cause);
	}

	public boolean instantiated() {
		return SIZE.get() == 1;
	}

	@Override
	public boolean instantiatedTo(int value) {
		return instantiated() && contains(value);
	}

	public boolean contains(int aValue) {
		if(aValue>=getLB() && aValue<=getUB()){
			for(int i=indexes.nextSetBit(LB.get());i>=0 && values[i]<=aValue;i=indexes.nextSetBit(i+1)){
				if(values[i]==aValue){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Retrieves the current value of the variable if instantiated, otherwier the lower bound.
	 *
	 * @return the current value (or lower bound if not yet instantiated).
	 */
	public int getValue() {
		assert instantiated() : name + " not instantiated";
		return getLB();
	}

	/**
	 * Retrieves the lower bound of the variable
	 *
	 * @return the lower bound
	 */
	public int getLB() {
		assert LB.get()>=0 && LB.get()<LENGTH;
		return values[LB.get()];
	}

	/**
	 * Retrieves the upper bound of the variable
	 *
	 * @return the upper bound
	 */
	public int getUB() {
		assert UB.get()>=0 && UB.get()<LENGTH;
		return values[UB.get()];
	}

	public int getDomainSize() {
		return SIZE.get();
	}

	public int nextValue(int aValue) {
		int lb = getLB();
		if (aValue < lb) return lb;
		if (aValue >= getUB()) return Integer.MAX_VALUE;
		int i;
		for(i=indexes.nextSetBit(LB.get());i>=0 && values[i]<=aValue;i=indexes.nextSetBit(i+1)){}
		return (i>=0)?values[i]:Integer.MAX_VALUE;
	}

	@Override
	public int previousValue(int aValue) {
		int ub = getUB();
		if (aValue > ub) return ub;
		if (aValue <= getLB()) return Integer.MIN_VALUE;
		int i;
		for(i=indexes.prevSetBit(UB.get());i>=0 && values[i]>=aValue;i=indexes.prevSetBit(i-1)){}
		return (i>=0)?values[i]:Integer.MIN_VALUE;
	}

	@Override
	public boolean hasEnumeratedDomain() {
		return true;
	}

	@Override
	public IEnumDelta getDelta() {
		return delta;
	}

	public String toString() {
		StringBuilder s = new StringBuilder(20);
		s.append(name).append(" = ");
		if (SIZE.get() == 1) {
			s.append(this.getLB());
		} else {
			s.append('{').append(getLB());
			int nb = 5;
			for (int i = nextValue(getLB()); i < Integer.MAX_VALUE && nb > 0; i = nextValue(i)) {
				s.append(',').append(i);
				nb--;
			}
			if (nb == 0 && SIZE.get() > 6) {
				s.append("...,").append(this.getUB());
			}
			s.append('}');
		}
		return s.toString();
	}

	////////////////////////////////////////////////////////////////
	///// methode liees au fait qu'une variable est observable /////
	////////////////////////////////////////////////////////////////


	@Override
	public void createDelta() {
		if (!reactOnRemoval) {
			delta = new EnumDelta(solver.getSearchLoop());
			reactOnRemoval = true;
		}
	}

	public IIntDeltaMonitor monitorDelta(ICause propagator) {
		createDelta();
		return new EnumDeltaMonitor(delta, propagator);
	}


	public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
		assert cause != null;
		notifyMonitors(event);
		if ((modificationEvents & event.mask) != 0) {
			//records.forEach(afterModification.set(this, event, cause));
			//solver.getEngine().onVariableUpdate(this, afterModification.set(this, event, cause));
			solver.getEngine().onVariableUpdate(this, event, cause);
		}
		notifyViews(event, cause);
	}


	public void notifyMonitors(EventType event) throws ContradictionException {
		for (int i = mIdx - 1; i >= 0; i--) {
			monitors[i].onUpdate(this, event);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	public AntiDomain antiDomain() {
		// TODO
		return new AntiDomBitset(this);
	}

	public void explain(VariableState what, Explanation to) {
		AntiDomain invdom = solver.getExplainer().getRemovedValues(this);
		DisposableValueIterator it = invdom.getValueIterator();
		while (it.hasNext()) {
			int val = it.next();
			if ((what == VariableState.LB && val < this.getLB())
					|| (what == VariableState.UB && val > this.getUB())
					|| (what == VariableState.DOM)) {
//                System.out.println("solver.explainer.explain(this,"+ val +") = " + solver.explainer.explain(this, val));
				to.add(solver.getExplainer().explain(this, val));
			}
		}
		it.dispose();
//        System.out.println("BitsetIntVarImpl.explain " + this + invdom +  " expl: " + expl);
	}


	@Override
	public void explain(VariableState what, int val, Explanation to) {
		to.add(solver.getExplainer().explain(this, val));
	}

	@Override
	public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
		assert cause != null;
//        records.forEach(onContradiction.set(this, event, cause));
		solver.getEngine().fails(cause, this, message);
	}


	@Override
	public int getTypeAndKind() {
		return VAR | INT;
	}

	@Override
	public IntVar duplicate() {
		return new BitsetArrayIntVarImpl(StringUtils.randomName(this.name), this.values.clone(), this.getSolver());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public DisposableValueIterator getValueIterator(boolean bottomUp) {
		if (_viterator == null || !_viterator.isReusable()) {
			_viterator = new DisposableValueIterator() {

				int index;

				@Override
				public void bottomUpInit() {
					super.bottomUpInit();
					index = indexes.nextSetBit(LB.get());
				}

				@Override
				public void topDownInit() {
					super.topDownInit();
					index = indexes.prevSetBit(UB.get());
				}

				@Override
				public boolean hasNext() {
					return index != -1;
				}

				@Override
				public boolean hasPrevious() {
					return index != -1;
				}

				@Override
				public int next() {
					int old = values[index];
					index = indexes.nextSetBit(index+1);
					return old;
				}

				@Override
				public int previous() {
					int old = values[index];
					index = indexes.prevSetBit(index-1);
					return old;
				}
			};
		}
		if (bottomUp) {
			_viterator.bottomUpInit();
		} else {
			_viterator.topDownInit();
		}
		return _viterator;
	}

	@Override
	public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
		if (_riterator == null || !_riterator.isReusable()) {
			_riterator = new DisposableRangeIterator() {

				int from;
				int to;

				@Override
				public void bottomUpInit() {
					super.bottomUpInit();
					this.from = indexes.nextSetBit(LB.get());
					this.to = indexes.nextClearBit(from + 1) - 1;
				}

				@Override
				public void topDownInit() {
					super.topDownInit();
					this.to = indexes.prevSetBit(UB.get());
					this.from = indexes.prevClearBit(to) + 1;
				}

				public boolean hasNext() {
					return this.from != -1;
				}

				@Override
				public boolean hasPrevious() {
					return this.to != -1;
				}

				public void next() {
					this.from = indexes.nextSetBit(this.to + 1);
					this.to = indexes.nextClearBit(this.from) - 1;
				}

				@Override
				public void previous() {
					this.to = indexes.prevSetBit(this.from - 1);
					this.from = indexes.prevClearBit(this.to) + 1;
				}

				@Override
				public int min() {
					return values[from];
				}

				@Override
				public int max() {
					return values[to];
				}
			};
		}
		if (bottomUp) {
			_riterator.bottomUpInit();
		} else {
			_riterator.topDownInit();
		}
		return _riterator;
	}
}
