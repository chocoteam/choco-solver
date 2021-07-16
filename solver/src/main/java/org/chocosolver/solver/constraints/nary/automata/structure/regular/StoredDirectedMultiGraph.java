/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.structure.regular;

import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.automata.structure.Node;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableIntIterator;
import org.chocosolver.util.objects.StoredIndexedBipartiteSet;
import org.chocosolver.util.objects.StoredIndexedBipartiteSetWithOffset;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Nov 4, 2009
 * Time: 1:07:19 PM
 */
public class StoredDirectedMultiGraph {

	private int[] starts;
	private int[] offsets;
	private TIntStack stack = new TIntArrayStack();
	private StoredIndexedBipartiteSetWithOffset[] supports;

	private class Nodes {
		private int[] states;
		private int[] layers;
		private StoredIndexedBipartiteSetWithOffset[] outArcs;
		private StoredIndexedBipartiteSetWithOffset[] inArcs;
	}

	private class Arcs {
		private int[] values;
		private int[] dests;
		private int[] origs;
	}

	private Nodes GNodes;
	private Arcs GArcs;

	public StoredDirectedMultiGraph(IEnvironment environment, DirectedMultigraph<Node, Arc> graph,
									int[] starts, int[] offsets, int supportLength) {
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

	//***********************************************************************************
	// EXTERNAL METHODS
	//***********************************************************************************

	public boolean hasSupport(int i, int j) {
		StoredIndexedBipartiteSetWithOffset sup = getSupport(i,j);
		return sup != null && !sup.isEmpty();
	}

	public void clearSupports(int idxVar, int val, Propagator<IntVar> p) throws ContradictionException {
		clearSupports(getSupport(idxVar, val), p);
	}

	//***********************************************************************************
	// INTERNAL METHODS
	//***********************************************************************************

	private int getIdx(int i, int j) {
		return starts[i] + j - offsets[i];
	}

	protected final StoredIndexedBipartiteSetWithOffset getSupport(int i, int j) {
		return supports[getIdx(i, j)];
	}

	private void removeArc(Propagator<IntVar> propagator) throws ContradictionException {
		while (stack.size() > 0) {
			int arcId = stack.pop();

			int orig = GArcs.origs[arcId];
			int dest = GArcs.dests[arcId];

			int layer = GNodes.layers[orig];
			int value = GArcs.values[arcId];

			StoredIndexedBipartiteSetWithOffset support = getSupport(layer, value);
			support.remove(arcId);

			if (support.isEmpty()) {
				IntVar var = propagator.getVar(layer);
				try {
					var.removeValue(value, propagator);
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
				if (in != null) {
					it = in.getIterator();
					while (it.hasNext()) {
						int id = it.next();
						stack.push(id);
					}
					it.dispose();
				}
			}

			in = GNodes.inArcs[dest];
			in.remove(arcId);

			if (GNodes.layers[dest] < propagator.getNbVars() && in.isEmpty()) {
				out = GNodes.outArcs[dest];
				if (out != null) {
					it = out.getIterator();
					while (it.hasNext()) {
						int id = it.next();
						stack.push(id);
					}
					it.dispose();
				}

			}
		}
	}

	private void clearSupports(StoredIndexedBipartiteSet supports, Propagator<IntVar> p) throws ContradictionException {
		if (supports != null) {
			DisposableIntIterator it = supports.getIterator();
			while (it.hasNext()) {
				int arcId = it.next();
				stack.push(arcId);
			}
			it.dispose();
			removeArc(p);
		}
	}

	@Override
	public String toString() {

		StringBuilder st = new StringBuilder();
		int nb = 0;
		for (int i = 0; i < supports.length; i++) {
			if (supports[i] != null && !supports[i].isEmpty()) {
				nb++;
			}
		}
		st.append("nb: ").append(nb).append("\n");

		for (int i = 0; i < supports.length; i++) {
			if (supports[i] != null && !supports[i].isEmpty()) {
				DisposableIntIterator it = supports[i].getIterator();
				while (it.hasNext()) {
					int arcId = it.next();
					st.append(arcId).append(",");
				}
				it.dispose();
				st.append("\n");
			}
		}
		return st.toString();
	}
}
