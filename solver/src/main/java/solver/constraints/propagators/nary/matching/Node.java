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

package solver.constraints.propagators.nary.matching;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.io.Serializable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 nov. 2010
 */
public class Node implements Serializable {

    IntVar var;

    IStateBitSet edges;

    int offset;

    int size;

    IStateInt refMatch;

    public Node(IntVar var, IEnvironment env) {
        this.var = var;
        offset = var.getLB();
        size = var.getUB() - offset + 1;
        edges = env.makeBitSet(size);
        int ub = var.getUB();
        for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
            edges.set(i - offset, true);
        }
        refMatch = env.makeInt(-1);
    }

    public int getSize() {
        return edges.cardinality();
    }

    public boolean contains(int i) {
        if (i - offset >= 0) {
            if (i - offset <= size) {
                return edges.get(i - offset);
            }
        }
        return false;
    }

    public void forceEdge(int i) {
        edges.clear();
        edges.set(i - offset, true);
    }

    public void setRefMatch(int i) {
        refMatch.set(i);
    }

    public int getRefMatch() {
        return refMatch.get();
    }

    public void removeEdge(int i) {
        edges.set(i - offset, false);
    }

    public boolean remove(int i, ICause cause) throws ContradictionException {
        boolean change = var.removeValue(i, cause, false);
        if (change) {
            removeEdge(i);
        }
        return change;
    }

    /**
     * Return the value after val (exclusive) or Integer.MIN_VALUE
     *
     * @param val initial value
     * @return a value or Integer.MIN_VALUE if doesn't exist
     */
    public int next(int val) {
        int w = edges.nextSetBit(val - offset + 1);
        return (w == -1 ? Integer.MIN_VALUE : w + offset);
    }

    /**
     * Return the value after val (exclusive) or Integer.MIN_VALUE
     *
     * @param val initial value
     * @return a value or Integer.MIN_VALUE if doesn't exist
     */
    public int previous(int val) {
        int w = edges.prevSetBit(val - offset - 1);
        return (w == -1 ? Integer.MIN_VALUE : w + offset);
    }

    public int[] edges(int minvalue) {
        int[] values = new int[edges.cardinality()];
        int j = 0;
        for (int i = edges.nextSetBit(0); i >= 0; i = edges.nextSetBit(i + 1)) {
            values[j++] = i + offset - minvalue;
        }
        return values;
    }

    public void check() {
        assert (var.getDomainSize() == edges.cardinality());
        int ub = var.getUB();
        for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
            assert (edges.get(i - offset));
        }
    }

    @Override
    public String toString() {
        return "NODE: " + var.toString() + " - " + offset + " - " + edges.toString() + " m:" + refMatch.get();
    }
}
