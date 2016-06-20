/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.nary.cumulative;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.sort.ArraySort;

/**
 * @author Jean-Guillaume FAGES
 */
public class DisjunctiveTaskIntervalFilter extends CumulFilter{

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private TIntArrayList list = new TIntArrayList();

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public DisjunctiveTaskIntervalFilter(int nbMaxTasks, Propagator cause) {
        super(nbMaxTasks,cause);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks) throws ContradictionException {
        // filtering algorithm for disjunctive constraint
        capa.updateUpperBound(1,aCause);
        // remove tasks that do not consume any resource
        list.clear();
        for(int t:tasks){
            if(d[t].getLB()>0 && h[t].getLB()>0){
                list.add(t);
            }
        }
        int[] tsks = list.toArray();
        ArraySort sort = new ArraySort(tsks.length,false,true);
        sort.sort(tsks, tsks.length, (i1, i2) -> s[i1].getLB()-s[i2].getLB());
        // run energetic reasoning
        for (int x=0;x<tsks.length;x++){
            int task1 = tsks[x];
            for(int y=0;y<tsks.length;y++) {
                if (x != y) {
                    int task2 = tsks[y];
                    int t1 = s[task1].getLB();
                    int t2 = e[task2].getUB();
                    if (t1 < t2 && (t1 < e[task2].getLB() || t2 > s[task1].getUB())) {
                        int W = 0;
                        for (int task3 : tsks) {
                            if (task3 != task1 && task3 != task2) {
                                if (s[task3].getLB() >= t2) {
                                    break;
                                }
                                int pB = d[task3].getLB() * h[task3].getLB();
                                int pbt1 = Math.max(0, pB - Math.max(0, t1 - s[task3].getLB()));
                                int pbt2 = Math.max(0, pB - Math.max(0, e[task3].getUB() - t2));
                                int pbt = Math.min(pbt1, pbt2);
                                W += Math.min(t2 - t1, pbt);
                            }
                        }
                        if (W + d[task1].getLB() + d[task2].getLB() > t2 - t1) {
                            s[task1].updateLowerBound(e[task2].getLB(), aCause);
                            e[task2].updateUpperBound(s[task1].getUB(), aCause);
                        }
                    }
                }
            }
        }
    }
}