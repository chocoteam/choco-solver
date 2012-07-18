package solver.constraints.propagators.nary.scheduling;

import solver.constraints.nary.scheduling.Cumulative;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.scheduling.dataStructures.*;
import solver.exception.ContradictionException;




public class DynamicSweepMax {

	private final Cumulative rsc;
	private Propagator prop;
	
	
	private int n;
	private int capa;
	private final int[] ls;
	private final int[] us;
	private final int[] ld;
	private final int[] le;
	private final int[] ue;
	private final int[] h;
	
	private final DecHeapEvents hEvents;
	private final HeapCheck hCheck;
	private final HeapConflict hConflict;
	
	private final boolean[] cp;
	private final int[] scp;
	private final int[] ecp;
	private final boolean[] evup;
	private final int[] maxe;
	private final int[] state;
	
	private final static int NONE = 0;
	private final static int CHECK = 1;
	private final static int CONFLICT = 2;
	
	
	private final int[] prune;
	private int iprune;
	private int gap;
	
	private int maxDate;
	private int minDate;
	
	public DynamicSweepMax(Cumulative rsc, Propagator prop, int n, int capa, int[] ls, int[] us, int[] ld, int[] le, int[] ue, int[] h) {
		//assert(n == ls.length && n == us.length && n == ld.length && n == le.length && n == ue.length && n == h.length);
		this.rsc = rsc;
		this.prop = prop;
		this.n = n;
		this.capa = capa;
		this.hEvents = new DecHeapEvents(4*n+2*rsc.nbTasks());
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
		this.maxe = new int[n];
		this.state = new int[n];
		this.prune = new int[n];
	}
	
	public void addAggregatedProfile(int[][] profile, int nbItems) {
		for(int i=0;i<nbItems;i++) {
			hEvents.add(profile[0][i]-1, -1, Event.AP, -profile[1][i]); // care
			if (profile[0][i] < this.minDate) minDate = profile[0][i];
			if (profile[0][i] > this.maxDate) maxDate = profile[0][i];
		}
	}
	
	protected void generateMaxEvents() {
		this.hEvents.clear();
		this.hCheck.clear();
		this.hConflict.clear();
		for(int t=0;t<n;t++) {
			if ( t == 0 || ls[t] < minDate ) minDate = ls[t];
			if ( t == 0 || ue[t] > maxDate ) maxDate = ue[t];
			evup[t] = fixed(t, ls, us, ld, le, ue);
			state[t] = NONE;
			if (!evup[t]) {
				hEvents.add(ue[t]-1,t,Event.PR,0);
			}
			cp[t] = us[t] < le[t];
			if (cp[t]) {
				hEvents.add(le[t]-1,t,Event.SCP,-h[t]);
				hEvents.add(us[t]-1,t,Event.ECP,h[t]);
				scp[t] = le[t]-1;
				ecp[t] = us[t]-1;				
			} else {
				hEvents.add(le[t]-1,t,Event.CCP,-h[t]);
			}
		}
	}
	
	protected IntBool getPrevDate(int delta) throws ContradictionException {
		IntBool res = new IntBool();
		res.pruning = false;
		if (hEvents.isEmpty()) {
			res.pruning = filterMax(delta, minDate);
		}
		res.date = synchronizeMax(delta);
		return res;
	}
	
	protected Integer synchronizeMax(int delta) {
		boolean sync;
		int[] evt = null;
		int newEcp;
		do {
			sync = true;
			evt = hEvents.peek();
			if (evt != null) {
				assert(evt[0] <= delta); // ASSERT
				if ((evt[2] == Event.ECP) && (!evup[evt[1]])) {
					assert(cp[evt[1]]); // ASSERT
					assert(state[evt[1]] != NONE); // ASSERT
					if (state[evt[1]] == CHECK) newEcp = maxe[evt[1]];
					else newEcp = scp[evt[1]];
					newEcp = Math.min(newEcp-ld[evt[1]], us[evt[1]]-1);
					assert(newEcp <= delta); // ASSERT
					if (newEcp < ecp[evt[1]]) {
						hEvents.poll();
						sync = false;
						hEvents.add(newEcp,evt[1],evt[2],evt[3]);
						ecp[evt[1]] = newEcp;
					}
					evup[evt[1]] = true;
				} else if (evt[2] == Event.CCP && !evup[evt[1]] && evt[0]==delta) {
					assert(!cp[evt[1]]); // ASSERT
					assert(state[evt[1]] != NONE); // ASSERT
					if (state[evt[1]] == CHECK) {
						if (Math.min(maxe[evt[1]]-ld[evt[1]], us[evt[1]]-1) < delta) {
							cp[evt[1]] = true;
							scp[evt[1]] = evt[0];
							ecp[evt[1]] = Math.min(maxe[evt[1]]-ld[evt[1]], us[evt[1]]-1);
							hEvents.add(scp[evt[1]],evt[1],Event.SCP,-h[evt[1]]);
							hEvents.add(ecp[evt[1]],evt[1],Event.ECP,h[evt[1]]);
						}
					} else if (state[evt[1]] == CONFLICT) {
						cp[evt[1]] = true;
						scp[evt[1]] = evt[0];
						ecp[evt[1]] = Math.min(evt[0]-ld[evt[1]], us[evt[1]]-1);
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

	protected boolean filterMax(int delta, int date) throws ContradictionException {
		boolean pruning;
		int t;
		int[] item = null;
		pruning = false;
		if (gap < 0) { prop.contradiction(null, "overflow: [delta="+delta+",date="+date+")"); }
		boolean check, conflict, adjust;
		while (true) {
			item = null;
			check = false;
			conflict = false;
			adjust = false;
			if (iprune > 0) {
				iprune--;
				t = prune[iprune];
				if ( h[t] > gap ) { 
					conflict = true;
				} else if (( ld[t] > delta-date ) || ( us[t]-1 < date )) {
					check = true;
				} else {
					evup[t] = true;
				}
			} else if ( !hCheck.isEmpty() && ( hEvents.isEmpty() || hCheck.peekHeight() > gap )) {
				item = hCheck.poll();
				t = item[1];
				if (( delta <= le[t]-1 ) || (maxe[t]-delta >= ld[t] ) || hEvents.isEmpty()) {
					adjust = true;
				} else {
					conflict = true;
				}
			} else if ( !hConflict.isEmpty() && hConflict.peekHeight() <= gap ) {
				item = hConflict.poll();
				t = item[1];
				if ( delta <= le[t]-1 ) {
					adjust = true;
					maxe[t] = le[t]-1;
				} else {
					if ( delta-date >= ld[t] && us[t]-1 >= date ) {
						adjust = true;
						maxe[t] = delta;
					} else {
						check = true;
					}
				}
			} else {
				return pruning;
			}
			if (check) {
				state[t] = CHECK;
				maxe[t] = delta;
				if (item == null) { item = new int[]{h[t],t}; }
				hCheck.add(item);
			} else if (conflict) {
				state[t] = CONFLICT;
				if (item == null) { item = new int[]{h[t],t}; }
				hConflict.add(item);
			} else if (adjust) {
				state[t] = NONE;
				if ( maxe[t] < ue[t]-1 ) { 
					pruning = true; 
				}
				if ( !adjustMaxEnd(t,maxe[t]+1) || !propagateFromMaxEnd(t) ) {
					prop.contradiction(null, "wtf!");
				}
				if ( !evup[t] ) {
					if ( !cp[t] && us[t] < le[t] ) {
						cp[t] = true;
						scp[t] = le[t]-1;
						ecp[t] = us[t]-1;
						hEvents.add(scp[t],t,Event.SCP,-h[t]);
						hEvents.add(ecp[t],t,Event.ECP,h[t]);
					} else if (cp[t] && us[t]-1 != ecp[t]) {
						ecp[t] = us[t]-1;
					}
					evup[t] = true;
				}
			}
		}
	}
	
	public boolean sweepMax(int[][] profile, int nbItems) throws ContradictionException {
		IntBool tmp;
		boolean pruning;
		int[] evt = null;
		int delta, date;
		this.iprune = 0;
		this.generateMaxEvents();
		this.addAggregatedProfile(profile, nbItems);
		tmp = this.getPrevDate(maxDate);
		pruning = tmp.pruning;
		gap = capa;
		delta = tmp.date;
		while (!hEvents.isEmpty()) {
			date = tmp.date;
			if (delta != date) {
				pruning |= filterMax(delta, date);
				delta = date;
			}
			assert(!hEvents.isEmpty()); // ASSERT
			tmp.date = synchronizeMax(delta);
			delta = tmp.date;
			evt = hEvents.poll();
			if (evt[2] == Event.SCP || evt[2] == Event.ECP || evt[2] == Event.AP) {
				gap += evt[3];
			} else if (evt[2] == Event.PR) {
				prune[iprune] = evt[1];
				iprune++;
			}
			tmp = getPrevDate(delta);
			pruning |= tmp.pruning;
		}
		
//System.out.println("SWEEP_MAX");
//for (int t=0;t<n;t++) printTask(t);
		
		assert(maxProperty());
		return pruning;
	}
	
	private boolean maxProperty() {
		int sum, t, tp, i;
		for(t=0;t<n;t++) {
			for(i=us[t];i<ue[t];i++) {
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

	private boolean adjustMaxStart(int task, int value) {
		if ( value >= ls[task] &&  value <= us[task] ) {
			us[task] = value;
			return true;
		}
		return false;
	}
	
	private boolean adjustMaxEnd(int task, int value) {
		if ( value >= le[task] &&  value <= ue[task] ) {
			ue[task] = value;
			return true;
		}
		return false;
	}
	
	private boolean propagateFromMaxEnd(int task) {
		return adjustMaxStart(task,ue[task]-ld[task]);
	}
	
	// ====================================================
	// ====================================================
	
	public void printTask(int i) {
		System.out.print("Task:"+i+" : s:["+ls[i]+".."+us[i]+"] d:["+ld[i]+".."+ld[i]+"] e:["+le[i]+".."+ue[i]+"]");
		if (cp[i]) System.out.println("| scp:"+scp[i]+" ecp:"+ecp[i]); else System.out.println();;
	}
	
}





















