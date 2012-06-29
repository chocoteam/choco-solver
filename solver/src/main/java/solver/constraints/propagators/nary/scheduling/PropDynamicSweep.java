package solver.constraints.propagators.nary.scheduling;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.scheduling.Cumulative;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.scheduling.dataStructures.Event;
import solver.constraints.propagators.nary.scheduling.dataStructures.IncHeapEvents;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

public class PropDynamicSweep extends Propagator<IntVar> {
	
	private Cumulative cu;
	
	private DynamicSweepGreedy greedy;
	private DynamicSweepMin sweepMin;
	private DynamicSweepMax sweepMax;
	private Solver s;
	private int greedyMode;
	private int aggregateMode;
	
	private int[] mapping;
	
	// results 
	int nbTasksInFilteringAlgo;
	int nbEventsToAdd;
	
	
	public PropDynamicSweep(int nbTasks, int limit, IntVar[] vars, Solver solver, Constraint<IntVar,Propagator<IntVar>> cstr, int greedyMode, int aggregatedProfile) {
		super(vars,solver,cstr,PropagatorPriority.QUADRATIC,true);
		this.s = solver;
		this.greedyMode = greedyMode;
		this.aggregateMode = aggregatedProfile;
		this.nbTasksInFilteringAlgo = ((Cumulative)(constraint)).nbTasks();
		this.nbEventsToAdd = 0;
		this.cu = (Cumulative) constraint;
	}

	
	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
	}
	

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		this.mainLoop();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		this.forcePropagate(EventType.FULL_PROPAGATION);
	}

	@Override
	public ESat isEntailed() {		
		Cumulative cu = ((Cumulative) constraint);
		int minStart = Integer.MAX_VALUE;
		int maxEnd = Integer.MIN_VALUE;
		// compute min start and max end
		for(int is=0, id=cu.nbTasks(), ie=2*cu.nbTasks(), ih=3*cu.nbTasks();is<cu.nbTasks();is++,id++,ie++,ih++) { // is = start index, id = duration index, ie = end index
			if (!vars[is].instantiated() || !vars[id].instantiated() || !vars[ie].instantiated() || !vars[ih].instantiated() ) return ESat.UNDEFINED;
			if (vars[is].getValue() < minStart) minStart = vars[is].getValue();
			if (vars[ie].getValue() > maxEnd) maxEnd = vars[ie].getValue();
		}
		int sumHeight;
		// scan the time axis and check the height
		for(int i=minStart;i<=maxEnd;i++) {
			sumHeight = 0;
			for(int is=0, ie=2*cu.nbTasks(), ih=3*cu.nbTasks();is<cu.nbTasks();is++,ie++,ih++) {
				if ( i >= vars[is].getValue() && i < vars[ie].getValue() ) sumHeight += vars[ih].getValue(); 
			}
			if (sumHeight > cu.limit()) { 
				//System.out.println("sumHeight = "+sumHeight+" at "+i);
				return ESat.FALSE;
			}
		}
		return ESat.TRUE;		
	}

	/**
	 * Copy bounds of rtasks into arrays.
	 * Build an aggregated cumulative profile with instantiated tasks.
	 * 
	 */
	public int[][] copyAndAggregate(int[] ls, int[] us,int[] ld, int[] le,int[] ue, int[] h) {
		nbEventsToAdd = 0;
		mapping = new int[cu.nbTasks()];
		if (aggregateMode != 0) {
			int copyIdx = 0;
			IncHeapEvents hEvents = new IncHeapEvents(2*cu.nbTasks());
			int[][] eventsToAdd = null;
			for(int is=0, id=cu.nbTasks(), ie=2*cu.nbTasks(), ih=3*cu.nbTasks();is<cu.nbTasks();is++,id++,ie++,ih++) {
				if (!vars[is].instantiated() || !vars[id].instantiated() || !vars[ie].instantiated() || !vars[ih].instantiated()) {
					ls[copyIdx] = vars[is].getLB();
					us[copyIdx] = vars[is].getUB();
					ld[copyIdx] = vars[id].getLB();
					le[copyIdx] = vars[ie].getLB();
					ue[copyIdx] = vars[ie].getUB();
					h[copyIdx] = vars[ih].getLB();
					mapping[copyIdx] = is;
					copyIdx++;
				} else {
					hEvents.add(vars[is].getUB(), is, Event.FSCP, vars[ih].getValue());
					hEvents.add(vars[ie].getLB(), is, Event.FECP, -vars[ih].getValue());
				}
			}
			nbTasksInFilteringAlgo = copyIdx;
			// build the aggregated profile
			if (!hEvents.isEmpty()) { // there are instantiated tasks
				eventsToAdd = new int[2][hEvents.nbEvents()];
				int evtIdx = 0;
				int [] curEvt = hEvents.peek();
				int delta, date, height, prevHeight;
				delta = curEvt[0];
				date = delta;
				prevHeight = 0;
				height = 0;
				while (!hEvents.isEmpty()) {
					if (date != delta) {
						if (prevHeight != height) { // variation of the consumption
							eventsToAdd[0][evtIdx] = delta; // event date
							eventsToAdd[1][evtIdx] = prevHeight-height; // decrement
							evtIdx++;
							prevHeight = height;
						}
						delta = date;
					}
					curEvt = hEvents.poll();
					height += curEvt[3];
					if (!hEvents.isEmpty()) date = hEvents.peekDate();
				}
				// creer le dernier !
				eventsToAdd[0][evtIdx] = date;
				eventsToAdd[1][evtIdx] = prevHeight-height;
				nbEventsToAdd = evtIdx + 1;
			}		
			return eventsToAdd;
		} else {
			for(int is=0, id=cu.nbTasks(), ie=2*cu.nbTasks(), ih=3*cu.nbTasks();is<cu.nbTasks();is++,id++,ie++,ih++) {
				ls[is] = vars[is].getLB();
				us[is] = vars[is].getUB();
				ld[is] = vars[id].getLB();
				le[is] = vars[ie].getLB();
				ue[is] = vars[ie].getUB();
				h[is] = vars[ih].getLB();
				mapping[is] = is;
			}
			return null;
		}
	}
	
	public final void mainLoop() throws ContradictionException {	
		int state = 0;
		boolean allTasksAreInstantiated;
		int[][] eventsToAdd;
		boolean succeed = false;
		boolean res, max;
		int[] ls = new int[cu.nbTasks()];
		int[] us = new int[cu.nbTasks()];
		int[] ld = new int[cu.nbTasks()];
		int[] le = new int[cu.nbTasks()];
		int[] ue = new int[cu.nbTasks()];
		int[] h = new int[cu.nbTasks()];
		
		
		do {
			for(int is=0;is<cu.nbTasks();is++) {
				//vars[is].updateCompulsoryPart();
				vars[is].notifyMonitors(null, null); // TODO equivalent a la ligne du dessus ???
			}
			// copy variable bounds to arrays
			eventsToAdd = copyAndAggregate(ls, us, ld, le, ue, h); // the number AP events is stored in nbEventsToAdd
	//for(int i=0;i<ls.length;i++) System.out.println("ls["+i+"]="+ls[i]);
			
			// ===== GREEDY MODE =====
			if (greedyMode!=0 && state==0) { // Greedy mode is ON and it is the first loop
				s.getEnvironment().worldPush(); // branch to an other world. (for trying greedy)
				this.greedy = new DynamicSweepGreedy(cu,this,nbTasksInFilteringAlgo,this.cu.limit(),ls,us,ld,le,ue,h);
				succeed = this.greedy.sweepGreedy(eventsToAdd, nbEventsToAdd);
				if (!succeed) { // Greedy fails. 
					s.getEnvironment().worldPop(); // backtrack to the previous world (for a non-greedy propagation)
				} else {
					assert(greedy.allTasksAreFixed()); // ASSERT
					for(int is=0;is<this.nbTasksInFilteringAlgo;is++) { // update variables and stop !
						vars[mapping[is]].updateLowerBound(greedy.ls(is), this); //updateEST(greedy.ls(i));
						vars[mapping[is]].updateUpperBound(greedy.us(is), this); //updateLST(greedy.us(i));
						vars[mapping[is]+cu.nbTasks()].updateLowerBound(greedy.ld(is), this); //updateMinDuration(greedy.ld(i));
						vars[mapping[is]+2*cu.nbTasks()].updateLowerBound(greedy.le(is), this); //updateECT(greedy.le(i));
						vars[mapping[is]+2*cu.nbTasks()].updateUpperBound(greedy.ue(is), this); //updateLCT(greedy.ue(i));
					}
					return;
				}
			}
			// ===== NORMAL MODE =====
			if (greedyMode==0 || !succeed) { // Greedy mode is OFF or fails. Run the dynamic sweep propagation.
				this.sweepMin = new DynamicSweepMin(cu,this,nbTasksInFilteringAlgo,this.cu.limit(),ls,us,ld,le,ue,h);
				this.sweepMax = new DynamicSweepMax(cu,this,nbTasksInFilteringAlgo,this.cu.limit(),ls,us,ld,le,ue,h);
				res = sweepMin.sweepMin(eventsToAdd, nbEventsToAdd);
				res = sweepMax.sweepMax(eventsToAdd, nbEventsToAdd);
				max = false;
				
				do {
					if (max) {
						if (sweepMin.isSweepMaxNeeded()) { // check if sweep max should be run.
							res = sweepMax.sweepMax(eventsToAdd, nbEventsToAdd);
						} else {
							res = false;
							assert(false == sweepMax.sweepMax(eventsToAdd, nbEventsToAdd));
						}
					} else {
						res = sweepMin.sweepMin(eventsToAdd, nbEventsToAdd);
					}
					max = !max;
				} while (res);
			}
			
			// DBG
			//for(int is=0;is<cu.nbTasks();is++) {
			//	System.out.println("t0: ["+ls[is]+".."+us[is]+"] + ["+ld[is]+".."+ld[is]+"] -> ["+le[is]+".."+ue[is]+"]");
			//}
			// DBG
			
			state = 0;
			// update variables.
			for(int is=0;is<nbTasksInFilteringAlgo;is++) {
				vars[mapping[is]].updateLowerBound(ls[is],this);
				vars[mapping[is]].updateUpperBound(us[is],this);
				vars[mapping[is]+cu.nbTasks()].updateLowerBound(ld[is],this);
				vars[mapping[is]+2*cu.nbTasks()].updateLowerBound(le[is],this);
				vars[mapping[is]+2*cu.nbTasks()].updateUpperBound(ue[is],this);
				state = state + (us[is]-ls[is])+(ue[is]-le[is])+ld[is];
			}
			// check !
			for(int is=0;is<nbTasksInFilteringAlgo;is++) {
				state = state-(vars[mapping[is]].getUB()-vars[mapping[is]].getLB())
							 -(vars[mapping[is]+2*cu.nbTasks()].getUB()-vars[mapping[is]+2*cu.nbTasks()].getLB())
							 -vars[mapping[is]+cu.nbTasks()].getLB();
				//if (!vars[mapping[is]].isInstantiated()) allTasksAreInstantiated = false;
			}
		} while (state != 0);
	}
	
	public void printMapping() {
		for(int is=0;is<cu.nbTasks();is++) {
			System.out.print("m["+is+"]="+mapping[is]+",");
		}
	}
	
}
