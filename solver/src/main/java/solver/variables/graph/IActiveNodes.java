package solver.variables.graph;

import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 */
public interface IActiveNodes {

    int nbActive();

    void activate(int idx);

    void desactivate(int idx);

    boolean isActive(int idx);

    int nextValue(int from);

    ActiveNodesIterator<IActiveNodes> iterator();
    
    void clear();
    
    int nbNodes();
}
