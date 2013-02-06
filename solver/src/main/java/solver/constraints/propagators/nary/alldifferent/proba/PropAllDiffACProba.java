/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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
package solver.constraints.propagators.nary.alldifferent.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.nary.alldifferent.PropAllDiffAC;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.util.Random;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 * <p/>
 * Uses Regin algorithm
 * Runs in O(m.n) worst case time for the initial propagation and then in O(n+m) time
 * per arc removed from the support
 * Has a good average behavior in practice
 * <p/>
 * Runs incrementally for maintaining a matching
 * <p/>
 *
 * @author Jean-Guillaume Fages
 */
public class PropAllDiffACProba extends PropAllDiffAC {

    private Random random;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
     *
     * @param vars
     * @param constraint
     * @param sol
     */
    public PropAllDiffACProba(IntVar[] vars, Constraint constraint, Solver sol, long seed) {
        super(vars, constraint, sol);
        random = new Random(seed);
    }

    @Override
    public boolean advise(int varIdx, int mask) {
        // BEWARE : le delta monitor a deja ete consomme!!
        if (super.advise(varIdx, mask)) {
            return random.nextBoolean();
        }
        return false;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            if (n2 < n * 2) {
                contradiction(null, "");
            }
            buildDigraph();
            for (int i = 0; i < idms.length; i++) {
                idms[i].unfreeze();
            }
        } else { // incremental
            free.clear();
            for (int i = 0; i < n; i++) {
                if (digraph.getPredecessorsOf(i).getSize() == 0) {
                    free.set(i);
                }
            }
            for (int i = n; i < n2; i++) {
                if (digraph.getSuccessorsOf(i).getSize() == 0) {
                    free.set(i);
                }
            }
        }
        repairMatching();
        filter();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }


    //***********************************************************************************
    // INFO
    //***********************************************************************************
    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropAllDiffACProba(");
        int i = 0;
        for (; i < Math.min(4, vars.length); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("...,");
        }
        st.append(vars[vars.length - 1].getName()).append(")");
        return st.toString();
    }
}
