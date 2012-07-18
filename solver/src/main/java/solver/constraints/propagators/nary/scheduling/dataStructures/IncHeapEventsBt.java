package solver.constraints.propagators.nary.scheduling.dataStructures;


import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.memory.trailing.EnvironmentTrailing;

/**
 * 
 * @author Arnaud Letort
 * A class implementing a backtrackable heap of Events. Events are sorted by increasing date.
 * 
 */
public class IncHeapEventsBt {
	
	private IStateIntVector date; // Key !
	private IStateIntVector task;
	private IStateIntVector type;
	private IStateIntVector dec;
	private IStateInt size;
	private IStateInt nbEvents;
	
	
	private final EnvironmentTrailing environment;
	
	public IncHeapEventsBt(final EnvironmentTrailing _env, int _size) {
		
		this.environment = _env;
		this.nbEvents = _env.makeInt(0);
		this.size = _env.makeInt(_size);
		date = _env.makeIntVector(_size, -666);
		task = _env.makeIntVector(_size, -666);
		type = _env.makeIntVector(_size, -666);
		dec = _env.makeIntVector(_size, -666);
		
	}
	
	public void clear() {
		nbEvents.set(0);
	}
	
	/**
	 * Inserts the specified event into this heap
	 * @param _date
	 * @param _task
	 * @param _type
	 * @param _dec
	 */
	public void add(int _date, int _task, int _type, int _dec) {
		int i = nbEvents.get();
		
		int parent = parent(i);
		while ( (i>0) && (date.get(parent) > _date) ) {
			date.set(i, date.get(parent));
			task.set(i, task.get(parent));
			type.set(i, type.get(parent));
			dec.set(i, dec.get(parent));
			i = parent;
			parent = parent(i);
		}
		date.set(i, _date);
		task.set(i, _task);
		type.set(i, _type);
		dec.set(i, _dec);
		nbEvents.add(1);
	}
	
	/**
	 * Retrieves and removes the top of this heap, or returns null if this queue is empty.
	 * @return 
	 */
	public int[] poll() {
		if (nbEvents.get() == 0) return null;
		int[] top = new int[4];
		top[0] = date.get(0);
		top[1] = task.get(0);
		top[2] = type.get(0);
		top[3] = dec.get(0);
		nbEvents.add(-1);
		date.set(0, date.get(nbEvents.get()));
		task.set(0, task.get(nbEvents.get()));
		type.set(0, type.get(nbEvents.get()));
		dec.set(0, dec.get(nbEvents.get()));
		int vdate = date.get(0);
		int vtask = task.get(0);
		int vtype = type.get(0);
		int vdec = dec.get(0);
		int i = 0;
		int j;
		while (!isLeaf(i)) {
			j = leftChild(i);
			if (hasRightChild(i) && date.get(rightChild(i)) < date.get(leftChild(i))) {
				j = rightChild(i);
			}
			if (vdate <= date.get(j)) break;
			date.set(i, date.get(j));
			task.set(i, task.get(j));
			type.set(i, type.get(j));
			dec.set(i, dec.get(j));
			i = j;
		}
		date.set(i, vdate);
		task.set(i, vtask);
		type.set(i, vtype);
		dec.set(i, vdec);
		return top;
	}

	/**
	 * Removes the top of this heap. (without any check)
	 * @return 
	 */
	public void remove() {
		nbEvents.add(-1);
		date.set(0, date.get(nbEvents.get()));
		task.set(0, task.get(nbEvents.get()));
		type.set(0, type.get(nbEvents.get()));
		dec.set(0, dec.get(nbEvents.get()));
		int vdate = date.get(0);
		int vtask = task.get(0);
		int vtype = type.get(0);
		int vdec = dec.get(0);
		int i = 0;
		int j;
		while (!isLeaf(i)) {
			j = leftChild(i);
			if (hasRightChild(i) && date.get(rightChild(i)) < date.get(leftChild(i))) {
				j = rightChild(i);
			}
			if (vdate <= date.get(j)) break;
			date.set(i, date.get(j));
			task.set(i, task.get(j));
			type.set(i, type.get(j));
			dec.set(i, dec.get(j));
			i = j;
		}
		date.set(i, vdate);
		task.set(i, vtask);
		type.set(i, vtype);
		dec.set(i, vdec);
	}
	
	/**
	 * Retrieves, but does not remove, the top event of this heap.
	 * @return
	 */
	public int[] peek() {
		if (isEmpty()) return null;
		else {
			int[] res = new int[4];
			res[0] = date.get(0);
			res[1] = task.get(0);
			res[2] = type.get(0);
			res[3] = dec.get(0);
			return res;
		}
	}
	
	/**
	 * Retrieves, but does not remove, the date of the top event of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekDate() {
		return date.get(0);
	}
	
	/**
	 * Retrieves, but does not remove, the task of the top event of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekTask() {
		return task.get(0);
	}
	
	/**
	 * Retrieves, but does not remove, the type of the top event of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekType() {
		return type.get(0);
	}
	
	/**
	 * Retrieves, but does not remove, the decrement of the top event of this heap. Doesn't check if the heap is empty.
	 * @return
	 */
	public int peekDec() {
		return dec.get(0);
	}
	
	public boolean isEmpty() {
		return (nbEvents.get() == 0);
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
		return ( (((i + 1) << 1) - 1) >= nbEvents.get());
	}
	
	private boolean hasRightChild(int i) {
		return ( ((i + 1) << 1) < nbEvents.get());
	}
	
	public String toString() {
		String res = "";
		int i;
		for(i=0;i<nbEvents.get();i++) {
			res += "<date="+date.get(i)+",task="+task.get(i)+",type=";
			switch(type.get(i)) {
				case Event.SCP : res += "SCP"; break;
				case Event.ECP : res += "ECP"; break;
				case Event.PR : res += "PR"; break;
				case Event.CCP : res += "CCP"; break;
				case Event.FSCP : res += "FSCP"; break;
				case Event.FECP : res += "FECP"; break;
				case Event.AP : res += "AP"; break;
				default : res += "UNKNOWN EVENT"; assert(false); break;
		}
			res += ",dec="+dec.get(i)+"> ";
		}
		return res;
	}
	
	public String peekEvent() {
		String res = "";
		res += "<date="+date.get(0)+",task="+task.get(0)+",type=";
		switch(type.get(0)) {
			case Event.SCP : res += "SCP"; break;
			case Event.ECP : res += "ECP"; break;
			case Event.PR : res += "PR"; break;
			case Event.CCP : res += "CCP"; break;
			case Event.FSCP : res += "FSCP"; break;
			case Event.FECP : res += "FECP"; break;
			case Event.AP : res += "AP"; break;
			default : res += "UNKNOWN EVENT"; assert(false); break;
		}
		res += ",dec="+dec.get(0)+"> ";
		return res;
	}
	
	public int nbEvents() {
		return nbEvents.get();
	}
	
}
