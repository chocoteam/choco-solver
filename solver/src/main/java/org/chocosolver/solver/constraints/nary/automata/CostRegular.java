/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata;

import static org.chocosolver.util.tools.ArrayUtils.concat;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.structure.Node;
import org.chocosolver.solver.constraints.nary.automata.structure.costregular.Arc;
import org.chocosolver.solver.constraints.nary.automata.structure.costregular.StoredValuedDirectedMultiGraph;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * COST_REGULAR constraint
 * <br/>
 *
 * @author Julien Menana, Charles Prud'homme
 * @since 06/06/11
 */
public class CostRegular extends Constraint {

    public CostRegular(IntVar[] ivars, IntVar cost, ICostAutomaton cautomaton) {
		super(ConstraintsName.COSTREGULAR,new PropCostRegular(
				ArrayUtils.concat(ivars, cost),
				cautomaton,
				initGraph(concat(ivars, cost), cautomaton)
		));
    }

    private static StoredValuedDirectedMultiGraph initGraph(IntVar[] vars, ICostAutomaton pi) {
		IEnvironment environment = vars[0].getEnvironment();
        int aid = 0;
        int nid = 0;

        int size = vars.length - 1;

        int[] offsets = new int[size];
        int[] sizes = new int[size];
        int[] starts = new int[size];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0; i < size; i++) {
            offsets[i] = vars[i].getLB();
            sizes[i] = vars[i].getUB() - vars[i].getLB() + 1;
            if (i > 0) starts[i] = sizes[i - 1] + starts[i - 1];
            totalSizes += sizes[i];
        }


        DirectedMultigraph<Node, Arc> graph;
        graph = new DirectedMultigraph<>(null, null, false);
        ArrayList<HashSet<Arc>> tmp = new ArrayList<>(totalSizes);
        for (int i = 0; i < totalSizes; i++)
            tmp.add(new HashSet<>());


        int i, j, k;
        TIntIterator layerIter;
        TIntIterator qijIter;

        ArrayList<TIntHashSet> layer = new ArrayList<>();
        TIntHashSet[] tmpQ = new TIntHashSet[totalSizes];
        // DLList[vars.length+1];

        for (i = 0; i <= size; i++) {
            layer.add(new TIntHashSet());// = new DLList(nbNodes);
        }

        //forward pass, construct all paths described by the automaton for word of length nbVars.

        layer.get(0).add(pi.getInitialState());

        TIntHashSet succ = new TIntHashSet();
        for (i = 0; i < size; i++) {
            int ub = vars[i].getUB();
            for (j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                layerIter = layer.get(i).iterator();
                while (layerIter.hasNext()) {
                    k = layerIter.next();
                    succ.clear();
                    pi.delta(k, j, succ);
                    if (!succ.isEmpty()) {
                        TIntIterator it = succ.iterator();
                        for (; it.hasNext(); )
                            layer.get(i + 1).add(it.next());
                        int idx = starts[i] + j - offsets[i];
                        if (tmpQ[idx] == null)
                            tmpQ[idx] = new TIntHashSet();

                        tmpQ[idx].add(k);


                    }
                }
            }
        }

        //removing reachable non accepting states
        layerIter = layer.get(size).iterator();
        while (layerIter.hasNext()) {
            k = layerIter.next();
            if (pi.isNotFinal(k)) {
                layerIter.remove();
            }

        }

        //backward pass, removing arcs that does not lead to an accepting state
        int nbNodes = pi.getNbStates();
        BitSet mark = new BitSet(nbNodes);

        Node[] in = new Node[pi.getNbStates() * (size + 1)];
        Node tink = new Node(pi.getNbStates() + 1, size + 1, nid++);
        graph.addVertex(tink);

        for (i = size - 1; i >= 0; i--) {
            mark.clear(0, nbNodes);
            int ub = vars[i].getUB();
            for (j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                int idx = starts[i] + j - offsets[i];
                TIntHashSet l = tmpQ[idx];
                if (l != null) {
                    qijIter = l.iterator();
                    while (qijIter.hasNext()) {
                        k = qijIter.next();
                        succ.clear();
                        pi.delta(k, j, succ);
                        TIntIterator it = succ.iterator();
                        boolean added = false;
                        for (; it.hasNext(); ) {
                            int qn = it.next();
                            if (layer.get(i + 1).contains(qn)) {
                                added = true;
                                Node a = in[i * pi.getNbStates() + k];
                                if (a == null) {
                                    a = new Node(k, i, nid++);
                                    in[i * pi.getNbStates() + k] = a;
                                    graph.addVertex(a);
                                }


                                Node b = in[(i + 1) * pi.getNbStates() + qn];
                                if (b == null) {
                                    b = new Node(qn, i + 1, nid++);
                                    in[(i + 1) * pi.getNbStates() + qn] = b;
                                    graph.addVertex(b);
                                }


                                Arc arc = new Arc(a, b, j, aid++, pi.getCostByState(i, j, a.state));
                                graph.addEdge(a, b, arc);
                                tmp.get(idx).add(arc);
                                mark.set(k);
                            }
                        }
                        if (!added)
                            qijIter.remove();
                    }
                }
            }
            layerIter = layer.get(i).iterator();

            // If no more arcs go out of a given state in the layer, then we remove the state from that layer
            while (layerIter.hasNext())
                if (!mark.get(layerIter.next()))
                    layerIter.remove();
        }

        TIntHashSet th = new TIntHashSet();
        int[][] intLayer = new int[size + 2][];
        for (k = 0; k < pi.getNbStates(); k++) {
            Node o = in[size * pi.getNbStates() + k];
            {
                if (o != null) {
                    Arc a = new Arc(o, tink, 0, aid++, 0.0);
                    graph.addEdge(o, tink, a);
                }
            }
        }


        for (i = 0; i <= size; i++) {
            th.clear();
            for (k = 0; k < pi.getNbStates(); k++) {
                Node o = in[i * pi.getNbStates() + k];
                if (o != null) {
                    th.add(o.id);
                }
            }
            intLayer[i] = th.toArray();
        }
        intLayer[size + 1] = new int[]{tink.id};


        if (intLayer[0].length > 0)
            return new StoredValuedDirectedMultiGraph(environment, graph, intLayer, starts, offsets, totalSizes);
        else
            throw new SolverException("intLayer[0].length <= 0");
    }

//	private StoredValuedDirectedMultiGraph initGraph(DirectedMultigraph<Node, Arc> graph, Node source, IntVar[] vars) {
//		int size = vars.length - 1;
//		int[] offsets = new int[size];
//		int[] sizes = new int[size];
//		int[] starts = new int[size];
//
//		int totalSizes = 0;
//
//		starts[0] = 0;
//		for (int i = 0; i < size; i++) {
//			offsets[i] = vars[i].getLB();
//			sizes[i] = vars[i].getUB() - vars[i].getLB() + 1;
//			if (i > 0) starts[i] = sizes[i - 1] + starts[i - 1];
//			totalSizes += sizes[i];
//		}
//
//		TIntArrayList[] layers = new TIntArrayList[size + 1];
//		for (int i = 0; i < layers.length; i++) {
//			layers[i] = new TIntArrayList();
//		}
//		Queue<Node> queue = new ArrayDeque<Node>();
//		source.layer = 0;
//		queue.add(source);
//
//		int nid = 0;
//		int aid = 0;
//		while (!queue.isEmpty()) {
//			Node n = queue.remove();
//			n.id = nid++;
//			layers[n.layer].add(n.id);
//			Set<Arc> tmp = graph.outgoingEdgesOf(n);
//			for (Arc a : tmp) {
//				a.id = aid++;
//				Node next = graph.getEdgeTarget(a);
//				next.layer = n.layer + 1;
//				queue.add(next);
//			}
//		}
//		int[][] lays = new int[layers.length][];
//		for (int i = 0; i < lays.length; i++) {
//			lays[i] = layers[i].toArray();
//		}
//		IEnvironment environment = vars[0].getEnvironment();
//		return new StoredValuedDirectedMultiGraph(environment, graph, lays, starts, offsets, totalSizes);
//	}
}
