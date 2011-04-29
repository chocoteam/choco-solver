package solver.variables.graph.directedGraph;

import solver.variables.graph.IGraph;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;

/**
 * @author Jean-Guillaume Fages
 * 
 * Provides services to manipulate directed graphs
 */
public interface IDirectedGraph extends IGraph {

    /**remove arc (from,to) from the graph
     * @param from
     * @param to
     * @return true iff arc (from,to) was in the graph
     */
    boolean removeArc(int from, int to);
    
    /**add arc (from,to) to the graph
     * @param from
     * @param to
     * @return true iff arc (from,to) was not already in the graph
     */
    boolean addArc(int from, int to);

    /**Get the successors of node x in the graph
     * @param x
     * @return successors of x in the graph 
     */
    INeighbors getSuccessorsOf(int x);
    
    /**Get the predecessors of node x in the graph
     * @param x
     * @return predecessors of x in the graph 
     */
    INeighbors getPredecessorsOf(int x);
    
    /**Test whether arc (x,y) exists or not in the graph
     * @param x
     * @param y
     * @return true iff arc (x,y) exists in the graph
     */
    boolean arcExists(int x, int y);

	/**
     * An iterator over the successors of node x in the graph
     * @param x node's index
     * @param <N> an iterable data structure representing the successors of node x
     * @return an iterator over the indices of the successors of node x in the graph
     */
    <N extends INeighbors> AbstractNeighborsIterator<N> successorsIteratorOf(int x);
    
    /**
     * An iterator over the predecessors of node x in the graph
     * @param x node's index
     * @param <N> an iterable data structure representing the predecessors of node x
     * @return an iterator over the indices of the predecessors of node x in the graph
     */
    <N extends INeighbors> AbstractNeighborsIterator<N> predecessorsIteratorOf(int x);
}
