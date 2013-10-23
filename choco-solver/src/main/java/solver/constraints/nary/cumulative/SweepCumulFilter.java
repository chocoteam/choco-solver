/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.cumulative;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.objects.setDataStructures.ISet;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Basic implementation of Sweep-based Time-Table for cumulative
 * @author Thierry Petit (refactoring Jean-Guillaume Fages)
 * @since 16/10/13
 */
public class SweepCumulFilter extends CumulFilter {

	// ---------------
	// Data structures
	// ---------------

	class FirstInOut<E> {
		// TODO: implement my own data structure
		protected LinkedList<E> l;
		public FirstInOut() {
			l = new LinkedList<E>();
		}
		public void add(E v) {
			l.add(v);
		}
		public E poll() {
			return l.removeFirst();
		}
		public boolean isEmpty() {
			return l.isEmpty();
		}
		public String toString() {
			return l.toString();
		}
		public int size() {
			return l.size();
		}
	}
	static class EventLtComparator implements Comparator<Event>, Serializable {
		@Override
		public int compare(Event o1, Event o2) {
			int date1 = o1.getDate();
			int date2 = o2.getDate();
			if (date1 < date2) {
				return -1;
			} else if (date1 == date2) {
				return 0;
			} else {
				return 1;
			}
		}
	}
	static class GTComparator implements Comparator<Integer>, Serializable {
		protected int[] hei_lb;
		public GTComparator(int[] hei_lb) {
			this.hei_lb = hei_lb;
		}
		@Override
		public int compare(Integer o1, Integer o2) {
			int i1 =  o1;
			int i2 =  o2;
			if (hei_lb[i1] > hei_lb[i2]) {
				return -1;
			} else if (hei_lb[i1] == hei_lb[i2]) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	public class Heap<E> implements Serializable {
		// TODO: implement my own data structure
		protected PriorityQueue heap;
		public Heap(int size, Comparator<E> comp) {
			heap = new PriorityQueue(size, comp);
		}
		public void add(Object e) {
			heap.add(e);
		}
		public Object getTop() {
			return heap.peek();
		}
		public Object poll() {
			return heap.poll();
		}
		public void remove (Object e) {
			heap.remove(e);
		}
		public boolean isEmpty() {
			return heap.isEmpty();
		}
		public void clear(){
			heap.clear();
		}
		public String toString() {
			return heap.toString();
		}
	}

	// ------
	// Events
	// ------

	class Event {

		protected int type;
		protected int index;
		protected int date;

		public Event(int type, int i, int date) {
			this.type = type;
			this.index = i;
			this.date = date;
		}
		public int getType() {
			return type;
		}
		public int getIndex() {
			return index;
		}
		protected int getDate() {
			return date;
		}
		public String toString() {
			String s = "";
			switch(type) {
				case SCP:{
					s += "SCP";
				}; break;
				case ECP:{
					s += "ECP";
				}; break;
				case PRU:{
					s += "PRU";
				}; break;
			}
			return "<" + s + ", a" + getIndex() + ", date " + getDate() + ">\n";
		}
	}

	// ----------
	// Constraint
	// ----------

	protected int[] start_lb_copy,start_ub_copy;
	protected int[] end_lb_copy,end_ub_copy;
	protected int[] dur_lb_copy;
	protected int[] hei_lb_copy;

	private Heap<Event> h_events_min;
	private Heap<Integer> h_max;

	private final static int SCP = 1,ECP = 2,PRU = 3;


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public SweepCumulFilter(IntVar[] st, IntVar[] du, IntVar[] en, IntVar[] he, IntVar capa, Propagator cause){
		super(st,du,en,he,capa,cause);
		int n = st.length;
		this.start_lb_copy = new int[n];
		this.start_ub_copy = new int[n];
		this.end_lb_copy = new int[n];
		this.end_ub_copy = new int[n];
		this.dur_lb_copy = new int[n];
		this.hei_lb_copy = new int[n];
	}

	@Override
	public void filter(ISet tasks) throws ContradictionException {
		int n = tasks.getSize();
		// increasing heap of events
		h_events_min = new Heap<Event>(6*n,new EventLtComparator());
		// heap of activity indexes (sorted by decreasing heights)
		h_max = new Heap<Integer>(n, new GTComparator(hei_lb_copy));

		// arrays initialization at each node
		int i = 0;
		for(int t=tasks.getFirstElement();t>=0;t=tasks.getNextElement()) {
			this.start_lb_copy[i]=this.s[t].getLB();
			this.start_ub_copy[i]=this.s[t].getUB();
			this.end_lb_copy[i]=this.e[t].getLB();
			this.end_ub_copy[i]=this.e[t].getUB();
			this.dur_lb_copy[i]=this.d[t].getLB();
			this.hei_lb_copy[i]=this.h[t].getLB();
			i++;
		}
		while(filterMin(n));
		pruneAll(tasks, true);
		// upper-bound
		i = 0;
		for(int t=tasks.getFirstElement();t>=0;t=tasks.getNextElement()) {
			this.end_lb_copy[i]=-this.s[t].getUB()+1;
			this.start_lb_copy[i]=-this.e[t].getUB()+1;
			this.end_ub_copy[i]=-this.s[t].getLB()+1;
			this.start_ub_copy[i]=-this.e[t].getLB()+1;
			this.dur_lb_copy[i]=this.d[t].getLB();
			this.hei_lb_copy[i]=this.h[t].getLB();
			i++;
		}
		while(filterMin(n));
		pruneAll(tasks, false);
	}

	public void pruneAll(ISet tasks, boolean min) throws ContradictionException {
		int i = 0;
		for(int t=tasks.getFirstElement();t>=0;t=tasks.getNextElement()) {
			if(min) {
				s[t].updateLowerBound(start_lb_copy[i], aCause);
			} else {
				e[t].updateUpperBound((-start_lb_copy[i]+1), aCause);
			}
			i++;
		}
	}

	// -------------------
	// Filtering Algorithm
	// -------------------

	public void generateMinEvents(int nbT) {
		for(int i=0; i<nbT; i++) {
			if(start_lb_copy[i]<start_ub_copy[i]) { // PRU
				h_events_min.add(new Event(PRU,i,start_lb_copy[i]));
			}
			if(start_ub_copy[i] < end_lb_copy[i]) { // SCP and ECP
				h_events_min.add(new Event(SCP,i,start_ub_copy[i]));
				h_events_min.add(new Event(ECP,i,end_lb_copy[i]));
			}
		}
	}

	public void filterminstart(int i, int newbound) throws ContradictionException {
		start_lb_copy[i]=newbound;
		if(newbound>start_ub_copy[i]) {
			aCause.contradiction(s[i],"");
		}
	}

	public boolean filterMin(int nbT) throws ContradictionException {
		boolean active = false;
		h_max.clear();
		h_events_min.clear();
		generateMinEvents(nbT);
		if(h_events_min.isEmpty())return false;//	assert !h_events_min.isEmpty();
		int delta = ((Event) h_events_min.getTop()).getDate();
		int deltap = delta;
		FirstInOut<Integer> tprune = new FirstInOut<Integer>();
		int gap = capamax.getUB();
		int conso = 0;
		int consoLB = conso;
		while(!h_events_min.isEmpty()) {
			// pruning
			if(delta<deltap) {
				if(gap<0) {
					aCause.contradiction(capamax,"");
				}
				FirstInOut<Integer> temp= new FirstInOut<Integer>();
				while(!tprune.isEmpty()) {
					int index = tprune.poll();
					int regret = 0;
					if((start_ub_copy[index] < end_lb_copy[index]) && (delta==start_ub_copy[index])) {
						regret = hei_lb_copy[index];
					}
					if(hei_lb_copy[index]-regret>gap) {
						filterminstart(index,deltap);
						active = true;
						if(regret>0){
							h[index].updateUpperBound(gap+regret,aCause);
						}
					} else {
						int po = 0;
						int endtask = start_lb_copy[index]+dur_lb_copy[index];
						if(start_ub_copy[index] < end_lb_copy[index]) {
							po = Math.min(endtask,start_ub_copy[index]+1);
						} else {
							po = endtask;
						}
						if(deltap<po) {
							temp.add(index);
						}
					}
				}
				while(!temp.isEmpty()) {
					tprune.add(temp.poll());
				}
				// move the sweep line
				delta = deltap;
			}
			// handle the current event
			Event event = (Event) h_events_min.poll();
			int type = event.getType();
			int index = event.getIndex();
			delta = event.getDate();
			switch(type) {
				case(SCP):{
					gap -= hei_lb_copy[index];
					conso += hei_lb_copy[index];
					consoLB = Math.max(consoLB, conso);
				}; break;
				case(ECP): {
					gap += hei_lb_copy[index];
					conso -= hei_lb_copy[index];
				}; break;
				case(PRU): {
					tprune.add(index);
				}; break;
			}
			// next event
			if(! h_events_min.isEmpty()) {
				deltap = ((Event) h_events_min.getTop()).getDate();
			}
		}
		//TODO filter capa max
//		capamax.updateLowerBound(consoLB, aCause); //does not seem to work...
		return active;
	}
}