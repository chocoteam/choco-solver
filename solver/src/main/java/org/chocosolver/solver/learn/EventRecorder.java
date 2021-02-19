/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.learn;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.view.IView;

import java.util.Optional;

/**
 * An explanation engine that learns general constraint from failures.
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 25/01/2017.
 */
public class EventRecorder extends AbstractEventObserver {

    /**
     * The implication graph
     */
    private final Implications mIG;

    /**
     * Create an explanation engine which is able to learn general constraint on conflict
     * @param solver solver this class relies on
     */
    public EventRecorder(Solver solver) {
        mIG = new LazyImplications(solver.getModel());
        solver.setEventObserver(this);
    }

    /**
     * Explain the removal of the {@code val} from {@code var}, due to {@code cause}.
     * This is the main explanation why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     */
    public void removeValue(IntVar var, int val, ICause cause) {
        mIG.pushEvent(var, cause, IntEventType.REMOVE, val, -1, -1);
        for (int i = 0; i < var.getNbViews(); i++) {
            IView view = var.getView(i);
            if (view != cause) {
                view.justifyEvent(IntEventType.REMOVE, val, -1, -1);
            }
        }
    }

    /**
     * Explain the removal of [{@code old},{@code value}[ from {@code var}, due to {@code cause}.
     * <p/>
     * Prerequisite: {@code value} should belong to {@code var}
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param value a value
     * @param cause a cause
     * @value old previous LB
     */
    public void updateLowerBound(IntVar var, int value, int old, ICause cause) {
        mIG.pushEvent(var, cause, IntEventType.INCLOW, value, old, -1);
        if (var.hasEnumeratedDomain() && !mIG.getDomainAt(mIG.size() - 1).isEmpty()) {
            // this is required when there are holes in the domain
            // the new lower bound may be largest that the one declared
            value = mIG.getDomainAt(mIG.size() - 1).min();
        }
        for (int i = 0; i < var.getNbViews(); i++) {
            IView view = var.getView(i);
            if (view != cause) {
                view.justifyEvent(IntEventType.INCLOW, value, old, -1);
            }
        }
    }

    /**
     * Explain the removal of ]{@code value},{@code old}] from {@code var}, due to {@code cause}.
     * <p/>
     * Prerequisite: {@code value} should belong to {@code var}
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param value a value
     * @param cause a cause
     * @value old previous LB
     */
    public void updateUpperBound(IntVar var, int value, int old, ICause cause) {
        mIG.pushEvent(var, cause, IntEventType.DECUPP, value, old, -1);
        if(var.hasEnumeratedDomain()){
            // this is required when there are holes in the domain
            // the new upper bound may be smallest that the one declared
            if (var.hasEnumeratedDomain() && !mIG.getDomainAt(mIG.size() - 1).isEmpty()) {
                value = mIG.getDomainAt(mIG.size() - 1).max();
            }
        }
        for (int i = 0; i < var.getNbViews(); i++) {
            IView view = var.getView(i);
            if (view != cause) {
                view.justifyEvent(IntEventType.DECUPP, value, old, -1);
            }
        }
    }

    /**
     * Explain the assignment to {@code val} of {@code var} due to {@code cause}.
     * This is the main reason why we create this class.
     * Record operations to execute for explicit call to explanation.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     * @param oldLB previous lb
     * @param oldUB previous ub
     */
    public void instantiateTo(IntVar var, int val, ICause cause, int oldLB, int oldUB) {
        mIG.pushEvent(var, cause, IntEventType.INSTANTIATE, val, oldLB, oldUB);
        for (int i = 0; i < var.getNbViews(); i++) {
            IView view = var.getView(i);
            if (view != cause) {
                view.justifyEvent(IntEventType.INSTANTIATE, val, oldLB, oldUB);
            }
        }
    }

    @Override
    public void pushDecisionLevel() {
        mIG.tagDecisionLevel();
    }

    public Optional<Implications> getGI() {
        return Optional.of(mIG);
    }

    @Override
    public void undo() {
        mIG.undoLastEvent();
    }
}
