/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;


import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.loop.move.Move;
import org.chocosolver.solver.search.loop.move.MoveRestart;
import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.IntMap;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Integer.MAX_VALUE;

/**
 * Implementation of the search described in:
 * "Activity-Based Search for Black-Box Constraint Propagramming Solver",
 * Laurent Michel and Pascal Van Hentenryck, CPAIOR12.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/06/12
 */
public class ActivityBased extends AbstractStrategy<IntVar> implements IMonitorDownBranch, IMonitorRestart, IMonitorInitialize,
        IVariableMonitor<IntVar>, Comparator<IntVar>/*, VariableSelector<IntVar>*/ {

    private static final double ONE = 1.0f;

    private static final double[] distribution = new double[]{// two-sided 95%
            999.99d,
            12.706f, 4.303f, 3.182f, 2.776f, 2.571f, // 1...5
            2.447f, 2.365f, 2.306f, 2.262f, 2.228f,  // 6...10
            2.201f, 2.179f, 2.160f, 2.145f, 2.131f,  // 10...15
            2.120f, 2.110f, 2.101f, 2.093f, 2.086f,  // 16...20
            2.080f, 2.074f, 2.069f, 2.064f, 2.060f,  // 21...25
            2.056f, 2.052f, 2.048f, 2.045f, 2.042f,  // 26...30
            2.040f, 2.037f, 2.035f, 2.032f, 2.030f,  // 31...35
            2.028f, 2.026f, 2.024f, 2.023f, 2.021f,  // 36...40
            2.000f, 1.990f, 1.984f, 1.980f, 1.977f,  // 60, 80, 100, 120, 140
            1.975f, 1.973f, 1.972f, 1.969f, 1.960f   // 160, 180, 200, 250, inf
    };

    private static double distribution(int n) {
        if (n <= 0) {
            throw new UnsupportedOperationException();
        } else if (n > 0 && n < 41) {
            return distribution[n - 1];
        } else if (n < 61) {
            return distribution[40];
        } else if (n < 81) {
            return distribution[41];
        } else if (n < 101) {
            return distribution[42];
        } else if (n < 121) {
            return distribution[43];
        } else if (n < 141) {
            return distribution[44];
        } else if (n < 161) {
            return distribution[45];
        } else if (n < 181) {
            return distribution[46];
        } else if (n < 201) {
            return distribution[47];
        } else if (n < 251) {
            return distribution[48];
        } else {
            return distribution[49];
        }
    }

    //////////////////////////////
    //////////////////////////////
    //////////////////////////////

    private final Model model;
    private final IntMap v2i;
    private final IntVar[] vars;

    private final double[] A; // activity of all variables
    private final double[] mA; // the mean -- maintained incrementally
    private final double[] sA; // the variance -- maintained incrementally -- std dev = sqrt(sA/path-1)
    private final IVal[] vAct; // activity of each value of all variables

    private final BitSet affected; // store affected variables

    private final double g, d; // g for aging, d for interval size estimation
    private final int a; // forget parameter

    private boolean sampling; // is this still in a sampling phase

    private int nb_probes; // probing size

    private int samplingIterationForced = 1; // CPRU: add this to force sampling phase

    private java.util.Random random; //  a random object for the sampling phase

    private int currentVar = -1, currentVal = -1;

    private TIntList bests = new TIntArrayList();

    private boolean restartAfterEachFail = true;

    private Move rfMove;

    // enables to detect that the heuristic has been removed
    private boolean hasBeenInitiaized;

    public ActivityBased(final Model model, IntVar[] vars, double g, double d, int a, int samplingIterationForced, long seed) {
        super(vars);
        this.model = model;
        this.vars = vars;
        A = new double[vars.length];
        mA = new double[vars.length];
        sA = new double[vars.length];
        vAct = new IVal[vars.length];
        affected = new BitSet(vars.length);

        this.v2i = new IntMap(vars.length);
        for (int i = 0; i < vars.length; i++) {
            v2i.put(vars[i].getId(), i);
            vars[i].addMonitor(this);
        }

        assert g >= 0.0f && g <= 1.0f;
        this.g = g;
        assert d >= 0.0f && d <= 1.0f;
        this.d = d;
        assert a > 0;
        this.a = a;
        sampling = true;
        random = new Random(seed);
        nb_probes = 0;
        this.samplingIterationForced = samplingIterationForced;
//        idx_large = 0; // start the first variable
        model.getSolver().setRestartOnSolutions();
        if (restartAfterEachFail) {
            rfMove = new MoveRestart(model.getSolver().getMove(),
                    new MonotonicRestartStrategy(1),
                    new FailCounter(model.getSolver().getModel(), 1),
                    MAX_VALUE);
            model.getSolver().set(rfMove);
        }
        model.getSolver().plugMonitor(this);
//        init(vars);
    }

    public ActivityBased(IntVar[] vars){
        this(vars[0].getModel(),vars,0.999d, 0.2d, 8, 1,0);
    }

    @Override
    public boolean init() {
        hasBeenInitiaized = true;
        for (int i = 0; i < vars.length; i++) {
            //TODO handle large domain size
            int ampl = vars[i].getUB() - vars[i].getLB() + 1;
            if (ampl > 512) {
                vAct[i] = new MapVal(vars[i].getLB());
            } else {
                vAct[i] = new ArrayVal(ampl, vars[i].getLB());
            }
        }
        return true;
    }

    @Override
    public void afterInitialize(){
        if(!hasBeenInitiaized){
            // the strategy has been erased
            model.getSolver().unplugMonitor(this);
        }
    }

    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.isInstantiated()) {
            return null;
        }
        if (currentVar==-1 || vars[currentVar] != variable) {
			if(sampling){
				return null;
			}
            // retrieve indice of the variable in vars
			for(int i=0;i<vars.length;i++){
				if(vars[i]==variable){
					currentVar = i;
				}
			}
            assert vars[currentVar] == variable;
        }
        currentVal = variable.getLB();
        if (sampling) {
            int ds = variable.getDomainSize();
            int n = random.nextInt(ds);
            if (variable.hasEnumeratedDomain()) {
                while (n-- > 0) {
                    currentVal = variable.nextValue(currentVal);
                }
            } else {
                currentVal += n;
            }
        } else {
            if (variable.hasEnumeratedDomain()) {
                bests.clear();
                double bestVal = Double.MAX_VALUE;
                DisposableValueIterator it = variable.getValueIterator(true);
                while (it.hasNext()) {
                    int value = it.next();
                    double current = vAct[currentVar].activity(value);
                    if (current < bestVal) {
                        bests.clear();
                        bests.add(value);
                        bestVal = current;
                    } else {
                        bests.add(value);
                    }
                }
                currentVal = bests.get(random.nextInt(bests.size()));
            } else {
                int lb = variable.getLB();
                int ub = variable.getUB();
                currentVal = vAct[currentVar].activity(lb) < vAct[currentVar].activity(ub) ?
                        lb : ub;
            }
        }
        return model.getSolver().getDecisionPath().makeIntDecision(variable, DecisionOperator.int_eq, currentVal);
    }

    @Override
    public Decision<IntVar> getDecision() {
        assert hasBeenInitiaized;
        IntVar best = null;
        bests.clear();
        double bestVal = -1.0d;
        for (int i = 0; i < vars.length; i++) {
            int ds = vars[i].getDomainSize();
            if (ds > 1) {
                double a = A[v2i.get(vars[i].getId())] / ds;
                if (a > bestVal) {
                    bests.clear();
                    bests.add(i);
                    bestVal = a;
                } else if (a == bestVal) {
                    bests.add(i);
                }
            }
        }
        if (bests.size() > 0) {
            currentVar = bests.get(random.nextInt(bests.size()));
            best = vars[currentVar];
        }
        return computeDecision(best);
    }

    @Override
    public int compare(IntVar o1, IntVar o2) {
        if (sampling) {
            return random.nextBoolean() ? 1 : -1;
        }
        // select var with the largest ratio A(x)/|D(x)|
        int id1 = v2i.get(o1.getId());
        int id2 = v2i.get(o2.getId());
        // avoid using / operation, * is faster
        double b1 = A[id1] * o2.getDomainSize();
        double b2 = A[id2] * o1.getDomainSize();
        if (b1 > b2) {
            return -1;
        } else if (b1 < b2) {
            return 1;
        }
        return 0;
    }

    public double getActivity(IntVar var) {
        if (v2i.containsKey(var.getId())) {
            return A[v2i.get(var.getId())] / var.getDomainSize();
        } else {
            return 0.0d;
        }
    }


    @Override
    public void onUpdate(IntVar var, IEventType evt) {
        affected.set(v2i.get(var.getId()));
    }

    @Override
    public void beforeDownBranch(boolean left) {
        if (left) {
            affected.clear();
        }
    }

    @Override
    public void afterDownBranch(boolean left) {
        if (left && currentVar > -1) {  // if the decision was computed by another strategy
            for (int i = 0; i < A.length; i++) {
                if (vars[i].getDomainSize() > 1) {
                    A[i] *= sampling ? ONE : g;
                }
                if (affected.get(i)) {
                    A[i] += 1;
                }
            }
            double act = vAct[currentVar].activity(currentVal);
            if (sampling) {
                vAct[currentVar].setactivity(currentVal, act + affected.cardinality());
            } else {
                vAct[currentVar].setactivity(currentVal, (act * (a - 1) + affected.cardinality()) / a);
            }
            currentVar = -1;
        }
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        if (sampling) {
            nb_probes++;
            for (int i = 0; i < A.length; i++) {
                double activity = A[i];
                double oldmA = mA[i];

                double U = activity - oldmA;
                mA[i] += (U / nb_probes);
                sA[i] += (U * (activity - mA[i]));
                A[i] = 0;
                vAct[i].update(nb_probes);
            }
            // check if sampling is still required
            int idx = 0;
            while (idx < vars.length && checkInterval(idx)) {
                idx++;
            }
            //BEWARE: when it fails very soon (after 1 node), it is worth forcing sampling
            if (nb_probes > samplingIterationForced && idx == vars.length) {
                sampling = false;
                if(restartAfterEachFail){
                    Solver sl = model.getSolver();
                    Move m = sl.getMove();
                    if(m == rfMove){
                        sl.set(rfMove.getChildMoves().get(0));
                    }else{
                        while(m.getChildMoves()!= null && m.getChildMoves().get(0)!= rfMove){
                            m = m.getChildMoves().get(0);
                        }
                        if(m.getChildMoves()!= rfMove){
                            m.setChildMoves(rfMove.getChildMoves());
                        }
                    }
                }
                restartAfterEachFail = false;

                // then copy values estimated
                System.arraycopy(mA, 0, A, 0, mA.length);
                for (int i = 0; i < A.length; i++) {
                    vAct[i].transfer();
                }
            }
        }
    }

    /**
     * Return true if the interval is small enough
     *
     * @param idx idx of the variable to check
     * @return true if the confidence interval is small enough, false otherwise
     */
    private boolean checkInterval(int idx) {
        if (!vars[idx].isInstantiated()) {
            double stdev = Math.sqrt(sA[idx] / (nb_probes - 1));
            double a = distribution(nb_probes) * stdev / Math.sqrt(nb_probes);
//            logger.debug("m: {}, v: {}, et: {} => {}", new Object[]{mA[idx], sA[idx], stdev, (a / mA[idx])});
            return (a / mA[idx]) < d;
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private interface IVal {

        double activity(int value);

        void setactivity(int value, double activity);

        void update(int nb_probes);

        void transfer();
    }

    private static final class ArrayVal implements IVal {

        private final double[] Av;
        private final double[] mAv;
        private final int size;
        private final int os;  // offset

        private ArrayVal(int size, int os) {
            this.size = size;
            this.os = os;
            this.Av = new double[size];
            this.mAv = new double[size];
        }

        @Override
        public double activity(int value) {
            return Av[value - os];
        }


        @Override
        public void setactivity(int value, double activity) {
            Av[value - os] = activity;
        }

        @Override
        public void update(int nb_probes) {
            double activity, oldmA, U;
            for (int j = 0; j < Av.length; j++) {
                activity = Av[j];
                oldmA = mAv[j];
                U = activity - oldmA;
                mAv[j] += (U / nb_probes);
            }
        }

        @Override
        public void transfer() {
            System.arraycopy(mAv, 0, Av, 0, size);
        }
    }

    private static final class MapVal implements IVal {

        private final TIntDoubleHashMap Av;
        private final TIntDoubleHashMap mAv;
        private final int os;  // offset

        private MapVal(int os) {
            this.os = os;
            this.Av = new TIntDoubleHashMap(32, 0.5f, 0, 0);
            this.mAv = new TIntDoubleHashMap(32, 0.5f, 0, 0);
        }

        @Override
        public double activity(int value) {
            return Av.get(value - os);
        }

        @Override
        public void setactivity(int value, double activity) {
            Av.put(value - os, activity);
        }

        @Override
        public void update(int nb_probes) {
            double activity, oldmA, U;
            for (int k : Av.keys()) {
                activity = Av.get(k);
                oldmA = mAv.get(k);
                U = activity - oldmA;
                mAv.adjustValue(k, U / nb_probes);
            }
        }

        @Override
        public void transfer() {
            Av.clear();
            Av.putAll(mAv);
        }
    }

}
