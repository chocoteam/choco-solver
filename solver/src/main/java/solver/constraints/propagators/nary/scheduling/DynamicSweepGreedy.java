package solver.constraints.propagators.nary.scheduling;

import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import solver.constraints.nary.scheduling.Cumulative;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.scheduling.dataStructures.*;

import java.util.logging.Level;
import java.util.logging.Logger;





public class DynamicSweepGreedy {

	private final Cumulative rsc;
	private Propagator prop;
	private final EnvironmentTrailing env;
	private Logger logger;
	
	private int n;
	private int capa;
	private final IStateIntVector ls;
	private final IStateIntVector us;
	private final int[] ld;
	private final IStateIntVector le;
	private final IStateIntVector ue;
	private final int[] h;
	private final IncHeapEventsBt hEvents; // the heap of events
	private final HeapCheckBt hCheck; // heap of tasks, order by decreasing height.
	private final HeapConflictBt hConflict; // heap of tasks, order by increasing height.
	private final IStateIntVector scp; // scp.get(t), indicates the start of the compulsory part of task t.
	private final IStateIntVector ecp; // ecp.get(t), indicates the end of the compulsory part of task t.
	private final IStateIntVector mins; // mins.get(t), gives the first possible starting date for task t.
	private final IStateIntVector state; // in which heap ? (check, conflict, none)
	private final IStateBitSet cp; // cp.get(t), indicates if the task t has a compulsory part or not.
	private final IStateBitSet fixed; // fixed.get(t), indicates if the task t is instantiated.
	private final Standby standby; // an ordered list which are not in the loop. The first task
	private final IStateBitSet in; // in.get(t), indicates if the task t is in the "loop".
	
	private final int[] prune;
	private int iprune;
	private final IStateInt delta;
	private final IStateInt gap;
	
	
	private final int MAX_ITEMS_IN_HEAPS = 10; // During the filter, a task with a height < gap in standby is not reintroduced in the loop if the number of items in Hcheck and Hconflict is greater or equal than MAX_ITEMS_IN_HEAPS.
	
	
	// resultats de methodes (evite de creer une classe pour les retours)
	private int resDate; // the date of the next event. Check resValid before ! (synchronize, getNextDate)
	private boolean resValid; // set to true if there is an other event in the heap. (synchronize)
	private boolean resSucceed; // set to false if a failure is detected. (getNextDate, filter, sweepGreedy)
	private boolean resFix; // set to true if a task is fixed during the procedure. (getNextDate, filter)
	private int resTask; // the task fixed during the procedure. (getNextDate, filter)
	private int resRdate; // the new start of the task resTask. (getNextDate, filter)
	// =====
	
	// values for state[t]
	private final static int NONE = 0;
	private final static int CHECK = 1;
	private final static int CONFLICT = 2;
	// =====
	
	private int maxDate;
	private int minDate;
	
	// ====
	
	public DynamicSweepGreedy(Cumulative rsc, Propagator prop, int n, int capa, int[] ls, int[] us, int[] ld, int[] le, int[] ue, int[] h) {
		//assert(n == ls.length && n == us.length && n == ld.length && n == le.length && n == ue.length && n == h.length); // ASSERT
		//System.out.println("DynamicSweepGreedy(nbUITasks="+n+", ls.length="+ls.length+")");
		this.rsc = rsc;
		this.prop = prop;
		this.env = new EnvironmentTrailing();
		this.n = n;
		this.capa = capa;
		this.hEvents = new IncHeapEventsBt(env, rsc.nbTasks()*5);
		this.hCheck = new HeapCheckBt(env, n);
		this.hConflict = new HeapConflictBt(env, n);
		this.ls = env.makeIntVector(ls);
		this.us = env.makeIntVector(us);
		this.le = env.makeIntVector(le);
		this.ue = env.makeIntVector(ue);
		this.h = h;
		this.ld = ld;
		this.cp = env.makeBitSet(n);
		this.scp = env.makeIntVector(n, 0);
		this.ecp = env.makeIntVector(n, 0);
		this.mins = env.makeIntVector(n, 0);
		this.state = env.makeIntVector(n, 0);
		this.fixed = env.makeBitSet(n);
		this.prune = new int[n];
		this.delta = env.makeInt();
		this.gap = env.makeInt(capa);
		this.standby = new Standby(env, n);
		this.in = env.makeBitSet(n);
		this.logger = logger;
	}

	// records instantiation
	public void commit(int[] ls, int[] us, int[] le, int[] ue) {
		for(int t=0;t<n;t++) {
			ls[t] = this.ls.get(t);
			us[t] = this.us.get(t);
			le[t] = this.le.get(t);
			ue[t] = this.ue.get(t);
		}
	}
	
	public void addAggregatedProfile(int[][] profile, int nbItems) {
		for(int i=0;i<nbItems;i++) {
			hEvents.add(profile[0][i], -1, Event.AP, profile[1][i]);
		}
	}
	
	protected void generateEvents() {
		this.hEvents.clear();
		this.hCheck.clear();
		this.hConflict.clear();
		for(int t=0;t<n;t++) {
			if ( t == 0 || ls.get(t) < minDate ) minDate = ls.get(t);
			if ( t == 0 || ue.get(t) > maxDate ) maxDate = ue.get(t);
			state.set(t,NONE);
			if (ls.get(t) == us.get(t) || ld[t] == 0) { // the task is already instantiated or has a duration = 0
				if (ld[t] == 0) {us.set(t, ls.get(t));}
				le.set(t, Math.max(ls.get(t)+ld[t], le.get(t)));
				ue.set(t, le.get(t));
				fixed.set(t,true);
				hEvents.add(ls.get(t), t, Event.FSCP, -h[t]);
				hEvents.add(le.get(t), t, Event.FECP, h[t]);
				cp.set(t,true);
				scp.set(t, ls.get(t));
				ecp.set(t, le.get(t));
				in.set(t, true);
			} else { // the task is not instantiated
				fixed.set(t, false);
				cp.set(t, us.get(t) < le.get(t));
				if (cp.get(t)) { // has a compulsory part
					hEvents.add(ls.get(t), t, Event.PR, 0);
					hEvents.add(us.get(t), t, Event.SCP, -h[t]);
					hEvents.add(le.get(t), t, Event.ECP, h[t]);
					scp.set(t, us.get(t));
					ecp.set(t, le.get(t));
					in.set(t, true);
				} else if (us.get(t) == le.get(t)) { // task t has nearly a compulsory part, so introduce it.
					hEvents.add(ls.get(t), t, Event.PR, 0);
					hEvents.add(us.get(t), t, Event.CCP, 0);
					in.set(t, true);
				} else { // No Compulsory Part.
					standby.add(/*ue.get(t) - (2*ld[t])*/us.get(t), t);  // compute latest reintroduction date of t, and add it into standby.
					in.set(t, false);
				}
			}
		}
		standby.sort();
		delta.set(minDate);
	}
	
	// resValid, resDate OR resFix, resTask, resRdate
	protected void getNextDate() {
		while (!standby.isEmpty() && in.get(standby.topTask()) ) standby.removeTop();
		resFix = false;
		resSucceed = true;
		if (hEvents.isEmpty()) {
			if (standby.isEmpty()) {
				filter(maxDate); // can generate new events.
				if (resFix) {
					resValid=false;
					return;
				}
			} else { // reintroduce a first task in the loop.
				int t = standby.topTask();
				if (ls.get(t) > delta.get()) {resDate=ls.get(t);} else {resDate=delta.get(); } // compute and update the next date (i.e. resRdate).
				reintroduce(t, resDate);
			}
		}
		synchronize(); // get the date of the next event.
		if (resFix) { // one task to fix !
			resValid = false;
			return;
		}
		while (!standby.isEmpty() && in.get(standby.topTask()) ) standby.removeTop();
		if (!resValid && !standby.isEmpty()) { // no more events but standby is not empty.
			int t = standby.topTask();
			if (ls.get(t) > delta.get()) {resDate=ls.get(t);} else {resDate=delta.get();} // compute and update the next date (i.e. resRdate).
			reintroduce(t, resDate);
		}  else if (resValid && !standby.isEmpty() && resDate > standby.topLid() ) { // check if the task at the top of standby must be reintroduce before the current next event.
			int t = standby.topTask();
			if (ls.get(t) > delta.get()) {resDate=ls.get(t);} else {resDate=delta.get();} // compute and update the next date (i.e. resRdate).
			reintroduce(t, resDate);
		}
	}
	
	// resValid, resDate OR resFix, resTask, resRdate
	protected void synchronize() {
		boolean sync;
		int[] evt;
		resFix = false;
		do {
			sync = true;
			evt = hEvents.peek();
			if (evt != null && evt[2] != Event.AP) {
				assert(evt[0] >= delta.get()); // ASSERT					
				if ( (evt[2] == Event.CCP || evt[2] == Event.SCP) && !fixed.get(evt[1]) && evt[0] == delta.get()) { // Sweep line is reading a CCP or SCP event of a non-instantiated task.
					assert(state.get(evt[1]) != NONE); // ASSERT
					if (state.get(evt[1]) == CHECK) { // The task is in Hcheck => it can be instantiated to mins[task]
						resFix = true;
						resTask = evt[1];
						resRdate = mins.get(evt[1]); 
						return;
					} else if (state.get(evt[1]) == CONFLICT) { // The task is in Hconflict => it can be instantiated to delta
						resFix = true;
						resTask = evt[1];
						resRdate = evt[0];
						return;
					}
				} else if (fixed.get(evt[1]) && (evt[2]==Event.PR || evt[2]==Event.CCP) ) { // the task is already instantiated and we read its PR event.
					hEvents.remove();
					sync = false;
				}
			}
		} while (!sync);
		if (evt != null) {
			resValid = true;
			resDate = evt[0];
		} else {
			resValid = false;
		}
	}
	
	protected boolean filter(int date) {
		int t;
		int[] item;
		if (gap.get() < 0) { resSucceed = false; return false;}
		boolean check, conflict, adjust;
		while (true) { // Nice !
			while (!hCheck.isEmpty() && fixed.get(hCheck.peekTask()) ) hCheck.remove();
			while (!hConflict.isEmpty() && fixed.get(hConflict.peekTask())) hConflict.remove();
			item = null;
			check = false;
			conflict = false;
			adjust = false; 
			if (iprune > 0) { 
				iprune--;
				t = prune[iprune];
				if ( h[t] > gap.get() ) { 
					conflict = true;
				} else if (( ld[t] > date-delta.get() ) || ( le.get(t) > date )) {
					check = true;
				} else {
					adjust = true; mins.set(t, ls.get(t));
				}
			} else if ( !hCheck.isEmpty() && ( hEvents.isEmpty() || hCheck.peekHeight() > gap.get() )) {
				item = hCheck.poll();
				t = item[1];
				assert(state.get(t) == CHECK); // ASSERT boolean state must be synchronized !
				assert(delta.get() < us.get(t)); // ASSERT the sweep line position cannot be greater (or eq) than the latest start of t.
				if ((delta.get()-mins.get(t) >= ld[t]) || hEvents.isEmpty()) {
					adjust = true;
				} else {
					conflict = true;
				}
			} else if ( !hConflict.isEmpty() && hConflict.peekHeight() <= gap.get() ) {
				item = hConflict.poll();
				t = item[1];
				assert(state.get(t) == CONFLICT); // ASSERT boolean state must be synchronized !
				assert(delta.get() < us.get(t)); // ASSERT the sweep line position cannot be greater (or eq) than the latest start of t.
				if ( date-delta.get() >= ld[t] && le.get(t) <= date ) {
					adjust = true;
					mins.set(t, delta.get());
				} else {
					check = true;
				}
			} else {
				while (!standby.isEmpty() && in.get(standby.topTask()) ) standby.removeTop();
				if (!standby.isEmpty()) {
					assert(standby.topLid() >= resDate); // ASSERT
					t = standby.topTask();
					if (h[t] <= gap.get() && ls.get(t) < resDate) { // height of task t is < gap and its earliest starting time is before resDate.
						if ((Math.max(delta.get(), ls.get(t))+ld[t]<=resDate) || (hCheck.nbItems()+hConflict.nbItems()<MAX_ITEMS_IN_HEAPS)) {
							backToDelta(); // back to the previous world
							reintroduce(t,Math.max(delta.get(), ls.get(t))); // and reintroduce task t.
							resSucceed = true;
							resFix = false;
							env.worldPush(); // save
							return true;
						}
					}
				}
				resSucceed = true;
				resFix = false;
				delta.set(resDate);
				cleanUp();
				env.worldPush(); // save
				return true;
			}
			if (check) {
				state.set(t, CHECK);
				mins.set(t, delta.get());
				if (item == null) { item = new int[2]; item[0]=h[t]; item[1]=t; }
				hCheck.add(item[0],item[1]); 
			} else if (conflict) {
				state.set(t, CONFLICT);
				if (item == null) { item = new int[2]; item[0]=h[t]; item[1]=t; }
				hConflict.add(item[0],item[1]);
			} else if (adjust) {
				resSucceed = true;
				resFix = true;
				resTask = t;
				resRdate = mins.get(t);
				delta.set(resDate);
				return true;
			}
		}
	}

	// Return true iff one task is fixed during the sweep.
	public boolean sweepGreedy(int[][] eventsToAdd, int nbItems) {
		int[] evt;
		this.iprune = 0;
		this.generateEvents(); 
		this.addAggregatedProfile(eventsToAdd, nbItems);
		this.getNextDate();
		resRdate = resDate;
		gap.set(capa);
		delta.set(resDate);
		env.worldPush(); // save
		RESYNC:
		while (!hEvents.isEmpty()) {
			if (delta.get() != resDate) {
				filter(resDate);
				if (!resSucceed) return false;
				if (resFix) {
					backToRdate(); // backtrack to resRdate 
					instantiate(); // and adjust start of task resTask to resRdate.
					continue RESYNC; // goto RESYNC
				}
			}
			assert(!hEvents.isEmpty()); // ASSERT
			synchronize();
			if (resFix) {
				backToRdate(); // backtrack to resRdate 
				instantiate(); // and adjust start of task resTask to resRdate.
				continue RESYNC; // goto RESYNC
			}			
			assert(resValid); // ASSERT
			delta.set(resDate);
			evt = hEvents.poll();
			assert(evt[2] == Event.AP || in.get(evt[1])); // ASSERT 
			if (evt[2] == Event.SCP || evt[2] == Event.ECP || evt[2] == Event.FSCP || evt[2] == Event.FECP) {
				if (!(fixed.get(evt[1]) && (evt[2]==Event.ECP || evt[2]==Event.SCP))) 
					gap.add(evt[3]);
				// unless the task is already instantiated and the event type is ECP or SCP, the gap is updated.
			} else if (evt[2] == Event.AP) {
				gap.add(evt[3]);
			}
			else if (evt[2] == Event.PR) {
				prune[iprune] = evt[1];
				iprune++;
			}
			getNextDate();
			if (!resSucceed) return false;
			if (resFix) {
				backToRdate(); // backtrack to resRdate 
				instantiate(); // and adjust start of task resTask to resRdate.
				continue RESYNC; // goto RESYNC
			}
		}
		assert(hCheck.isEmpty()); // ASSERT
		assert(hConflict.isEmpty()); // ASSERT
		return true;
	}
	
	protected void backToRdate() {
		assert(fixed.get(resTask) == false); // ASSERT
		// Backtrack to resRdate
		env.worldPop();
		while (delta.get() != resRdate) {
			env.worldPop();
		}
	}
	
	protected void backToDelta() {
		env.worldPop();
	}
	
	/**
	 * instantiate task resTask with its start = resRdate and push the world.
	 */
	protected void instantiate() {
		// Apply changes related to task resTask which is now fixed.
		ls.set(resTask, resRdate);
		us.set(resTask, resRdate);
		le.set(resTask, Math.max(resRdate+ld[resTask], le.get(resTask)));
		ue.set(resTask, le.get(resTask));
		fixed.set(resTask,true);
		cp.set(resTask,true);
		scp.set(resTask, resRdate);
		ecp.set(resTask, le.get(resTask));
		state.set(resTask, NONE);
		in.set(resTask,true);
		cleanUp();
		hEvents.add(resRdate, resTask, Event.FSCP, -h[resTask]);
		hEvents.add(le.get(resTask), resTask, Event.FECP, h[resTask]);
		env.worldPush();
		resFix = false;
		resDate = delta.get();
		iprune = 0;
	}
	
	/**
	 * instantiate task t with its start = iDate ( does not push the world)
	 * @param t the index of the task to instantiate.
	 * @param iDate the date of the start of task t.
	 */
	protected void instantiate(int t, int iDate) {
		// Apply changes related to task t which is now fixed.
		ls.set(t, iDate);
		us.set(t, iDate);
		le.set(t, iDate+ld[t]);
		ue.set(t, le.get(t));
		hEvents.add(iDate, t, Event.FSCP, -h[t]);
		hEvents.add(le.get(t), t, Event.FECP, h[t]);
		fixed.set(t,true);
		cp.set(t,true);
		scp.set(t, iDate);
		ecp.set(t, le.get(t));
		state.set(t, NONE);
		in.set(t,true);
	}
	
	/**
	 * instantiate task t with its start = iDate ( does not push the world)
	 * @param t the index of the task to instantiate.
	 * @param iDate the date of the start of task t.
	 */
	protected void instantiateTMP(int t, int iDate) {
		// Apply changes related to task t which is now fixed.
		ls.set(t, iDate);
		us.set(t, iDate);
		le.set(t, iDate+ld[t]);
		ue.set(t, le.get(t));
		gap.add(-h[t]);
		hEvents.add(le.get(t), t, Event.FECP, h[t]);
		fixed.set(t,true);
		cp.set(t,true);
		scp.set(t, iDate);
		ecp.set(t, le.get(t));
		state.set(t, NONE);
		in.set(t,true);
	}
	
	
	/**
	 * Reintroduce a task in the loop.
	 * @param t the task to reintroduce.
	 * @param reintroDate the date of the PR event of task t.
	 */
	protected void reintroduce(int t,int reintroDate) {
		assert(in.get(t) == false); // ASSERT
		assert(ls.get(t) <= resDate); // ASSERT
		assert(reintroDate <= us.get(t));
		if (reintroDate == us.get(t)) { // task t is reintroduced at(to?) its latest position <=> instantiate the task.
			instantiate(t, reintroDate);
		} else if (reintroDate+ld[t] > us.get(t)) { // task t is reintroduced to a position which generate a compulsory part.
			int ecpT = reintroDate+ld[t];
			hEvents.add(reintroDate, t, Event.PR, 0);
			hEvents.add(us.get(t), t, Event.SCP, -h[t]);
			hEvents.add(ecpT, t, Event.ECP, h[t]);
			ls.set(t, reintroDate); // synchronize data
			le.set(t, ecpT); // synchronize data
			cp.set(t, true); // synchronize data
			scp.set(t, us.get(t)); // synchronize data
			ecp.set(t, ecpT); // synchronize data			
			in.set(t, true);
		} else { // task t is reintroduce to a position which does not generate a compulsory part.
			hEvents.add(reintroDate, t, Event.PR, 0);
			hEvents.add(us.get(t), t, Event.CCP, 0);
			ls.set(t, reintroDate); // synchronize data
			le.set(t, reintroDate+ld[t]); // synchronize data
			in.set(t, true);
		}
	}
	
	protected void cleanUp() {
		while (!hEvents.isEmpty() && fixed.get(hEvents.peekTask()) && ( hEvents.peekType() == Event.PR || hEvents.peekType() == Event.CCP) ) { hEvents.remove(); }
		while (!standby.isEmpty() && in.get(standby.topTask()) ) { standby.removeTop(); }
		while (!hCheck.isEmpty() && fixed.get(hCheck.peekTask()) ) { hCheck.remove(); }
		while (!hConflict.isEmpty() && fixed.get(hConflict.peekTask())) { hConflict.remove(); }
	}
	
	
	// ====================================================
	// ====================================================
	

	

	// ===== PRINT & LOG =====
		
	public void printWorld() {
		System.out.println(" ===== WORLD (index="+env.getWorldIndex()+") ===== ");
		System.out.println(" events      = "+hEvents);
		System.out.println(" delta       = "+delta.get());
		System.out.println(" gap         = "+gap.get());
		System.out.println(" check       = "+hCheck);
		System.out.println(" conflict    = "+hConflict);
		System.out.println(" standby     = "+standby);
		System.out.println(" tasks fixed = "+tasksFixed());
		System.out.println(" ===================================");
		System.out.println();
	}

	public static void printEvent(int[] evt) {
		System.out.print("<date="+evt[0]+",task="+evt[1]+",type=");
		switch (evt[2]) {
			case Event.SCP : System.out.print("SCP"); break;
			case Event.ECP : System.out.print("ECP"); break;
			case Event.PR : System.out.print("PR"); break;
			case Event.CCP : System.out.print("CCP"); break;
			case Event.FSCP : System.out.print("FSCP"); break;
			case Event.FECP : System.out.print("FECP"); break;
			case Event.AP : System.out.println("AP"); break;
		}
		System.out.println(",dec="+evt[3]+">");
	}
	
	
	public String tasksNotFixed() {
		String res = "";
		for(int t=0;t<n;t++) {
			if (ls.get(t) != us.get(t) || le.get(t) != ue.get(t)) res += t+",";
		}
		return res;
	}
	
	public String tasksFixed() {
		String res = "";
		for(int t=0;t<n;t++) {
			if (ls.get(t) == us.get(t) && le.get(t) == ue.get(t)) res += t+",";
		}
		return res;
	}
	
	public void printHEvents() {
		logger.log(Level.INFO,"EVENTS:"+hEvents);
	}
	
	public void logTask(int i) {
		logger.log(Level.INFO, "Task:"+i+" : s:["+ls.get(i)+".."+us.get(i)+"] d:["+ld[i]+".."+ld[i]+"] e:["+le.get(i)+".."+ue.get(i)+"] h="+h[i]);
		if (cp.get(i)) logger.log(Level.INFO, "| scp:"+scp.get(i)+" ecp:"+ecp.get(i)); else logger.log(Level.INFO,"\n");
	}
	
	public void printTask(int i) {
		System.out.print("Task:"+i+" : s:["+ls.get(i)+".."+us.get(i)+"] d:["+ld[i]+".."+ld[i]+"] e:["+le.get(i)+".."+ue.get(i)+"] h="+h[i]);
		if (cp.get(i)) System.out.println("| scp:"+scp.get(i)+" ecp:"+ecp.get(i)); else System.out.println();
	}
	
	public void printTasks() {
		for (int t=0;t<n;t++) {
			printTask(t);
		}
	}
	
	// ===== CHECK & ASSERTIONS =====
	
	public boolean allTasksAreFixed() {
		for(int t=0;t<n;t++) {
			if (ls.get(t) != us.get(t) || le.get(t) != ue.get(t)) return false;
		}
		return true;
	}
	
	// ===== GETTERS =====
	public int ls(int t) {return ls.get(t);}
	public int us(int t) {return us.get(t);}
	public int le(int t) {return le.get(t);}
	public int ue(int t) {return ue.get(t);}
	public int ld(int t) {return ld[t];}
	public int h(int t) {return h[t];}

}





















