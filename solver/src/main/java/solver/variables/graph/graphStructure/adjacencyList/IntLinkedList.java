package solver.variables.graph.graphStructure.adjacencyList;

import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.iterators.LinkedListIterator;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 */
public class IntLinkedList implements INeighbors {

    /**
     * The first cell of the linked list
     */
    IntCell first;
    int size;

    public IntLinkedList() {
        this.first = null;
        this.size = 0;
    }

    public IntLinkedList(IntCell first) {
        this.first = first;
        int k = 0;
        IntCell current = first;
        while (current != null) {
            k++;
            current = current.getNext();
        }
        this.size = k;
    }

    @Override
    public LinkedListIterator<IntLinkedList> iterator() {
        return new LinkedListIterator<IntLinkedList>(this);
    }

    @Override
    /**
     * Test for an empty list
     * @return true iff the list is empty
     */
    public boolean isEmpty() {
        return this.first == null;
    }

    @Override
    /**
     * The number of elements in the linked list
     * @return the number of cells
     */
    public int neighborhoodSize() {
        return this.size;
    }

    /**
     * Accessor on the first cell of the linked list
     * @return a cell
     */
    public IntCell get() {
        return first;
    }

    @Override
    /**
     * Check if the linked list contain the value element
     * @param element an int
     * @return true iff the linked list contain the value element
     */
    public boolean contain(int element) {
        boolean res = false;
        IntCell current = first;
        while (!res && current != null) {
            if (current.contains(element)) {
                res = true;
            }
            current = current.getNext();
        }
        return res;
    }

    public IntCell getFirst() {
        return first;
    }

    /**
     * Return the cell containing the value element if it exists
     * @param element an int
     * @return the cell containing the value element, null otherwise
     */
    public IntCell getCell(int element) {
        IntCell current = first;
        IntCell res = null;
        while (current != null) {
            if (current.contains(element)) {
                res = current;
            }
            current = current.getNext();
        }
        return res;
    }

    /**
     * Search for the last element of the linked list
     * @return the last cell of the linked list
     */
    public IntCell getLast() {
        IntCell current = first;
        IntCell res = null;
        while (current != null) {
            res = current;
            current = current.getNext();
        }
        return res;
    }

    @Override
    /**
     * Add an element in the first position. Beware, there is no garanty this element does not exist in the linked list
     * @param element an int
     */
    public void add(int element) {
        this.first = new IntCell(element, first);
        this.size++;
    }

    @Override
    /**
     * Remove the first occurrence of the element.
     * @param element an int
     * @return true iff the element has been effectively removed
     */
    public boolean remove(int element) {
        IntCell current = first;
        IntCell previous = null;
        boolean removed = false;
        while (!removed && current != null) {
            if (current.contains(element)) {
                if (previous == null) {
                    this.first = current.getNext();
                } else {
                    previous.setNext(current.getNext());
                }
                removed = true;
            }
            previous = current;
            current = current.getNext();
        }
        if (removed) {
            this.size--;
        }
        return removed;
    }

    /**
     * Add the linked list list at the end of the current linked list
     * @param list a linked list
     */
    public void merge(IntLinkedList list) {
        this.getLast().setNext(list.get());
        this.size += list.size;
    }

    @Override
    public String toString() {
        String res = "";
        IntCell current = first;
        while (current != null) {
            res += current;
            current = current.getNext();
        }
        return res;
    }

	@Override
	public void clear() {
		first = null;
		size = 0;
	}
	@Override
	public int getFirstElement() {
		if(first==null){
			return -1;
		}
		return first.getElement();
	}
}
