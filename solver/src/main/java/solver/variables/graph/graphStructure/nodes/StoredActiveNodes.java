package solver.variables.graph.graphStructure.nodes;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.S64BitSet;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 */
public class StoredActiveNodes extends S64BitSet implements IActiveNodes {

    public StoredActiveNodes(IEnvironment environment, int nbits) {
        super(environment, nbits);
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
