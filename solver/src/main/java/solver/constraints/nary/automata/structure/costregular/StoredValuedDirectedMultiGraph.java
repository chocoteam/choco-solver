/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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

package solver.constraints.nary.automata.structure.costregular;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateDoubleVector;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import choco.kernel.memory.structure.StoredIndexedBipartiteSetWithOffset;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;
import org.jgrapht.graph.DirectedMultigraph;
import solver.constraints.nary.automata.structure.Node;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.BitSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 4, 2009
 * Time: 1:07:19 PM
 */
public class StoredValuedDirectedMultiGraph {

    int[] starts;
    int[] offsets;

    public int sourceIndex;
    public int tinkIndex;


    StoredIndexedBipartiteSetWithOffset[] supports;
    public int[][] layers;
    public BitSet inStack;
    public StoredIndexedBipartiteSet inGraph;
    public TIntStack toUpdateLeft;
    public TIntStack toUpdateRight;


    public class Nodes {
        public int[] states;
        public int[] layers;
        public StoredIndexedBipartiteSetWithOffset[] outArcs;
        public StoredIndexedBipartiteSetWithOffset[] inArcs;

        public IStateIntVector nextSP;
        public IStateIntVector prevSP;
        public IStateIntVector nextLP;
        public IStateIntVector prevLP;

        public IStateDoubleVector spfs;
        public IStateDoubleVector spft;
        public IStateDoubleVector lpfs;
        public IStateDoubleVector lpft;

    }


    public class Arcs {
        public int[] values;
        public int[] dests;
        public int[] origs;
        public double[] costs;
    }


    public Nodes GNodes;
    public Arcs GArcs;


    public StoredValuedDirectedMultiGraph(IEnvironment environment,
                                          DirectedMultigraph<Node, Arc> graph, int[][] layers, int[] starts,
                                          int[] offsets, int supportLength) {
        this.starts = starts;
        this.offsets = offsets;
        this.layers = layers;
        this.sourceIndex = layers[0][0];
        this.tinkIndex = layers[layers.length - 1][0];
        this.toUpdateLeft = new TIntStack();
        this.toUpdateRight = new TIntStack();

        this.GNodes = new Nodes();
        this.GArcs = new Arcs();

        TIntHashSet[] sups = new TIntHashSet[supportLength];
        this.supports = new StoredIndexedBipartiteSetWithOffset[supportLength];


        Set<Arc> arcs = graph.edgeSet();

        this.inStack = new BitSet(arcs.size());//constraint.getSolver().getEnvironment().makeBitSet(arcs.size());

        GArcs.values = new int[arcs.size()];
        GArcs.dests = new int[arcs.size()];
        GArcs.origs = new int[arcs.size()];
        GArcs.costs = new double[arcs.size()];


        int[] inginit = new int[arcs.size()];
        int tmp = 0;
        for (Arc a : arcs) {
            inginit[tmp++] = a.id;
            GArcs.values[a.id] = a.value;
            GArcs.dests[a.id] = a.dest.id;
            GArcs.origs[a.id] = a.orig.id;
            GArcs.costs[a.id] = a.cost;

            if (a.orig.layer < starts.length) {
                int idx = starts[a.orig.layer] + a.value - offsets[a.orig.layer];
                if (sups[idx] == null)
                    sups[idx] = new TIntHashSet();
                sups[idx].add(a.id);
            }

        }

        this.inGraph = new StoredIndexedBipartiteSet(environment, inginit);
        // this.inGraph = constraint.getSolver().getEnvironment().makeBitSet(arcs.size());
//        this.inGraph.set(0,arcs.size());
        // System.out.println(this.inGraph.size());
        for (int i = 0; i < sups.length; i++) {
            if (sups[i] != null)
                supports[i] = new StoredIndexedBipartiteSetWithOffset(environment, sups[i].toArray());
        }

        Set<Node> nodes = graph.vertexSet();
        GNodes.outArcs = new StoredIndexedBipartiteSetWithOffset[nodes.size()];
        GNodes.inArcs = new StoredIndexedBipartiteSetWithOffset[nodes.size()];
        GNodes.layers = new int[nodes.size()];
        GNodes.states = new int[nodes.size()];

        GNodes.prevLP = environment.makeIntVector(nodes.size(), Integer.MIN_VALUE);
        GNodes.nextLP = environment.makeIntVector(nodes.size(), Integer.MIN_VALUE);
        GNodes.prevSP = environment.makeIntVector(nodes.size(), Integer.MIN_VALUE);
        GNodes.nextSP = environment.makeIntVector(nodes.size(), Integer.MIN_VALUE);


        GNodes.lpfs = environment.makeDoubleVector(nodes.size(), Double.NEGATIVE_INFINITY);
        GNodes.lpft = environment.makeDoubleVector(nodes.size(), Double.NEGATIVE_INFINITY);
        GNodes.spfs = environment.makeDoubleVector(nodes.size(), Double.POSITIVE_INFINITY);
        GNodes.spft = environment.makeDoubleVector(nodes.size(), Double.POSITIVE_INFINITY);


        for (Node n : nodes) {
            GNodes.layers[n.id] = n.layer;
            GNodes.states[n.id] = n.state;

            int i;
            Set<Arc> outarc = graph.outgoingEdgesOf(n);
            if (!outarc.isEmpty()) {
                int[] out = new int[outarc.size()];
                i = 0;
                for (Arc a : outarc) {
                    out[i++] = a.id;
                }
                GNodes.outArcs[n.id] = new StoredIndexedBipartiteSetWithOffset(environment, out);
            }

            Set<Arc> inarc = graph.incomingEdgesOf(n);
            if (!inarc.isEmpty()) {
                int[] in = new int[inarc.size()];
                i = 0;
                for (Arc a : inarc) {
                    in[i++] = a.id;
                }
                GNodes.inArcs[n.id] = new StoredIndexedBipartiteSetWithOffset(environment, in);
            }
        }


        initPathInfo();

    }

    public void initPathInfo() {
        int start = layers[0][0];
        int end = layers[layers.length - 1][0];
        GNodes.spfs.quickSet(start, 0.0);
        GNodes.lpfs.quickSet(start, 0.0);
        GNodes.spft.quickSet(end, 0.0);
        GNodes.lpft.quickSet(end, 0.0);


        for (int i = 1; i < layers.length; i++) {
            int[] layer = layers[i];
            for (int q : layer) {
                DisposableIntIterator it = GNodes.inArcs[q].getIterator();
                while (it.hasNext()) {
                    int arc = it.next();
                    double acost = GArcs.costs[arc];
                    int orig = GArcs.origs[arc];
                    double otherS = GNodes.spfs.quickGet(orig) + acost;
                    if (otherS < GNodes.spfs.quickGet(q)) {
                        GNodes.spfs.quickSet(q, otherS);
                        GNodes.prevSP.quickSet(q, arc);
                    }

                    double otherL = GNodes.lpfs.quickGet(orig) + acost;
                    if (otherL > GNodes.lpfs.quickGet(q)) {
                        GNodes.lpfs.quickSet(q, otherL);
                        GNodes.prevLP.quickSet(q, arc);
                    }

                }
                it.dispose();

            }

        }

        for (int i = layers.length - 2; i >= 0; i--) {
            int[] layer = layers[i];
            for (int q : layer) {
                DisposableIntIterator it = GNodes.outArcs[q].getIterator();
                while (it.hasNext()) {
                    int arc = it.next();
                    double acost = GArcs.costs[arc];
                    int dest = GArcs.dests[arc];
                    double otherS = GNodes.spft.quickGet(dest) + acost;
                    if (otherS < GNodes.spft.quickGet(q)) {
                        GNodes.spft.quickSet(q, otherS);
                        GNodes.nextSP.quickSet(q, arc);
                    }

                    double otherL = GNodes.lpft.quickGet(dest) + acost;
                    if (otherL > GNodes.lpft.quickGet(q)) {
                        GNodes.lpft.quickSet(q, otherL);
                        GNodes.nextLP.quickSet(q, arc);
                    }
                }
                it.dispose();
            }
        }

        //   System.out.println(GNodes.lpfs.get(end));
        //   System.out.println(GNodes.lpft.get(start));


    }

    public final StoredIndexedBipartiteSetWithOffset getSupport(int i, int j) {
        int idx = starts[i] + j - offsets[i];
        return supports[idx];


    }


    public void removeArc(int arcId, TIntStack toRemove, Propagator<IntVar> propagator) throws ContradictionException {
        clearInStack(arcId);
        inGraph.remove(arcId);

        int orig = GArcs.origs[arcId];
        int dest = GArcs.dests[arcId];

        int layer = GNodes.layers[orig];
        int value = GArcs.values[arcId];


        if (layer < starts.length) {
            StoredIndexedBipartiteSetWithOffset support = getSupport(layer, value);
            support.remove(arcId);

            if (support.isEmpty()) {
                IntVar var = propagator.getVar(layer);
                var.removeValue(value, propagator, false);
            }
        }

        StoredIndexedBipartiteSetWithOffset out = GNodes.outArcs[orig];
        StoredIndexedBipartiteSetWithOffset in;

        out.remove(arcId);

        in = GNodes.inArcs[dest];
        in.remove(arcId);


        if (GNodes.nextSP.quickGet(orig) == arcId || GNodes.nextLP.quickGet(orig) == arcId) {
            updateRight(orig, toRemove, propagator);
        }
        if (GNodes.prevSP.quickGet(dest) == arcId || GNodes.prevLP.quickGet(dest) == arcId) {
            updateLeft(dest, toRemove, propagator);
        }


    }

    public void updateRight(int nid, TIntStack toRemove, Propagator<IntVar> propagator) {

        double tempPval = Double.POSITIVE_INFINITY;
        double tempPval2 = Double.NEGATIVE_INFINITY;
        int tempP = Integer.MIN_VALUE;
        int temp2 = Integer.MIN_VALUE;
        DisposableIntIterator it = GNodes.outArcs[nid].getIterator();


        while (it.hasNext()) {
            int arcId = it.next();
            int dest = GArcs.dests[arcId];
            double spft = GNodes.spft.quickGet(dest) + GArcs.costs[arcId];
            if (tempPval > spft) {
                tempPval = spft;
                tempP = arcId;
            }

            double lpft = GNodes.lpft.quickGet(dest) + GArcs.costs[arcId];
            if (tempPval2 < lpft) {
                tempPval2 = lpft;
                temp2 = arcId;
            }


        }
        it.dispose();
        double old = GNodes.spft.quickSet(nid, tempPval);
        GNodes.nextSP.quickSet(nid, tempP);

        double old2 = GNodes.lpft.quickSet(nid, tempPval2);
        GNodes.nextLP.quickSet(nid, temp2);

        if (nid != sourceIndex && (old != tempPval || old2 != tempPval2)) {
            it = GNodes.inArcs[nid].getIterator();

            while (it.hasNext()) {
                int arcId = it.next();
                int orig = GArcs.origs[arcId];
                if ((GNodes.nextSP.quickGet(orig) == arcId && old != tempPval) || (old2 != tempPval2 && GNodes.nextLP.quickGet(orig) == arcId)) {
                    toUpdateRight.push(orig);
                    //updateRight(orig,toRemove);
                }
                double spfs = GNodes.spfs.quickGet(orig);
                double lpfs = GNodes.lpfs.quickGet(orig);

                double acost = GArcs.costs[arcId];
                if (!isInStack(arcId) && (tempPval + spfs + acost > propagator.getVar(starts.length).getUB()
                        || tempPval2 + lpfs + acost < propagator.getVar(starts.length).getLB())) {
                    setInStack(arcId);
                    toRemove.push(arcId);
                }
            }
            it.dispose();
        }


    }


    public void updateSPFT(int nid, TIntStack toRemove, Propagator<IntVar> propagator) {


        double tempPval = Double.POSITIVE_INFINITY;
        int tempP = Integer.MIN_VALUE;
        DisposableIntIterator it = GNodes.outArcs[nid].getIterator();


        while (it.hasNext()) {
            int arcId = it.next();
            int dest = GArcs.dests[arcId];
            double spft = GNodes.spft.quickGet(dest) + GArcs.costs[arcId];
            if (tempPval > spft) {
                tempPval = spft;
                tempP = arcId;
            }

        }
        it.dispose();
        double old = GNodes.spft.quickSet(nid, tempPval);
        GNodes.nextSP.quickSet(nid, tempP);

        if (nid != sourceIndex && old != tempPval) {
            it = GNodes.inArcs[nid].getIterator();

            while (it.hasNext()) {
                int arcId = it.next();
                int orig = GArcs.origs[arcId];
                if (GNodes.nextSP.quickGet(orig) == arcId) {
                    updateSPFT(orig, toRemove, propagator);
                }
                double spfs = GNodes.spfs.quickGet(orig);
                double acost = GArcs.costs[arcId];
                if (!isInStack(arcId) && tempPval + spfs + acost > propagator.getVar(starts.length).getUB()) {
                    setInStack(arcId);
                    toRemove.push(arcId);
                }
            }
            it.dispose();
        }

    }

    public void updateLPFT(int nid, TIntStack toRemove, Propagator<IntVar> propagator) {


        double tempPval = Double.NEGATIVE_INFINITY;
        int tempP = Integer.MIN_VALUE;
        DisposableIntIterator it = GNodes.outArcs[nid].getIterator();


        while (it.hasNext()) {
            int arcId = it.next();
            int dest = GArcs.dests[arcId];
            double lpft = GNodes.lpft.quickGet(dest) + GArcs.costs[arcId];
            if (tempPval < lpft) {
                tempPval = lpft;
                tempP = arcId;
            }

        }
        it.dispose();
        double old = GNodes.lpft.quickSet(nid, tempPval);
        GNodes.nextLP.quickSet(nid, tempP);

        if (nid != sourceIndex && old != tempPval) {
            it = GNodes.inArcs[nid].getIterator();

            while (it.hasNext()) {
                int arcId = it.next();
                int orig = GArcs.origs[arcId];
                if (GNodes.nextLP.quickGet(orig) == arcId) {
                    updateLPFT(orig, toRemove, propagator);
                }
                double lpfs = GNodes.lpfs.quickGet(orig);
                double acost = GArcs.costs[arcId];
                if (!isInStack(arcId) && tempPval + lpfs + acost < propagator.getVar(starts.length).getLB()) {
                    setInStack(arcId);
                    toRemove.push(arcId);
                }
            }
            it.dispose();
        }

    }

    public void updateLeft(int nid, TIntStack toRemove, Propagator<IntVar> propagator) {
        double tempPval = Double.POSITIVE_INFINITY;
        int tempP = Integer.MIN_VALUE;

        double tempPval2 = Double.NEGATIVE_INFINITY;
        int tempP2 = Integer.MIN_VALUE;

        DisposableIntIterator it = GNodes.inArcs[nid].getIterator();


        while (it.hasNext()) {
            int arcId = it.next();
            int orig = GArcs.origs[arcId];
            double spfs = GNodes.spfs.quickGet(orig) + GArcs.costs[arcId];
            if (tempPval > spfs) {
                tempPval = spfs;
                tempP = arcId;
            }
            double lpfs = GNodes.lpfs.quickGet(orig) + GArcs.costs[arcId];
            if (tempPval2 < lpfs) {
                tempPval2 = lpfs;
                tempP2 = arcId;
            }

        }

        it.dispose();
        double old = GNodes.spfs.quickSet(nid, tempPval);
        GNodes.prevSP.quickSet(nid, tempP);
        double old2 = GNodes.lpfs.quickSet(nid, tempPval2);
        GNodes.prevLP.quickSet(nid, tempP2);

        if (nid != tinkIndex && (old != tempPval || old2 != tempPval2)) {
            it = GNodes.outArcs[nid].getIterator();

            while (it.hasNext()) {
                int arcId = it.next();
                int dest = GArcs.dests[arcId];
                if ((old != tempPval && GNodes.prevSP.quickGet(dest) == arcId) || (old2 != tempPval2 && GNodes.prevLP.quickGet(dest) == arcId)) {
                    // updateLeft(dest,toRemove);
                    toUpdateLeft.push(dest);
                }
                double spft = GNodes.spft.quickGet(dest);
                double acost = GArcs.costs[arcId];
                double lpft = GNodes.lpft.quickGet(dest);
                if (!isInStack(arcId) && (tempPval + spft + acost > propagator.getVar(starts.length).getUB()
                        || tempPval2 + lpft + acost < propagator.getVar(starts.length).getLB())) {
                    setInStack(arcId);
                    toRemove.push(arcId);
                }
            }
            it.dispose();
        }


    }

    public void updateSPFS(int nid, TIntStack toRemove, Propagator<IntVar> propagator) {


        double tempPval = Double.POSITIVE_INFINITY;
        int tempP = Integer.MIN_VALUE;
        DisposableIntIterator it = GNodes.inArcs[nid].getIterator();


        while (it.hasNext()) {
            int arcId = it.next();
            int orig = GArcs.origs[arcId];
            double spfs = GNodes.spfs.quickGet(orig) + GArcs.costs[arcId];
            if (tempPval > spfs) {
                tempPval = spfs;
                tempP = arcId;
            }

        }
        it.dispose();
        double old = GNodes.spfs.quickSet(nid, tempPval);
        GNodes.prevSP.quickSet(nid, tempP);

        if (nid != tinkIndex && old != tempPval) {
            it = GNodes.outArcs[nid].getIterator();

            while (it.hasNext()) {
                int arcId = it.next();
                int dest = GArcs.dests[arcId];
                if (GNodes.prevSP.quickGet(dest) == arcId) {
                    updateSPFS(dest, toRemove, propagator);
                }
                double spft = GNodes.spft.quickGet(dest);
                double acost = GArcs.costs[arcId];
                if (!isInStack(arcId) && tempPval + spft + acost > propagator.getVar(starts.length).getUB()) {
                    setInStack(arcId);
                    toRemove.push(arcId);
                }
            }
            it.dispose();
        }

    }

    public void updateLPFS(int nid, TIntStack toRemove, Propagator<IntVar> propagator) {


        double tempPval = Double.NEGATIVE_INFINITY;
        int tempP = Integer.MIN_VALUE;
        DisposableIntIterator it = GNodes.inArcs[nid].getIterator();


        while (it.hasNext()) {
            int arcId = it.next();
            int orig = GArcs.origs[arcId];
            double lpfs = GNodes.lpfs.quickGet(orig) + GArcs.costs[arcId];
            if (tempPval < lpfs) {
                tempPval = lpfs;
                tempP = arcId;
            }

        }
        it.dispose();
        double old = GNodes.lpfs.quickSet(nid, tempPval);
        GNodes.prevLP.quickSet(nid, tempP);


        if (nid != tinkIndex && old != tempPval) {
            it = GNodes.outArcs[nid].getIterator();

            while (it.hasNext()) {
                int arcId = it.next();
                int dest = GArcs.dests[arcId];
                if (GNodes.prevLP.quickGet(dest) == arcId) {
                    updateLPFS(dest, toRemove, propagator);
                }
                double lpft = GNodes.lpft.quickGet(dest);
                double acost = GArcs.costs[arcId];
                if (!isInStack(arcId) && tempPval + lpft + acost < propagator.getVar(starts.length).getLB()) {
                    setInStack(arcId);
                    toRemove.push(arcId);
                }
            }
            it.dispose();
        }

    }


    /**
     * Getter, the idx th bit of the inStack bitSet
     *
     * @param idx the index of the arc
     * @return true if a given arc is to be deleted
     */
    public final boolean isInStack(int idx) {
        return inStack.get(idx);
    }

    /**
     * Set the idx th bit of the to be removed bitset
     *
     * @param idx the index of the bit
     */
    public final void setInStack(int idx) {
        inStack.set(idx);
    }

    /**
     * Clear the idx th bit of the to be removed bitset
     *
     * @param idx the index of the bit
     */
    public final void clearInStack(int idx) {
        inStack.clear(idx);
    }

}