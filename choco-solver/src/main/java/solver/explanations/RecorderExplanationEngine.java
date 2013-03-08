/*
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

package solver.explanations;

import common.util.iterators.DisposableValueIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.exception.SolverException;
import solver.explanations.antidom.AntiDomain;
import solver.propagation.queues.CircularQueue;
import solver.search.loop.monitors.IMonitorInitPropagation;
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;
import solver.variables.Variable;

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

    TIntObjectHashMap<AntiDomain> removedvalues; // maintien du domaine courant
    TIntObjectHashMap<TIntObjectHashMap<ValueRemoval>> valueremovals; // maintien de la base de deduction
    TIntObjectHashMap<Explanation> database; // base d'explications

    TIntObjectHashMap<TIntObjectHashMap<BranchingDecision>> leftbranchdecisions; // maintien de la base de left BranchingDecision
    TIntObjectHashMap<TIntObjectHashMap<BranchingDecision>> rightbranchdecisions; // maintien de la base de right BranchingDecision

    protected TIntHashSet expanded = new TIntHashSet();
    protected TIntHashSet toexpand = new TIntHashSet();
    protected CircularQueue<Deduction> pending = new CircularQueue<Deduction>(16);

    public RecorderExplanationEngine(Solver solver) {
        super(solver);
        if (!Configuration.PLUG_EXPLANATION) {
            throw new SolverException("\nExplanations are not plugged in.\n" +
                    "To activate explanations, create a user.property file at project root directory " +
                    "which contains the following two lines:\n" +
                    "# Enabling explanations:\n" +
                    "PLUG_EXPLANATION=true\n");
        }
        removedvalues = new TIntObjectHashMap<AntiDomain>();
        valueremovals = new TIntObjectHashMap<TIntObjectHashMap<ValueRemoval>>();
        database = new TIntObjectHashMap<Explanation>();
        leftbranchdecisions = new TIntObjectHashMap<TIntObjectHashMap<BranchingDecision>>();
        rightbranchdecisions = new TIntObjectHashMap<TIntObjectHashMap<BranchingDecision>>();
        solver.getSearchLoop().plugSearchMonitor(this);
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
            valueremovals.put(vid, new TIntObjectHashMap<ValueRemoval>());
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
            hm = new TIntObjectHashMap<ValueRemoval>();
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
    public BranchingDecision getDecision(Decision decision, boolean isLeft) {
        int vid = decision.getDecisionVariable().getId();
        TIntObjectHashMap<BranchingDecision> mapvar = isLeft ? leftbranchdecisions.get(vid) : rightbranchdecisions.get(vid);
        BranchingDecision vr;
        if (mapvar == null) {
            mapvar = new TIntObjectHashMap<BranchingDecision>();
            if (isLeft) {
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
    public void delete(Deduction deduction) {
        database.remove(deduction.id);
    }

    @Override
    public void removeLeftDecisionFrom(Decision decision, Variable var) {
        leftbranchdecisions.get(var.getId()).remove(decision.getId());
    }

    @Override
    public void removeValue(IntVar var, int val, ICause cause) {
        assert cause != null;
        // 1. get the deduction
        Deduction vr = getValueRemoval(var, val);
        // 2. explain the deduction
        Explanation expl = database.get(vr.id);
        if (expl == null) {
            expl = new Explanation();
            database.put(vr.id, expl);
        } else {
            expl.reset();
        }
        cause.explain(vr, expl);
        // 3. store it within the database

        // 4. store the removed value withing the inverse domain
        AntiDomain invdom = getRemovedValues(var);
        invdom.set(val);

        // 5. explanations monitoring
        if (Configuration.PRINT_EXPLANATION && LOGGER.isInfoEnabled()) {
            onRemoveValue(var, val, cause, expl);
        }
    }

    @Override
    public void updateLowerBound(IntVar var, int old, int val, ICause cause) {
        assert cause != null;
        AntiDomain invdom = getRemovedValues(var);
//        Explanation explanation = new Explanation();
        for (int v = old; v < val; v++) {    // iteration explicite des valeurs retirees
            if (!invdom.get(v)) {
                Deduction vr = getValueRemoval(var, v);
                Explanation expl = database.get(vr.id);
                if (expl == null) {
                    expl = new Explanation();
                    database.put(vr.id, expl);
                } else {
                    expl.reset();
                }
                cause.explain(vr, expl);
                invdom.set(v);
//                explanation.add(expl);
                if (Configuration.PRINT_EXPLANATION && LOGGER.isInfoEnabled()) {
                    onRemoveValue(var, v, cause, expl);
                }
            }
        }
    }

    @Override
    public void updateUpperBound(IntVar var, int old, int val, ICause cause) {
        assert cause != null;
        AntiDomain invdom = getRemovedValues(var);
//        Explanation explanation = new Explanation();
        for (int v = old; v > val; v--) {    // iteration explicite des valeurs retirees
            if (!invdom.get(v)) {
                Deduction vr = getValueRemoval(var, v);
                Explanation expl = database.get(vr.id);
                if (expl == null) {
                    expl = new Explanation();
                    database.put(vr.id, expl);
                } else {
                    expl.reset();
                }
                cause.explain(vr, expl);
                invdom.set(v);
                if (Configuration.PRINT_EXPLANATION && LOGGER.isInfoEnabled()) {
                    onRemoveValue(var, v, cause, expl);
                }
            }
        }
//        emList.onUpdateUpperBound(var, old, val, cause, explanation);
    }


    @Override
    public void instantiateTo(IntVar var, int val, ICause cause) {
        assert cause != null;
        AntiDomain invdom = getRemovedValues(var);
        DisposableValueIterator it = var.getValueIterator(true);
//        Explanation explanation = new Explanation();
        while (it.hasNext()) {
            int v = it.next();
            if (v != val) {
                Deduction vr = getValueRemoval(var, v);
                Explanation expl = database.get(vr.id);
                if (expl == null) {
                    expl = new Explanation();
                    database.put(vr.id, expl);
                } else {
                    expl.reset();
                }
                cause.explain(vr, expl);
                invdom.set(v);
                if (Configuration.PRINT_EXPLANATION && LOGGER.isInfoEnabled()) {
                    onRemoveValue(var, v, cause, expl);
                }
            }
        }
//        emList.onInstantiateTo(var, val, cause, explanation);
    }

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

            if (e != null) {
                int nbp = e.nbPropagators();
                for (int i = 0; i < nbp; i++) {
                    toreturn.add(e.getPropagator(i));
                }
                nbd = e.nbDeductions();
                for (int i = 0; i < nbd; i++) {
                    ded = e.getDeduction(i);
                    if (!expanded.contains(ded.id) && toexpand.add(ded.id)) {
                        pending.addLast(ded);
                    }
                }
            } else {
                toreturn.add(ded);
            }
        }
        return toreturn;
    }

    @Override
    public Explanation flatten(IntVar var, int val) {
        // TODO check that it is always called with val NOT in var
        return flatten(getValueRemoval(var, val));
    }

    @Override
    public Explanation flatten(Deduction deduction) {
        Explanation expl = new Explanation();
        expl.add(deduction);
        return flatten(expl);
    }

    @Override
    public Deduction explain(IntVar var, int val) {
        return explain(getValueRemoval(var, val));
    }

    @Override
    public Deduction explain(Deduction deduction) {
        return deduction;
    }

}
