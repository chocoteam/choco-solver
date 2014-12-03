/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.ACounter;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public class SequenceNeighborhood extends ANeighbor {


    protected int who;
    protected int count;
    protected INeighbor[] neighbors;
    protected int[] counters;

    public SequenceNeighborhood(INeighbor... neighbors) {
        super(null);
        this.neighbors = neighbors;
        who = 0;
        count = neighbors.length;
        counters = new int[count];
        counters[0] = -1;
    }

    @Override
    public void recordSolution() {
        counters[who]++;
        for (int i = 0; i < count; i++) {
            neighbors[i].recordSolution();
        }
        who = count - 1; // forces to start with the first neighbor
//        System.out.printf("%s %s\n", "% REPARTITION", Arrays.toString(counters));
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        nextNeighbor();
        if (who == count) who = 0;
        neighbors[who].fixSomeVariables(cause);
    }

    @Override
    public void restrictLess() {
        neighbors[who].restrictLess();
    }

    @Override
    public boolean isSearchComplete() {
        boolean isComplete = false;
        for (int i = 0; i < count; i++) {
            isComplete |= neighbors[i].isSearchComplete();
        }
        return isComplete;
    }

    @Override
    public void activeFastRestart() {
        for (int i = 0; i < count; i++) {
            neighbors[i].activeFastRestart();
        }
    }

    @Override
    public void fastRestart(ACounter counter) {
        for (int i = 0; i < count; i++) {
            neighbors[i].fastRestart(counter);
        }
    }

    protected void nextNeighbor(){
        who++;
    }
}
