package solver.constraints.propagators.nary.scheduling.dataStructures;

import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.memory.trailing.EnvironmentTrailing;

public class HeapCheckBt {

	private IStateIntVector height; // Key !
	private IStateIntVector task;
	private IStateInt size;
	private IStateInt nbItems;
	
	private final EnvironmentTrailing environment;
	
	public HeapCheckBt(final EnvironmentTrailing _env, int _size) {
		this.environment = _env;
		this.nbItems = _env.makeInt(0);
		this.size = _env.makeInt(_size);
		height = _env.makeIntVector(size.get(), -666);
		task = _env.makeIntVector(size.get(), -666);
	}
	
	public void clear() {
		nbItems.set(0);
	}
	
	/**
	 * Inserts the specified item into this heap
	 * @param _height
	 * @param _task
	 */
	public void add(int _height, int _task) {
		int i = nbItems.get();
		
		int parent = parent(i);
		while ( (i>0) && (height.get(parent) < _height) ) {
			height.set(i, height.get(parent));
			task.set(i, task.get(parent));
			i = parent;
			parent = parent(i);
		}
		height.set(i, _height);
		task.set(i, _task);
		nbItems.add(1);
		
	}
	
	/**
	 * Retrieves and removes the top of this heap, or returns null if this queue is empty.
	 * @return 
	 */
	public int[] poll() {
		if (nbItems.get() == 0) return null;
		int[] top = new int[2];
		top[0] = height.get(0);
		top[1] = task.get(0);
		nbItems.add(-1);
		height.set(0, height.get(nbItems.get()));
		task.set(0, task.get(nbItems.get()));
		int vheight = height.get(0);
		int vtask = task.get(0);
		int i = 0;
		int j;
		while (!isLeaf(i)) {
			j = leftChild(i);
			if (hasRightChild(i) && height.get(rightChild(i)) > height.get(leftChild(i))) {
				j = rightChild(i);
			}
			if (vheight >= height.get(j)) break;
			height.set(i, height.get(j));
			task.set(i, task.get(j));
			i = j;
		}
		height.set(i, vheight);
		task.set(i, vtask);
		
		return top;
	}
	
	/**
	 * Removes the top of this heap. (without any check)
	 * @return 
	 */
	public void remove() {
		nbItems.add(-1);
		height.set(0, height.get(nbItems.get()));
		task.set(0, task.get(nbItems.get()));
		int vheight = height.get(0);
		int vtask = task.get(0);
		int i = 0;
		int j;
		while (!isLeaf(i)) {
			j = leftChild(i);
			if (hasRightChild(i) && height.get(rightChild(i)) > height.get(leftChild(i))) {
				j = rightChild(i);
			}
			if (vheight >= height.get(j)) break;
			height.set(i, height.get(j));
			task.set(i, task.get(j));
			i = j;
		}
		height.set(i, vheight);
		task.set(i, vtask);
		
	}
	
	/**
	 * Retrieves, but does not remove, the height of the top item of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekHeight() {
		return height.get(0);
	}
	
	/**
	 * Retrieves, but does not remove, the task of the top item of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekTask() {
		return task.get(0);
	}
	
	public boolean isEmpty() {
		return (nbItems.get() == 0);
	}
	
	private int parent(int _child) {
		return ((_child + 1) >> 1) - 1;
	}
	
	private int leftChild(int _parent) {
		return ((_parent + 1) << 1) - 1;
	}
	
	private int rightChild(int _parent) {
		return ((_parent + 1) << 1);
	}
	
	private boolean isLeaf(int i) {
		return ( (((i + 1) << 1) - 1) >= nbItems.get());
	}
	
	private boolean hasRightChild(int i) {
		return ( ((i + 1) << 1) < nbItems.get());
	}
	
	public String toString() {
		String res = "";
		for(int i=0;i<nbItems.get();i++) {
			res += "<height="+height.get(i)+",task="+task.get(i)+">";
		}
		return res;
	}
	
	public int nbItems() {
		return nbItems.get();
	}
	
	public boolean isCorrect() {
		int peek = peekHeight();
		System.out.println("peek="+peek);
		for (int i=1;i<nbItems();i++) {
			if (height.get(i) > peek) { System.out.println("failure"); return false; }
		}
		return true;
	}
	
	public int[] getHeights() {
		int[] res = new int[nbItems.get()];
		for(int i=0;i<nbItems.get();i++) {
			res[i] = height.get(i);
		}
		return res;
	}
	
}
