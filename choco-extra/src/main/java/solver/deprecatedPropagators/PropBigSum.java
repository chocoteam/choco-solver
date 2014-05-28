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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 18/06/12
 * Time: 18:32
 */

package solver.deprecatedPropagators;

import memory.IEnvironment;
import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

/**
 * Sum constraint that ensure that the sum of integer variables vars is equal
 * to the integer sum
 * <p/>
 * use a tree representation to get a log behavior
 * <p/>
 * Should be used for large cases (vars.length>>100)
 * <p/>
 * <cpru> the data structure can be improved
 */
@Deprecated
public class PropBigSum extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    final int[] coeffs; // list of coefficients
    final int pos; // number of positive coefficients -- the first 'pos' elements of coeffs
    final int sum;
    Node root;
    Node[] leafs;
    int index;
	public static int BIG_SUM_SIZE = 160;
	public static int BIG_SUM_GROUP = 20;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Sum constraint that ensure that the sum of integer variables vars is equal
     * to the integer sum
     * <p/>
     * use a tree representation to get a log behavior
     * <p/>
     * Should be used for large cases (vars.length>>100)
     *
     * @param vars
     * @param sum
     */
    public PropBigSum(IntVar[] vars, int[] coeffs, int pos, int sum) {
        super(vars, PropagatorPriority.LINEAR, true);
        this.coeffs = coeffs;
        this.pos = pos;
        this.sum = sum;
        int nbLayers = computeNbLayers(this.vars.length);
        this.leafs = new Node[this.vars.length];
        this.root = new Node(nbLayers, null);
    }

    private int computeNbLayers(int nbElements) {
        if (nbElements < BIG_SUM_GROUP) {
            return 1;
        }
        int nb = nbElements / BIG_SUM_GROUP;
        if (nbElements % BIG_SUM_GROUP > 0) {
            nb++;
        }
        return computeNbLayers(nb) + 1;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // init structure
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            root.reset();
            for (int i = 0; i < pos; i++) {
                leafs[i].incLB(vars[i].getLB() * coeffs[i]);
                leafs[i].decUB(vars[i].getUB() * coeffs[i]);
            }
            for (int i = pos; i < vars.length; i++) {
                leafs[i].incLB(vars[i].getUB() * coeffs[i]);
                leafs[i].decUB(vars[i].getLB() * coeffs[i]);
            }
        }
        // filter
        int max = root.oldUB.get();
        filter_min(root, max);
        int min = root.oldLB.get();
        filter_max(root, root.oldLB.get());
        while (max != root.oldUB.get() || min != root.oldLB.get()) {
            if (max != root.oldUB.get()) {
                max = root.oldUB.get();
                filter_min(root, max);
            }
            if (min != root.oldLB.get()) {
                min = root.oldLB.get();
                filter_max(root, root.oldLB.get());
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int dub, dlb;
        if (idxVarInProp < pos) {
            dub = vars[idxVarInProp].getUB() * coeffs[idxVarInProp] - leafs[idxVarInProp].oldUB.get();
            dlb = vars[idxVarInProp].getLB() * coeffs[idxVarInProp] - leafs[idxVarInProp].oldLB.get();
        } else {
            dub = vars[idxVarInProp].getLB() * coeffs[idxVarInProp] - leafs[idxVarInProp].oldUB.get();
            dlb = vars[idxVarInProp].getUB() * coeffs[idxVarInProp] - leafs[idxVarInProp].oldLB.get();
        }
        if (dub != 0) {
            leafs[idxVarInProp].decUB(dub);
        }
        if (dlb != 0) {
            leafs[idxVarInProp].incLB(dlb);
        }
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.DECUPP.mask + EventType.INCLOW.mask;
    }

    @Override
    public ESat isEntailed() {
        int lb = 0;
        int ub = 0;
        for (int i = 0; i < pos; i++) {
            lb += vars[i].getLB() * coeffs[i];
            ub += vars[i].getUB() * coeffs[i];
        }
        for (int i = pos; i < vars.length; i++) {
            lb += vars[i].getUB() * coeffs[i];
            ub += vars[i].getLB() * coeffs[i];
        }
        if (ub == sum && lb == sum) {
            return ESat.TRUE;
        } else if (lb > sum || ub < sum) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    private void filter_min(Node node, int rootub) throws ContradictionException {
        if (rootub - node.oldUB.get() + node.oldLB.get() < sum) {
            int index = node.leafIndex;
            if (index == -1) {
                for (int i = 0; i < BIG_SUM_GROUP; i++) {
                    filter_min(node.childs[i], rootub);
                }
            } else {
                IntVar v = vars[index];
                int val;
                if (index < pos) {
                    val = divCeil(sum - rootub + node.oldUB.get(), coeffs[index]);
                    v.updateLowerBound(val, aCause);
                    val = v.getLB() * coeffs[index];
                } else {
                    val = divFloor(-(sum - rootub + node.oldUB.get()), -coeffs[index]);
                    v.updateUpperBound(val, aCause);
                    val = v.getUB() * coeffs[index];
                }

                node.incLB(val - node.oldLB.get());
            }
        }
    }

    private void filter_max(Node node, int rootlb) throws ContradictionException {
        if (rootlb - node.oldLB.get() + node.oldUB.get() > sum) {
            int index = node.leafIndex;
            if (index == -1) {
                for (int i = 0; i < BIG_SUM_GROUP; i++) {
                    filter_max(node.childs[i], rootlb);
                }
            } else {
                IntVar v = vars[index];
                int val;
                if (index < pos) {
                    val = divFloor(sum - rootlb + node.oldLB.get(), coeffs[index]);
                    v.updateUpperBound(val, aCause);
                    val = v.getUB() * coeffs[index];
                } else {
                    val = divCeil(-(sum - rootlb + node.oldLB.get()), -coeffs[index]);
                    v.updateLowerBound(val, aCause);
                    val = v.getLB() * coeffs[index];
                }
                node.decUB(val - node.oldUB.get());
            }
        }
    }

    private class Node {

        private Node father;
        private Node[] childs;
        private IStateInt oldLB, oldUB;
        private int leafIndex;

        private Node(int depth, Node father) {
            this.father = father;
            leafIndex = -1;
            if (depth > 0) {
                childs = new Node[BIG_SUM_GROUP];
                for (int i = 0; i < BIG_SUM_GROUP; i++) {
                    childs[i] = new Node(depth - 1, this);
                }
            } else if (index < vars.length) {
                leafIndex = index;
                leafs[index++] = this;
            }
			IEnvironment environment = solver.getEnvironment();
            oldLB = environment.makeInt();
            oldUB = environment.makeInt();
        }

        private void incLB(int delta) {
            oldLB.add(delta);
            if (father != null) {
                father.incLB(delta);
            }
        }

        private void decUB(int delta) {
            oldUB.add(delta);
            if (father != null) {
                father.decUB(delta);
            }
        }

        private void reset() {
            oldLB.set(0);
            oldUB.set(0);
            if (childs != null) {
                for (int i = 0; i < childs.length; i++) {
                    childs[i].reset();
                }
            }
        }
    }


    private int divFloor(int a, int b) {
        // <!> we assume b > 0
        if (a >= 0) {
            return (a / b);
        } else {
            return (a - b + 1) / b;
        }
    }

    private int divCeil(int a, int b) {
        // <!> we assume b > 0
        if (a >= 0) {
            return ((a + b - 1) / b);
        } else {
            return a / b;
        }
    }
}