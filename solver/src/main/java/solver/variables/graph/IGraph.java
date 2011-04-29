package solver.variables.graph;

import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;
import solver.variables.graph.IActiveNodes;

/**
 * @author Jean-Guillaume Fages, Xavier Lorca
 * 
 * Provide an interface for the graph manipulation
 */
public interface IGraph {

	
	/**
	 * @return the collection of active nodes
	 */
	IActiveNodes getActiveNodes();
	
	/**Activate node x
	 * @param x
	 * @return true iff x was not already activated
	 */
	boolean activateNode(int x);
	
	/**Desactivate node x
	 * @param x
	 * @return true iff x was activated
	 */
	boolean desactivateNode(int x);
	
	/**test whether (x,y) is in the graph or not
	 * @param x
	 * @param y
	 * @return true iff (x,y) is in the graph
	 */
	boolean edgeExists(int x, int y);
	
	/**Add edge (x,y) to the graph
	 * @param x
	 * @param y
	 * @return true iff (x,y) was not already in the graph
	 */
	boolean addEdge(int x, int y);
	
	/**Remove edge (x,y) from the graph
	 * @param x
	 * @param y
	 * @return true iff (x,y) was in the graph
	 */
	boolean removeEdge(int x,int y);
	INeighbors getNeighborsOf(int x);
	
    /**
     * The number of nodes of the graph
     * @return the number of nodes of the graph
     */
    int getNbNodes();
    
    /**
     * An iterator over the nodes active nodes
     * @return an iterator over the indices of the active nodes
     */
    ActiveNodesIterator<IActiveNodes> activeNodesIterator();

    /**
     * Neighborhood size of node x
     * @param x node's index
     * @return the size of the neighborhood of node x
     */
    int getNeighborhoodSize(int x);

    /**
     * An iterator over the neighborhood of node x
     * @param x node's index
     * @param <N> an iterable data structure representing the neighborhood of node x
     * @return an iterator over the indices of the neighbors of node x
     */
    <N extends INeighbors> AbstractNeighborsIterator<N> neighborsIteratorOf(int x);
    
    /**Get the type of the graph
     * @return the type of the graph SPARSE or DENSE
     */
    GraphType getType();
    
}
