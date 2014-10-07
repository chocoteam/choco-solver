/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package solver.explanations;

import memory.IEnvironment;
import memory.IStateInt;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.Propagator;
import solver.explanations.antidom.AntiDomain;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.events.IEventType;
import solver.variables.events.IntEventType;
import solver.variables.events.PropagatorEventType;

/**
 * A explanation engine that works in a lazy way.
 * It stores events on the fly, but do not compute anything until an explicit call is made.
 * However, it needs to be used AFTER RESTART only. Otherwise, it can be not synchronized anymore.
 *
 *
 * @author Charles Prud'homme
 * @since 25/03/13
 */
public class LazyExplanationEngineFromRestart extends RecorderExplanationEngine {

	private static final int CHUNK_SIZE = 128;

	//*****************************************//
	// STRUCTURES DEDICATED TO EVENT RECORDING //
	//*****************************************//
	IntVar[][] varChunks;   // to store variables, in chronological order
	ICause[][] cauChunks;   // to store causes, in chronological order
	IEventType[][] masChunks;// to store masks, in chronological order
	int[][] val1Chunks;     // to store values, in chronological order
	int[][] val2Chunks;     // to store values, in chronological order
	int[][] val3Chunks;     // to store values, in chronological order

	IStateInt curChunk;
	IStateInt nextTop;

	boolean up2date;  // to indicate if the database is up-to-date

	/**
	 * Builds an ExplanationEngine
	 *
	 * @param slv associated solver's environment
	 */
	public LazyExplanationEngineFromRestart(Solver slv) {
		super(slv);
		IEnvironment env = slv.getEnvironment();
		curChunk = env.makeInt(0);
		nextTop = env.makeInt(0);

		varChunks = new IntVar[1][];
		varChunks[0] = new IntVar[CHUNK_SIZE];

		cauChunks = new ICause[1][];
		cauChunks[0] = new ICause[CHUNK_SIZE];

		masChunks = new IEventType[1][];
		masChunks[0] = new IEventType[CHUNK_SIZE];

		val1Chunks = new int[1][];
		val1Chunks[0] = new int[CHUNK_SIZE];

		val2Chunks = new int[1][];
		val2Chunks[0] = new int[CHUNK_SIZE];

		val3Chunks = new int[1][];
		val3Chunks[0] = new int[CHUNK_SIZE];

		up2date = false;
	}

	private void pushEvent(IntVar var, ICause cause, IEventType mask, int one, int two, int three) {
		assert cause != Cause.Null: "cause null";
		int currentC = curChunk.get();
		int currentI = nextTop.add(1);
		if (currentI > CHUNK_SIZE) {
			currentC = curChunk.add(1);
			int l = varChunks.length;
			if (currentC == l) {
				increase(l);
			}
			nextTop.set(1);
			currentI = 0;
		} else {
			currentI--;
		}
		varChunks[currentC][currentI] = var;
		cauChunks[currentC][currentI] = cause;
		masChunks[currentC][currentI] = mask;
		val1Chunks[currentC][currentI] = one;
		val2Chunks[currentC][currentI] = two;
		val3Chunks[currentC][currentI] = three;
		up2date = false;
	}

	private void increase(int l) {
		IntVar[][] varBigger = new IntVar[l + 1][];
		System.arraycopy(varChunks, 0, varBigger, 0, l);
		varBigger[l] = new IntVar[CHUNK_SIZE];
		varChunks = varBigger;

		ICause[][] cauBigger = new ICause[l + 1][];
		System.arraycopy(cauChunks, 0, cauBigger, 0, l);
		cauBigger[l] = new ICause[CHUNK_SIZE];
		cauChunks = cauBigger;

		IEventType[][] masBigger = new IEventType[l + 1][];
		System.arraycopy(masChunks, 0, masBigger, 0, l);
		masBigger[l] = new IEventType[CHUNK_SIZE];
		masChunks = masBigger;

		int[][] valBigger = new int[l + 1][];
		System.arraycopy(val1Chunks, 0, valBigger, 0, l);
		valBigger[l] = new int[CHUNK_SIZE];
		val1Chunks = valBigger;

		valBigger = new int[l + 1][];
		System.arraycopy(val2Chunks, 0, valBigger, 0, l);
		valBigger[l] = new int[CHUNK_SIZE];
		val2Chunks = valBigger;

		valBigger = new int[l + 1][];
		System.arraycopy(val3Chunks, 0, valBigger, 0, l);
		valBigger[l] = new int[CHUNK_SIZE];
		val3Chunks = valBigger;
	}

	@Override
	public void beforeInitialPropagation() {
		for (Variable v : solver.getVars()) {
			super.getRemovedValues((IntVar) v);
		}
	}

	/**
	 * This is the main reason why we create this class.
	 * Record operations to execute for explicit call to explanation.
	 *
	 * @param var   an integer variable
	 * @param val   a value
	 * @param cause a cause
	 */
	@Override
	public void removeValue(IntVar var, int val, ICause cause) {
		pushEvent(var, cause, IntEventType.REMOVE, val, 0, 0);
	}

	/**
	 * This is the main reason why we create this class.
	 * Record operations to execute for explicit call to explanation.
	 *
	 * @param intVar an integer variable
	 * @param value  a value
	 * @param cause  a cause
	 * @value old previous LB
	 */
	@Override
	public void updateLowerBound(IntVar intVar, int old, int value, ICause cause) {
		pushEvent(intVar, cause, IntEventType.INCLOW, old, value, 0);
	}

	/**
	 * This is the main reason why we create this class.
	 * Record operations to execute for explicit call to explanation.
	 *
	 * @param var   an integer variable
	 * @param value a value
	 * @param cause a cause
	 * @value old previous LB
	 */
	@Override
	public void updateUpperBound(IntVar var, int old, int value, ICause cause) {
		pushEvent(var, cause, IntEventType.DECUPP, old, value, 0);
	}

	/**
	 * This is the main reason why we create this class.
	 * Record operations to execute for explicit call to explanation.
	 *
	 * @param var   an integer variable
	 * @param val   a value
	 * @param cause a cause
	 * @param oldLB previous lb
	 * @param oldUB previous ub
	 */
	@Override
	public void instantiateTo(IntVar var, int val, ICause cause, int oldLB, int oldUB) {
		pushEvent(var, cause, IntEventType.INSTANTIATE, val, oldLB, oldUB);
	}

	@Override
	public void activePropagator(BoolVar var, Propagator propagator) {
		pushEvent(var, propagator, PropagatorEventType.FULL_PROPAGATION, 0, 0, 0);
	}

	@Override
	public BranchingDecision getDecision(Decision decision, boolean isLeft) {
		BranchingDecision br = super.getDecision(decision, isLeft);
		if (!isLeft) {
			// a refutation is explained thanks to the previous ones which are refutable
			if (decision != RootDecision.ROOT) {
				Explanation explanation = database.get(br.getId());
				if (explanation == null) {
					explanation = new Explanation();
				} else {
					explanation.reset();
				}

				Decision d = decision.getPrevious();
				while ((d != RootDecision.ROOT)) {
					if (d.hasNext()) {
						explanation.add(d.getPositiveDeduction());
					}
					d = d.getPrevious();
				}
				this.store(br, explanation);
			}
		}
		return br;
	}

	// *********************** //

	private void playEvents() {
		up2date = true;
		int currentC = curChunk.get();
		int currentI = nextTop.get();

        boolean bug;
		for (int chunk = 0; chunk <= currentC; chunk++) {
			int to = (chunk == currentC ? currentI : CHUNK_SIZE);
			for (int cell = 0; cell < to; cell++) {
				IntVar var = varChunks[chunk][cell];
				IEventType etype = masChunks[chunk][cell];
				ICause cause = cauChunks[chunk][cell];
				int one = val1Chunks[chunk][cell];
				int two = val2Chunks[chunk][cell];
				int three = val3Chunks[chunk][cell];

 				bug = true;
				if(etype == IntEventType.REMOVE) {
					super.removeValue(var, one, cause);
					bug = false;
				}
				if(etype == IntEventType.INSTANTIATE){
					super.instantiateTo(var, one, cause, two, three);
					bug = false;
				}
				if(etype == IntEventType.INCLOW){
					super.updateLowerBound(var, one, two, cause);
					bug = false;
				}
				if(etype == IntEventType.DECUPP){
					super.updateUpperBound(var, one, two, cause);
					bug = false;
				}
				if(etype == PropagatorEventType.FULL_PROPAGATION){
					Propagator prop = (Propagator) cause;
					BoolVar bVar = (BoolVar) var;
					super.activePropagator(bVar, prop);
					bug = false;
				}
				if(bug) {
					throw new UnsupportedOperationException("Unknown type " + etype);
				}
			}
		}

	}

	// *********************** //


	@Override
	public AntiDomain getRemovedValues(IntVar v) {
		if (!up2date) {
			playEvents();
		}
		return super.getRemovedValues(v);
	}

	public ValueRemoval getValueRemoval(IntVar var, int val) {
		if (!up2date) {
			playEvents();
		}
		return super.getValueRemoval(var, val);
	}

	@Override
	public PropagatorActivation getPropagatorActivation(Propagator propagator) {
		if (!up2date) {
			playEvents();
		}
		return super.getPropagatorActivation(propagator);
	}
}
