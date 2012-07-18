package solver.constraints.propagators.nary.scheduling;

import solver.constraints.nary.scheduling.Cumulative;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.scheduling.dataStructures.*;
import solver.exception.ContradictionException;




public class DynamicSweepMin {

	private final Cumulative rsc;
	private Propagator prop;
	
	
	private int n;
	private int capa;
	private final int[] ls; // earliest start
	private final int[] us; // latest start
	private final int[] ld; // duration
	private final int[] le; // earliest end
	private final int[] ue; // latest end
	private final int[] h; // height
	
	private final IncHeapEvents hEvents; 	
	private final HeapCheck hCheck;
	private final HeapConflict hConflict;
	
	private final boolean[] cp;
	private final int[] scp;
	private final int[] ecp;
	private final boolean[] evup;
	private final int[] mins;
	private final int[] state;
	
	private final static int NONE = 0;
	private final static int CHECK = 1;
	private final static int CONFLICT = 2;
	
	
	private final int[] prune;
	private int iprune;
	private int gap;
	
	private int maxDate;
	private int minDate;
	
	public DynamicSweepMin(Cumulative rsc, Propagator prop,int n, int limit, int[] ls, int[] us, int[] ld, int[] le, int[] ue, int[] h) {
		//assert(n == ls.length && n == us.length && n == ld.length && n == le.length && n == ue.length && n == h.length);
		this.rsc = rsc;
		this.prop = prop;
		this.n = n;
		this.capa = limit;
		this.hEvents = new IncHeapEvents(4*n+2*rsc.nbTasks());
		this.hCheck = new HeapCheck(n);
		this.hConflict = new HeapConflict(n);
		this.ls = ls;
		this.us = us;
		this.le = le;
		this.ue = ue;
		this.h = h;
		this.ld = ld;
		this.cp = new boolean[n];
		this.scp = new int[n];
		this.ecp = new int[n];
		this.evup = new boolean[n];
		this.mins = new int[n];
		this.state = new int[n];
		this.prune = new int[n];
	}
	
	public void addAggregatedProfile(int[][] profile, int nbItems) {
		for(int i=0;i<nbItems;i++) {
			hEvents.add(profile[0][i], -1, Event.AP, profile[1][i]);
			if (profile[0][i] < this.minDate) minDate = profile[0][i];
			if (profile[0][i] > this.maxDate) maxDate = profile[0][i];
		}
	}
	
	protected void generateMinEvents() {		
		this.hEvents.clear();
		this.hCheck.clear();
		this.hConflict.clear();
		for(int t=0;t<n;t++) {
			if ( t == 0 || ls[t] < minDate ) minDate = ls[t];
			if ( t == 0 || ue[t] > maxDate ) maxDate = ue[t];
			evup[t] = fixed(t, ls, us, ld, le, ue);
			state[t] = NONE;
			if (!evup[t]) {
				hEvents.add(ls[t],t,Event.PR,0);
			}
			cp[t] = us[t] < le[t];
			if (cp[t]) {
				hEvents.add(us[t],t,Event.SCP,-h[t]);
				hEvents.add(le[t],t,Event.ECP,h[t]);
				scp[t] = us[t];
				ecp[t] = le[t];
			} else {
				hEvents.add(us[t],t,Event.CCP,-h[t]);
			}
		}
	}
	
	protected IntBool getNextDate(int delta) throws ContradictionException {
		IntBool res = new IntBool();
		res.pruning = false;
		if (hEvents.isEmpty()) {
			res.pruning = filterMin(delta, maxDate);
		}
		res.date = synchronizeMin(delta);
		return res;
	}
	
	protected Integer synchronizeMin(int delta) {
		boolean sync;
		int[] evt;
		int newEcp;
		do {
			sync = true;
			evt = hEvents.peek();
			if (evt != null) {
				assert(evt[0] >= delta); // ASSERT
				if ((evt[2] == Event.ECP) && (!evup[evt[1]])) {
					assert(cp[evt[1]]); // ASSERT
					assert(state[evt[1]] != NONE); // ASSERT
					if (state[evt[1]] == CHECK) newEcp = mins[evt[1]];
					else newEcp = scp[evt[1]];
					newEcp = Math.max(newEcp+ld[evt[1]], le[evt[1]]);
					assert(newEcp >= delta); // ASSERT
					if (newEcp > ecp[evt[1]]) {
						hEvents.poll();
						sync = false;
						hEvents.add(newEcp,evt[1],evt[2],evt[3]);
						ecp[evt[1]] = newEcp;
					}
					evup[evt[1]] = true;
				} else if (evt[2] == Event.CCP && !evup[evt[1]] && evt[0] == delta) {
					assert(!cp[evt[1]]); // ASSERT
					assert(state[evt[1]] != NONE); // ASSERT
					if (state[evt[1]] == CHECK) {
						if (Math.max(mins[evt[1]]+ld[evt[1]], le[evt[1]]) > delta) {
							cp[evt[1]] = true;
							scp[evt[1]] = evt[0];
							ecp[evt[1]] = Math.max(mins[evt[1]]+ld[evt[1]], le[evt[1]]);
							hEvents.add(scp[evt[1]],evt[1],Event.SCP,-h[evt[1]]);
							hEvents.add(ecp[evt[1]],evt[1],Event.ECP,h[evt[1]]);
						}
					} else if (state[evt[1]] == CONFLICT) {
						cp[evt[1]] = true;
						scp[evt[1]] = evt[0];
						ecp[evt[1]] = Math.max(evt[0]+ld[evt[1]], le[evt[1]]);
						hEvents.add(scp[evt[1]],evt[1],Event.SCP,-h[evt[1]]);
						hEvents.add(ecp[evt[1]],evt[1],Event.ECP,h[evt[1]]);
					}
					evup[evt[1]] = true;
				} else if ( evt[2] == Event.ECP && evup[evt[1]] ) {
					if ( ecp[evt[1]] != evt[0] ) {
						hEvents.remove();
						sync = false;
						hEvents.add(ecp[evt[1]],evt[1],Event.ECP,h[evt[1]]);
					}
				}
			}
		} while (!sync);
		if (evt != null) {
			return evt[0];
		} else {
			return null;
		}
	}
	
	protected boolean filterMin(int delta, int date) throws ContradictionException {
		//System.out.println("filter(delta="+delta+", date="+date+", gap="+gap+")");
		boolean pruning;
		int t;
		int[] item = null;
		pruning = false;
		if (gap < 0) { prop.contradiction(null, "overflow: [delta="+delta+",date="+date+")"); }
		boolean check, conflict, adjust;
		while (true) { // Nice !
			item = null;
			check = false;
			conflict = false;
			adjust = false; 
			if (iprune > 0) { 
				iprune--;
				t = prune[iprune];
				if ( h[t] > gap ) { 
					conflict = true;
				} else if (( ld[t] > date-delta ) || ( le[t] > date )) {
					check = true;
				} else {
					evup[t] = true; 
				}
			} else if ( !hCheck.isEmpty() && ( hEvents.isEmpty() || hCheck.peekHeight() > gap )) {
				item = hCheck.poll();
				t = item[1];
				if (( delta >= us[t] ) || (delta-mins[t] >= ld[t]) || hEvents.isEmpty()) {
					adjust = true;
				} else {
					conflict = true;
				}
			} else if ( !hConflict.isEmpty() && hConflict.peekHeight() <= gap ) {
				item = hConflict.poll();
				t = item[1];
				if ( delta >= us[t] ) {
					adjust = true;
					mins[t] = us[t];
				} else {
					if ( date-delta >= ld[t] && le[t] <= date ) {
						adjust = true;
						mins[t] = delta;
					} else {
						check = true;
					}
				}
			} else {
				return pruning;
			}
			if (check) {
				//System.out.println("\t add t"+t+" into hCheck");
				state[t] = CHECK;
				mins[t] = delta;
				if (item == null) { item = new int[]{h[t],t}; }
				hCheck.add(item); 
			} else if (conflict) {
				//System.out.println("\t add t"+t+" into hConflict");
				state[t] = CONFLICT;
				if (item == null) { item = new int[]{h[t],t}; }
				hConflict.add(item);
			} else if (adjust) {
				//System.out.println("\t adjust t"+t+" at "+mins[t]);
				state[t] = NONE;
				if ( mins[t] > ls[t] ) { 
					pruning = true; 
				}
				if ( !adjustMinStart(t,mins[t]) || !propagateFromMinStart(t) ) {
					prop.contradiction(null, "wtf!");
				}
				if ( !evup[t] ) {
					if ( !cp[t] && us[t] < le[t] ) {
						cp[t] = true;
						scp[t] = us[t];
						ecp[t] = le[t];
						hEvents.add(scp[t],t,Event.SCP,-h[t]);
						hEvents.add(ecp[t],t,Event.ECP,h[t]);
					}
					else if (cp[t] && le[t] != ecp[t]) {
						ecp[t] = le[t];
					}
					evup[t] = true;
				}
			}
		}
	}
	
	public boolean sweepMin(int[][] profile, int nbItems) throws ContradictionException {
//for(int i=0;i<n;i++) printTask(i);
		IntBool tmp;
		boolean pruning;
		int[] evt = null;
		int delta, date;
		this.iprune = 0;
		this.generateMinEvents();
		this.addAggregatedProfile(profile, nbItems);
		tmp = this.getNextDate(minDate);
		pruning = tmp.pruning;
		gap = capa;
		delta = tmp.date;
		while (!hEvents.isEmpty()) {
			date = tmp.date;
			if (delta != date) {
				pruning |= filterMin(delta, date);
				delta = date;
			}
			assert(!hEvents.isEmpty()); // ASSERT
			tmp.date = synchronizeMin(delta);
			delta = tmp.date;
			evt = hEvents.poll();
			if (evt[2] == Event.SCP || evt[2] == Event.ECP || evt[2] == Event.AP) {
				gap += evt[3];
			} else if (evt[2] == Event.PR) {
				prune[iprune] = evt[1];
				iprune++;
			}
			tmp = getNextDate(delta);
			pruning |= tmp.pruning;
		}
		assert(minProperty());
		return pruning;
	}
	
	public boolean isSweepMaxNeeded() {
		
		int t, tMaxEcp = -1, maxEcp1 = Integer.MIN_VALUE, maxEcp2 = Integer.MIN_VALUE;
		// compute the 2 max values of ecp.
		for(t=0;t<n;t++) {
			if (cp[t] && ecp[t] >= maxEcp1) {
				maxEcp2 = maxEcp1;
				maxEcp1 = ecp[t];
				tMaxEcp = t;
			} else if (cp[t] && ecp[t] > maxEcp2) {
				maxEcp2 = ecp[t];
			}
		}
		if (tMaxEcp == -1) return false; // if no CP, stop saturation !
		for(t=0;t<n;t++) {
			if ((ls[t] != us[t] && t != tMaxEcp && us[t] < maxEcp1) || 
				(ls[t] != us[t] && t == tMaxEcp && us[t] < maxEcp2)) {
				return true;
			}
		}
		return false;		
	}
	
	private boolean minProperty() {
		int sum, t, tp, i;
		for(t=0;t<n;t++) {
			for(i=ls[t];i<le[t];i++) {
				sum = h[t];
				for(tp=0;tp<n;tp++) {
					if ((t != tp) && (us[tp] <= i) && (i<le[tp]))
						sum += h[tp];
				}
				if (sum > capa) return false;
			}
		}
		return true;
	}
	
	// ====================================================
	// ====================================================
	
	private boolean fixed(int t, int[] ls, int[] us, int[] ld, int[] le, int[] ue) {
		return (ls[t] == us[t] && le[t] == ue[t]);
	}
	
	private boolean adjustMinStart(int task, int value) {
		if ( value >= ls[task] &&  value <= us[task] ) {
			ls[task] = value;
			return true;
		}
		return false;
	}
	
	private boolean adjustMinEnd(int task, int value) {
		if ( value >= le[task] &&  value <= ue[task] ) {
			le[task] = value;
			return true;
		}
		return false;
	}
		
	private boolean propagateFromMinStart(int task) {
		return adjustMinEnd(task,ls[task]+ld[task]);
	}
	
	// ====================================================
	// ====================================================
	
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
	
	public void printTask(int i) {
		System.out.print("Task:"+i+" : s:["+ls[i]+".."+us[i]+"] d:["+ld[i]+".."+ld[i]+"] e:["+le[i]+".."+ue[i]+"]");
		if (cp[i]) System.out.println("| scp:"+scp[i]+" ecp:"+ecp[i]); else System.out.println();;
	}
	
}





















