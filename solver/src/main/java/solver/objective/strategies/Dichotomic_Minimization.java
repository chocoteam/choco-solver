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

package solver.objective.strategies;

import choco.kernel.common.util.PoolManager;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.objective.MinObjectiveManager;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;

public class Dichotomic_Minimization extends AbstractStrategy<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int lb, ub;
    private IntVar obj;
    private long nbSols;
    private Solver solver;
    private PoolManager<FastDecision> pool;
    private boolean firstCall;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public Dichotomic_Minimization(IntVar obj, Solver solver) {
        super(new IntVar[]{obj});
        this.pool = new PoolManager<FastDecision>();
        this.solver = solver;
        this.obj = obj;
        this.firstCall = true;
        solver.getSearchLoop().restartAfterEachSolution(true);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void init() {
    }

    @Override
    public Decision getDecision() {
		if(firstCall){
			firstCall = false;
			lb = obj.getLB();
		}
		if(nbSols == solver.getMeasures().getSolutionCount()){
			return null;
		}else{
			nbSols = solver.getMeasures().getSolutionCount();
			ub = obj.getUB();
			lb = Math.max(lb,obj.getLB());//check
			MinObjectiveManager man = (MinObjectiveManager)solver.getSearchLoop().getObjectivemanager();
			man.updateLB(lb);
			if(lb==obj.getUB()){
				return null;
			}
			if(lb>ub){// we should post a cut instead
				solver.getSearchLoop().interrupt();
				return null;
			}
			int target;
			target = (lb+ub)/2;
//			if(target<lb+10){
//				System.out.println("dich");
//				target = (lb+ub)/2;
//			}
			System.out.println(lb+" : "+ub+" -> "+target);
			FastDecision dec = pool.getE();
			if(dec==null){
				dec = new FastDecision(pool);
			}
			dec.set(obj,target, objCut);
			return dec;
		}
	}

    private DecisionOperator<IntVar> objCut = new DecisionOperator<IntVar>() {
        @Override
        public void apply(IntVar var, int value, ICause cause) throws ContradictionException {
            var.updateUpperBound(value, cause);
        }

        @Override
        public void unapply(IntVar var, int value, ICause cause) throws ContradictionException {
            lb = value + 1;
            System.out.println("unapply objective decision");
            var.updateLowerBound(lb, cause);
			MinObjectiveManager man = (MinObjectiveManager)solver.getSearchLoop().getObjectivemanager();
			man.updateLB(lb);
			var.updateLowerBound(lb, cause);
        }

        @Override
        public String toString() {
            return " split ";
        }

        @Override
        public boolean isValid(IntVar var, int value) {
            return var.getUB() > value;
        }
    };
}
