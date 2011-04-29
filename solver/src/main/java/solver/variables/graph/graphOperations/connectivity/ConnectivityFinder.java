package solver.variables.graph.graphOperations.connectivity;

import java.util.BitSet;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.IGraph;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;

/**Class containing algorithms to find all connected components and articulation points of graph by performing one dfs
 * it uses Tarjan algorithm in a non recursive way and can be performed in O(M+N) time c.f. Gondrand Minoux
 * @author Jean-Guillaume Fages
 */
public class ConnectivityFinder {

	//***********************************************************************************
	// CONNECTED COMPONENTS AND ARTICULATION POINTS IN ONE DFS
	//***********************************************************************************
	
	/**Find all connected components and articulation points of graph by performing one dfs
	 * Complexity : O(M+N)
	 * @param graph
	 * @return a ConnectivityObject that encapsulates all connected components and articulation points of graph
	 */
	public static ConnectivityObject findAllCCandAP(IGraph graph){
		int nb = graph.getNbNodes();
		ConnectivityObject co = new ConnectivityObject(nb);
		int[] p = new int[nb];
		int[] num = new int[nb];
		int[] inf = new int[nb];
		AbstractNeighborsIterator<INeighbors>[] neighbors = new AbstractNeighborsIterator[nb];
		BitSet notOpenedNodes = new BitSet(nb);
		ActiveNodesIterator<IActiveNodes> iter = graph.activeNodesIterator();
		int i;
		while (iter.hasNext()){
			i = iter.next();
			inf[i] = Integer.MAX_VALUE;
			p[i] = -1;
			notOpenedNodes.set(i);
		}
		int first = 0;
		first = notOpenedNodes.nextSetBit(first);
		while(first>=0){
			findCCandAP(co, graph, neighbors, first, p, num, inf, notOpenedNodes);
			first = notOpenedNodes.nextSetBit(first);
		}
		return co;
	}
	
	/**
	 * @param co the object which encapsulates CC and AP
	 * @param graph the studied graph
	 * @param neighbors iterators for neighbors of nodes
	 * @param start the starting node of the procedure
	 * @param p the array of parents of nodes in the dfs
	 * @param num dfs numerotation
	 * @param inf array used to find AP
	 * @param notOpenedNodes enables to find the next starting point to consider
	 */
	private static void findCCandAP(ConnectivityObject co, IGraph graph, AbstractNeighborsIterator<INeighbors>[] neighbors, int start, int[] p, int[] num, int[] inf, BitSet notOpenedNodes){
		co.newCC();
		int i = start;
		int k = 1;
		num[start] = 1;
		p[start] = start;
		notOpenedNodes.clear(start);
		neighbors[start] = graph.neighborsIteratorOf(start);
		int j,q;
		co.addCCNode(start);
		int nbRootChildren = 0;
		while((i!=start) || neighbors[i].hasNext()){
			if(!neighbors[i].hasNext()){
				if (p[i]==-1){Exception e = new Exception("error in DFS");e.printStackTrace();System.exit(0);}
				q = inf[i];
				i = p[i];
				inf[i] = Math.min(q, inf[i]);
				if (q >= num[i] && i!=start){
					co.addArticulationPoint(i);
				}
			}else{
				j = neighbors[i].next();
				if (p[j]==-1) {
					p[j] = i;
					if (i == start){
						nbRootChildren++;
					}
					i = j;
					neighbors[i] = graph.neighborsIteratorOf(i);
					notOpenedNodes.clear(i);
					k++;
					num[i] = k;
					inf[i] = num[i];
					co.addCCNode(i);
				}else if(p[i]!=j){
					inf[i] = Math.min(inf[i], num[j]);
				}
			}
		}
		if(nbRootChildren>1){
			co.addArticulationPoint(start);
		}
	}
	
	//***********************************************************************************
	// CONNECTED COMPONENTS ONLY
	//***********************************************************************************
	
	/**Find all connected components of graph by performing one dfs
	 * Complexity : O(M+N) but light and fast in practice
	 * @param graph
	 * @return a ConnectivityObject that encapsulates all connected components of graph but has no articulation points (null pointer)
	 */
	public static ConnectivityObject findAllCConly(IGraph graph){
		int nb = graph.getNbNodes();
		ConnectivityObject co = new ConnectivityObject();
		int[] p = new int[nb];
		AbstractNeighborsIterator<INeighbors>[] neighbors = new AbstractNeighborsIterator[nb];
		BitSet notOpenedNodes = new BitSet(nb);
		ActiveNodesIterator<IActiveNodes> iter = graph.activeNodesIterator();
		int i;
		while (iter.hasNext()){
			i = iter.next();
			p[i] = -1;
			notOpenedNodes.set(i);
		}
		int first = 0;
		first = notOpenedNodes.nextSetBit(first);
		while(first>=0){
			findCC(co, graph, neighbors, first, p, notOpenedNodes);
			first = notOpenedNodes.nextSetBit(first);
		}
		return co;
	}
	
	/**
	 * @param co the object which encapsulates CC and AP but here AP will be empty
	 * @param graph the studied graph
	 * @param neighbors iterators for neighbors of nodes
	 * @param start the starting node of the procedure
	 * @param p the array of parents of nodes in the dfs
	 * @param notOpenedNodes enables to find the next starting point to consider
	 */
	private static void findCC(ConnectivityObject co, IGraph graph, AbstractNeighborsIterator<INeighbors>[] neighbors, int start, int[] p, BitSet notOpenedNodes){
		co.newCC();
		int i = start;
		int k = 1;
		p[start] = start;
		notOpenedNodes.clear(start);
		neighbors[start] = graph.neighborsIteratorOf(start);
		int j;
		co.addCCNode(start);
		int nbRemainings = notOpenedNodes.cardinality();
		while((i!=start) || neighbors[i].hasNext()){
			if(!neighbors[i].hasNext()){
				if (p[i]==-1){Exception e = new Exception("error in DFS");e.printStackTrace();System.exit(0);}
				i = p[i];
			}else{
				j = neighbors[i].next();
				if (p[j]==-1) {
					p[j] = i;
					i = j;
					neighbors[i] = graph.neighborsIteratorOf(i);
					notOpenedNodes.clear(i);
					nbRemainings--;
					k++;
					co.addCCNode(i);
					if(nbRemainings==0){
						return;
					}
				}
			}
		}
	}
}
