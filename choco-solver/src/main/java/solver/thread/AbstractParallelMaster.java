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

package solver.thread;

/**
 * Master a set of slaves which will work in parallel
 *
 * @param <S>
 */
public class AbstractParallelMaster<S extends AbstractParallelSlave> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

	public S[] slaves;
	public int nbWorkingSlaves;
	public Thread mainThread;
    public boolean wait;

    public AbstractParallelMaster() {
        mainThread = Thread.currentThread();
    }

    //***********************************************************************************
    // DISTRIBUTED METHODS
    //***********************************************************************************

    /**
     * Make the slaves work in parallel
     */
    public void distributedSlavery() {
        nbWorkingSlaves = slaves.length;
        for (int i = 0; i < slaves.length; i++) {
            slaves[i].workInParallel();
        }
        wait = true;
        try {
            while (wait)
                mainThread.sleep(20);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Make the slaves work in sequence
     */
    public void sequentialSlavery() {
        nbWorkingSlaves = slaves.length;
        for (int i = 0; i < slaves.length; i++) {
            slaves[i].work();
        }
    }

    /**
     * A slave notify the master that he fulfilled his task
     */
    public synchronized void wishGranted() {
        nbWorkingSlaves--;
        if (nbWorkingSlaves == 0) {
            wait = false;
        }
    }
}