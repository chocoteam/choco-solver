package solver.variables.graph.undirectedGraph;

import choco.kernel.memory.IEnvironment;
import solver.variables.graph.GraphType;
import solver.variables.graph.IStoredGraph;
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.StoredIntLinkedList;
import solver.variables.graph.graphStructure.matrix.StoredBitSetNeighbors;
import solver.variables.graph.graphStructure.nodes.StoredActiveNodes;

/**Class representing an undirected graph with a backtrable structure
 * @author Jean-Guillaume Fages */
public class StoredUndirectedGraph extends UndirectedGraph implements IStoredGraph {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected IEnvironment environment;


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public StoredUndirectedGraph(IEnvironment env, int order, GraphType type) {
    	this.type = type;
    	environment = env;
    	switch (type) {
            case SPARSE:
                this.neighbors = new StoredIntLinkedList[order];
                for (int i = 0; i < order; i++) {
                    this.neighbors[i] = new StoredIntLinkedList(environment);
                }
                break;
            case DENSE:
                this.neighbors = new StoredBitSetNeighbors[order];
                for (int i = 0; i < order; i++) {
                    this.neighbors[i] = new StoredBitSetNeighbors(environment,order);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        this.activeIdx = new StoredActiveNodes(environment, order);
        for (int i = 0; i < order; i++) {
            this.activeIdx.activate(i);
        }
    }


	@Override
	public IEnvironment getEnvironment() {
		return environment;
	}
}
