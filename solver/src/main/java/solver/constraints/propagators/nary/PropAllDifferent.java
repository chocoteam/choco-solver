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

package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.memory.IEnvironment;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.matching.AbstractBipartiteMatching;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * <p/>
 * Filter on ALLDIFFERENT(X[])
 */
@Deprecated
public class PropAllDifferent extends AbstractBipartiteMatching {


    public PropAllDifferent(IntVar[] vars, IEnvironment environment, IntConstraint constraint) {
        super(vars.clone(), vars.length, PropAllDifferent.getValueGap(vars), environment, constraint,
                PropagatorPriority.CUBIC, true);
        minValue = Integer.MAX_VALUE;
        maxValue = Integer.MIN_VALUE;
        for (IntVar var : vars) {
            minValue = Math.min(var.getLB(), minValue);
            maxValue = Math.max(var.getUB(), maxValue);
        }
    }

    @Override
    protected void reinit() {
        super.reinit();
        minValue = Integer.MAX_VALUE;
        maxValue = Integer.MIN_VALUE;
        for (IntVar var : vars) {
            minValue = Math.min(var.getLB(), minValue);
            maxValue = Math.max(var.getUB(), maxValue);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        // the propagator does not react on bound events, but the constraint has to simulate them.
        // (convertion from bound events to remaval events).
        return EventType.ALL_MASK();
    }


    /**
     * Static method for one parameter constructor
     *
     * @param vars domain variable list
     * @return gap between min and max value
     */
    private static int getValueGap(IntVar[] vars) {
        int minValue = Integer.MAX_VALUE, maxValue = Integer.MIN_VALUE;
        for (IntVar var : vars) {
            minValue = Math.min(var.getLB(), minValue);
            maxValue = Math.max(var.getUB(), maxValue);
        }
        return maxValue - minValue + 1;
    }


    // The next two functions implement the main two events:

    /**
     * when an edge is definitely chosen in the bipartite assignment graph.
     *
     * @param i idx src
     * @param j idx dest
     * @throws solver.exception.ContradictionException
     *          fail
     */
    public void setEdgeAndPublish(int i, int j) throws ContradictionException {
        this.setMatch(i, j);
        for (int i2 = 0; i2 < this.nbLeftVertices; i2++) {
            if (i2 != i) {
                if (this.vars[i2].removeValue(j + this.minValue, this)) {
                    this.deleteMatch(i2, j);
                }
            }
        }
    }

    /**
     * when an edge is definitely removed from the bipartite assignment graph.
     *
     * @param i          idx src
     * @param j          idx dest
     * @throws ContradictionException
     */
    public boolean deleteEdgeAndPublish(int i, int j) throws ContradictionException {
        this.vars[i].removeValue(j + this.minValue, this);
        return this.deleteMatch(i, j);
    }

    // propagation functions: reacting to choco events

    /**
     * when a value is removed from a domain var, removed the corresponding edge in current matching
     *
     * @param idx the variable index
     * @param val the removed value
     */
    protected void awakeOnRem(int idx, int val) throws ContradictionException {
        this.deleteMatch(idx, val - this.minValue);
    }

    /**
     * update current matching when a variable has been instantiated
     *
     * @param idx the variable index
     * @throws ContradictionException
     */
    protected void awakeOnInst(int idx) throws ContradictionException {
        this.setEdgeAndPublish(idx, this.vars[idx].getValue() - this.minValue);
    }

    @Override
    public ESat isEntailed() {
        //throw new UnsupportedOperationException();
        return ESat.UNDEFINED;
    }
}
