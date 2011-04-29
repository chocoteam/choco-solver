package solver.variables.graph;

import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 */
public interface INeighbors {

    /**Add element to the neighborhood
     * Does not guaranty there is no duplications
     * @param element
     */
    void add(int element);

    /**Remove the first occurence of element from the neighborhood
     * @param element
     * @return true iff element was in the neighborhood and has been removed
     */
    boolean remove(int element);

    /**Test the existence of element in the neighborhood
     * @param element
     * @return true iff the neighborhood contains element
     */
    boolean contain(int element);

    /**
     * @return true iff the neighborhood is empty
     */
    boolean isEmpty();

    /**
     * @return the number of elements in the neighborhood
     */
    int neighborhoodSize();

    /**Get an iterator to iterate on the neighborhood
     * @param <N>
     * @return an iterator to iterate on the neighborhood
     */
    <N extends INeighbors> AbstractNeighborsIterator<N> iterator();
    
    /**
     * Remove all elements from the neighborhood
     */
    void clear();
    
    /**
     * @return the first element of the neighborhood, -1 empty set
     */
    int getFirstElement();
}
