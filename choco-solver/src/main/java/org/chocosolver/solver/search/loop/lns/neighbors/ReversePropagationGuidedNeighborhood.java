/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

/**
 * A Propagation Guided LNS
 * <p/>
 * Based on "Propagation Guided Large Neighborhood Search", Perron et al. CP2004.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/04/13
 */
public class ReversePropagationGuidedNeighborhood extends PropagationGuidedNeighborhood {

    public ReversePropagationGuidedNeighborhood(IntVar[] vars, int fgmtSize, int listSize, long seed) {
        super(vars, fgmtSize, listSize, seed);
    }

    @Override
    protected void update() throws ContradictionException {
        while (logSum > fgmtSize && fragment.cardinality() > 0) {
            all.clear();
            // 1. pick a variable
            int id = selectVariable();

            // 2. fix it to its solution value
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related

                mModel.getEnvironment().worldPush();
                vars[id].instantiateTo(bestSolution[id], Cause.Null);
                mModel.getSolver().propagate();
                fragment.clear(id);

                for (int i = 0; i < n; i++) {
                    int ds = vars[i].getDomainSize();
                    if (fragment.get(i)) { // if not frozen until now
                        if (ds == 1) { // if fixed by side effect
                            fragment.clear(i); // set it has fixed
                        } else {
                            int closeness = (int) ((dsize[i] - ds) / (dsize[i] * 1.) * 100);
                            //                            System.out.printf("%d -> %d :%d\n", dsize[i], ds, closeness);
                            if (closeness > 0) {
                                all.put(i, Integer.MAX_VALUE - closeness); // add it to candidate list
                            }
                        }
                    }
                }
                mModel.getEnvironment().worldPop();
                candidate.clear();
                int k = 1;
                while (!all.isEmpty() && candidate.size() < listSize) {
                    int first = all.firstKey();
                    all.remove(first);
                    if (fragment.get(first)) {
                        candidate.put(first, k++);
                    }
                }
                logSum = 0;
                for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
                    logSum += Math.log(vars[i].getDomainSize());
                }
            } else {
                fragment.clear(id);
            }

        }
        for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
            if (vars[i].contains(bestSolution[i])) {
                impose(i);
            }
        }
        mModel.getSolver().propagate();

        logSum = 0;
        for (int i = 0; i < n; i++) {
            logSum += Math.log(vars[i].getDomainSize());
        }
    }
}
