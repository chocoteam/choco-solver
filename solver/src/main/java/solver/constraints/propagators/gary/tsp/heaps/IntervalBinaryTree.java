package solver.constraints.propagators.gary.tsp.heaps;

import gnu.trove.map.hash.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 30/03/12
 */
public class IntervalBinaryTree implements Heap{
    final int size;
	int[] mates;
    TIntObjectHashMap<Node> nodeFromElement;
    Node root;
    Deque<Node> freeNodes; // dynamic allocation of free nodes
    boolean mergeSide; // when a removal has to be executed, alternatively change the side of the merge operation is performed

    // WARNING : size nodes are allocated in the dynamic pool of nodes
    public IntervalBinaryTree(int size) {
		mates = new int[size];
        this.freeNodes = new ArrayDeque<Node>();
        this.mergeSide = true;
        this.size = size;
        this.nodeFromElement = new TIntObjectHashMap<Node>(this.size);
        for (int i = 0; i < size; i++) {
            this.freeNodes.addFirst(new Node(-1, -1));
        }
        this.root = null;
    }

	@Override
	public void add(int element, double element_key, int mate) {
		if(nodeFromElement.contains(element)){
			decreaseKey(element,element_key,mate);
		}else{
			mates[element] = mate;
			insert(element,element_key);
		}
	}

	@Override
	public void decreaseKey(int element, double new_element_key, int newMate) {
		if(new_element_key<nodeFromElement.get(element).value){
			mates[element] = newMate;
			update(element,new_element_key);
		}
	}

	@Override
	public int pop() {
		return removeMin();
	}

	@Override
	public void clear() {
		while(!this.isEmpty()){
			removeMin();
		}
	}

	// O(1)
    public boolean isEmpty() {
        return root == null;
    }

	@Override
	public int getMate(int element) {
		return mates[element];  //To change body of implemented methods use File | Settings | File Templates.
	}

	// 0(1)
    public int size() {
        return size - freeNodes.size();
    }

    // O(1)
    public double getLastRemValue() {
        return freeNodes.getLast().value;
    }

    // O(log(n))
    public void insert(int element, double value) {
        Node n = this.freeNodes.removeFirst();
        n.clear();
        n.setNode(element, value);
        nodeFromElement.put(element, n);
        if (root == null) {
            root = n;
        } else {
            root.put(n);
        }
        assert flattenBinTree(Type.INSERT): n;
    }

    // O(log(n))
    public int remove(int element) {
        Node n = nodeFromElement.get(element);
        Node potRoot = n.remove(mergeSide);
        if (n == root) {
            if (potRoot != null) {
                root = potRoot;
            } else { // last element removal
                assert size() == 1 : flattenBinTree(Type.REMOVE);
                root = null;
            }
        }
        freeNode(n);
        mergeSide = !mergeSide;
        assert flattenBinTree(Type.REMOVE): n;
        return n.element;
    }

    // O(log(n))
    public void update(int element, double newValue) {
        insert(remove(element), newValue);
    }

    // O(log(n))
    public int removeMin() {
        return remove(root.minChildren().element);
    }

    private void freeNode(Node n) {
        nodeFromElement.remove(n.element);
        freeNodes.addLast(n);
    }

    /**
     * ******* only for debug *********
     */
    private ArrayList<Node> sortedNodes;

    enum Type {
        INSERT, REMOVE, CONSTRUCTOR
    }

    private boolean flattenBinTree(Type t) {
        sortedNodes = new ArrayList<Node>();
        if (root != null) {
            openNode(root);
        }
        //System.out.println("[" + t + "]sorted nodes: " + sortedNodes);
        //System.out.println("[" + t + "]free nodes: " + freeNodes);
        return sortedNodes.size() == size() && checkResult();
    }

    private void openNode(Node next) {
        if (next.left == null && next.right == null) {
            sortedNodes.add(next);
        } else {
            if (next.left != null) {
                openNode(next.left);
            }
            sortedNodes.add(next);
            if (next.right != null) {
                openNode(next.right);
            }
        }
    }

    private boolean checkResult() {
        for (int i = 0; i < sortedNodes.size() - 1; i++) {
            if (sortedNodes.get(i).value > sortedNodes.get(i + 1).value) {
                System.out.println(this.toString());
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < sortedNodes.size(); i++) {
            s += sortedNodes.get(i) + ",";
        }
        return s;
    }
    /**
     * ******* end debug *********
     */

    private class Node {
        int element;
        double value;
        Node father;
        Node left;
        Node right;

        Node(int element, double value) {
            this.element = element;
            this.value = value;
            this.father = null;
            this.right = null;
            this.left = null;
        }

        public void clear() {
            this.element = -1;
            this.value = -1;
            this.left = null;
            this.right = null;
            this.father = null;
        }

        public void setNode(int element, double value) {
            this.element = element;
            this.value = value;
        }

        // log(n)
        public void put(Node next) {
            Node cur = this;
            while (cur != null) {
                if (next.value < cur.value ||(next.value == cur.value && next.element < cur.element)) {  // next go to the left hand side
                    if (cur.left != null) {
                        cur = cur.left;
                    } else {
                        cur.left = next;
                        cur.left.father = cur;
                        cur = null;
                    }
                } else {  // next go to the right hand side
                    if (cur.right != null) {
                        cur = cur.right;
                    } else {
                        cur.right = next;
                        cur.right.father = cur;
                        cur = null;
                    }
                }
            }
        }

        // O(log(n))
        public Node remove(boolean side) {
            return side ? removeLeft() : removeRight();
        }

        private Node removeLeft() {
            if (left != null) {
                return this.mergeLeftUp();
            } else {
                if (right != null) {
                    return this.mergeRightUp();
                } else {
                    if (this.father != null) {
                        if (this.father.left == this) {
                            this.father.left = null;
                        } else {
                            this.father.right = null;
                        }
                        this.father = null;
                    }
                    return null;
                }
            }
        }

        private Node removeRight() {
            if (right != null) {
                return this.mergeRightUp();
            } else {
                if (left != null) {
                    return this.mergeLeftUp();
                } else {
                    if (this.father != null) {
                        if (this.father.left == this) {
                            this.father.left = null;
                        } else {
                            this.father.right = null;
                        }
                        this.father = null;
                    }
                    return null;
                }
            }
        }

        // O(log(n)) : Note that this.left is not null here
        private Node mergeLeftUp() {
            Node potRoot = null;
            this.left.father = this.father;
            if (this.father != null) {
                if (this.father.left == this) {
                    this.father.left = this.left;
                } else {
                    this.father.right = this.left;
                }
            } else {
                potRoot = this.left;
            }
            if (this.right != null) {
                Node maxRightLeft = this.left.maxChildren();
                maxRightLeft.right = this.right;
                this.right.father = maxRightLeft;
            }
            return potRoot;
        }

        // O(log(n)) : Note that this.right is not null here
        private Node mergeRightUp() {
            Node potRoot = null;
            this.right.father = this.father;
            if (this.father != null) {
                if (this.father.left == this) {
                    this.father.left = this.right;
                } else {
                    this.father.right = this.right;
                }
            } else {
                potRoot = this.right;
            }
            if (this.left != null) {
                Node minLeftRight = this.right.minChildren();
                minLeftRight.left = this.left;
                this.left.father = minLeftRight;
            }
            return potRoot;
        }

        // O(log(n))
        public Node maxChildren() {
            Node cur = this;
            Node rcur = right;
            while (rcur != null) {
                cur = rcur;
                rcur = rcur.right;
            }
            return cur;
        }

        // O(log(n))
        public Node minChildren() {
            Node cur = this;
            Node lcur = left;
            while (lcur != null) {
                cur = lcur;
                lcur = lcur.left;
            }
            return cur;
        }

        public String toString() {
            return "(" + element + "," + value + ")";
        }

        /**
         * ******* only for debug *********
         */
        public String completeString() {
            if (father != null) {
                if (left != null) {
                    if (right != null) {
                        return this + "--F:" + father + ";G:" + left + ";D:" + right;
                    } else {
                        return this + "--F:" + father + ";G:" + left + ";D:-";
                    }
                } else {
                    if (right != null) {
                        return this + "--F:" + father + ";G:-" + ";D:" + right;
                    } else {
                        return this + "--F:" + father + ";G:-" + ";D:-";
                    }
                }
            } else {
                if (left != null) {
                    if (right != null) {
                        return this + "--F:-" + ";G:" + left + ";D:" + right;
                    } else {
                        return this + "--F:-" + ";G:" + left + ";D:-";
                    }
                } else {
                    if (right != null) {
                        return this + "--F:-" + ";G:-" + ";D:" + right;
                    } else {
                        return this + "--F:-" + ";G:-" + ";D:-";
                    }
                }
            }
        }
        /********** end debug **********/
    }
}
