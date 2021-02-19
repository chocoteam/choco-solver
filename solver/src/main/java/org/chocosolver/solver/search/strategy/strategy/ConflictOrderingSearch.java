/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.variables.Variable;

import java.util.*;

/**
 * Conflict Ordering Search
 * Composite heuristic which hacks a mainStrategy by forcing the
 * use of variables involved in recent conflicts
 * See "Conflict Ordering Search for Scheduling Problems", Steven Gay et al., CP2015.
 *
 * @author Charles Prud'homme
 * @since 15/06/2016
 */
public class ConflictOrderingSearch<V extends Variable> extends AbstractStrategy<V> implements IMonitorContradiction {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    /**
     * The target solver
     */
    protected Model model;

    /**
     * The main strategy declared in the solver
     */
    private AbstractStrategy<V> mainStrategy;
    /**
     * Store the variables in conflict
     */
    List<V> vars;
    /**
     * Get the position of a variable (thanks to its ID) in {@code #vars}
     */
    private TIntIntHashMap var2pos;
    /**
     * Get the position of the variable just before the variable 'i' wrt the stamp
     */
    TIntList prev;
    /**
     * Get the position of the variable just after the variable 'i' wrt the stamp
     */
    TIntList next;
    /**
     * position, in {@code #vars}, of the last variable in conflict
     */
    int pcft;

    protected Set<V> scope;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a conflict-ordering search
     *
     * @param model        the solver to attach this to
     * @param mainStrategy the main strategy declared
     */
    public ConflictOrderingSearch(Model model, AbstractStrategy<V> mainStrategy) {
        super(mainStrategy.vars);
        this.model = model;
        this.mainStrategy = mainStrategy;
        // internal datastructures
        vars = new ArrayList<>();
        var2pos = new TIntIntHashMap(16, .5f, -1, -1);
        prev = new TIntArrayList();
        next = new TIntArrayList();
        pcft = -1;
        this.scope = new HashSet<>(Arrays.asList(mainStrategy.vars));
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean init() {
        if(model.getSolver().getSearchMonitors().contains(this)) {
            model.getSolver().plugMonitor(this);
        }
        return mainStrategy.init();
    }

    @Override
    public void remove() {
        this.mainStrategy.remove();
        if(model.getSolver().getSearchMonitors().contains(this)) {
            model.getSolver().unplugMonitor(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Decision<V> getDecision() {
        V decVar = firstNotInst();
        if (decVar != null) {
            Decision d = mainStrategy.computeDecision(decVar);
            if (d != null) {
                return d;
            }
        }
        return mainStrategy.getDecision();
    }

    //***********************************************************************************
    // Monitor
    //***********************************************************************************


    @Override
    public void onContradiction(ContradictionException cex) {
        //noinspection unchecked
        Decision<V> dec = model.getSolver().getDecisionPath().getLastDecision();
        if(dec != RootDecision.ROOT) {
            if (scope.contains(dec.getDecisionVariable())) {
                stampIt(dec.getDecisionVariable());
            }
        }
    }

    void stampIt(V cftVar) {
        int id = cftVar.getId();
        int pos = var2pos.get(id);
        if (pos == -1) {
            // first, declare cftVar
            pos = vars.size();
            vars.add(cftVar);
            var2pos.put(id, pos);
            // then retrieve lcft
            if (pcft > -1) {
                next.add(-1);
                next.set(pcft, pos);
                prev.add(pcft);
            } else {
                assert pos == 0;
                prev.add(-1);
                next.add(-1);
            }
        } else if (pos != pcft) {
            int p = prev.get(pos);
            int n = next.get(pos);
            if (p > -1) {
                next.set(p, n);
            }
            next.set(pcft, pos);
            next.set(pos, -1);
            if (n > -1) {
                prev.set(n, p);
            }
            prev.set(pos, pcft);
        }
        pcft = pos;
    }

    //***********************************************************************************
    //***********************************************************************************

    V firstNotInst() {
        int p = pcft;
        V v;
        while (p > -1) {
            v = vars.get(p);
            if (!v.isInstantiated()) {
                return vars.get(p);
            }
            p = prev.get(p);
        }
        return null;
    }

    boolean check(){
        boolean ok = true;
        int first = -1;
        for(int i = 0; i < vars.size() && ok; i++){
            int p = prev.get(i);
            int n = next.get(i);
            ok = (i == pcft && n == -1) || prev.get(n) == i;
            ok &= p == -1 || next.get(p) == i;
            if(p == -1){
                ok &= first == -1;
                first = i;
            }
        }
        return ok;
    }

}
