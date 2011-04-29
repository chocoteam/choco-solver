package solver.variables.graph.graphStructure.matrix;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.S64BitSet;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 */
public class StoredBitSetNeighbors extends S64BitSet implements INeighbors {

    public StoredBitSetNeighbors(IEnvironment environment, int nbits) {
        super(environment, nbits);
    }

    @Override
    public void add(int element) {
        this.set(element,true);
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
    public int neighborhoodSize() {
        return this.cardinality();
    }

    public int nextValue(int from) {
        return this.nextSetBit(from);
    }

    @Override
    public AbstractNeighborsIterator<StoredBitSetNeighbors> iterator() {
        return new SBIterator(this);  //To change body of implemented methods use File | Settings | File Templates.
    }
    
    @Override
	public int getFirstElement() {
		return nextSetBit(0);
	}
    
    private class SBIterator extends AbstractNeighborsIterator<StoredBitSetNeighbors>{

    	private int index;
    	
		public SBIterator(StoredBitSetNeighbors data) {
			super(data);
			index = -1;
		}

		@Override
		public boolean hasNext() {
			return data.nextSetBit(index+1)>=0;
		}

		@Override
		public int next() {
			index = data.nextSetBit(index+1);
			return index;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
    }
}
