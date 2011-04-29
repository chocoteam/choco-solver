package solver.variables.graph.graphStructure.iterators;

import choco.kernel.common.util.iterators.IntIterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 10 févr. 2011
 */
public abstract class AbstractNeighborsIterator<INeighbors> implements IntIterator {

    protected INeighbors data;

    public AbstractNeighborsIterator(INeighbors data) {
        this.data = data;
    }
}
