package solver.variables.graph.graphStructure.adjacencyList.storedStructures;

import choco.kernel.memory.IEnvironment;
import solver.variables.graph.graphStructure.adjacencyList.IntCell;
import solver.variables.graph.graphStructure.adjacencyList.IntLinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 10 févr. 2011
 */
public class StoredIntLinkedList extends IntLinkedList {

    /**
     * The first cell of the linked list
     */
    IntCell first;

    final IEnvironment environment;

    public StoredIntLinkedList(IEnvironment environment) {
        this.environment = environment;
        this.first = null;
    }

    @Override
    public void add(int element) {
        this._add(element);
        new RemOperation(environment, this, element);
    }

    protected void _add(int element) {
        super.add(element);
    }


    @Override
    public boolean remove(int element) {
        boolean done = this._remove(element);
        if (done) {
            new AddOperation(environment, this, element);
        }
        return done;
    }

    protected boolean _remove(int element) {
        return super.remove(element);
    }

}
