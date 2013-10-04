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
package solver.propagation.hardcoded;

import memory.IEnvironment;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationTrigger;
import solver.propagation.hardcoded.util.AId2AbId;
import solver.propagation.hardcoded.util.IId2AbId;
import solver.propagation.queues.AQueue;
import solver.propagation.queues.CircularQueue;
import solver.variables.EventType;
import solver.variables.Variable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Not that fast yet
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 04/10/13
 */
public class FastVariableEngine implements IPropagationEngine {

	protected final ContradictionException exception; // the exception in case of contradiction
	protected final IEnvironment environment; // environment of backtrackable objects
	protected final Variable[] variables;
	protected final AQueue<Variable> var_queue;
	protected int lastVarIdx;
	protected final boolean[] schedule;
	protected final PropagationTrigger trigger; // an object that starts the propagation
	protected final IId2AbId v2i; // mapping between propagator ID and its absolute index
	private boolean init;
	protected final ICause[][] varevtcause;

	private final static ICause NONE = new ICause() {
		public void explain(Deduction d, Explanation e) {
			throw new UnsupportedOperationException();
		}
	};
	private final static boolean FINE_SCHEDULE = true;
	public static long nbPOP = 0;

	public FastVariableEngine(Solver solver) {
		this.exception = new ContradictionException();
		this.environment = solver.getEnvironment();
		this.trigger = new PropagationTrigger(this, solver);
		variables = solver.getVars();
		int maxID = 0;
		for (int i = 0; i < variables.length; i++) {
			if (maxID == 0 || maxID < variables[i].getId()) {
				maxID = variables[i].getId();
			}
		}
		Constraint[] constraints = solver.getCstrs();
		List<Propagator> _propagators = new ArrayList<Propagator>();
		for (int c = 0; c < constraints.length; c++) {
			_propagators.addAll(Arrays.asList(constraints[c].getPropagators()));
		}
		trigger.addAll(_propagators.toArray(new Propagator[_propagators.size()]));
		var_queue = new CircularQueue<Variable>(variables.length / 2);
		v2i = new AId2AbId(0, maxID, -1);
		for (int j = 0; j < variables.length; j++) {
			v2i.set(variables[j].getId(), j);
		}
		schedule = new boolean[variables.length];
		varevtcause = new ICause[variables.length][4];
		for(int i=0;i<variables.length;i++){
			for(int j=0;j<4;j++){
				varevtcause[i][j] = NONE;
			}
		}
		lastVarIdx = -1;
		init = true;
	}

	@Override
	public boolean isInitialized() {
		return init;
	}

	// method variables (avoid gc)
	protected final ICause[] prop_causes = new ICause[4];
	protected Propagator   prop_pr;
	protected Propagator[] prop_props;
	protected Variable prop_v;
	protected int prop_id, prop_idx, prop_mask;

	@SuppressWarnings({"NullableProblems"})
	@Override
	public void propagate() throws ContradictionException {
		if (trigger.needToRun()) {
			trigger.propagate();
		}
		while (!var_queue.isEmpty()) {
			nbPOP++;
			prop_v = var_queue.pollFirst();
			prop_id = v2i.get(prop_v.getId());
			lastVarIdx = prop_id;
			assert schedule[prop_id];
			schedule[prop_id] = false;
			for(int i=0;i<4;i++){
				prop_causes[i] = varevtcause[prop_id][i];
				varevtcause[prop_id][i] = NONE;
			}
			prop_props = prop_v.getPropagators();
			int n = prop_props.length;
			for(int i=0;i<n;i++){
				prop_pr = prop_props[i];
				if(prop_pr.isActive()){
					prop_mask = 0;
					if(prop_causes[0]!=NONE && prop_causes[0]!=prop_pr){
						prop_mask |= EventType.INSTANTIATE.strengthened_mask;
					}
					else{
						if(prop_causes[1]!=NONE && prop_causes[1]!=prop_pr){
							prop_mask |= EventType.INCLOW.strengthened_mask;
						}
						if(prop_causes[2]!=NONE && prop_causes[2]!=prop_pr){
							prop_mask |= EventType.DECUPP.strengthened_mask;
						}
						else if(prop_causes[3]!=NONE && prop_causes[3]!=prop_pr){
							prop_mask |= EventType.REMOVE.strengthened_mask;
						}
					}
					prop_idx = prop_v.getIndiceInPropagator(i);
					if(prop_mask>0 && prop_pr.advise(prop_idx,prop_mask)){
						prop_pr.fineERcalls++;
						prop_pr.propagate(prop_idx, prop_mask);
					}
				}
			}
		}
		lastVarIdx = -1;
	}

	@Override
	public void flush() {
		if (lastVarIdx>=0) {
			schedule[lastVarIdx] = false;
			for(int i=0;i<4;i++){
				varevtcause[lastVarIdx][i] = NONE;
			}
		}
		while (!var_queue.isEmpty()) {
			lastVarIdx = v2i.get(var_queue.pollFirst().getId());
			schedule[lastVarIdx] = false;
			for(int i=0;i<4;i++){
				varevtcause[lastVarIdx][i] = NONE;
			}
		}
		lastVarIdx = -1;
	}

	@Override
	public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
		throw exception.set(cause, variable, message);
	}

	@Override
	public ContradictionException getContradictionException() {
		return exception;
	}

	@Override
	public void clear() {}

	// method variables (avoid gc)
	protected int ovu_vid, ovu_mask;
	protected ICause[] ovu_causes;
	protected Propagator[] ovu_props;
	protected Propagator ovu_p;
	protected boolean ovu_bool;
	@Override
	public void onVariableUpdate(Variable variable, EventType type, ICause cause) throws ContradictionException {
		ovu_vid = v2i.get(variable.getId());
		ovu_mask = type.mask;
		ovu_causes = varevtcause[ovu_vid];
		if((ovu_mask & EventType.INSTANTIATE.mask)!=0){
			ovu_causes[0] = cause;
		}
		if((ovu_mask & EventType.INCLOW.mask)!=0){
			ovu_causes[1] = cause;
		}
		if((ovu_mask & EventType.DECUPP.mask)!=0){
			ovu_causes[2] = cause;
		}
		if((ovu_mask & EventType.REMOVE.mask)!=0){
			ovu_causes[3] = cause;
		}
		if (!schedule[ovu_vid]) {
			ovu_bool = !FINE_SCHEDULE;
			ovu_props = variable.getPropagators();
			for(int i=0;i<ovu_props.length && !ovu_bool;i++){
				ovu_p = ovu_props[i];
				if(cause!=ovu_p){
					if(ovu_p.isActive()){
						ovu_bool = true;
					}
				}
			}
			if(ovu_bool){
//				if(ovu_vid==lastVarIdx){
//					var_queue.addFirst(variable);
//				}else{
					var_queue.addLast(variable);
//				}
				schedule[ovu_vid] = true;
			}
		}
	}

	@Override
	public void onPropagatorExecution(Propagator propagator) {propagator.coarseERcalls++;}

	@Override
	public void desactivatePropagator(Propagator propagator) {}

	@Override
	public void dynamicAddition(Constraint c, boolean cut) {
		throw new UnsupportedOperationException("Dynamic constraint addition is not available within FastVariableEngine");
	}
}
