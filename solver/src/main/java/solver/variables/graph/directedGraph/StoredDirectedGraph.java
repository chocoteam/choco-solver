package solver.variables.graph.directedGraph;

import java.util.BitSet;

import choco.kernel.memory.IEnvironment;
import solver.variables.graph.GraphType;
import solver.variables.graph.IStoredGraph;
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.StoredIntLinkedList;
import solver.variables.graph.graphStructure.matrix.StoredBitSetNeighbors;
import solver.variables.graph.graphStructure.nodes.StoredActiveNodes;

/**Class representing a directed graph with a backtrable structure
 * @author Jean-Guillaume Fages */
public class StoredDirectedGraph extends DirectedGraph implements IStoredGraph{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected IEnvironment environment;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public StoredDirectedGraph(IEnvironment env, int nb, GraphType type) {
		super();
		this.type = type;
		environment = env;
		switch (type) {
		case SPARSE:
			this.successors = new StoredIntLinkedList[nb];
			this.predecessors = new StoredIntLinkedList[nb];
			for (int i = 0; i < nb; i++) {
				this.successors[i] = new StoredIntLinkedList(environment);
				this.predecessors[i] = new StoredIntLinkedList(environment);
			}
			break;
		case DENSE:
			this.successors = new StoredBitSetNeighbors[nb];
			this.predecessors = new StoredBitSetNeighbors[nb];
			for (int i = 0; i < nb; i++) {
				this.successors[i] = new StoredBitSetNeighbors(environment,nb);
				this.predecessors[i] = new StoredBitSetNeighbors(environment,nb);
			}
			break;
		default:
			throw new UnsupportedOperationException();
		}
		this.activeIdx = new StoredActiveNodes(environment, nb);
		for (int i = 0; i < nb; i++) {
			this.activeIdx.activate(i);
		}
	}

	public StoredDirectedGraph(IEnvironment env, BitSet[] data, GraphType type) {
		this(env,data.length,type);
		for (int i = 0; i < data.length; i++) {
			for(int j=data[i].nextSetBit(0);j>=0;j=data[i].nextSetBit(j+1)){
				successors[i].add(j);
				predecessors[j].add(i);
			}
		}
	}

	@Override
	public IEnvironment getEnvironment() {
		return environment;
	}
}
