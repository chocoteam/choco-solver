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

package solver.constraints.propagators.gary.constraintSpecific;

import choco.kernel.ESat;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import solver.variables.graph.graphOperations.dominance.SimpleDominatorsFinder;

public class PropNTree<V extends Variable> extends GraphPropagator<V> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    DirectedGraphVar g;
    IntVar nTree;
    int minTree = 0;
    private TIntArrayList nonSinks;
	private StrongConnectivityFinder SCCfinder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNTree(DirectedGraphVar graph, IntVar nT, Solver solver,
                     Constraint<V, Propagator<V>> constraint) {
        super((V[]) new Variable[]{graph, nT}, solver, constraint, PropagatorPriority.QUADRATIC);
        g = graph;
        nTree = nT;
		SCCfinder = new StrongConnectivityFinder(g.getEnvelopGraph());
		nonSinks = new TIntArrayList();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

//	private boolean checkFeasibility() throws ContradictionException {
//		int n = g.getEnvelopGraph().getNbNodes();
//		computeSinks();
//		int MINTREE = minTree;
//		int MAXTREE = calcMaxTree();
//		INeighbors nei;
//		if (nTree.getLB()<=MAXTREE && nTree.getUB()>=MINTREE){
//			IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
//			DirectedGraph Grs = new DirectedGraph(n+1, g.getEnvelopGraph().getType());//ATENTION TYPE
//			for (int node=env.getFirstElement();node>=0;node=env.getNextElement()){
//				if (g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()<1 || g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize()>1){
//					return false;
//				}
//				nei = g.getEnvelopGraph().getSuccessorsOf(node);
//				for(int suc=nei.getFirstElement(); suc>=0; suc = nei.getNextElement()){
//					Grs.addArc(suc, node);
//					if(suc==node){
//						Grs.addArc(node, n);
//						Grs.addArc(n, node);
//					}
//				}
//			}
//			int[] numDFS = GraphTools.performDFS(n, Grs);
//			for (int node=env.getFirstElement();node>=0;node=env.getNextElement()){
//				if(numDFS[node]==0){
//					g.removeNode(node, this, false);
//				}
//			}
//		}else{
//			return false;
//		}
//		return true;
//	}

//	private int calcMaxTree() {
//		int ct = 0;
//		IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
//		for (int node=env.getFirstElement();node>=0;node=env.getNextElement()){
//			if (g.getEnvelopGraph().arcExists(node, node)){
//				ct++;
//			}
//		}
//		return ct;
//	}

    private void filtering() throws ContradictionException {
        computeSinks();
        //1) Bound pruning
        minTreePruning();
        //2) structural pruning
        structuralPruning();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filtering();
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        filtering();
    }

    private void structuralPruning() throws ContradictionException {
        int n = g.getEnvelopGraph().getNbNodes();
        DirectedGraph Grs = new DirectedGraph(n + 1, g.getEnvelopGraph().getType());
        INeighbors nei;
        for (int node = 0; node < n; node++) {
            nei = g.getEnvelopGraph().getSuccessorsOf(node);
            for (int suc = nei.getFirstElement(); suc >= 0; suc = nei.getNextElement()) {
                if (suc == node) {
                    Grs.addArc(n, node);
                } else {
                    Grs.addArc(suc, node);
                }
            }
        }
        //dominators
        AbstractLengauerTarjanDominatorsFinder dominatorsFinder = new SimpleDominatorsFinder(n, Grs);
        if (dominatorsFinder.findDominators()) {
            for (int x = 0; x < n; x++) {
                nei = g.getEnvelopGraph().getSuccessorsOf(x);
                for (int y = nei.getFirstElement(); y >= 0; y = nei.getNextElement()) {
                    //--- STANDART PRUNING
                    if (dominatorsFinder.isDomminatedBy(y, x)) {
                        g.removeArc(x, y, this);
                    }
                    // ENFORCE ARC-DOMINATORS (redondant)
                }
            }
        } else {
            contradiction(g, "the source cannot reach all nodes");
        }
    }

    private void minTreePruning() throws ContradictionException {
        nTree.updateLowerBound(minTree, this);
        if (nTree.getUB() == minTree) {
            int node,scc;
            for (int k=nonSinks.size()-1;k>=0;k--) {
				scc = nonSinks.get(k);
				node = SCCfinder.getSCCFirstNode(scc);
				while(node!=-1){
					if (g.getEnvelopGraph().arcExists(node, node)) {
                        g.removeArc(node, node, this);
                    }
					node = SCCfinder.getNextNode(node);
				}
            }
        }
    }

    private void computeSinks() {
		SCCfinder.findAllSCC();
        int[] sccOf = SCCfinder.getNodesSCC();
        nonSinks.clear();
        boolean looksSink;
        INeighbors nei;
        int node;
		int nbSinks = 0;
		for(int i=SCCfinder.getNbSCC()-1;i>=0;i--){
            looksSink = true;
            boolean inKer = false;
			node = SCCfinder.getSCCFirstNode(i);
			while(node!=-1){
                if (g.getKernelGraph().getActiveNodes().isActive(node)) {
                    inKer = true;
                }
                nei = g.getEnvelopGraph().getSuccessorsOf(node);
                for (int suc = nei.getFirstElement(); suc >= 0 && looksSink; suc = nei.getNextElement()) {
                    if (sccOf[suc] != sccOf[node]) {
                        looksSink = false;
						break;
                    }
                }
                if (!looksSink) {
                    node = -1;
                }else{
					node = SCCfinder.getNextNode(node);
				}
            }
            if (looksSink && inKer) {
				nbSinks++;
            } else {
                nonSinks.add(i);
            }
        }
        minTree = nbSinks;
    }

//	private void computeSinks() {
//        int n = g.getEnvelopGraph().getNbNodes();
//        ArrayList<TIntArrayList> allSCC = StrongConnectivityFinder.findAllSCCOf(g.getEnvelopGraph());
//        int[] sccOf = new int[n];
//        int sccNum = 0;
//        int node;
//        for (TIntArrayList scc : allSCC) {
//            for (int x = 0; x < scc.size(); x++) {
//                sccOf[scc.get(x)] = sccNum;
//            }
//            sccNum++;
//        }
//        sinks = new LinkedList<TIntArrayList>();
//        nonSinks = new LinkedList<TIntArrayList>();
//        boolean looksSink = true;
//        INeighbors nei;
//        for (TIntArrayList scc : allSCC) {
//            looksSink = true;
//            boolean inKer = false;
//            for (int x = 0; x < scc.size(); x++) {
//                node = scc.get(x);
//                if (g.getKernelGraph().getActiveNodes().isActive(node)) {
//                    inKer = true;
//                }
//                nei = g.getEnvelopGraph().getSuccessorsOf(node);
//                for (int suc = nei.getFirstElement(); suc >= 0 && looksSink; suc = nei.getNextElement()) {
//                    if (sccOf[suc] != sccOf[node]) {
//                        looksSink = false;
//                    }
//                }
//                if (!looksSink) {
//                    x = scc.size();
//                }
//            }
//            if (looksSink && inKer) {
//                sinks.add(scc);
//            } else {
//                nonSinks.add(scc);
//            }
//        }
//        minTree = sinks.size();
//    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.REMOVENODE.mask;
    }

    @Override
    public ESat isEntailed() {
        if (g.instantiated()) {
            try {
                structuralPruning();
            } catch (Exception e) {
                return ESat.FALSE;
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
