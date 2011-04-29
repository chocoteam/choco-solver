package solver.variables.graph.graphStructure.matrix;

import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.BitSetIterator;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 */
public class BitSetNeighbors extends BitSet implements INeighbors {

    public BitSetNeighbors(int nbits) {
        super(nbits);
    }

    public int nextValue(int from) {
        return this.nextSetBit(from);
    }

    @Override
    public AbstractNeighborsIterator<BitSetNeighbors> iterator() {
        return new BitSetIterator<BitSetNeighbors>(this);
    }

    @Override
    public void add(int element) {
        this.set(element, true);
    }

    @Override
    public boolean remove(int element) {
        boolean isIn = this.get(element);
        if (isIn) {
            this.set(element, false);
        }
        return isIn;
    }

    @Override
    public boolean contain(int element) {
        return this.get(element);
    }

    @Override
    public boolean isEmpty() {
        return this.cardinality() == 0;
    }

    @Override
    public int neighborhoodSize() {
        return this.cardinality();
    }

	@Override
	public int getFirstElement() {
		return nextSetBit(0);
	}
}
