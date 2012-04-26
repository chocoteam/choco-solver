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
package samples.graph.jg_sandbox;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import java.util.BitSet;

public class PropAtLeastKWorkers extends GraphPropagator<Variable> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int firstTaskIndex,n2;
	private IntVar kWorkers;
	private GraphVar g;
	private IActiveNodes activeNodes;
	// for augmenting matching
	private DirectedGraph digraph;
	private int[] matching,fifo,father;
	private BitSet free,in;
	private StrongConnectivityFinder SCCfinder;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAtLeastKWorkers(GraphVar graph, IntVar kworkers, int firstTaskIndex, Constraint constraint, Solver sol) {
		super(new Variable[]{graph,kworkers}, sol, constraint, PropagatorPriority.QUADRATIC);
		n2 = graph.getEnvelopGraph().getNbNodes();
		g = graph;
		this.firstTaskIndex = firstTaskIndex;
		matching = new int[n2];
		digraph = new DirectedGraph(n2+1, GraphType.LINKED_LIST);
		free = new BitSet(n2);
		this.kWorkers = kworkers;
		father = new int[n2];
		in = new BitSet(n2);
		fifo = new int[n2];
		SCCfinder = new StrongConnectivityFinder(digraph);
		activeNodes = g.getEnvelopGraph().getActiveNodes();
	}

	//***********************************************************************************
	// Initialization
	//***********************************************************************************

	private void buildDigraph() {
		free.set(0,n2);
		int j;
		INeighbors nei;
		for(int i=0;i<n2;i++){
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
			matching[i] = -1;
		}
		for(int i=0;i<firstTaskIndex;i++){
			if(activeNodes.isActive(i)){
				digraph.activateNode(i);
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(free.get(i) && free.get(j)){
						digraph.addArc(j, i);
						free.clear(i);
						free.clear(j);
					}else{
						digraph.addArc(i,j);
					}
				}
			}
			else{
				digraph.desactivateNode(i);
				free.clear(i);
			}
		}
	}

	//***********************************************************************************
	// MATCHING
	//***********************************************************************************

	private int repairMatching() throws ContradictionException {
		for(int i=free.nextSetBit(0);i>=0 && i<firstTaskIndex; i=free.nextSetBit(i+1)){
			tryToMatch(i);
		}
		int p;
		int cardinality = 0;
		for (int i=0;i<firstTaskIndex;i++) {
			p = digraph.getPredecessorsOf(i).getFirstElement();
			if(p!=-1){
				cardinality++;
				matching[p]=i;
			}
			matching[i]=p;
		}
		return cardinality;
	}

	private void tryToMatch(int i) throws ContradictionException {
		int mate = augmentPath_BFS(i);
		if(mate!=-1){
			free.clear(mate);
			free.clear(i);
			int tmp = mate;
			while(tmp!=i){
				digraph.removeArc(father[tmp],tmp);
				digraph.addArc(tmp,father[tmp]);
				tmp = father[tmp];
			}
		}
	}

	private int augmentPath_BFS(int root) {
        in.clear();
        int indexFirst = 0, indexLast = 0;
        fifo[indexLast++] = root;
        int x, y;
        INeighbors succs;
        while (indexFirst != indexLast) {
            x = fifo[indexFirst++];
            succs = digraph.getSuccessorsOf(x);
            for (y = succs.getFirstElement(); y >= 0; y = succs.getNextElement()) {
                if (!in.get(y)) {
                    father[y] = x;
                    fifo[indexLast++] = y;
                    in.set(y);
                    if (free.get(y)) {
                        return y;
                    }
                }
            }
        }
        return -1;
    }

	//***********************************************************************************
	// PRUNING
	//***********************************************************************************

	private void filter() throws ContradictionException {
		digraph.desactivateNode(n2);
		digraph.activateNode(n2);
		for (int i = firstTaskIndex; i < n2; i++) {
			if (free.get(i)) {
				digraph.addArc(i, n2);
			} else {
				digraph.addArc(n2, i);
			}
		}
		SCCfinder.findAllSCC();
		int[] nodeSCC = SCCfinder.getNodesSCC();
		digraph.desactivateNode(n2);

		INeighbors succ;
		int j;
		for (int node = 0;node<firstTaskIndex;node++) {
			if(activeNodes.isActive(node)){
				succ = g.getEnvelopGraph().getSuccessorsOf(node);
				for (j = succ.getFirstElement(); j >= 0; j = succ.getNextElement()) {
					if (nodeSCC[node] != nodeSCC[j]) {
						if (matching[node] == j && matching[j] == node) {
							g.enforceArc(node, j, this);
						} else {
							g.removeArc(node, j, this);
							digraph.removeArc(node, j);
							digraph.removeArc(j, node);
						}
					}
				}
			}
		}
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		buildDigraph();
		int card = repairMatching();
		kWorkers.updateUpperBound(card,this);
		if(card == kWorkers.getLB()){
			filter();
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.REMOVENODE.mask
			 + EventType.INSTANTIATE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;
	}
}
