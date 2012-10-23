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
import solver.objective.MinObjectiveManager;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;

public class BottomUp_Minimization extends AbstractStrategy<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

	private IntVar obj;
	private int val;
	private PoolManager<FastDecision> pool;
	private boolean firstCall;
	private int UB;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

	public BottomUp_Minimization(IntVar obj) {
		super(new IntVar[]{obj});
		this.obj = obj;
		firstCall = true;
		pool = new PoolManager<FastDecision>();
		// waits a first solution before triggering the bottom-up minimization
		obj.getSolver().getSearchLoop().restartAfterEachSolution(true);
		obj.getSolver().getSearchLoop().getLimitsBox().setSolutionLimit(2);
	}

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void init() {
    }

	@Override
	public Decision getDecision() {
		if(obj.getSolver().getMeasures().getSolutionCount()==0){
			return null;
		}
		if(obj.instantiated()){
			return null;
		}
		if(firstCall){
			firstCall = false;
			val = obj.getLB();
			UB = obj.getUB();
		}
		if(val>UB){
			return null;
		}
		val = Math.max(val,obj.getLB());
		MinObjectiveManager man = (MinObjectiveManager)obj.getSolver().getSearchLoop().getObjectivemanager();
		man.updateLB(val);
		System.out.println(obj.getLB()+" : "+obj.getUB()+" -> "+val+"  tps: "+(int)(obj.getSolver().getMeasures().getTimeCount()/1000)+"s");
		FastDecision dec = pool.getE();
		if(dec==null){
			dec = new FastDecision(pool);
		}
		///
//			String txt =  obj.getSolver().getMeasures().getFailCount() +";"+
//						  obj.getSolver().getMeasures().getNodeCount() + ";"+
//						  (int)(obj.getSolver().getMeasures().getTimeCount()) +";"+
//						  val+";\n";
//			HCP_Parser.writeTextInto(txt, "/Users/jfages07/Desktop/Evolution.csv");
			///
		dec.set(obj,val, DecisionOperator.int_eq);
		val ++;
		return dec;
	}
}
