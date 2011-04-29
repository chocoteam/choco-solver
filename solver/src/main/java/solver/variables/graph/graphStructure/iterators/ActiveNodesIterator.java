package solver.variables.graph.graphStructure.iterators;

import solver.variables.graph.IActiveNodes;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 10 févr. 2011
 */
public class ActiveNodesIterator<N extends IActiveNodes> implements Iterator<Integer> {

    N data;
    int current;

    public ActiveNodesIterator(N data) {
        this.data = data;
        current = data.nextValue(0);
    }

    @Override
    public boolean hasNext() {
        return current > -1;
    }

    @Override
    public Integer next() {
        int val = current;
        current = data.nextValue(val+1);
        return val;
    }

    @Override
    public void remove() {
        data.desactivate(current);
        current = data.nextValue(current);
    }

}
