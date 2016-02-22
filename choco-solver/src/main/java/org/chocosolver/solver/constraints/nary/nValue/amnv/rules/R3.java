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
package org.chocosolver.solver.constraints.nary.nValue.amnv.rules;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.nValue.amnv.mis.F;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * R3 filtering rule (back-propagation)
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class R3 implements R {


    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private int[] valToRem;
    private ISet[] learntEqualities;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public R3(int nbDecVars, Model model) {
        n = nbDecVars;
        valToRem = new int[31];
        learntEqualities = new ISet[n];
        for (int i = 0; i < n; i++) {
            learntEqualities[i] = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void filter(IntVar[] vars, UndirectedGraph graph, F heur, Propagator aCause) throws ContradictionException {
        assert n == vars.length - 1;
        BitSet mis = heur.getMIS();
        if (mis.cardinality() == vars[n].getUB()) {
            for (int i = mis.nextClearBit(0); i >= 0 && i < n; i = mis.nextClearBit(i + 1)) {
                int mate = -1;
                int last = 0;
                if (valToRem.length < vars[i].getDomainSize()) {
                    valToRem = new int[vars[i].getDomainSize() * 2];
                }
                int ub = vars[i].getUB();
                int lb = vars[i].getLB();
                for (int k = lb; k <= ub; k = vars[i].nextValue(k)) {
                    valToRem[last++] = k;
                }
                for (int j : graph.getNeighOf(i)) {
                    if (mis.get(j)) {
                        if (mate == -1) {
                            mate = j;
                        } else if (mate >= 0) {
                            mate = -2;
                        }
                        for (int ik = 0; ik < last; ik++) {
                            if (vars[j].contains(valToRem[ik])) {
                                last--;
                                if (ik < last) {
                                    valToRem[ik] = valToRem[last];
                                    ik--;
                                }
                            }
                        }
                        if (mate == -2 && last == 0) break;
                    }
                }
                if (mate >= 0) {
                    enforceEq(i, mate, vars, aCause);
                } else {
                    for (int ik = 0; ik < last; ik++) {
                        vars[i].removeValue(valToRem[ik], aCause);
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j : learntEqualities[i]) {
                enforceEq(i, j, vars, aCause);
            }
        }
    }

    protected void enforceEq(int i, int j, IntVar[] vars, Propagator aCause) throws ContradictionException {
        if (i > j) {
            enforceEq(j, i, vars, aCause);
        } else {
            learntEqualities[i].add(j);
            learntEqualities[j].add(i);
            IntVar x = vars[i];
            IntVar y = vars[j];
            while (x.getLB() != y.getLB() || x.getUB() != y.getUB()) {
                x.updateLowerBound(y.getLB(), aCause);
                x.updateUpperBound(y.getUB(), aCause);
                y.updateLowerBound(x.getLB(), aCause);
                y.updateUpperBound(x.getUB(), aCause);
            }
            if (x.hasEnumeratedDomain() && y.hasEnumeratedDomain()) {
                int ub = x.getUB();
                for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                    if (!y.contains(val)) {
                        x.removeValue(val, aCause);
                    }
                }
                ub = y.getUB();
                for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                    if (!x.contains(val)) {
                        y.removeValue(val, aCause);
                    }
                }
            }
        }
    }

    @Override
    public R duplicate(Model model) {
        return new R3(n, model);
    }
}
