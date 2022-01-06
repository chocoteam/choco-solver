/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/*
@author Arthur Godet <arth.godet@gmail.com>
@since 01/06/2020
*/

package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorUpBranch;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.Arrays;
import java.util.HashMap;

import static org.chocosolver.solver.variables.events.IntEventType.*;

public class SetTimes extends Propagator<IntVar> implements VariableSelector<IntVar>, IMonitorUpBranch {
    private final Task[] tasks;
    private final HashMap<IntVar, Integer> notSelectable;

    public SetTimes(Task[] tasks) {
        super(Arrays.stream(tasks).map(Task::getStart).toArray(IntVar[]::new), PropagatorPriority.UNARY, true);
        this.tasks = tasks;
        this.notSelectable = new HashMap<>(tasks.length);
    }

    private IntVar selectNextVariable() {
        int small_idx = -1;
        int small_est = Integer.MAX_VALUE;
        int small_lst = Integer.MAX_VALUE;
        for (int idx = 0; idx < tasks.length; idx++) {
            int dsize = tasks[idx].getStart().getDomainSize();
            int est = tasks[idx].getStart().getLB();
            int lst = tasks[idx].getEnd().getLB();
            if (dsize > 1 && selectable(idx, est)) {
                if(est < small_est || (est == small_est && lst < small_lst)) {
                    small_est = est;
                    small_lst = lst;
                    small_idx = idx;
                }
            }
        }
        return small_idx > -1 ? tasks[small_idx].getStart() : null;
    }

    private boolean selectable(int idx, int est) {
        Integer est_ = notSelectable.get(tasks[idx].getStart());
        return est_ == null || est != est_;
    }

    @Override
    public void beforeUpBranch() {
        if(this.getModel().getSolver().getDecisionPath().size()>1) {
            IntDecision dec = (IntDecision) this.getModel().getSolver().getDecisionPath().getLastDecision();
            if (dec.hasNext()) {
                notSelectable.put(dec.getDecisionVariable(), dec.getDecisionVariable().getLB());
            }
        }
    }

    @Override
    public IntVar getVariable(IntVar[] variables) {
        return selectNextVariable();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        //if (!isCompletelyInstantiated() && selectNextVariable() == null) {
        //    fails();
        //}
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.combine(INCLOW, INSTANTIATE);
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        IntVar start =  tasks[idxVarInProp].getStart();
        Integer est = notSelectable.get(start);
        if(est!=null && est > start.getLB()){
            notSelectable.remove(start);
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
