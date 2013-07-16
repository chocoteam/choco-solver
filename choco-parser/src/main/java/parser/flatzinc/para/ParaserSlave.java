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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 15/07/13
 * Time: 16:31
 */

package parser.flatzinc.para;

import parser.flatzinc.ParseAndSolve;
import samples.sandbox.parallelism.AbstractParallelSlave;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.objective.IntObjectiveManager;
import solver.objective.ObjectiveManager;
import solver.search.loop.monitors.IMonitorSolution;
import java.io.IOException;

public class ParaserSlave extends AbstractParallelSlave<ParaserMaster>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	String[] args;
	Solver solver;
	ParseAndSolve PAS;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public ParaserSlave(ParaserMaster master, int id, String[] args){
		super(master,id);
		this.args = args;
		PAS = new ParseAndSolve();
		try {// sequential parsing (safer)
			PAS.parse(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void work() {
		try {
			// plug monitor to communicate bounds (should not be added in the sequential phasis)
			solver = PAS.getSolver();
			final ObjectiveManager om = solver.getSearchLoop().getObjectivemanager();
			if(om!=null && om.isOptimization()){
				solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
					@Override
					public void onSolution() {
						master.newSol(om.getBestSolutionValue().intValue(), om.getPolicy());
					}
				});
			}
			// go!
			PAS.solve();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void findBetterThan(int val, ResolutionPolicy policy) {
//		String operator = ">";
//		if(policy==ResolutionPolicy.MINIMIZE){
//			operator = "<";
//		}
		if(solver==null){// can happen if a solution is found before this thread is fully ready
			return;
		}
		IntObjectiveManager iom = (IntObjectiveManager) solver.getSearchLoop().getObjectivemanager();
		if(policy==ResolutionPolicy.MINIMIZE){
			iom.updateBestUB(val);
		}else{
			iom.updateBestLB(val);
		}
//		solver.postCut(ICF.arithm(iom.getObjective(), operator, val));
	}
}
