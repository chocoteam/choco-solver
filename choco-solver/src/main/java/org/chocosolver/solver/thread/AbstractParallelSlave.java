/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.thread;

/**
 * Slave born to be mastered and work in parallel
 *
 * @author Jean-Guillaume Fages
 */
public abstract class AbstractParallelSlave<P extends AbstractParallelMaster> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    public P master;
    public final int id;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Create a slave born to be mastered and work in parallel
     *
     * @param master master solver
     * @param id     slave unique name
     */
    public AbstractParallelSlave(P master, int id) {
        this.master = master;
        this.id = id;
    }

    //***********************************************************************************
    // SUB-PROBLEM SOLVING
    //***********************************************************************************

    /**
     * Creates a new thread to work in parallel
     */
    public void workInParallel() {
        Thread t = new Thread() {
            @Override
            public void run() {
                work();
                master.wishGranted();
            }
        };
        t.start();
    }

    /**
     * do something
     */
    public abstract void work();
}
