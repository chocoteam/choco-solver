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
import util.tools.ArrayUtils;

public class ParaserMaster extends AbstractParallelMaster<ParaserSlave>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int nbCores;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

//	public ParaserMaster(int nb, String[] args){
//		nbCores = nb;
//		slaves = new ParaserSlave[nbCores];
//		for(int i=0;i<nbCores;i++){
//			slaves[i] = new ParaserSlave(this,i,args);
//		}
//	}

	public ParaserMaster(String[] args){
		String[][] config = new String[][]{
				{},
				{"-lf"},
//				{"-lf","-i","-bbss","1","-dv"},
//				{"-lf","-i","-bbss","2","-dv"},
//				{"-lf","-i","-bbss","3","-dv"},
				{"-lf","-lns","RLNS","-dv"},
//				{"-lf","-lns","PGLNS","-dv"}
		};
		nbCores = config.length;
		slaves = new ParaserSlave[nbCores];
		for(int i=0;i<nbCores;i++){
			String[] options = ArrayUtils.append(args,config[i]);
			slaves[i] = new ParaserSlave(this,i, options);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public synchronized void wishGranted() {
		// once a slave has CLOSED THE SEARCH TREE, every one should stop!
		System.exit(0);
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
