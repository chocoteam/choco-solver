/**
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.nary.cumulative;

import gnu.trove.list.array.TIntArrayList;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.objects.graphs.UndirectedGraph;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;
import util.sort.ArraySort;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Random;

/**
 * Graph based cumulative
 * Maintains incrementally overlapping tasks
 * Performs energy checking and mandatory part based filtering
 * BEWARE : not idempotent, use two propagators to get the fix point
 *
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropGraphCumulative extends PropFullCumulative {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected final UndirectedGraph g;
	protected ISet tasks,toCompute;
	protected long timestamp;
	// optim (fast mode)
	protected final Random rd = new Random(0);
	protected int maxrd = 10;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Graph-based cumulative propagator:
	 * - only filters over subsets of overlapping tasks
	 *
	 * @param s		start 		variables
	 * @param d		duration	variables
	 * @param e		end			variables
	 * @param h		height		variables
	 * @param capa	capacity	variable
	 * @param fast	optimization parameter (reduces the amount of filtering calls, when set to true)
	 * @param filters	filtering algorithm to use
	 */
	public PropGraphCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa,
							   boolean fast, Cumulative.Filter... filters) {
		super(s, d, e, h, capa,true,fast, filters);
		this.g = new UndirectedGraph(solver.getEnvironment(), n, SetType.BITSET, true);
		this.tasks = SetFactory.makeSwap(n,false);
		this.toCompute = SetFactory.makeSwap(n, false);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
			super.propagate(evtmask);
			for (int i = 0; i < n; i++) {
				g.getNeighborsOf(i).clear();
			}
//			naiveGraphComputation();
			sweepBasedGraphComputation();
		}else{
			int count = 0;
			for (int i = toCompute.getFirstElement(); i >= 0; i = toCompute.getNextElement()) {
				count += g.getNeighborsOf(i).getSize();
			}
			if(count>=2*n){
				filter(allTasks);
			}else{
				for (int i = toCompute.getFirstElement(); i >= 0; i = toCompute.getNextElement()) {
					filterAround(i);
				}
			}
		}
		toCompute.clear();
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		if(timestamp!=solver.getEnvironment().getWorldIndex()){
			timestamp=solver.getEnvironment().getWorldIndex();
			toCompute.clear();
		}
		if (varIdx < 4 * n) {
			int v = varIdx % n;
			if((!fast) || mandPartExists(v) || rd.nextInt(maxrd)==0){
				if (!toCompute.contain(v)) {
					toCompute.add(v);
				}
			}
		} else {
			updateMaxCapa();
			toCompute.clear();
			for (int i = 0; i < n; i++) {
				toCompute.add(i);
			}
		}
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	protected void filterAround(int taskIndex) throws ContradictionException {
		tasks.clear();
		tasks.add(taskIndex);
		ISet env = g.getNeighborsOf(taskIndex);
		for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
			if (disjoint(taskIndex, i)) {
				g.removeEdge(taskIndex, i);
			} else {
				tasks.add(i);
			}
		}
		filter(tasks);
	}

	protected boolean mandPartExists(int i) {
		return s[i].getUB()<e[i].getLB();
	}

	protected boolean disjoint(int i, int j) {
		return s[i].getLB() >= e[j].getUB() || s[j].getLB() >= e[i].getUB();
	}

	private void naiveGraphComputation(){
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (!disjoint(i, j)) {
					g.addEdge(i, j);
				}
			}
		}
	}

	private void sweepBasedGraphComputation(){
		Event[] events = new Event[2*n];
		ArraySort<Event> sort = new ArraySort<>(events.length,true,false);
		Comparator<Event> eventComparator = new Comparator<Event>(){
			@Override
			public int compare(Event e1, Event e2) {
				if(e1.date == e2.date){
					return e1.type-e2.type;
				}
				return e1.date - e2.date;
			}
		};
		BitSet tprune = new BitSet(n);
		for(int i=0; i<n; i++) {
			events[i] = new Event();
			events[i].set(START, i, s[i].getLB());
			events[i+n] = new Event();
			events[i+n].set(END, i, e[i].getUB());
		}
		sort.sort(events,2*n,eventComparator);
		int timeIndex = 0;
		while(timeIndex<n*2){
			Event event = events[timeIndex++];
			switch(event.type) {
				case(START):
					for(int i=tprune.nextSetBit(0);i>=0;i=tprune.nextSetBit(i+1)){
						g.addEdge(i,event.index);
					}
					tprune.set(event.index);
					break;
				case(END):
					tprune.clear(event.index);
					break;
				default:throw new UnsupportedOperationException();
			}
		}
	}

	private static class Event {
		protected int type;
		protected int index;
		protected int date;

		protected void set(int t, int i, int d) {
			date = d;
			type = t;
			index= i;
		}
	}

	private final static int START = 1, END = 2;
}