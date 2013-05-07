package solver.constraints.propagators.nary.cumulative;

import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.objects.setDataStructures.ISet;
import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: Arnaud Letort
 * Date: 18/03/13
 * Time: 13:57
 *
 * Implementation of the k-dimensional sweep algorithm RING version (without precedence relations).
 *
 *
 */
public class TTDynamicSweep {

	private ICause cause;

	private int nbTasks;
	private int nbResources;
	private IntVar[] vars, allVars;
	private IntVar[] capacities;

	private DynamicSweepMinKDimRings sweepMin;
	private DynamicSweepMaxKDimRings sweepMax;
	private final Rings ring;
	private int[] mapping; // mapping[relative id] = absolute id
	private int nbTasksInFilteringAlgo;
	private int nbEventsToAdd;
	private int[] datesAPEvents;
	private int[][] heightsAPEvents;

	private int[] ls;
	private int[] us;
	private int[] ld;
	private int[] le;
	private int[] ue;
	private int[][] h;


	public TTDynamicSweep(IntVar[] vars, int nbTasks, int nbResources, ICause cause) {
		this.allVars = new IntVar[nbTasks*4];
		this.vars = new IntVar[nbTasks*4];
		this.capacities = new IntVar[nbResources];
		for(int i=0;i<vars.length-nbResources;i++){
			this.allVars[i] = vars[i];
		}
		for(int i=0;i<nbResources;i++){
			this.capacities[i] = vars[i+nbTasks*4];
		}
		this.nbTasks = nbTasks;
		this.nbResources = nbResources;
		this.cause = cause;
		this.ring = new Rings(nbResources,nbTasks);
		this.ls = new int[nbTasks];
		this.us = new int[nbTasks];
		this.ld = new int[nbTasks];
		this.le = new int[nbTasks];
		this.ue = new int[nbTasks];
		this.h = new int[nbTasks][nbResources];
	}

	public void set(ISet subsetOfTasks) {
		this.nbTasksInFilteringAlgo = subsetOfTasks.getSize();
		// todo reremplir vars`
		int idx = 0;
		for(int t=subsetOfTasks.getFirstElement();t>=0;t=subsetOfTasks.getNextElement()){
			vars[idx] = allVars[t];
			vars[idx+nbTasksInFilteringAlgo] = allVars[t+nbTasks];
			vars[idx+2*nbTasksInFilteringAlgo] = allVars[t+2*nbTasks];
			vars[idx+3*nbTasksInFilteringAlgo] = allVars[t+3*nbTasks];
			idx++;
		}
		this.nbEventsToAdd = 0;
		this.datesAPEvents = null;
		this.heightsAPEvents = null;
	}

	public void mainLoop() throws ContradictionException {
		// copy variable bounds into arrays
		copyAndAggregate();
		// ===== NORMAL MODE =====
		this.sweepMin = new DynamicSweepMinKDimRings();
		this.sweepMax = new DynamicSweepMaxKDimRings();
		sweepMin.sweepMin();
		sweepMax.sweepMax();
		//update variable bounds
		for(int is=0;is<nbTasksInFilteringAlgo;is++) {
			vars[mapping[is]].updateLowerBound(ls[is],cause);
			vars[mapping[is]].updateUpperBound(us[is],cause);
			vars[mapping[is]+2*nbTasksInFilteringAlgo].updateLowerBound(le[is],cause);
			vars[mapping[is]+2*nbTasksInFilteringAlgo].updateUpperBound(ue[is],cause);
		}
	}

	/**
	 * Copy bounds of rtasks into arrays.
	 * Build an aggregated cumulative profile with instantiated tasks.
	 *
	 */
	public void copyAndAggregate() {
		nbEventsToAdd = 0;
		mapping = new int[nbTasksInFilteringAlgo];
		for(int is=0, id=nbTasksInFilteringAlgo, ie=2*nbTasksInFilteringAlgo, ih=3*nbTasksInFilteringAlgo;is<nbTasksInFilteringAlgo;is++,id++,ie++,ih+=nbResources) {
			ls[is] = vars[is].getLB();
			us[is] = vars[is].getUB();
			ld[is] = vars[id].getLB();
			le[is] = vars[ie].getLB();
			ue[is] = vars[ie].getUB();
			for(int r=0;r<nbResources;r++) {
				h[is][r] = vars[ih+r].getLB();
			}
			mapping[is] = is;
		}
	}

	/**
	 * Circular double linked lists which record the status of the tasks.
	 */
	class Rings {

		protected final int nbItems;

		public final int ptrNone;
		public final int ptrReady;
		public final int ptrCheck;
		public final int ptrConflict; // add the resource id to get the corresponding list of tasks in conflict

		public final static int NONE = -1;
		public final static int READY = -2;
		public final static int CHECK = -3;

		protected final int[] ring;
		protected final int[] backward;
		protected final int[] forward;

		public Rings(int k, int n) {
			this.nbItems = n;
			this.ring = new int[n+3+k];
			this.backward = new int[n+3+k];
			this.forward = new int[n+3+k];
			for (int i=0;i<n;i++) {
				this.ring[i] = -1;
				this.backward[i] = i;
				this.forward[i] = i;
			}
			this.ptrNone = n;
			this.ptrReady = n+1;
			this.ptrCheck = n+2;
			this.ptrConflict = n+3;
			for (int i=n;i<ring.length;i++) {
				ring[i] = -666;
				backward[i] = i;
				forward[i] = i;
			}
		}

		public void setNone(int t) {
			// supprime t du ring courant
			forward[backward[t]] = forward[t];  // le suivant du precedent de t = le suivant de t
			backward[forward[t]] = backward[t]; // le precedent du suivant de t = le precedent de t
			// pas de ring pour none en pratique. (car aucun parcours de cette liste)
			forward[t] = t;
			backward[t] = t;
			ring[t] = NONE;
		}

		public void setReady(int t) {
			// supprime t du ring courant
			forward[backward[t]] = forward[t];  // le suivant du precedent de t = le suivant de t
			backward[forward[t]] = backward[t]; // le precedent du suivant de t = le precedent de t
			// insertion dans le ring none.
			forward[t] = forward[ptrReady];
			backward[t] = ptrReady;
			forward[ptrReady] = t;
			backward[forward[t]] = t;
			ring[t] = READY;
		}

		public void setCheck(int t) {
			// supprime t du ring courant
			forward[backward[t]] = forward[t];  // le suivant du precedent de t = le suivant de t
			backward[forward[t]] = backward[t]; // le precedent du suivant de t = le precedent de t
			// insertion dans le ring check.
			forward[t] = forward[ptrCheck];
			backward[t] = ptrCheck;
			forward[ptrCheck] = t;
			backward[forward[t]] = t;
			ring[t] = CHECK;
		}

		public void setConflict(int t, int rc) {
			assert(rc >= 0);
			// supprime t du ring courant
			forward[backward[t]] = forward[t];  // le suivant du precedent de t = le suivant de t
			backward[forward[t]] = backward[t]; // le precedent du suivant de t = le precedent de t
			// insertion dans le ring conflict.
			forward[t] = forward[ptrConflict+rc];
			backward[t] = ptrConflict+rc;
			forward[ptrConflict+rc] = t;
			backward[forward[t]] = t;
			ring[t] = rc;
		}


		public int firstInCheck() {
			return forward[ptrCheck];
		}

		public int firstInConflict(int r) {
			return forward[ptrConflict+r];
		}

		public int firstInReady() {
			return forward[ptrReady];
		}

		public int next(int t) {
			return forward[t];
		}

		public boolean inConflict(int t) {
			return (ring[t] >= 0);
		}

		public boolean inNone(int t) {
			return (ring[t] == NONE);
		}

		public boolean inReady(int t) {
			return (ring[t] == READY);
		}

		public boolean inCheck(int t) {
			return (ring[t] == CHECK);
		}

		public boolean isEmptyReady() {
			return (ptrReady == forward[ptrReady]);
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("NONE     : "+noneToString()+"\n")
					.append("READY    : "+readyToString()+"\n")
					.append("CHECK    : "+checkToString()+"\n");
			//          .append("CONFLICT : "+conflictToString()+"\n");
			return sb.toString();
		}

		public String noneToString() {
			StringBuffer sb = new StringBuffer("[ ");
			for (int i=0;i<nbItems;i++) {
				if (ring[i] == NONE) {
					sb.append(" t"+i);
				}
			}
			sb.append(" ]");
			return sb.toString();
		}

		public String readyToString() {
			StringBuffer sb = new StringBuffer("[ ");
			int ptrCur = forward[ptrReady];
			while (ptrCur != ptrReady) {
				sb.append(" t"+ptrCur);
				ptrCur = forward[ptrCur];
			}
			sb.append(" ]");
			return sb.toString();
		}

		public String checkToString() {
			StringBuffer sb = new StringBuffer("[ ");
			int ptrCur = forward[ptrCheck];
			while (ptrCur != ptrCheck) {
				sb.append(" t"+ptrCur);
				ptrCur = forward[ptrCur];
			}
			sb.append(" ]");
			return sb.toString();
		}
	}

	/**
	 * Heap recording the events by ascending order.
	 */
	class IncHeapEvents {
		private int[] date; // Key !
		private int[] task;
		private int[] type;
		private int[] dec;
		private int size;
		private int nbEvents;

		public final int[][] bufferPoll;

		public IncHeapEvents(int _size) {
			this.nbEvents = 0;
			this.size = _size;
			date = new int[size];
			task = new int[size];
			type = new int[size];
			dec = new int[size];
			bufferPoll = new int[size][];
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

		public int pollAllTopItems() {
			if (nbEvents == 0) return 0;
			int firstDate = date[0];
			int nbExtractedItems = 0;
			while (!isEmpty() && date[0] == firstDate) {
				bufferPoll[nbExtractedItems] = poll();
				if (bufferPoll[nbExtractedItems] != null) { nbExtractedItems++; }
				else { break; }
			}
			return nbExtractedItems;
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

	/**
	 * Heap recording the events by descending order.
	 */
	class DecHeapEvents {

		private int[] date; // Key !
		private int[] task;
		private int[] type;
		private int[] dec;
		private int size;
		private int nbEvents;

		public final int[][] bufferPoll;

		public DecHeapEvents(int _size) {

			this.nbEvents = 0;
			this.size = _size;
			date = new int[size];
			task = new int[size];
			type = new int[size];
			dec = new int[size];
			bufferPoll = new int[size][];
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
			while ( (i>0) && (date[parent] < _date) ) {
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
				if (hasRightChild(i) && date[rightChild(i)] > date[leftChild(i)]) {
					j = rightChild(i);
				}
				if (vdate >= date[j]) break;
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

		public int pollAllTopItems() {
			if (nbEvents == 0) return 0;
			int firstDate = date[0];
			int nbExtractedItems = 0;
			while (!isEmpty() && date[0] == firstDate) {
				bufferPoll[nbExtractedItems] = poll();
				if (bufferPoll[nbExtractedItems] != null) { nbExtractedItems++; }
				else { break; }
			}
			return nbExtractedItems;
		}

		/**
		 * Removes the top of this heap. (without any check)
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
				if (hasRightChild(i) && date[rightChild(i)] > date[leftChild(i)]) {
					j = rightChild(i);
				}
				if (vdate >= date[j]) break;
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
				res += ",dec="+dec[i]+"> ";
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
			res += ",dec="+dec[0]+"> ";
			return res;
		}

		public int nbEvents() {
			return nbEvents;
		}
	}

	/**
	 * Event types
	 */
	class Event {
		public final static int SCP = 0; // start of a compulsory part of a fixed task.
		public final static int ECP = 1; // end of a compulsory part of a non-fixed task.
		public final static int PR = 2; // earliest start of a task.
		public final static int CCP = 3; // Latest start of a task initially without compulsory part.
		//public final static int RS = 7; // earliest end of a task, needed for precedences ("Release Successors")
		public final static int FSCP = 4; // Start of a compulsory part of a fixed task.
		public final static int FECP = 5; // End of a compulsory part of a fixed task.
		public final static int AP = 6; // Aggregation event
	}

	/**
	 * Sweep from left to right in order to adjust the earliest start (and end) of the tasks.
	 */
	class DynamicSweepMinKDimRings {

		private final IncHeapEvents hEvents;
		private int delta;
		private int date;
		private final int[] bufferPR;
		private int nbItemsBufferPR;
		private boolean prunning;
		private final int[] gap;
		private final int[] gapi;
		private int maxDate;
		private int minDate;

		// h[task][resource]
		public DynamicSweepMinKDimRings() {
			this.hEvents = new IncHeapEvents(4*nbTasksInFilteringAlgo);
			this.gap = new int[nbResources];
			this.gapi = new int[nbResources];
			this.bufferPR = new int[nbTasksInFilteringAlgo];
			this.nbItemsBufferPR = 0;
			this.prunning = false;
		}

		public void addAggregatedProfile() {
			for(int i=0;i<nbEventsToAdd;i++) {
				hEvents.add(datesAPEvents[i], -1, Event.AP, i);
				if (datesAPEvents[i] < this.minDate) minDate = datesAPEvents[i];
				if (datesAPEvents[i] > this.maxDate) maxDate = datesAPEvents[i];
			}
		}

		public void adjustMin(int t, int minStart, int minEnd) {
			assert(minStart < minEnd);
			if ( minStart > ls[t] ) {
				prunning = true;
				ls[t] = minStart;
				le[t] = minEnd;
			}
		}

		// OK
		protected void generateMinEvents() {
			this.hEvents.clear();
			for (int r=0;r<nbResources;r++) {
				gap[r] = capacities[r].getUB();
				gapi[r] = capacities[r].getUB();
			}
			for(int t=0;t<nbTasksInFilteringAlgo;t++) {
				if ( t == 0 || ls[t] < minDate ) minDate = ls[t];
				if ( t == 0 || ue[t] > maxDate ) maxDate = ue[t];
				ring.setNone(t);
				hEvents.add(us[t],t, Event.SCP,-1);
				if ( us[t] < le[t] ) { // has a compulsory part
					hEvents.add(le[t],t, Event.ECP,-1);
				}
				if ( ls[t] < us[t] ) { // not scheduled
					hEvents.add(ls[t],t, Event.PR,-1);
				} else {
					ring.setReady(t);
				}
			}
		}

		// OK
		public boolean sweepMin() throws ContradictionException {
			prunning = false;
			generateMinEvents();
			addAggregatedProfile();
			while (!hEvents.isEmpty()) {
				processEvents();
				filter();
			}
			assert (minProperty());
			return prunning;
		}

		private void processEvents() {
			int t, ecpi, rc;
			int nbExtractedItems = hEvents.pollAllTopItems(); // result in hEvents.bufferPoll
			int[][] evts = hEvents.bufferPoll; // 0:date,1:task;2:type;3:dec
			nbItemsBufferPR = 0;
			assert(nbExtractedItems != 0);
			delta = evts[0][0];
			for (int i=0;i<nbExtractedItems;i++) {
				if ( evts[i][2] == Event.SCP ) {
					t = evts[i][1];
					ecpi = le[t];
					if (ring.inConflict(t)) { // = CONFLICT
						adjustMin(t,us[t],ue[t]);
						ring.setReady(t);
					} else if (ring.inCheck(t)) {
						ring.setReady(t);
					}
					if (delta < le[t]) {
						for (int r=0;r<nbResources;r++) {
							gap[r] -= h[t][r];
						}
						if (ecpi <= delta) {
							hEvents.add(le[t],t, Event.ECP,-1);
						}
					}
				} else if ( evts[i][2] == Event.ECP ) {
					t = evts[i][1];
					if (ring.inConflict(t)) { // = CONFLICT
						adjustMin(t,us[t],ue[t]);
						ring.setReady(t);
					}
					if (le[t] > delta) {
						hEvents.add(le[t],t, Event.ECP,-1);

					} else {
						if (ring.inCheck(t)) {
							ring.setReady(t);
						}
						for (int r=0;r<nbResources;r++) {
							gap[r] += h[t][r];
						}
					}
				} else if ( evts[i][2] == Event.PR ) { // PR event are processed later
					bufferPR[nbItemsBufferPR] = evts[i][1];
					nbItemsBufferPR++;
				} else if ( evts[i][2] == Event.AP ) {
					for(int k=0;k<nbResources;k++) {
						gap[k] += heightsAPEvents[k][evts[i][3]];
					}
				}
			}
			if (!hEvents.isEmpty()) { date = hEvents.peekDate(); }
			else { date = maxDate; }
			for (int i=0;i<nbItemsBufferPR;i++) { // PR event are processed now
				t = bufferPR[i];
				if ( ( rc = exceedGap(t)) != -1) { // one of the gap is exceeded
					ring.setConflict(t,rc);
				} else if (le[t] > date) {
					ring.setCheck(t);
				} else {
					ring.setReady(t);
				}
			}
		}

		private void filter() throws ContradictionException {
			int r,t,next,rc,ecpi;
			for (r=0;r<nbResources;r++) {
				if (gap[r]<0) {
					vars[0].instantiateTo(vars[0].getLB()-1,cause);
//                    contradiction(null,"overload in ["+delta+","+date+") on resource r"+r);
				}
			}
			for (r=0;r<nbResources;r++) {
				if (gapi[r] > gap[r]) { // the gap decreases
					next = ring.firstInCheck();
					while (next != ring.ptrCheck) {
						t = next;
						next = ring.next(t);
						if (h[t][r]>gap[r]) {
							if (le[t] > delta) {
								ring.setConflict(t,r);
							} else {
								ring.setReady(t);
							}
						}
					}
					gapi[r] = gap[r];
				}
			}
			for (r=0;r<nbResources;r++) {
				if (gapi[r]<gap[r]) { // the gap increases
					next = ring.firstInConflict(r);
					while (next != ring.ptrConflict+r) {
						t = next;
						next = ring.next(t);
						if (h[t][r]<=gap[r]) {
							if ((rc=exceedGap(t)) != -1) { // one of the gapi is exceeded.
								ring.setConflict(t,rc);
							} else {
								ecpi = le[t];
								adjustMin(t,delta,delta+ld[t]);
								if (le[t]>date) {ring.setCheck(t);} else {ring.setReady(t);}
								if (us[t]>=ecpi && us[t]<le[t]) {
									hEvents.add(le[t],t, Event.ECP,-1);
								}
							}
						}
					}
					gapi[r] = gap[r];
				}
			}
		}

		// It returns -1 is the task does not exceed any gap.
		// Otherwise, it returns the a resource where it exceeds.
		private int exceedGap(int t) {
			for (int r=0;r<nbResources;r++) {
				if ( h[t][r] > gap[r] ) return r;
			}
			return -1;
		}

		public boolean isSweepMaxNeeded() {
			int t, tMaxEcp = -1, maxEcp1 = Integer.MIN_VALUE, maxEcp2 = Integer.MIN_VALUE;
			boolean cp;
			int scp, ecp;

			// compute the 2 max values of ecp.
			for(t=0;t<nbTasksInFilteringAlgo;t++) {
				if ( us[t] < le[t] ) {
					cp = true;
					scp = us[t];
					ecp = le[t];
				} else {
					cp = false;
					scp = -1;
					ecp = -1;
				}
				if (cp && ecp >= maxEcp1) {
					maxEcp2 = maxEcp1;
					maxEcp1 = ecp;
					tMaxEcp = t;
				} else if (cp && ecp > maxEcp2) {
					maxEcp2 = ecp;
				}
			}
			if (tMaxEcp == -1) return false; // if no CP, stop saturation !
			for(t=0;t<nbTasksInFilteringAlgo;t++) {
				if ((ls[t] != us[t] && t != tMaxEcp && us[t] < maxEcp1) ||
						(ls[t] != us[t] && t == tMaxEcp && us[t] < maxEcp2)) {
					return true;
				}
			}
			return false;
		}

		private boolean minProperty() {
			int[] sum = new int[nbResources];
			int t, tp, i, r;
			for(t=0;t<nbTasksInFilteringAlgo;t++) { // for each task t ...
				for(i=ls[t];i<le[t];i++) { // ... scheduled to its earliest position
					for (r=0;r<nbResources;r++) {
						sum[r] = h[t][r];
					}
					for(tp=0;tp<nbTasksInFilteringAlgo;tp++) { // compute the
						if ((t != tp) && (us[tp] <= i) && (i<le[tp])) {
							for (r=0;r<nbResources;r++) {
								sum[r] += h[tp][r];
							}
						}
					}
					for (r=0;r<nbResources;r++) {
						if (sum[r] > capacities[r].getUB()) {
							return false;
						}
					}
				}
			}
			return true;
		}

		// ====================================================
		// ====================================================

		public void printEvent(int[] evt) {
			System.out.print("<date="+evt[0]+",task="+evt[1]+",type=");
			switch (evt[2]) {
				case Event.SCP : System.out.print("SCP"); break;
				case Event.ECP : System.out.print("ECP"); break;
				case Event.PR : System.out.print("PR"); break;
				case Event.CCP : System.out.print("CCP"); break;
				case Event.FSCP : System.out.print("FSCP"); break;
				case Event.FECP : System.out.print("FECP"); break;
				case Event.AP : System.out.print("AP"); break;
			}
			System.out.println(",dec="+evt[3]+">");
		}

		public void printTasks() {
			for (int t=0;t<nbTasksInFilteringAlgo;t++) {
				printTask(t);
			}
		}

		public void printTask(int i) {
			System.out.print("Task:"+i+" : s:["+ls[i]+".."+us[i]+"] d:["+ld[i]+".."+ld[i]+"] e:["+le[i]+".."+ue[i]+"]");
			if (us[i] < le[i]) System.out.println("| scp:"+us[i]+" ecp:"+le[i]); else System.out.println();;
		}
	}

	/**
	 * Sweep from right to left in order to adjust the latest end (and start) of the tasks.
	 */
	class DynamicSweepMaxKDimRings {

		private final DecHeapEvents hEvents;
		private int delta;
		private int date;
		private final int[] bufferPR;
		private int nbItemsBufferPR;
		private boolean prunning;
		private final int[] gap;
		private final int[] gapi;
		private int maxDate;
		private int minDate;


		// h[task][resource]
		public DynamicSweepMaxKDimRings() {
			this.hEvents = new DecHeapEvents(4*nbTasksInFilteringAlgo);
			this.gap = new int[nbResources];
			this.gapi = new int[nbResources];
			this.bufferPR = new int[nbTasksInFilteringAlgo];
			this.nbItemsBufferPR = 0;
			this.prunning = false;
		}

		public void addAggregatedProfile() {
			for(int i=0;i<nbEventsToAdd;i++) {
				hEvents.add(datesAPEvents[i]-1, -1, Event.AP, i);
				if (datesAPEvents[i] < this.minDate) minDate = datesAPEvents[i];
				if (datesAPEvents[i] > this.maxDate) maxDate = datesAPEvents[i];
			}
		}

		public void adjustMax(int t, int maxStart, int maxEnd) {
			assert(maxStart < maxEnd);
			assert(maxStart + ld[t] == maxEnd);
			if ( maxEnd < ue[t] ) {
				prunning = true;
				us[t] = maxStart;
				ue[t] = maxEnd;
			}
		}

		protected void generateMaxEvents() {
			this.hEvents.clear();
			for (int r=0;r<nbResources;r++) {
				gap[r] = capacities[r].getUB();
				gapi[r] = capacities[r].getUB();
			}
			for(int t=0;t<nbTasksInFilteringAlgo;t++) {
				if ( t == 0 || ls[t] < minDate ) minDate = ls[t];
				if ( t == 0 || ue[t] > maxDate ) maxDate = ue[t];
				ring.setNone(t);
				hEvents.add(le[t]-1,t, Event.SCP,-1);
				if ( us[t] < le[t] ) { // has a compulsory part
					hEvents.add(us[t]-1,t, Event.ECP,-1);
				}
				if ( ls[t] < us[t] ) { // not scheduled
					hEvents.add(ue[t]-1,t, Event.PR,-1);
				} else {
					ring.setReady(t);
				}
			}
		}

		public boolean sweepMax() throws ContradictionException {
			prunning = false;
			generateMaxEvents();
			addAggregatedProfile();
			while (!hEvents.isEmpty()) {
				processEvents();
				filter();
			}
			assert (maxProperty());
			return prunning;
		}

		private void processEvents() {
			int t, ecpi, rc;
			int nbExtractedItems = hEvents.pollAllTopItems(); // result in hEvents.bufferPoll
			int[][] evts = hEvents.bufferPoll; // 0:date,1:task;2:type;3:dec
			nbItemsBufferPR = 0;
			assert(nbExtractedItems != 0);
			delta = evts[0][0];
			for (int i=0;i<nbExtractedItems;i++) {
				if ( evts[i][2] == Event.SCP ) {
					t = evts[i][1];
					ecpi = us[t]-1;
					if (ring.inConflict(t)) { // = CONFLICT
						adjustMax(t,ls[t],le[t]);
						ring.setReady(t);
					} else if (ring.inCheck(t)) {
						ring.setReady(t);
					}
					if (delta > us[t]-1) {
						for (int r=0;r<nbResources;r++) {
							gap[r] -= h[t][r];
						}
						if (ecpi >= delta) {
							hEvents.add(us[t]-1,t, Event.ECP,-1);
						}
					}
				} else if ( evts[i][2] == Event.ECP ) {
					t = evts[i][1];
					if (ring.inConflict(t)) { // = CONFLICT
						adjustMax(t,ls[t],le[t]);
						ring.setReady(t);
					}
					if (us[t]-1 < delta) {
						hEvents.add(us[t]-1,t, Event.ECP,-1);
					} else {
						if (ring.inCheck(t)) {
							ring.setReady(t);
						}
						for (int r=0;r<nbResources;r++) {
							gap[r] += h[t][r];
						}
					}
				} else if ( evts[i][2] == Event.PR ) { // PR event are processed later
					bufferPR[nbItemsBufferPR] = evts[i][1];
					nbItemsBufferPR++;
				} else if ( evts[i][2] == Event.AP ) {
					for(int k=0;k<nbResources;k++) {
						gap[k] -= heightsAPEvents[k][evts[i][3]];
					}
				}
			}
			if (!hEvents.isEmpty()) { date = hEvents.peekDate(); }
			else { date = minDate; }
			for (int i=0;i<nbItemsBufferPR;i++) { // PR event are processed now
				t = bufferPR[i];
				if ( ( rc = exceedGap(t)) != -1) { // one of the gap is exceeded
					ring.setConflict(t,rc);
				} else if (us[t]-1 < date) {
					ring.setCheck(t);
				} else {
					ring.setReady(t);
				}
			}
		}

		private void filter() throws ContradictionException {
			int r,t,next,rc,ecpi;
			for (r=0;r<nbResources;r++) {
				if (gap[r]<0) {
					vars[0].instantiateTo(vars[0].getLB()-1,cause);
//                    contradiction(null, "overload in ["+delta+","+date+") on resource r"+r);
				}
			}
			for (r=0;r<nbResources;r++) {
				if (gapi[r] > gap[r]) {
					next = ring.firstInCheck();
					while (next != ring.ptrCheck) {
						t = next;
						next = ring.next(t);
						if (h[t][r]>gap[r]) {
							if (us[t]-1 < delta) {
								ring.setConflict(t,r);
							} else {
								ring.setReady(t);
							}
						}
					}
					gapi[r] = gap[r];
				}
			}
			for (r=0;r<nbResources;r++) {
				if (gapi[r]<gap[r]) {
					next = ring.firstInConflict(r);
					while (next != ring.ptrConflict+r) {
						t = next;
						next = ring.next(t);
						if (h[t][r]<=gap[r]) {
							if ((rc=exceedGap(t)) != -1) { // one of the gapi is exceeded.
								ring.setConflict(t,rc);
							} else {
								ecpi = us[t]-1;
								adjustMax(t,delta-ld[t]+1,delta+1);
								if (us[t]-1<date) {ring.setCheck(t);} else {ring.setReady(t);}
								if (le[t]-1<=ecpi && us[t]<le[t]) {
									hEvents.add(us[t]-1,t, Event.ECP,-1);
								}
							}
						}
					}
					gapi[r] = gap[r];
				}
			}
		}

		// It returns -1 is the task does not exceed any gap.
		// Otherwise, it returns the a resource where it exceeds.
		private int exceedGap(int t) {
			for (int r=0;r<nbResources;r++) {
				if ( h[t][r] > gap[r] ) return r;
			}
			return -1;
		}


		private boolean maxProperty() {
			int[] sum = new int[nbResources];
			int t, tp, i, r;
			for(t=0;t<nbTasksInFilteringAlgo;t++) { // for each task t ...
				for(i=us[t];i<ue[t];i++) { // ... scheduled to its latest position
					for (r=0;r<nbResources;r++) {
						sum[r] = h[t][r];
					}
					for(tp=0;tp<nbTasksInFilteringAlgo;tp++) { // compute the
						if ((t != tp) && (us[tp] <= i) && (i<le[tp])) {
							for (r=0;r<nbResources;r++) {
								sum[r] += h[tp][r];
							}
						}
					}
					for (r=0;r<nbResources;r++) {
						if (sum[r] > capacities[r].getUB()) {
							return false;
						}
					}
				}
			}
			return true;
		}

		public void printEvent(int[] evt) {
			System.out.print("<date="+evt[0]+",task="+evt[1]+",type=");
			switch (evt[2]) {
				case Event.SCP : System.out.print("SCP"); break;
				case Event.ECP : System.out.print("ECP"); break;
				case Event.PR : System.out.print("PR"); break;
				case Event.CCP : System.out.print("CCP"); break;
				case Event.FSCP : System.out.print("FSCP"); break;
				case Event.FECP : System.out.print("FECP"); break;
				case Event.AP : System.out.print("AP"); break;
			}
			System.out.println(",dec="+evt[3]+">");
		}

	}
}
