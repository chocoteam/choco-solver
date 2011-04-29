package solver.variables.graph.undirectedGraph;

import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.IGraph;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.adjacencyList.IntLinkedList;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;
import solver.variables.graph.graphStructure.nodes.ActiveNodes;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 *
 * Specific implementation of an undirected graph
 */
public class UndirectedGraph implements IGraph {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	INeighbors[] neighbors;
    /** activeIdx represents the nodes available in the graph */
    IActiveNodes activeIdx;
	GraphType type;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	protected UndirectedGraph() {}

    public UndirectedGraph(int order, GraphType type) {
    	this.type = type;
        switch (type) {
            case SPARSE:
                this.neighbors = new IntLinkedList[order];
                for (int i = 0; i < order; i++) {
                    this.neighbors[i] = new IntLinkedList();
                }
                break;
            case DENSE:
                this.neighbors = new BitSetNeighbors[order];
                for (int i = 0; i < order; i++) {
                    this.neighbors[i] = new BitSetNeighbors(order);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        this.activeIdx = new ActiveNodes(order);
        for (int i = 0; i < order; i++) {
            this.activeIdx.activate(i);
        }
    }

    public UndirectedGraph(int order, boolean[][] matrix, GraphType type) {
        this(order,type);
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                if (matrix[i][j]) {
                    this.neighbors[i].add(j);
                }
            }
        }
    }

    //***********************************************************************************
	// METHODS
	//***********************************************************************************

	public String toString() {
    	 String res = "";
         for (ActiveNodesIterator<IActiveNodes> itNode = activeIdx.iterator(); itNode.hasNext();) {
             int i = itNode.next();
             res += "pot-" + i + ": ";
             for (AbstractNeighborsIterator<INeighbors> itNext = neighbors[i].iterator(); itNext.hasNext();) {
                 int j = itNext.next();
                 res += j + " ";
             }
             res += "\n";
         }
         return res;
    }

    @Override
    /**
     * @inheritedDoc
     */
    public int getNbNodes() {
        return activeIdx.nbNodes();
    }
    
    @Override
    /**
     * @inheritedDoc
     */
    public IActiveNodes getActiveNodes() {
        return activeIdx;
    }
    
    @Override
    /**
     * @inheritedDoc
     */
    public ActiveNodesIterator<IActiveNodes> activeNodesIterator() {
        return activeIdx.iterator();
    }

    @Override
    /**
     * @inheritedDoc
     */
    public int getNeighborhoodSize(int x) {
        return neighbors[x].neighborhoodSize();
    }

    @Override
    /**
     * @inheritedDoc
     */
    public <N extends INeighbors> AbstractNeighborsIterator<N> neighborsIteratorOf(int x) {
        return neighbors[x].iterator();
    }

	@Override
	/**
     * @inheritedDoc
     */
    public GraphType getType() {
		return type;
	}

	@Override
	public boolean activateNode(int x) {
		if(activeIdx.isActive(x))return false;
		activeIdx.activate(x);
		return true;
	}

	@Override
	public boolean desactivateNode(int x) {
		if(!activeIdx.isActive(x))return false;
		activeIdx.desactivate(x);
		AbstractNeighborsIterator<INeighbors> iter = neighborsIteratorOf(x); 
		while (iter.hasNext()){
			neighbors[iter.next()].remove(x);
		}
		neighbors[x].clear();
		return true;
	}

	@Override
	public boolean addEdge(int x, int y) {
		if ((!neighbors[x].contain(y)) && (!neighbors[y].contain(x))){
			neighbors[x].add(y);
			neighbors[y].add(x);
			return true;
		}
		if ((!neighbors[x].contain(y)) || (!neighbors[y].contain(x))){
			throw new UnsupportedOperationException("asymmetric adjacency matrix in an undirected graph");
		}
		return false;
	}
	
	@Override
	public boolean edgeExists(int x, int y) {
		if(neighbors[x].contain(y) && neighbors[y].contain(x)){
			return true;
		}
		if(neighbors[x].contain(y) || neighbors[y].contain(x)){
			throw new UnsupportedOperationException("asymmetric adjacency matrix in an undirected graph");
		}
		return false;
	}

	@Override
	public boolean removeEdge(int x, int y) {
		if ((neighbors[x].contain(y)) && (neighbors[y].contain(x))){
			neighbors[x].remove(y);
			neighbors[y].remove(x);
			return true;
		}
		if ((neighbors[x].contain(y)) || (neighbors[y].contain(x))){
			throw new UnsupportedOperationException("asymmetric adjacency matrix in an undirected graph");
		}
		return false;
	}

	@Override
	public INeighbors getNeighborsOf(int x) {
		return neighbors[x];
	}
}
