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

package solver.objective;

import solver.ICause;
import solver.ResolutionPolicy;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.Variable;

/**
 * Class to monitor the objective function and avoid exploring "worse" solutions
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class ObjectiveManager<V extends Variable, N extends Number> implements ICause {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	final protected ResolutionPolicy policy;
	final protected boolean strict;
	final protected V objective;

	final private boolean intOrReal;
	final private double precision;

	protected N bestProvedLB, bestProvedUB; // best bounds found so far

	// creates an objective manager for satisfaction problems
	public static final ObjectiveManager SAT(){
		return new ObjectiveManager(null,ResolutionPolicy.SATISFACTION,false);
	}

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	private ObjectiveManager(V objective, ResolutionPolicy policy, double precision, boolean strict, boolean intOrReal) {
		this.policy = policy;
		this.strict = strict;
		this.objective = objective;
		this.precision = precision;
		this.intOrReal = intOrReal;
		if (isOptimization()) {
			this.bestProvedLB = getObjLB();
			this.bestProvedUB = getObjUB();
		}
	}

	/**
	 * Creates an optimization manager for an integer objective function (represented by an IntVar)
	 * Enables to cut "worse" solutions
	 *
	 * @param objective variable (represent the value of a solution)
	 * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
	 * @param strict    Forces to compute strictly better solutions.
	 *                  Enables to enumerate better or equal solutions when set to false.
	 */
	public ObjectiveManager(IntVar objective, ResolutionPolicy policy, boolean strict){
		this((V)objective,policy,0,strict,true);
	}

	/**
	 * Creates an optimization manager for a continuous objective function (represented by a RealVar)
	 * Enables to cut "worse" solutions
	 *
	 * @param objective variable (represent the value of a solution)
	 * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
	 * @param precision precision parameter defining the minimum objective improvement between two solutions
	 *                  (avoids wasting time enumerating a huge set of equivalent solutions)
	 * @param strict    Forces to compute strictly better solutions.
	 *                  Enables to enumerate better or equal solutions when set to false.
	 */
	public ObjectiveManager(RealVar objective, ResolutionPolicy policy, double precision, boolean strict) {
		this((V)objective,policy,precision,strict,false);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**
	 * @return true iff the problem is an optimization problem
	 */
	public boolean isOptimization() {
		return policy != ResolutionPolicy.SATISFACTION;
	}

	/**
	 * Updates the lower (or upper) bound of the objective variable, considering its best know value.
	 *
	 * @param decision
	 * @throws solver.exception.ContradictionException if this application leads to a contradiction  @param decision
	 */
	public void apply(Decision decision) throws ContradictionException {
		decision.apply();
	}

	@Override
	public void explain(Deduction val, Explanation e) {
		if (isOptimization()) {
			objective.explain(VariableState.DOM, e);
		}
	}

	@Override
	public String toString() {
		String st = "";
		switch (policy) {
			case SATISFACTION:
				return "SAT";
			case MINIMIZE: st = "Minimize"; break;
			case MAXIMIZE: st = "Maximize"; break;
			default:
				throw new UnsupportedOperationException("no objective manager");
		}
		if(intOrReal){
			return String.format(st+" %s = %d", this.objective.getName(), getBestSolutionValue());
		}else{
			return String.format(st+" %s = %."+getNbDecimals()+"f", this.objective.getName(), getBestSolutionValue());
		}
	}

	protected int getNbDecimals(){
		int dec = 0;
		double p = precision;
		while((int)p<=0 && dec<=12){
			dec++;
			p*=10;
		}
		return dec;
	}

	/**
	 * Informs the manager that a new solution has been found
	 */
	public void update() {
		if(isOptimization()){
			assert objective.isInstantiated();
			if (policy == ResolutionPolicy.MINIMIZE) {
				this.bestProvedUB = getObjUB();
			} else {
				this.bestProvedLB = getObjLB();
			}
		}
	}

	/**
	 * Prevent the solver from computing worse quality solutions
	 *
	 * @throws solver.exception.ContradictionException
	 */
	public void postDynamicCut() throws ContradictionException {
		if(isOptimization()){
			if(intOrReal){
				int offset = 0;
				if (objective.getSolver().getMeasures().getSolutionCount() > 0 && strict) {
					offset = 1;
				}
				IntVar io = (IntVar) objective;
				if (policy == ResolutionPolicy.MINIMIZE) {
					io.updateUpperBound(bestProvedUB.intValue() - offset, this);
					io.updateLowerBound(bestProvedLB.intValue(), this);
				} else {
					io.updateUpperBound(bestProvedUB.intValue(), this);
					io.updateLowerBound(bestProvedLB.intValue() + offset, this);
				}
			} else {
				double offset = 0;
				if (objective.getSolver().getMeasures().getSolutionCount() > 0 && strict) {
					offset = precision;
				}
				RealVar io = (RealVar) objective;
				if (policy == ResolutionPolicy.MINIMIZE) {
					io.updateUpperBound(bestProvedUB.doubleValue() - offset, this);
					io.updateLowerBound(bestProvedLB.doubleValue(), this);
				} else {
					io.updateUpperBound(bestProvedUB.doubleValue(), this);
					io.updateLowerBound(bestProvedLB.doubleValue() + offset, this);
				}
			}
		}
	}

	/**
	 * @return the best solution value found so far (returns the initial bound if no solution has been found yet)
	 */
	public N getBestSolutionValue() {
		if (policy == ResolutionPolicy.MINIMIZE) {
			return bestProvedUB;
		}
		if (policy == ResolutionPolicy.MAXIMIZE) {
			return bestProvedLB;
		}
		throw new UnsupportedOperationException("There is no objective variable in satisfaction problems");
	}

	/**
	 * States that lb is a global lower bound on the problem
	 *
	 * @param lb lower bound
	 */
	public void updateBestLB(N lb) {
		assert isOptimization();
		if(lb.doubleValue()> bestProvedLB.doubleValue()){
			bestProvedLB = lb;
		}
	}

	/**
	 * States that ub is a global upper bound on the problem
	 *
	 * @param ub upper bound
	 */
	public void updateBestUB(N ub) {
		assert isOptimization();
		if(ub.doubleValue()< bestProvedUB.doubleValue()){
			bestProvedUB = ub;
		}
	}

	private N getObjLB(){
		assert isOptimization();
		if(intOrReal){
			return (N) new Integer(((IntVar)objective).getLB());
		}else{
			return (N) new Double(((RealVar)objective).getLB());
		}
	}

	private N getObjUB(){
		assert isOptimization();
		if(intOrReal){
			return (N) new Integer(((IntVar)objective).getUB());
		}else{
			return (N) new Double(((RealVar)objective).getUB());
		}
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	/**
	 * @return the ResolutionPolicy of the problem
	 */
	public ResolutionPolicy getPolicy() {
		return policy;
	}

	/**
	 * @return the objective variable
	 */
	public V getObjective() {
		return objective;
	}

	/**
	 * @return the best lower bound computed so far
	 */
	public N getBestLB() {
		return bestProvedLB;
	}

	/**
	 * @return the best upper bound computed so far
	 */
	public N getBestUB() {
		return bestProvedUB;
	}
}
