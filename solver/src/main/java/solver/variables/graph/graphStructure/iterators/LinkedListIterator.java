package solver.variables.graph.graphStructure.iterators;

import solver.variables.graph.graphStructure.adjacencyList.IntCell;
import solver.variables.graph.graphStructure.adjacencyList.IntLinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 10 févr. 2011
 */
public class LinkedListIterator<N extends IntLinkedList> extends AbstractNeighborsIterator<N> {

    IntCell current;

    public LinkedListIterator(N list) {
        super(list);
        this.current = list.getFirst();
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    public int next() {
        int val = current.getElement();
        current = current.getNext();
        return val;
    }

    public void remove() {
        IntCell next = current.getNext();
        data.remove(current.getElement());
        current = next;
    }
}
