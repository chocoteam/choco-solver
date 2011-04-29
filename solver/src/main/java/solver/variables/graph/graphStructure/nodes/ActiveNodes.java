package solver.variables.graph.graphStructure.nodes;

import solver.variables.graph.IActiveNodes;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 */
public class ActiveNodes extends BitSet implements IActiveNodes {

    public ActiveNodes(int nbBits) {
        super(nbBits);
    }

    @Override
    public int nextValue(int from) {
        return this.nextSetBit(from);
    }

    @Override
    public int nbActive() {
        return this.cardinality();
    }

    @Override
    public void activate(int idx) {
        this.set(idx,true);
    }

    @Override
    public void desactivate(int idx) {
        this.set(idx,false);
    }

    @Override
    public boolean isActive(int idx) {
        return this.get(idx);
    }

    @Override
    public ActiveNodesIterator<IActiveNodes> iterator() {
        return new ActiveNodesIterator<IActiveNodes>(this);
    }

	@Override
	public int nbNodes() {
		return this.cardinality();
	}
}
