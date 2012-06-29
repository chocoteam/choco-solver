package solver.constraints.propagators.nary.scheduling.dataStructures;



public class HeapConflict {

	private int[] height; // Key !
	private int[] task;
	private int size;
	private int nbItems;

	
	public HeapConflict(int _size) {
		this.nbItems = 0;
		this.size = _size;
		height = new int[_size];
		task = new int[_size];
	}
	
	public void clear() {
		nbItems = 0;
	}
	
	/**
	 * Inserts the specified item into this heap
	 * @param _height
	 * @param _task
	 */
	public void add(int _height, int _task) {
		int i = nbItems;
		
		int parent = parent(i);
		while ( (i>0) && (height[parent] > _height) ) {
			height[i] = height[parent];
			task[i] = task[parent];
			i = parent;
			parent = parent(i);
		}
		height[i] = _height;
		task[i] = _task;
		nbItems++;
		
	}
	
	public void add(int[] item) {
		int i = nbItems;
		
		int parent = parent(i);
		while ( (i>0) && (height[parent] > item[0]) ) {
			height[i] = height[parent];
			task[i] = task[parent];
			i = parent;
			parent = parent(i);
		}
		height[i] = item[0];
		task[i] = item[1];
		nbItems++;
		
	}
	
	/**
	 * Retrieves and removes the top of this heap, or returns null if this queue is empty.
	 * @return 
	 */
	public int[] poll() {
		if (nbItems == 0) return null;
		int[] top = new int[2];
		top[0] = height[0];
		top[1] = task[0];
		nbItems--;
		height[0] = height[nbItems];
		task[0] = task[nbItems];
		int vheight = height[0];
		int vtask = task[0];
		int i = 0;
		int j;
		while (!isLeaf(i)) {
			j = leftChild(i);
			if (hasRightChild(i) && height[rightChild(i)] < height[leftChild(i)]) {
				j = rightChild(i);
			}
			if (vheight <= height[j]) break;
			height[i] = height[j];
			task[i] = task[j];
			i = j;
		}
		height[i] = vheight;
		task[i] = vtask;
		return top;
	}
	
	/**
	 * Removes the top of this heap. (without any check)
	 * @return 
	 */
	public void remove() {
		nbItems--;
		height[0] = height[nbItems];
		task[0] = task[nbItems];
		int vheight = height[0];
		int vtask = task[0];
		int i = 0;
		int j;
		while (!isLeaf(i)) {
			j = leftChild(i);
			if (hasRightChild(i) && height[rightChild(i)] < height[leftChild(i)]) {
				j = rightChild(i);
			}
			if (vheight <= height[j]) break;
			height[i] = height[j];
			task[i] = task[j];
			i = j;
		}
		height[i] = vheight;
		task[i] = vtask;
		
	}
	
	/**
	 * Retrieves, but does not remove, the height of the top item of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekHeight() {
		return height[0];
	}
	
	/**
	 * Retrieves, but does not remove, the task of the top item of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekTask() {
		return task[0];
	}
	
	public boolean isEmpty() {
		return (nbItems == 0);
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
		return ( (((i + 1) << 1) - 1) >= nbItems);
	}
	
	private boolean hasRightChild(int i) {
		return ( ((i + 1) << 1) < nbItems);
	}
	
	public String toString() {
		String res = "";
		for(int i=0;i<nbItems;i++) {
			res += "<height="+height[i]+",task="+task[i]+"> ";
		}
		return res;
	}
	
	public int[] getHeights() {
		int[] res = new int[nbItems];
		for(int i=0;i<nbItems;i++) {
			res[i] = height[i];
		}
		return res;
	}
	
	public boolean isCorrect() {
		int peek = peekHeight();
		for (int i=1;i<nbItems();i++) {
			if (height[i] < peek) { System.out.println("failure"); return false; }
		}
		return true;
	}
	
	public int nbItems() {
		return nbItems;
	}
	
}
