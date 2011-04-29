package solver.constraints.propagators.gary;

import java.util.LinkedList;
import choco.kernel.ESat;
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.constraints.gary.NTree;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphTools;
import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.FlowGraphManager;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;
import solver.views.GraphView;
import solver.views.IView;

public class PropNTree<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar nTree;
	int minTree = 0;
	private LinkedList<INeighbors> sinks;
	private LinkedList<INeighbors> nonSinks;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNTree(DirectedGraphVar graph, IntVar nT,IEnvironment environment,
			Constraint<V, Propagator<V>> constraint,
			PropagatorPriority priority, boolean reactOnPromotion) {
		super((V[]) new Variable[]{graph,nT}, environment, constraint, priority, reactOnPromotion);
		g = graph;
		nTree = nT;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	private boolean checkFeasibility() {
		int n = g.getEnvelopGraph().getNbNodes();
		computeSinks();
		int MINTREE = minTree;
		int MAXTREE = calcMaxTree();
		if (nTree.getLB()<=MAXTREE && nTree.getUB()>=MINTREE){
			ActiveNodesIterator<IActiveNodes> nodeIter = g.getEnvelopGraph().getActiveNodes().iterator();
			int node;
			DirectedGraph Grs = new DirectedGraph(n+1, g.getEnvelopGraph().getType());//ATENTION TYPE
			while (nodeIter.hasNext()){
				node = nodeIter.next();
				if (g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()<1 || g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize()>1){
					return false;
				}
				AbstractNeighborsIterator<INeighbors> sucIter = g.getEnvelopGraph().successorsIteratorOf(node);
				int suc;
				while (sucIter.hasNext()){
					suc = sucIter.next();
					Grs.addArc(suc, node);
					if(suc==node){
						Grs.addArc(node, n);
						Grs.addArc(n, node);
					}
				}
			}
			int[] numDFS = GraphTools.performDFS(n, Grs);
			boolean rootFound = false;
			for(int i:numDFS){
				if(rootFound && i==0)return false;
				if(i==0)rootFound = true;
			}
		}else{
			return false;
		}
		return true;
	}

	private int calcMaxTree() {
		int ct = 0;
		ActiveNodesIterator<IActiveNodes> nodeIter = g.getEnvelopGraph().activeNodesIterator();
		int node;
		while(nodeIter.hasNext()){
			node = nodeIter.next();
			if (g.getEnvelopGraph().arcExists(node, node)){
				ct++;
			}
		}
		return ct;
	}

	private void filtering() throws ContradictionException{
		NTree.filteringCounter++;
		computeSinks();
		//1) Bound pruning
		minTreePruning(); // MAXTREE pruning is done by PropNLoops
		//2) structural pruning
		structuralPruning();
	}

	@Override
	public void propagate() throws ContradictionException {
		NTree.filteringCounter++;
		if(!checkFeasibility()){
			ContradictionException.throwIt(this, g, "infeasible");
		}else{
			structuralPruning();
		}
	}

	@Override
	public void propagateOnView(IView<V> view, int idxVarInProp, int mask) throws ContradictionException {
		if (view instanceof GraphView) {
			filtering();
		}
	}

	private void structuralPruning() throws ContradictionException {
		int n = g.getEnvelopGraph().getNbNodes();
		ActiveNodesIterator<IActiveNodes> nodeIter = g.getEnvelopGraph().getActiveNodes().iterator();
		int node;
		DirectedGraph Grs = new DirectedGraph(n+1, g.getEnvelopGraph().getType());
		AbstractNeighborsIterator<INeighbors> sucIter;
		int suc;
		while (nodeIter.hasNext()){
			node = nodeIter.next();
			sucIter = g.getEnvelopGraph().successorsIteratorOf(node);
			while (sucIter.hasNext()){
				suc = sucIter.next();
				Grs.addArc(suc, node);
				if(suc==node){
					Grs.addArc(node, n);
					Grs.addArc(n, node);
				}
			}
		}
		//dominators
		FlowGraphManager flowGM = new FlowGraphManager(n, Grs); 
		//LCA preprocessing
		DirectedGraph dominatorGraph = new DirectedGraph(n+1, GraphType.SPARSE);
		nodeIter = g.getEnvelopGraph().getActiveNodes().iterator();
		while (nodeIter.hasNext()){
			node = nodeIter.next();
			dominatorGraph.addArc(flowGM.getImmediateDominatorsOf(node), node);
		}
		//PREPROCESSING
		int[] in = new int[n+1];
		int[] out = new int[n+1];
		int[] father = new int[n+1];
		AbstractNeighborsIterator<INeighbors>[] successors = new AbstractNeighborsIterator[n+1];
		for (int i=0; i<n+1; i++){
			father[i] = -1;
			successors[i] = dominatorGraph.successorsIteratorOf(i);
		}
		int time = 0;
		int currentNode = n;
		int nextNode;
		father[n] = n;
		in[n] = 0;
		while((currentNode!=n) || successors[currentNode].hasNext()){
			if(!successors[currentNode].hasNext()){
				time++;
				out[currentNode] = time;
				currentNode = father[currentNode];
			}else{
				nextNode = successors[currentNode].next();
				if (father[nextNode]==-1) {
					time++;
					in[nextNode] = time;
					father[nextNode] = currentNode;
					currentNode = nextNode;
				}
			}
		}
		time++;
		out[n] = time;
		//END_PREPROCESSING
		//queries
		LinkedList<Integer> toRemove = new LinkedList<Integer>();
		nodeIter = g.getEnvelopGraph().getActiveNodes().iterator();
		while (nodeIter.hasNext()){
			node = nodeIter.next();
			sucIter = g.getEnvelopGraph().successorsIteratorOf(node);
			while (sucIter.hasNext()){
				suc = sucIter.next();
				//--- STANDART PRUNING
				if (node != suc && in[suc]>in[node] && out[suc]<out[node]){
					g.removeArc(node, suc, this);
				}
			}
		}
	}

	private void minTreePruning() throws ContradictionException {
		nTree.updateLowerBound(minTree, this);
		if (nTree.getUB()==minTree){
			AbstractNeighborsIterator<INeighbors> iter;
			int node;
			for (INeighbors scc:nonSinks){
				iter = scc.iterator();
				while(iter.hasNext()){
					node = iter.next();
					if(g.getEnvelopGraph().arcExists(node, node)){
						g.removeArc(node, node, this);
					}
				}
			}
		}
	}

	private void computeSinks() {
		int n = g.getEnvelopGraph().getNbNodes();
		LinkedList<INeighbors> allSCC = StrongConnectivityFinder.findAllSCC(g.getEnvelopGraph());
		int[] sccOf = new int[n];
		int sccNum = 0;
		AbstractNeighborsIterator<INeighbors> iter;
		int node;
		for (INeighbors scc:allSCC){
			iter = scc.iterator();
			while(iter.hasNext()){
				node = iter.next();
				sccOf[node] = sccNum;
			}
			sccNum++;
		}
		sinks = new LinkedList<INeighbors>();
		nonSinks = new LinkedList<INeighbors>();
		boolean looksSink = true;
		int suc;
		AbstractNeighborsIterator<INeighbors> succIter;
		for (INeighbors scc:allSCC){
			iter = scc.iterator();
			looksSink = true;
			while(looksSink && iter.hasNext()){
				node = iter.next();
				succIter = g.getEnvelopGraph().successorsIteratorOf(node);
				while(looksSink && succIter.hasNext()){
					suc = succIter.next();
					if (sccOf[suc]!=sccOf[node]){
						looksSink = false;
					}
				}
			}
			if(looksSink){
				sinks.add(scc);
			}else{
				nonSinks.add(scc);
			}
		}
		minTree = sinks.size();
	}

	@Override
	public int getPropagationConditions() {
		return EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
