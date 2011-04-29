package solver.variables.graph.graphStructure.iterators;

import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 10 févr. 2011
 */
public class BitSetIterator<N extends BitSetNeighbors> extends AbstractNeighborsIterator<N> {

    int current;

    public BitSetIterator(N data) {
        super(data);
        this.current = data.nextValue(0);
    }

    @Override
    public boolean hasNext() {
        return current > -1;
    }

    @Override
    public int next() {
        int val = current;
        current = data.nextValue(val+1);
        return val;
    }

    @Override
    public void remove() {
        data.remove(current);
        current = data.nextValue(current);
    }
}
