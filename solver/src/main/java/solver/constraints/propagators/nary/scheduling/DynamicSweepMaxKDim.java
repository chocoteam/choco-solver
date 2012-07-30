package solver.constraints.propagators.nary.scheduling;

import solver.constraints.nary.scheduling.CumulativeKDim;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.scheduling.dataStructures.*;
import solver.exception.ContradictionException;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: Arnaud Letort
 * Date: 03/07/12
 * Time: 16:06
 */
public class DynamicSweepMaxKDim {

    private final CumulativeKDim rsc;
	private Propagator prop;


	private int n;
	private int[] capa;
	private final int[] ls; // earliest start
	private final int[] us; // latest start
	private final int[] ld; // duration
	private final int[] le; // earliest end
	private final int[] ue; // latest end
	private final int[][] h; // height

	private final DecHeapEvents hEvents;
	private final HeapCheck2[] hCheck;
	private final HeapConflict2[] hConflict;

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
	private int[] gap;

    private final BitSet processed;

	private int maxDate;
	private int minDate;


    // h[task][resource]
	public DynamicSweepMaxKDim(CumulativeKDim rsc, Propagator prop, int n, int[] limit, int[] ls, int[] us, int[] ld, int[] le, int[] ue, int[][] h) {
		//assert(n == ls.length && n == us.length && n == ld.length && n == le.length && n == ue.length && n == h.length);
		this.rsc = rsc;
		this.prop = prop;
		this.n = n;
		this.capa = limit;
		this.hEvents = new DecHeapEvents(4*n+2*rsc.nbTasks());
		this.hCheck = new HeapCheck2[rsc.nbResources()];
        for(int i=0;i<hCheck.length;i++) { hCheck[i] = new HeapCheck2(n); }
		this.hConflict = new HeapConflict2[rsc.nbResources()];
        for(int i=0;i<hConflict.length;i++) { hConflict[i] = new HeapConflict2(n); }
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
        this.processed = new BitSet(n);
	}

    /* // TODO
	public void addAggregatedProfile(int[][] profile, int nbItems) {
		for(int i=0;i<nbItems;i++) {
			hEvents.add(profile[0][i], -1, Event.AP, -profile[1][i]);
			if (profile[0][i] < this.minDate) minDate = profile[0][i];
			if (profile[0][i] > this.maxDate) maxDate = profile[0][i];
		}
	}
    */

	protected void generateMaxEvents() {
		this.hEvents.clear();
		for(int t=0;t<n;t++) {
			if ( t == 0 || ls[t] < minDate ) minDate = ls[t];
			if ( t == 0 || ue[t] > maxDate ) maxDate = ue[t];
			evup[t] = fixed(t, ls, us, ld, le, ue);
			state[t] = NONE;
			if (!evup[t]) {
				hEvents.add(ue[t]-1,t, Event.PR,-1);
			}
			cp[t] = us[t] < le[t];
			if (cp[t]) {
				hEvents.add(le[t]-1,t,Event.SCP,-1);
				hEvents.add(us[t]-1,t,Event.ECP,-1);
				scp[t] = le[t]-1;
				ecp[t] = us[t]-1;
			} else {
				hEvents.add(le[t]-1,t,Event.CCP,-1);
			}
		}
	}

	protected IntBool getNextDate(int delta) throws ContradictionException {
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
						hEvents.add(newEcp,evt[1],evt[2],-1);
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
							hEvents.add(scp[evt[1]],evt[1],Event.SCP,-1);
							hEvents.add(ecp[evt[1]],evt[1],Event.ECP,-1);
						}
					} else if (state[evt[1]] == CONFLICT) {
						cp[evt[1]] = true;
						scp[evt[1]] = evt[0];
						ecp[evt[1]] = Math.min(evt[0]-ld[evt[1]], us[evt[1]]-1);
						hEvents.add(scp[evt[1]],evt[1],Event.SCP,-1);
						hEvents.add(ecp[evt[1]],evt[1],Event.ECP,-1);
					}
					evup[evt[1]] = true;
				} else if ( evt[2] == Event.ECP && evup[evt[1]] ) {
					if ( ecp[evt[1]] != evt[0] ) {
						hEvents.remove();
						sync = false;
						hEvents.add(ecp[evt[1]],evt[1],Event.ECP,-1);
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

    private boolean adjustTask(int t) throws ContradictionException {
        boolean pruning = false;
        state[t] = NONE;
        if ( maxe[t] < ue[t]-1 ) {
            pruning = true;
        }
        if ( !adjustMaxEnd(t, maxe[t]+1) || !propagateFromMaxEnd(t) ) {
            prop.contradiction(null, "wtf!");
        }
        if ( !evup[t] ) {
			if ( !cp[t] && us[t] < le[t] ) {
				cp[t] = true;
				scp[t] = le[t]-1;
				ecp[t] = us[t]-1;
				hEvents.add(scp[t],t,Event.SCP,-1);
				hEvents.add(ecp[t],t,Event.ECP,-1);
			} else if (cp[t] && us[t]-1 != ecp[t]) {
				ecp[t] = us[t]-1;
			}
			evup[t] = true;
		}
        return pruning;
    }

	protected boolean filterMax(int delta, int date) throws ContradictionException {
		//System.out.print("filter(delta="+delta+", date="+date+")");
		boolean pruning = false, adjust;
		int t, rc;
		int[] item = null;
		for(int k=0;k<this.rsc.nbResources();k++) {
            if (gap[k] < 0) {
                //System.out.println(" ///// overload in ["+delta+","+date+") on resource r"+k+ " gap = "+gap[k]);
                prop.contradiction(null, "overload in ["+delta+","+date+") on resource r"+k);
            }
        }
        processed.clear(); // set all bits to false
        for (int r=0;r<rsc.nbResources();r++) {
//System.out.println(hConflict[r].nbItems()+" items in hConflict["+r+"]");
            adjust = false;
            while ( !hConflict[r].isEmpty() && hConflict[r].peekHeight() <= gap[r] ) {
                item = hConflict[r].poll();
                t = item[1];
                processed.set(t,true);
//System.out.println("task "+t+" removed from hConflict["+r+"]");
                if ( delta <= le[t]-1 ) {
                    adjust = true;
                    maxe[t] = le[t]-1;
                } else {
                    rc = -1;
                    for (int j=0;j<rsc.nbResources();j++) {
                        if ( h[t][j] > gap[j] ) {
                            rc = j; break;
                        }
                    }
                    if ( delta - date >= ld[t] && rc == -1 ) { // no conflict; sweep interval large enough
                        adjust = true; maxe[t] = delta;
                     } else if ( delta - date < ld[t] && rc == -1 ) { // no conflict; sweep interval too small
                        state[t] = CHECK;
                        maxe[t] = delta;
                        for (int j=0;j<rsc.nbResources();j++) { hCheck[j].add(h[t][j],t); }
                    } else { // conflict
                        state[t] = CONFLICT;
                        hConflict[rc].add(h[t][rc],t);
                    }
                }
                if ( adjust ) {
                    pruning |= adjustTask(t);
                }
            }
        }
        for (int r=0;r<rsc.nbResources();r++) {
            //System.out.println("Resource n¡"+r);
            //System.out.println("hCheck de r "+hCheck[r]);
            assert(hCheck[r].noDuplication());
            while ( !hCheck[r].isEmpty() && (hEvents.isEmpty() || hCheck[r].peekHeight() > gap[r]) ) {
                item = hCheck[r].poll();
                t = item[1];
                if ( processed.get(t) ) continue;
                if ( delta <= le[t]-1 || hEvents.isEmpty() || (maxe[t]-delta >= ld[t] && state[t] == CHECK) ) {
                    pruning |= adjustTask(t);
                } else if ( state[t] == CHECK ) {
                    state[t] = CONFLICT;
                    hConflict[r].add(item);
                }
            }
        }
        return pruning;
	}

	public boolean sweepMax(int[][] profile, int nbItems) throws ContradictionException {
//System.out.println(" $$$$$ SWEEP_MAX $$$$$");
//for(int i=0;i<n;i++) printTask(i);
		IntBool tmp;
		boolean pruning;
		int[] evt = null;
		int delta, date, t, rc;
		this.iprune = 0;
		this.generateMaxEvents();
		//this.addAggregatedProfile(profile, nbItems); // TODO
		tmp = this.getNextDate(maxDate);
		pruning = tmp.pruning;
		gap = capa;
		delta = tmp.date;
		while (!hEvents.isEmpty()) {
			date = tmp.date;
			if (delta != date) {
                while ( iprune > 0) {
                    iprune--; t = prune[iprune]; rc = -1;
                    for (int r=0;r<rsc.nbResources();r++) {
                        if ( h[t][r] > gap[r]) { rc = r; break; }
                    }
                    if ( rc != -1 ) {
//System.out.println("task "+t+" added in hConflict["+rc+"]");
                        state[t] = CONFLICT;
                        hConflict[rc].add(h[t][rc],t);
                    } else if ( ld[t] > date - delta ) {
//System.out.println("task "+t+" added in hCheck[all]");
                        state[t] = CHECK;
                        maxe[t] = delta;
                        for (int r=0;r<rsc.nbResources();r++) {
                            hCheck[r].add(h[t][r],t);
                        }
                    } else {
//System.out.println("task "+t+" nothing can be deduced");
                        evup[t] =true;
                    }
                }
				pruning |= filterMax(delta, date);
				delta = date;
			}
			assert(!hEvents.isEmpty()); // ASSERT
			tmp.date = synchronizeMax(delta);
			delta = tmp.date;
			evt = hEvents.poll();
//System.out.print("evt : ");
//printEvent(evt);
			if (evt[2] == Event.SCP) {
				for(int k=0;k<rsc.nbResources();k++) {
                    gap[k] -= h[evt[1]][k];
                }
            } else if (evt[2] == Event.ECP) {
                for(int k=0;k<rsc.nbResources();k++) {
                    gap[k] += h[evt[1]][k];
                }
            } /*else if (evt[2] == Event.AP) { //
                assert(false); //TODO
                //for(int k=0;k<rsc.nbResources();k++) {
                //    gap[k] += ;
                //}
            } */ else if (evt[2] == Event.PR) {
				prune[iprune] = evt[1];
				iprune++;
			}
			tmp = getNextDate(delta);
			pruning |= tmp.pruning;
		}
		assert(maxProperty());
		return pruning;
	}


	private boolean maxProperty() {
		int[] sum = new int[rsc.nbResources()];
        int t, tp, i, r;
		for(t=0;t<n;t++) { // for each task t ...
			for(i=us[t];i<ue[t];i++) { // ... scheduled to its earliest position
                for (r=0;r<rsc.nbResources();r++) {
                    sum[r] = h[t][r];
                }
                for(tp=0;tp<n;tp++) { // compute the
                    if ((t != tp) && (us[tp] <= i) && (i<le[tp])) {
                        for (r=0;r<rsc.nbResources();r++) {
                            sum[r] += h[tp][r];
                        }
                    }
                }
                for (r=0;r<rsc.nbResources();r++) {
                    if (sum[r] > capa[r]) return false;
                }
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
		return adjustMaxStart(task, ue[task] - ld[task]);
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
