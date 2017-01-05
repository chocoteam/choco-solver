/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.hardcoded.TwoBucketPropagationEngine;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * BEWARE : SHOULD BE UNIQUE!
 *
 * FOR OPTIMIZATION ONLY
 * Search heuristic combined with a constraint performing strong consistency on the decision variable
 * and branching on the value with the best objective bound
 *
 * @author Jean-Guillaume FAGES
 */
public class BoundSearch extends AbstractStrategy<IntVar>{

	private PropBound pb;
	private IntStrategy definedSearch;
	private static boolean active; // TODO find a better solution than static (hooks?)

	public BoundSearch(IntStrategy mainSearch, IntVar... vars){
		super(vars);
		Model m = vars[0].getModel();
		this.definedSearch = mainSearch;
		pb = new PropBound(vars, definedSearch.getVarSelector());
		new Constraint("BoundSearch",pb).post();
	}

	@Override
	public Decision<IntVar> getDecision() {
		IntVar var = pb.getVar();
		if(var == null) return null;
		if(pb.hold()) {
			return pb.getModel().getSolver().getDecisionPath().makeIntDecision(var, DecisionOperatorFactory.makeIntEq(), pb.getBestVal());
		}else {
			return definedSearch.computeDecision(var);
		}
	}

	private class PropBound extends Propagator<IntVar> {

		private IPropagationEngine internalEngine, masterEngine;
		private VariableSelector<IntVar> varsel;
		private TIntIntHashMap vb = new TIntIntHashMap();
		private int LIMIT = 100;
		private IntVar variable;
		private boolean hold;

		public PropBound(IntVar[] variables, VariableSelector<IntVar> varsel) {
			super(variables, PropagatorPriority.VERY_SLOW,false);
			this.varsel = varsel;
		}

		@Override
		public void propagate(int evtmask) throws ContradictionException {
			if(active)return;
			if(internalEngine == null) {
				init();
			}
			if(variable == null || variable.isInstantiated()) {
				hold = false;
				variable = varsel.getVariable(vars);
				if (variable != null && variable.getDomainSize() < LIMIT) {
					hold = true;
					if(variable.hasEnumeratedDomain()) {
						vb.clear();
						model.getSolver().setEngine(internalEngine);
						for (int v = variable.getLB(); v <= variable.getUB(); v = variable.nextValue(v)) {
							vb.put(v, bound(v));
						}
						model.getSolver().setEngine(masterEngine);
						for (int v = variable.getLB(); v <= variable.getUB(); v = variable.nextValue(v)) {
							if (vb.get(v) == Integer.MAX_VALUE) {
								variable.removeValue(v, this);
							}
						}
						if(variable.isInstantiated())propagate(0);
					}
				}
			}
		}

		private int bound(int val){
			int cost;
			active = true;
			model.getEnvironment().worldPush();
			try {
				variable.instantiateTo(val, this);
				internalEngine.propagate();
				IntVar obj = (IntVar) model.getObjective();
				cost = model.getSolver().getObjectiveManager().getPolicy()== ResolutionPolicy.MINIMIZE?obj.getLB():-obj.getUB();
			} catch (ContradictionException cex) {
				cost = Integer.MAX_VALUE;
			}
			internalEngine.flush();
			model.getEnvironment().worldPop();
			active = false;
			return cost;
		}

		public int getBestVal(){
			int coef = model.getSolver().getSolutionCount()==0?1:1;
			if(variable.hasEnumeratedDomain()){
				int bestCost = Integer.MAX_VALUE;
				int bestV = variable.getUB();
				for (int v = variable.getLB(); v <= variable.getUB(); v = variable.nextValue(v)) {
					int c = vb.get(v);
					if (c < bestCost * coef) {
						bestCost = c;
						bestV = v;
					}
				}
				return bestV;
			}else {
				return bound(variable.getLB())<bound(variable.getUB())*coef?variable.getLB():variable.getUB();
			}
		}

		private void init() throws ContradictionException {
			masterEngine = model.getSolver().getEngine();
			internalEngine = new TwoBucketPropagationEngine(model);
			internalEngine.initialize();
			model.getSolver().setEngine(internalEngine);
			active = true;
			boolean fail = false;
			try {
				// FAILS on isJ9.czn (also assertion error on mspsp_h1.fzn)
				internalEngine.propagate();
			}catch (ContradictionException e){
				fail = true;
			}
			active = false;
			model.getSolver().setEngine(masterEngine);
			if(fail)fails();
		}

		public boolean hold() {
			return hold;
		}

		public IntVar getVar(){
			return hold?variable:varsel.getVariable(vars);
		}

		@Override
		public ESat isEntailed() {
			return ESat.TRUE;
		}
	}
}
