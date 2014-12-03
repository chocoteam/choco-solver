/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.extension.nary;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.StoredSparseSet;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;

/**
 * Implementation based on "Maintaining GAC on adhoc r-ary constraints", Cheng and Yap, CP12.
 * <p/>
 * Created by cprudhom on 04/11/14.
 * Project: choco.
 */
public class PropLargeMDDC extends Propagator<IntVar> {

    final TIntSet yes;
    final TIntSet[] sets;
    final StoredSparseSet no;
    final MultivaluedDecisionDiagram MDD;
    final int nvars;

    /**
     * Create a propagator maintaining GAC based on a MDD.
     * The MDD can be shared between multiple propagators, no copy is achieved.
     *
     * @param MDD  Multi-valued Decision Diagram, stores the solutions
     * @param VARS the related variables -- the order is important, and need to match the MDD.
     */
    public PropLargeMDDC(MultivaluedDecisionDiagram MDD, IntVar... VARS) {
        super(VARS, PropagatorPriority.QUADRATIC, false);
        this.MDD = MDD;
        this.nvars = vars.length;
        this.yes = new TIntHashSet();
        this.no = new StoredSparseSet(VARS[0].getSolver().getEnvironment());
        this.sets = new TIntHashSet[nvars];
        for (int i = 0; i < nvars; i++) {
            this.sets[i] = new TIntHashSet(vars[i].getDomainSize());
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        mddc();
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int[] diag = MDD.getDiagram();
            int l = 0;
            int n = vars[l].getValue() - MDD.getOffset(l);
            while (l < nvars - 1 && diag[n] > 0) {
                l++;
                n = diag[n] + vars[l].getValue() - MDD.getOffset(l);
            }
            return ESat.eval(l == nvars - 1 && diag[n] == MultivaluedDecisionDiagram.TERMINAL);
        }
        return ESat.UNDEFINED;
    }


    protected void mddc() throws ContradictionException {
        yes.clear();
        for (int i = 0; i < nvars; i++) {
            sets[i].clear();
            int o = MDD.getOffset(i);
            int UB = vars[i].getUB();
            for (int j = vars[i].getLB(); j <= UB; j = vars[i].nextValue(j)) {
                sets[i].add(j - o);
            }
        }
        mddcSeekSupport(0, 0);
        for (int i = 0; i < nvars; i++) {
            int o = MDD.getOffset(i);
            int[] values = sets[i].toArray();
            for (int j = 0; j < values.length; j++) {
                vars[i].removeValue(values[j] + o, this);
            }
        }
    }

    protected boolean mddcSeekSupport(int node, int layer) {
        // If the node has already been visited
        if (yes.contains(node)) return true;
        if (no.contains(node)) return false;
        // otherwise ...
        boolean res = false;
        // get the initial LB of the variable, required for 'contains'
        int o = MDD.getOffset(layer);
        for (int i = 0; i < MDD.getNodeSize(layer); i++) { // for all node of the same layer
            int sG = MDD.getEdge(node + i); // get the sub-mmd induced
            // if there is a sub-mdds valid
            if (sG != MultivaluedDecisionDiagram.EMPTY && vars[layer].contains(i + o)
                    && (sG == MultivaluedDecisionDiagram.TERMINAL || mddcSeekSupport(sG, layer + 1))) {
                res = true;
                sets[layer].remove(i);
                int l2 = layer;
                while (l2 < nvars && sets[l2].isEmpty()) {
                    l2++;
                }
                if (l2 == nvars) break;
            }
        }
        if (res) {
            yes.add(node);
        } else {
            no.add(node);
        }
        return res;
    }


    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            identitymap.put(this, new PropLargeMDDC(MDD.duplicate(), aVars));
        }
    }
}
