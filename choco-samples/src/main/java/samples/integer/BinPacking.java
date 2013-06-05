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
 * Date: 05/06/13
 * Time: 14:48
 */

package samples.integer;

import samples.AbstractProblem;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.ICF;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;
import util.tools.ArrayUtils;

/**
 * Bin packing example
 * @author Jean-Guillaume Fages
 */
public class BinPacking extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int nbItems;
	IntVar[] bins;
	int[] weights;
	int nbBins;
	IntVar[] loads;
	IntVar minLoad;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void createSolver() {
		solver = new Solver("bin packing sample");
	}

	@Override
	public void buildModel() {
		// input
		nbItems = d1_w.length;
		weights = d1_w;
		nbBins  = d1_nb;
		// variables
		int offset = 0;
		bins = VF.enumeratedArray("bin",nbItems,offset,offset+nbBins-1,solver);
		loads= VF.boundedArray("load",nbBins,0,1000,solver);
		minLoad = VF.bounded("minLoad",0,1000,solver);
		// created variables
		BoolVar[][] xbi = VF.boolMatrix("xbi",nbItems,nbBins,solver);
		int sum = 0;
		for(int w:weights){
			sum += w;
		}
		IntVar sumView = VF.fixed(sum,solver);
		// constraints
		solver.post(ICF.sum(loads,sumView));
		for(int i=0;i<nbItems;i++){
			solver.post(ICF.boolean_channeling(ArrayUtils.getColumn(xbi,i),bins[i],offset));
		}
		for(int b=0;b<nbBins;b++){
			solver.post(ICF.scalar(xbi[b],weights,loads[b]));
		}
		solver.post(ICF.minimum(minLoad,loads));
	}

	@Override
	public void configureSearch() {

	}

	@Override
	public void solve() {
		solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE,minLoad);
	}

	@Override
	public void prettyOut() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	//***********************************************************************************
	// DATA
	//***********************************************************************************

	public static void main(String[] args){
	    new BinPacking().execute(args);
	}
	private final static int[] d1_w = new int[]{
			2,5,3,4,12,9,1,0,5,6,2,4,6,7,3,13,5,18,3,9,4,12,11,1
	};
	private final static int d1_nb = 4;
}
