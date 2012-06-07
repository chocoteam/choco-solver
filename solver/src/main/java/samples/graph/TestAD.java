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
 * Date: 06/06/12
 * Time: 15:39
 */

package samples.graph;

import solver.Solver;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

public class TestAD {

	public static void main(String[] args){

		System.out.println("Hello World");

		Solver solver = new Solver();
		final IntVar[] vars = new IntVar[5];
		vars[0] = VariableFactory.bounded("v0",4,5,solver);
		vars[1] = VariableFactory.bounded("v1",2,6,solver);
		vars[2] = VariableFactory.bounded("v2",7,7,solver);
		vars[3] = VariableFactory.bounded("v3",6,6,solver);
		vars[4] = VariableFactory.bounded("v4",4,5,solver);

		for(int i=0;i<5;i++){
			System.out.println(vars[i]);
		}

		solver.post(new AllDifferent(vars,solver,AllDifferent.Type.AC));
		solver.getSearchLoop().getLimitsBox().setNodeLimit(2);
		SearchMonitorFactory.log(solver, true, false);
		solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor(){
			public void afterInitialPropagation() {
				System.out.println("youhou");
				for(int i=0;i<5;i++){
					System.out.println(vars[i]);
				}
				System.exit(0);
			}
		});
		solver.findSolution();

		System.out.println("finish");
		for(int i=0;i<5;i++){
			System.out.println(vars[i]);
		}

	}

}
