package solver.variables.graph.directedGraph;

import solver.variables.graph.GraphTools;
import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
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
 * *
 * Specific implementation of a directed graph
 */
public class DirectedGraph implements IDirectedGraph {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	INeighbors[] successors;
    INeighbors[] predecessors;
    /** activeIdx represents the nodes available in the graph */
    IActiveNodes activeIdx;
    GraphType type;

    //***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public DirectedGraph(int order, GraphType type) {
    	this.type = type;
    	switch (type) {
            case SPARSE:
                this.successors = new IntLinkedList[order];
                this.predecessors = new IntLinkedList[order];
                for (int i = 0; i < order; i++) {
                    this.successors[i] = new IntLinkedList();
                    this.predecessors[i] = new IntLinkedList();
                }
                break;
            case DENSE:
                this.successors = new BitSetNeighbors[order];
                this.predecessors = new BitSetNeighbors[order];
                for (int i = 0; i < order; i++) {
                    this.successors[i] = new BitSetNeighbors(order);
                    this.predecessors[i] = new BitSetNeighbors(order);
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

    public DirectedGraph(int order, boolean[][] matrix, GraphType type) {
        this(order,type);
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                if (matrix[i][j]) {
                    this.successors[i].add(j);
                    this.predecessors[j].add(i);
                }
            }
        }
    }
    
    public DirectedGraph() {}

    
	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public String toString() {
        return "Successors :\n"+toStringSuccs() +"\nPredecessors :\n"+ toStringPreds();
    }

    public String toStringSuccs() {
        String res = "";
        for (ActiveNodesIterator<IActiveNodes> itNode = activeIdx.iterator(); itNode.hasNext();) {
            int i = itNode.next();
            res += "pot-" + i + ": ";
            AbstractNeighborsIterator<INeighbors> itNext = successors[i].iterator();
            while(itNext.hasNext()) {
                int j = itNext.next();
                res += j + " ";
            }
            res += "\n";
        }
        return res;
    }
    
    public String toStringPreds() {
        String res = "";
        for (ActiveNodesIterator<IActiveNodes> itNode = activeIdx.iterator(); itNode.hasNext();) {
            int i = itNode.next();
            res += "pot-" + i + ": ";
            AbstractNeighborsIterator<INeighbors> itPrev = predecessors[i].iterator();
            while(itPrev.hasNext()) {
                int j = itPrev.next();
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
     * WARNING : not in O(1) but in O(nbSuccs[x]+nbPreds[x])
     */
    public int getNeighborhoodSize(int x) {
    	return GraphTools.mergeNeighborhoods(successors[x],predecessors[x], getNbNodes()).neighborhoodSize();
    }

    @Override
    /**
     * @inheritedDoc
     * WARNING : not in O(1) but in O(nbSuccs[x]+nbPreds[x])
     */
    public <N extends INeighbors> AbstractNeighborsIterator<N> neighborsIteratorOf(int x) {
        return GraphTools.mergeNeighborhoods(successors[x],predecessors[x], getNbNodes()).iterator();
    }

	@Override
	/**
     * @inheritedDoc
     */
    public <N extends INeighbors> AbstractNeighborsIterator<N> successorsIteratorOf(int x) {
		return successors[x].iterator();
	}

	@Override
	/**
     * @inheritedDoc
     */
    public <N extends INeighbors> AbstractNeighborsIterator<N> predecessorsIteratorOf(int x) {
		return predecessors[x].iterator();
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
		AbstractNeighborsIterator<INeighbors> iter = successorsIteratorOf(x); 
		while (iter.hasNext()){
			predecessors[iter.next()].remove(x);
		}
		successors[x].clear();
		iter = predecessorsIteratorOf(x); 
		while (iter.hasNext()){
			successors[iter.next()].remove(x);
		}
		predecessors[x].clear();
		return true;
	}

	@Override
	public boolean addEdge(int x, int y) {
		return addArc(x, y) || addArc(y, x);
	}

	@Override
	public boolean removeEdge(int x, int y) {
		return removeArc(x, y) || removeArc(y, x);
	}
	
	@Override
	public boolean edgeExists(int x, int y) {
		return arcExists(x, y) || arcExists(y, x);
	}

	@Override
	public boolean removeArc(int from, int to) {
		if ((successors[from].contain(to)) && (predecessors[to].contain(from))){
			successors[from].remove(to);
			predecessors[to].remove(from);
			return true;
		}
		if ((successors[from].contain(to)) || (predecessors[to].contain(from))){
			throw new UnsupportedOperationException("incoherent directed graph");
		}
		return false;
	}
	
	@Override
	public boolean arcExists(int from, int to){
		if (successors[from].contain(to) || predecessors[to].contain(from)){
			if (successors[from].contain(to) && predecessors[to].contain(from)){
				return true;
			}
			throw new UnsupportedOperationException("incoherent directed graph");
		}return false;
	}

	@Override
	public boolean addArc(int from, int to) {
		if ((!successors[from].contain(to)) && (!predecessors[to].contain(from))){
			successors[from].add(to);
			predecessors[to].add(from);
			return true;
		}
		if ((!successors[from].contain(to)) || (!predecessors[to].contain(from))){
			throw new UnsupportedOperationException("incoherent directed graph");
		}
		return false;
	}

	@Override
	public INeighbors getNeighborsOf(int x) {
		return GraphTools.mergeNeighborhoods(successors[x],predecessors[x], getNbNodes());
	}

	@Override
	public INeighbors getSuccessorsOf(int x) {
		return successors[x];
	}

	@Override
	public INeighbors getPredecessorsOf(int x) {
		return predecessors[x];
	}
}
