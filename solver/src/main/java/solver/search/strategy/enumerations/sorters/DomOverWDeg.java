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
package solver.search.strategy.enumerations.sorters;

import choco.kernel.memory.IStateInt;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.search.loop.monitors.FailPerPropagator;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * Naive implementation of
 * "Boosting systematic search by weighting constraints"
 * F.Boussemart, F.Hemery, C.Lecoutre and L.Sais
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/05/11
 */
public final class DomOverWDeg extends AbstractSorter<IntVar> implements IVariableMonitor {

    final Solver solver;
    FailPerPropagator counter;
    TIntIntMap vid2dsize, vid2degree, vid2weig;
    TIntObjectHashMap<IStateInt> pid2ari;
    TIntIntHashMap pid2arity;
    Random rand;

    protected DomOverWDeg(Solver solver, long seed) {
        this.solver = solver;
        rand = new Random(seed);
        counter = new FailPerPropagator(solver.getCstrs(), solver);
        vid2dsize = new TIntIntHashMap();
        vid2degree = new TIntIntHashMap();
        vid2weig = new TIntIntHashMap();
        pid2ari = new TIntObjectHashMap<IStateInt>();
        pid2arity = new TIntIntHashMap(10, 0.5F, -1, -1);

        Variable[] vars = solver.getVars();
        for (int i = 0; i < vars.length; i++) {
            vars[i].addMonitor(this);
        }
        Constraint[] cstrs = solver.getCstrs();
        for (int i = 0; i < cstrs.length; i++) {
            Propagator[] props = cstrs[i].propagators;
            for (int j = 0; j < props.length; j++) {
                pid2ari.put(props[j].getId(), solver.getEnvironment().makeInt(props[j].arity()));
            }
        }
    }

    private int weight(IntVar v) {
        int w = 0;
        Propagator[] propagators = v.getPropagators();
        for (int p = 0; p < propagators.length; p++) {
            Propagator prop = propagators[p];
            int pid = prop.getId();
            if (pid2arity.get(pid) > 1) {
                w += counter.getFails(prop);
            } else {
                int a = pid2ari.get(pid).get();
                pid2arity.put(pid, a);
                if (a > 1) {
                    w += counter.getFails(prop);
                }
            }
        }
        return w;
    }


    @Override
    public int compare(IntVar o1, IntVar o2) {
        int vid1 = o1.getId();
        int vid2 = o2.getId();
        int w1 = vid2weig.get(vid1);
        int w2 = vid2weig.get(vid2);
        int s1 = vid2dsize.get(vid1);
        int s2 = vid2dsize.get(vid2);
        int d1 = vid2degree.get(vid1);
        int d2 = vid2degree.get(vid2);
        int r = (s1 * w2 * d2) - (s2 * w1 * d1);
        if (r == 0) {
            return rand.compare(o1, o2);
        } else return r;
    }

    @Override
    public int minima(IntVar[] elements, int from, int to) {
        vid2dsize.clear();
        vid2degree.clear();
        vid2weig.clear();
        pid2arity.clear();
        for (int i = from; i <= to; i++) {
            int vid = elements[i].getId();
            vid2dsize.put(vid, elements[i].getDomainSize());
            vid2degree.put(vid, elements[i].getNbProps());
            vid2weig.put(vid, weight(elements[i]));
        }
        return super.minima(elements, from, to);
    }

    @Override
    public void onUpdate(Variable var, EventType evt, ICause cause) {
        if (evt == EventType.INSTANTIATE) {
            Propagator[] props = var.getPropagators();
            for (int i = 0; i < props.length; i++) {
                int pid = props[i].getId();
                pid2ari.get(pid).add(-1);
            }
        }
    }
}
