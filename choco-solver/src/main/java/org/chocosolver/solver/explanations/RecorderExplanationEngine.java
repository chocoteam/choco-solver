/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.explanations;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.antidom.AntiDomain;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitPropagation;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.queues.CircularQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 14:18:18
 * <p/>
 * An RecorderExplanationEngine is used to record explanations throughout computation.
 * Here we just record the explanations in a HashMap ...
 * <p/>
 */
public class RecorderExplanationEngine extends ExplanationEngine implements IMonitorInitPropagation {

    static Logger LOGGER = LoggerFactory.getLogger(RecorderExplanationEngine.class);

    TIntObjectHashMap<AntiDomain> removedvalues; // maintien du domaine courant
    TIntObjectHashMap<TIntObjectHashMap<ValueRemoval>> valueremovals; // maintain deduction base
    // maintain cause of propagator activation -- can be sparse
    TIntObjectHashMap<PropagatorActivation> propactivs;
    TIntObjectHashMap<Explanation> database; // base d'explications

    TIntObjectHashMap<TIntObjectHashMap<BranchingDecision>> leftbranchdecisions; // maintien de la base de left BranchingDecision
    TIntObjectHashMap<TIntObjectHashMap<BranchingDecision>> rightbranchdecisions; // maintien de la base de right BranchingDecision

    protected TIntHashSet expanded = new TIntHashSet();
    protected TIntHashSet toexpand = new TIntHashSet();
    protected CircularQueue<Deduction> pending = new CircularQueue<>(16);

    public RecorderExplanationEngine(Solver solver) {
        super(solver);
        if (!solver.getSettings().plugExplanationIn()) {
            throw new SolverException("\nExplanations are not plugged in.\n" +
                    "To activate explanations, create a user.property file at project root directory " +
                    "which contains the following two lines:\n" +
                    "# Enabling explanations:\n" +
                    "PLUG_EXPLANATION=true\n");
        }
        removedvalues = new TIntObjectHashMap<>();
        valueremovals = new TIntObjectHashMap<>();
        propactivs = new TIntObjectHashMap<>();
        database = new TIntObjectHashMap<>();
        leftbranchdecisions = new TIntObjectHashMap<>();
        rightbranchdecisions = new TIntObjectHashMap<>();
        solver.plugMonitor(this);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void beforeInitialPropagation() {
        for (Variable v : solver.getVars()) {
            getRemovedValues((IntVar) v);
        }
    }

    @Override
    public void afterInitialPropagation() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public AntiDomain getRemovedValues(IntVar v) {
        int vid = v.getId();
        AntiDomain toreturn = removedvalues.get(vid);
        if (toreturn == null) {
            toreturn = v.antiDomain();
            removedvalues.put(vid, toreturn);
            TIntObjectHashMap<ValueRemoval> hm = valueremovals.get(vid);
            if (hm == null) {
                hm = new TIntObjectHashMap<>();
                valueremovals.put(vid, hm);
            }
        }
        return toreturn;
    }

    @Override
    public Explanation retrieve(IntVar var, int val) {
        return database.get(getValueRemoval(var, val).id);
    }

    public ValueRemoval getValueRemoval(IntVar var, int val) {
        int vid = var.getId();
        ValueRemoval vr;
        TIntObjectHashMap<ValueRemoval> hm = valueremovals.get(vid);
        if (hm == null) {
            hm = new TIntObjectHashMap<>();
            valueremovals.put(vid, hm);
        }
        vr = hm.get(val);
        if (vr == null) {
            vr = new ValueRemoval(var, val);
            valueremovals.get(vid).put(val, vr);
        }
        return vr;
    }

    @Override
    public PropagatorActivation getPropagatorActivation(Propagator propagator) {
//        assert propagator.isActive();
        int pid = propagator.getId();
        PropagatorActivation pa;
        pa = propactivs.get(pid);
        if (pa == null) {
            pa = new PropagatorActivation(propagator);
            propactivs.put(pid, pa);
        }
        return pa;
    }

    @Override
    public BranchingDecision getDecision(Decision decision, boolean isLeft) {
        int vid = decision.getDecisionVariable().getId();
        TIntObjectHashMap<BranchingDecision> mapvar = isLeft ? leftbranchdecisions.get(vid) : rightbranchdecisions.get(vid);
        BranchingDecision vr;
        if (mapvar == null) {
            mapvar = new TIntObjectHashMap<>();
            if (isLeft) {
                if (!decision.hasNext()) {
                    System.out.println(decision);
                    throw new SolverException("Arg!");
                }
                leftbranchdecisions.put(vid, mapvar);
            } else {
                rightbranchdecisions.put(vid, mapvar);
            }
        }
        vr = mapvar.get(decision.getId());
        if (vr == null) {
            vr = new BranchingDecision(decision, isLeft);
            mapvar.put(decision.getId(), vr);
        }
        return vr;
    }

    @Override
    public void store(Deduction deduction, Explanation explanation) {
        database.put(deduction.id, explanation);
    }

    @Override
    public void removeLeftDecisionFrom(Decision decision, Variable var) {
        leftbranchdecisions.get(var.getId()).remove(decision.getId());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// ACTIONS ON VARIABLE MODIFICATIONS ///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void activePropagator(BoolVar var, Propagator propagator) {
        PropagatorActivation pa = getPropagatorActivation(propagator);
        Explanation expl = database.get(pa.id);
        if (expl == null) {
            expl = new Explanation();
        } else {
            expl.reset();
        }
        var.explain(this, VariableState.DOM, expl);
        if (LOGGER.isDebugEnabled()) {
            onActivatePropagator(propagator, expl);
        }
        store(pa, expl);
    }


    private void explainValueRemoval(IntVar var, int val, ICause cause) {
        // 1. retrieve the deduction
        Deduction vr = getValueRemoval(var, val);
        // 2. get the previous explanation, if any
        Explanation expl = database.get(vr.id);
        if (expl == null) {
            expl = new Explanation();
            store(vr, expl);
        } else {
            expl.reset();
        }
        // 3. explain the value removal thanks to the cause
        cause.explain(this, vr, expl);
        // 4. explanations monitoring
        if (LOGGER.isDebugEnabled()) {
            onRemoveValue(var, val, cause, expl);
        }
    }

    @Override
    public void removeValue(IntVar var, int val, ICause cause) {
        assert cause != null;
        // 1. explain the value removal
        explainValueRemoval(var, val, cause);
        // 2. update the inverse domain
        AntiDomain invdom = getRemovedValues(var);
        invdom.add(val);
    }

    @Override
    public void updateLowerBound(IntVar var, int old, int val, ICause cause) {
        assert cause != null;
        AntiDomain invdom = getRemovedValues(var);
        if (invdom.isEnumerated()) {
            for (int v = old; v < val; v++) {
                if (!invdom.get(v)) {
                    explainValueRemoval(var, v, cause);
                    invdom.add(v);
                }
            }
        } else {
            // PREREQUISITE: val is the new LB, so val-1 is the one explained
            val--;
            if (!invdom.get(val)) {
                explainValueRemoval(var, val, cause);
                // we add +1, because val is the value just BEFORE the new LB
                invdom.updateLowerBound(old, val + 1);
            }
        }
    }

    @Override
    public void updateUpperBound(IntVar var, int old, int val, ICause cause) {
        assert cause != null;
        AntiDomain invdom = getRemovedValues(var);
        if (invdom.isEnumerated()) {
            for (int v = old; v > val; v--) {
                if (!invdom.get(v)) {
                    explainValueRemoval(var, v, cause);
                    invdom.add(v);
                }
            }
        } else {
            // PREREQUISITE: val is the new UB, so val+1 is the one explained
            val++;
            if (!invdom.get(val)) {
                explainValueRemoval(var, val, cause);
                // we add -1, because val is the value just AFTER the new LB
                invdom.updateUpperBound(old, val - 1);
            }
        }
    }


    @Override
    public void instantiateTo(IntVar var, int val, ICause cause, int oldLB, int oldUB) {
        AntiDomain invdom = getRemovedValues(var);

        if (invdom.isEnumerated()) {
            for (int v = oldLB; v < val; v++) {
                if (!invdom.get(v)) {
                    explainValueRemoval(var, v, cause);
                    invdom.add(v);
                }
            }
            for (int v = oldUB; v > val; v--) {
                if (!invdom.get(v)) {
                    explainValueRemoval(var, v, cause);
                    invdom.add(v);
                }
            }
        } else {
            if (val < oldLB) {
                // domain wipe out
                explainValueRemoval(var, oldLB, cause);
                invdom.updateUpperBound(oldUB, oldLB - 1);
            } else if (val > oldUB) {
                // domain wipe out
                explainValueRemoval(var, oldUB, cause);
                invdom.updateLowerBound(oldLB, oldUB + 1);
            } else {
                if (val > oldLB && !invdom.get(val)) {
                    explainValueRemoval(var, val - 1, cause);
                    invdom.updateLowerBound(oldLB, val);
                }
                if (val < oldUB && !invdom.get(val)) {
                    explainValueRemoval(var, val + 1, cause);
                    invdom.updateUpperBound(oldUB, val);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public Explanation flatten(Explanation expl) {
        Explanation toreturn = new Explanation();


        expanded.clear();
        toexpand.clear();
        pending.clear();

        Deduction ded;
        int nbd = expl.nbDeductions();
        for (int i = 0; i < nbd; i++) {
            ded = expl.getDeduction(i);
            pending.addLast(ded);
            toexpand.add(ded.id);
        }


        while (!pending.isEmpty()) {
            ded = pending.pollFirst();
            toexpand.remove(ded.id);
            expanded.add(ded.id);

            Explanation e = database.get(ded.id);
//            System.out.printf("%s \n", ded);
            if (e != null) {
                nbd = e.nbDeductions();
                for (int i = 0; i < nbd; i++) {
                    ded = e.getDeduction(i);
//                    System.out.printf("\t-> %s\n", ded);
                    if (!expanded.contains(ded.id) && toexpand.add(ded.id)) {
                        pending.addLast(ded);
                    }
                }
            } else {
                toreturn.add(ded);
            }
//            System.out.printf("\n");
        }
        return toreturn;
    }

    @Override
    public Explanation flatten(IntVar var, int val) {
        // TODO check that it is always called with val NOT in var
        AntiDomain ad = getRemovedValues(var);
        return flatten(getValueRemoval(var, ad.getKeyValue(val)));
    }

    @Override
    public Explanation flatten(Deduction deduction) {
        Explanation expl = new Explanation();
        expl.add(deduction);
        return flatten(expl);
    }

    @Override
    public Deduction explain(IntVar var, int val) {
        AntiDomain ad = getRemovedValues(var);
        return explain(getValueRemoval(var, ad.getKeyValue(val)));
    }

    @Override
    public Deduction explain(Deduction deduction) {
        return deduction;
    }
}
