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
 * Date: 22/05/12
 * Time: 17:54
 */

package samples.parallel.schema;

/**Slave born to be mastered and solve sub-problems in parallel
 * @author Jean-Guillaume Fages
 */
public abstract class AbstractParallelSlave {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private AbstractParallelMaster master;
	public final int id;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Create a slave born to be mastered and solve sub-problems in parallel
	 * @param master
	 * @param id slave unique name
	 */
	public AbstractParallelSlave(AbstractParallelMaster master, int id){
		this.master = master;
		this.id = id;
	}

	//***********************************************************************************
	// SUB-PROBLEM SOLVING
	//***********************************************************************************

	/**
	 * Creates a new thread to solve the sub-problem
	 */
	public void solveSubProblemInParallel() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				solveSubProblem();
				master.wishGranted();
			}
		});
		t.start();
	}

	/**
	 * Model and solve the subproblem
	 */
	public abstract void solveSubProblem();
}
