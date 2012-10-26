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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 25/05/12
 * Time: 15:39
 */

package solver.objective;

import choco.kernel.ResolutionPolicy;
import choco.kernel.common.util.PoolManager;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.objective.ObjectiveManager;
import solver.objective.OptimizationPolicy;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;

public class ObjectiveStrategy extends AbstractStrategy<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int lb, ub;
	private int coefLB,coefUB;
    private IntVar obj;
    private long nbSols;
    private Solver solver;
    private PoolManager<FastDecision> pool;
    private boolean firstCall;
	private DecisionOperator<IntVar> decOperator;

	//***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

	public ObjectiveStrategy(IntVar obj, OptimizationPolicy policy, Solver solver) {
        this(obj,getCoefs(policy),policy,solver);
    }

	public ObjectiveStrategy(IntVar obj, int[] coefs, OptimizationPolicy policy, Solver solver) {
        super(new IntVar[]{obj});
        this.pool = new PoolManager<FastDecision>();
        this.solver = solver;
        this.obj = obj;
        this.firstCall = true;
        solver.getSearchLoop().restartAfterEachSolution(true);
		this.coefLB = coefs[0];
		this.coefUB = coefs[1];
		if(coefLB<0 || coefUB<0 || coefLB+coefUB==0){
			throw new UnsupportedOperationException("coefLB<0, coefUB<0 and coefLB+coefUB==0 are forbidden");
		}
		if(coefLB+coefUB!=1 && policy!=OptimizationPolicy.DICHOTOMIC){
			throw new UnsupportedOperationException("Invalid coefficients for BOTTOM_UP or TOP_DOWN optimization" +
			"\nuse signature public ObjectiveStrategy(IntVar obj, OptimizationPolicy policy, Solver solver) instead");
		}
		decOperator = getOperator(policy,solver.getSearchLoop().getObjectivemanager().getPolicy());
    }

	private static int[] getCoefs(OptimizationPolicy policy){
		switch (policy){
			case BOTTOM_UP:	return new int[]{1,0};
			case TOP_DOWN:	return new int[]{0,1};
			case DICHOTOMIC:return new int[]{1,1};
			default:throw new UnsupportedOperationException();
		}
	}

	private DecisionOperator<IntVar> getOperator(OptimizationPolicy optPolicy, ResolutionPolicy resoPolicy){
			switch (optPolicy){
				case BOTTOM_UP:	return decUB;
				case TOP_DOWN:	return incLB;
				case DICHOTOMIC:
					switch (resoPolicy){
						case MINIMIZE:
							decOperator = decUB;
							break;
						case MAXIMIZE:
							decOperator = incLB;
							break;
					}
				default:throw new UnsupportedOperationException();
			}
		}

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void init() {}

    @Override
    public Decision getDecision() {
		if(nbSols == solver.getMeasures().getSolutionCount()
		|| obj.instantiated()){
			return null;
		}
		if(firstCall){
			firstCall = false;
			lb = obj.getLB();
			ub = obj.getUB();
		}
		nbSols = solver.getMeasures().getSolutionCount();
		ub = obj.getUB();
		lb = Math.max(lb,obj.getLB());//check
		ub = Math.min(ub,obj.getUB());//check
		ObjectiveManager man = solver.getSearchLoop().getObjectivemanager();
		man.updateLB(lb);
		man.updateUB(ub);
		if(lb>ub){
			return null;
		}
		int target;
		target = (lb*coefLB+ub*coefUB)/(coefLB+coefUB);
		System.out.println(lb+" : "+ub+" -> "+target);
		FastDecision dec = pool.getE();
		if(dec==null)dec = new FastDecision(pool);
		dec.set(obj,target, decOperator);
		return dec;
	}

    private DecisionOperator<IntVar> decUB = new DecisionOperator<IntVar>() {
        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateUpperBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
			System.out.println("unapply objective decision");
			lb = value + 1;
			solver.getSearchLoop().getObjectivemanager().updateLB(lb);
			var.updateLowerBound(lb, cause);
        }

        @Override
        public String toString() {
            return " objective split("+coefLB+","+coefUB+"), decreases the upper bound first";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.getUB() > value;
        }
    };

	private DecisionOperator<IntVar> incLB = new DecisionOperator<IntVar>() {
        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateLowerBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
			System.out.println("unapply objective decision");
			ub = value - 1;
			solver.getSearchLoop().getObjectivemanager().updateUB(ub);
			var.updateUpperBound(ub, cause);
        }

        @Override
        public String toString() {
            return " objective split("+coefLB+","+coefUB+"), increases the lower bound first";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.getLB() < value;
        }
    };
}
