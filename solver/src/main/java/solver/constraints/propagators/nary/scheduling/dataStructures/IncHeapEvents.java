package solver.constraints.propagators.nary.scheduling.dataStructures;

/**
 * 
 * @author Arnaud Letort
 * A class implementing a heap of Events. Events are sorted by increasing date.
 * 
 */
public class IncHeapEvents {
	
	private int[] date; // Key !
	private int[] task;
	private int[] type;
	private int[] dec;
	private int size;
	private int nbEvents;
	
	
	public IncHeapEvents(int _size) {
		
		this.nbEvents = 0;
		this.size = _size;
		date = new int[size];
		task = new int[size];
		type = new int[size];
		dec = new int[size];
		
	}
	
	public void clear() {
		nbEvents = 0;
	}
	
	/**
	 * Inserts the specified event into this heap
	 * @param _date
	 * @param _task
	 * @param _type
	 * @param _dec
	 */
	public void add(int _date, int _task, int _type, int _dec) {
		int i = nbEvents;
		
		int parent = parent(i);
		while ( (i>0) && (date[parent] > _date) ) {
			date[i] = date[parent];
			task[i] = task[parent];
			type[i] = type[parent];
			dec[i] = dec[parent];
			i = parent;
			parent = parent(i);
		}
		date[i] = _date;
		task[i] = _task;
		type[i] = _type;
		dec[i] = _dec;
		nbEvents++;
	}
	
	/**
	 * Retrieves and removes the top of this heap, or returns null if this queue is empty.
	 * @return 
	 */
	public int[] poll() {
		if (nbEvents == 0) return null;
		int[] top = new int[4];
		top[0] = date[0];
		top[1] = task[0];
		top[2] = type[0];
		top[3] = dec[0];
		nbEvents--;
		date[0] = date[nbEvents];
		task[0] = task[nbEvents];
		type[0] = type[nbEvents];
		dec[0] = dec[nbEvents];
		int vdate = date[0];
		int vtask = task[0];
		int vtype = type[0];
		int vdec = dec[0];
		int i = 0;
		int j;
		while (!isLeaf(i)) {
			j = leftChild(i);
			if (hasRightChild(i) && date[rightChild(i)] < date[leftChild(i)]) {
				j = rightChild(i);
			}
			if (vdate <= date[j]) break;
			date[i] = date[j];
			task[i] = task[j];
			type[i] = type[j];
			dec[i] = dec[j];
			i = j;
		}
		date[i] = vdate;
		task[i] = vtask;
		type[i] = vtype;
		dec[i] = vdec;
		return top;
	}

	/**
	 * Removes the top of this heap. (does not check if empty)
	 * @return 
	 */
	public void remove() {
		nbEvents--;
		date[0] = date[nbEvents];
		task[0] = task[nbEvents];
		type[0] = type[nbEvents];
		dec[0] = dec[nbEvents];
		int vdate = date[0];
		int vtask = task[0];
		int vtype = type[0];
		int vdec = dec[0];
		int i = 0;
		int j;
		while (!isLeaf(i)) {
			j = leftChild(i);
			if (hasRightChild(i) && date[rightChild(i)] < date[leftChild(i)]) {
				j = rightChild(i);
			}
			if (vdate <= date[j]) break;
			date[i] = date[j];
			task[i] = task[j];
			type[i] = type[j];
			dec[i] = dec[j];
			i = j;
		}
		date[i] = vdate;
		task[i] = vtask;
		type[i] = vtype;
		dec[i] = vdec;
	}
	
	/**
	 * Retrieves, but does not remove, the top event of this heap.
	 * @return
	 */
	public int[] peek() {
		if (isEmpty()) return null;
		else {
			int[] res = new int[4];
			res[0] = date[0];
			res[1] = task[0];
			res[2] = type[0];
			res[3] = dec[0];
			return res;
		}
	}
	
	/**
	 * Retrieves, but does not remove, the date of the top event of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekDate() {
		return date[0];
	}
	
	/**
	 * Retrieves, but does not remove, the task of the top event of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekTask() {
		return task[0];
	}
	
	/**
	 * Retrieves, but does not remove, the type of the top event of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekType() {
		return type[0];
	}
	
	/**
	 * Retrieves, but does not remove, the decrement of the top event of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekDec() {
		return dec[0];
	}
	
	public boolean isEmpty() {
		return (nbEvents == 0);
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
		return ( (((i + 1) << 1) - 1) >= nbEvents);
	}
	
	private boolean hasRightChild(int i) {
		return ( ((i + 1) << 1) < nbEvents);
	}
	
	public String toString() {
		String res = "";
		int i;
		for(i=0;i<nbEvents;i++) {
			res += "<date="+date[i]+",task="+task[i]+",type=";
			switch(type[i]) {
				case Event.SCP : res += "SCP"; break;
				case Event.ECP : res += "ECP"; break;
				case Event.PR : res += "PR"; break;
				case Event.CCP : res += "CCP"; break;
				case Event.FSCP : res += "FSCP"; break;
				case Event.FECP : res += "FECP"; break;
				default : res += "UNKNOWN EVENT"; assert(false); break;
		}
			res += ",dec="+dec[i]+">\n";
		}
		return res;
	}
	
	public String peekEvent() {
		String res = "";
		res += "<date="+date[0]+",task="+task[0]+",type=";
		switch(type[0]) {
			case Event.SCP : res += "SCP"; break;
			case Event.ECP : res += "ECP"; break;
			case Event.PR : res += "PR"; break;
			case Event.CCP : res += "CCP"; break;
			case Event.FSCP : res += "FSCP"; break;
			case Event.FECP : res += "FECP"; break;
			default : res += "UNKNOWN EVENT"; assert(false); break;
		}
		res += ",dec="+dec[0]+">\n";	
		return res;
	}
	
	public int nbEvents() {
		return nbEvents;
	}
	
}
