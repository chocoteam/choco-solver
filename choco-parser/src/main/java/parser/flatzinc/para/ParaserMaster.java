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

package parser.flatzinc.para;

import samples.sandbox.parallelism.*;
import solver.ResolutionPolicy;
import util.tools.ArrayUtils;

public class ParaserMaster extends AbstractParallelMaster<ParaserSlave>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int nbCores;
	public final static String[][] config = new String[][]{
			{},									// fix
			{"-lf"},							// fix+lf
			{"-lf","-lns","RLNS"},		// LNS random + fix + lf
			{"-lf","-lns","PGLNS"},		// LNS propag + fix + lf

//				{"-lf","-i","-bbss","1","-dv"},		// ABS on dec vars + lf
//				{"-lf","-i","-bbss","2","-dv"},	// IBS on dec vars + lf
//				{"-lf","-i","-bbss","3","-dv"},	// WDeg on dec vars + lf
	};

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public ParaserMaster(String[] args){
		nbCores = config.length;
		for(int i=0;i<args.length;i++){
			if(args[i].equals("-p")){
				// -p option defines the number of slaves
				nbCores = Math.min(nbCores,Integer.parseInt(args[i+1]));
				// each slave has one thread
				args[i+1] = "1";
				break;
			}
		}
		slaves = new ParaserSlave[nbCores];
		for(int i=0;i<nbCores;i++){
			String[] options = ArrayUtils.append(args,config[i]);
			slaves[i] = new ParaserSlave(this,i, options);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**
	 * A slave has CLOSED ITS SEARCH TREE, every one should stop!
	 */
	public synchronized void wishGranted() {
		System.exit(0);
	}

	/**
	 * A solution of cost val has been found
	 * informs slaves that they must find better
	 * @param val
	 * @param policy
	 */
	public synchronized void newSol(int val, ResolutionPolicy policy) {
		for (int i = 0; i < slaves.length; i++) {
			slaves[i].findBetterThan(val,policy);
		}
	}

	public static void main(String[] args){
		System.out.println("initial arguments");
		for(String s:args){
			System.out.println(s);
		}
	    ParaserMaster master = new ParaserMaster(args);
		System.out.println("launch parallel resolution");
		master.distributedSlavery();
	}
}
