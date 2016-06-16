/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgement:
 * This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p/>
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
package org.chocosolver.solver.search.strategy.strategy;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

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
    protected AbstractStrategy<V> mainStrategy;
    /**
     * Store the variables in conflict
     */
    List<Variable> vars;
    /**
     * Get the position of a variable (thanks to its ID) in {@code #vars}
     */
    TIntIntHashMap var2pos;
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
        model.getSolver().plugMonitor(this);
        // internal datastructures
        vars = new ArrayList<>();
        var2pos = new TIntIntHashMap(16, .5f, -1, -1);
        prev = new TIntArrayList();
        next = new TIntArrayList();
        pcft = -1;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean init() {
        return mainStrategy.init();
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
        stampIt(model.getSolver().getDecisionPath().getLastDecision().getDecisionVariable());
    }

    void stampIt(Variable cftVar) {
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
        Variable v;
        while (p > -1) {
            v = vars.get(p);
            if (!v.isInstantiated()) {
                return (V) vars.get(p);
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
