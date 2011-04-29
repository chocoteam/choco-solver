package solver.variables.graph.graphStructure.adjacencyList;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 fŽvr. 2011
 */
public class IntCell {

    int element;

    IntCell next;

    public IntCell(int element) {
        this(element,null);
    }

    public IntCell(int element, IntCell next) {
        this.element = element;
        this.next = next;
    }

    public IntCell(int element, int nextElement) {
        this.element = element;
        this.next = new IntCell(nextElement);
    }

    public int getElement() {
        return element;
    }

    public IntCell getNext() {
        return next;
    }

    public void setNext(IntCell next) {
        this.next = next;
    }

    public boolean equals(Object o) {
        if (o instanceof IntCell) {
            IntCell c = (IntCell) o;
            return c.getElement() == this.getElement();
        } else {
            return false;
        }
    }

    public boolean contains(int element) {
        return this.element == element;
    }

    public String toString() {
        if (next == null) {
            return ""+element;
        } else {
            return ""+element+" -> ";
        }
    }

}
