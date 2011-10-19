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

package solver.constraints.nary.automata.structure.regular;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.StoredIndexedBipartiteSetWithOffset;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;
import org.jgrapht.graph.DirectedMultigraph;
import solver.constraints.nary.automata.structure.Node;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 4, 2009
 * Time: 1:07:19 PM
 */
public class StoredDirectedMultiGraph {

    int[] starts;
    int[] offsets;
    TIntStack stack = new TIntStack();


    StoredIndexedBipartiteSetWithOffset[] supports;


    class Nodes {
        int[] states;
        int[] layers;
        StoredIndexedBipartiteSetWithOffset[] outArcs;
        StoredIndexedBipartiteSetWithOffset[] inArcs;
    }


    public class Arcs {
        int[] values;
        int[] dests;
        int[] origs;
    }


    public Nodes GNodes;
    public Arcs GArcs;


    public StoredDirectedMultiGraph(IEnvironment environment,
                                    DirectedMultigraph<Node, Arc> graph, int[] starts, int[] offsets, int supportLength) {
        this.starts = starts;
        this.offsets = offsets;

        this.GNodes = new Nodes();
        this.GArcs = new Arcs();

        TIntHashSet[] sups = new TIntHashSet[supportLength];
        this.supports = new StoredIndexedBipartiteSetWithOffset[supportLength];


        Set<Arc> arcs = graph.edgeSet();

        GArcs.values = new int[arcs.size()];
        GArcs.dests = new int[arcs.size()];
        GArcs.origs = new int[arcs.size()];

        for (Arc a : arcs) {
            GArcs.values[a.id] = a.value;
            GArcs.dests[a.id] = a.dest.id;
            GArcs.origs[a.id] = a.orig.id;

            int idx = starts[a.orig.layer] + a.value - offsets[a.orig.layer];
            if (sups[idx] == null)
                sups[idx] = new TIntHashSet();
            sups[idx].add(a.id);


        }

        for (int i = 0; i < sups.length; i++) {
            if (sups[i] != null)
                supports[i] = new StoredIndexedBipartiteSetWithOffset(environment, sups[i].toArray());
        }

        Set<Node> nodes = graph.vertexSet();
        GNodes.outArcs = new StoredIndexedBipartiteSetWithOffset[nodes.size()];
        GNodes.inArcs = new StoredIndexedBipartiteSetWithOffset[nodes.size()];
        GNodes.layers = new int[nodes.size()];
        GNodes.states = new int[nodes.size()];


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


    }

    public final StoredIndexedBipartiteSetWithOffset getSupport(int i, int j) {
        int idx = starts[i] + j - offsets[i];
        return supports[idx];


    }


    public void removeArc(int arcId, Propagator<IntVar> propagator) throws ContradictionException {
        int orig = GArcs.origs[arcId];
        int dest = GArcs.dests[arcId];

        int layer = GNodes.layers[orig];
        int value = GArcs.values[arcId];

        StoredIndexedBipartiteSetWithOffset support = getSupport(layer, value);
        support.remove(arcId);

        if (support.isEmpty()) {
            IntVar var = propagator.getVar(layer);
            try {
                var.removeValue(value, propagator, false);
            } catch (ContradictionException ex) {
                stack.clear();
                throw ex;
            }
        }

        DisposableIntIterator it;
        StoredIndexedBipartiteSetWithOffset out = GNodes.outArcs[orig];
        StoredIndexedBipartiteSetWithOffset in;

        out.remove(arcId);


        if (GNodes.layers[orig] > 0 && out.isEmpty()) {
            in = GNodes.inArcs[orig];
            it = in.getIterator();
            while (it.hasNext()) {
                int id = it.next();
                stack.push(id);
            }
            it.dispose();
        }

        in = GNodes.inArcs[dest];
        in.remove(arcId);

        if (GNodes.layers[dest] < propagator.getNbVars() && in.isEmpty()) {
            out = GNodes.outArcs[dest];
            it = out.getIterator();
            while (it.hasNext()) {
                int id = it.next();
                stack.push(id);
            }
            it.dispose();

        }

        while (!(stack.size() == 0)) {
            removeArc(stack.pop(), propagator);
        }
    }


}