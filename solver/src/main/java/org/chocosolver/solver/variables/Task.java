/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.view.integer.IntAffineView;
import org.chocosolver.util.ESat;

import java.util.function.Consumer;

/**
 * Container representing a task:
 * It ensures that: start + duration = end
 *
 * @author Arthur Godet
 * @since 25/11/2023
 */
@Explained
public class Task extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected final IntVar start;
    protected final IntVar duration;
    protected final IntVar end;

    protected Task mirror = null;

    private boolean insidePropagation = false;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Container representing a task:
     * It ensures that: start + duration = end, end being an offset view of start + duration.
     *
     * @param model the Model of the variables
     * @param est earliest starting time
     * @param lst latest starting time
     * @param d duration
     * @param ect earliest completion time
     * @param lct latest completion time
     */
    public Task(Model model, int est, int lst, int d, int ect, int lct) {
        this(buildVars(model, est, lst, d, ect, lct));
    }

    private static IntVar[] buildVars(Model model, int est, int lst, int d, int ect, int lct) {
        IntVar start = model.intVar(Math.max(est, ect - d), Math.min(lst, lct - d));
        IntVar duration = model.intVar(d);
        IntVar end = start.getModel().offset(start, d);
        return new IntVar[]{start, duration, end};
    }

    /**
     * Container representing a task:
     * It ensures that: start + duration = end, end being an offset view of start + duration.
     *
     * @param s start variable
     * @param d duration value
     */
    public Task(IntVar s, int d) {
        this(s, s.getModel().intVar(d), s.getModel().offset(s, d));
    }

    /**
     * Container representing a task:
     * It ensures that: start + duration = end, end being an offset view of start + duration.
     *
     * @param s start variable
     * @param d duration value
     */
    public Task(IntVar s, IntVar d) {
        this(
                s,
                d,
                d.isInstantiated() ? s.getModel().offset(s, d.getValue())
                        : s.getModel().intVar(s.getLB() + d.getLB(), s.getUB() + d.getUB())
        );
    }

    /**
     * Container representing a task:
     * It ensures that: start + duration = end, end being an offset view of start + duration.
     *
     * @param s start variable
     * @param d duration value
     * @param e end variable
     */
    public Task(IntVar s, int d, IntVar e) {
        this(s, s.getModel().intVar(d), e);
    }

    /**
     * Container representing a task:
     * It ensures that: start + duration = end
     *
     * @param s start variable
     * @param d duration variable
     * @param e end variable
     */
    public Task(IntVar s, IntVar d, IntVar e) {
        super(new IntVar[]{s, d, e}, PropagatorPriority.TERNARY, false, false);
        start = s;
        duration = d;
        end = e;
        if (shouldPassivate(s, d, e)) {
            setActive();
            setPassive();
        } else {
            this.getModel().post(new Constraint("Task relation", this));
        }
    }

    private Task(IntVar[] vars) {
        this(vars[0], vars[1], vars[2]);
    }

    public static boolean isOffsetView(IntVar s, int d, IntVar e) {
        if (e instanceof IntAffineView) {
            IntAffineView<?> intOffsetView = (IntAffineView<?>) e;
            if (intOffsetView.p) {
                return intOffsetView.equals(s, 1, d);
            } else {
                intOffsetView = (IntAffineView<?>) s;
                return intOffsetView.equals(e.neg().intVar(), -1, -d);
            }
        }
        return false;
    }

    public static boolean shouldPassivate(final IntVar s, final IntVar d, final IntVar e) {
        return d.isInstantiated() && isOffsetView(s, d.getValue(), e);
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    public IntVar getStart() {
        return start;
    }

    public IntVar getDuration() {
        return duration;
    }

    public IntVar getEnd() {
        return end;
    }

    public int getEst() {
        return start.getLB();
    }

    public int getLst() {
        return start.getUB();
    }

    public int getEct() {
        return end.getLB();
    }

    public int getLct() {
        return end.getUB();
    }

    public int getMinDuration() {
        return duration.getLB();
    }

    public int getMaxDuration() {
        return duration.getUB();
    }

    public boolean hasCompulsoryPart() {
        return getLst() < getEct();
    }

    public boolean isStartInstantiated() {
        return start.isInstantiated();
    }

    public boolean isEndInstantiated() {
        return end.isInstantiated();
    }

    public boolean updateEst(int est, ICause cause) throws ContradictionException {
        if (start.updateLowerBound(est, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateLst(int lst, ICause cause) throws ContradictionException {
        if (start.updateUpperBound(lst, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateEct(int ect, ICause cause) throws ContradictionException {
        if (end.updateLowerBound(ect, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateLct(int lct, ICause cause) throws ContradictionException {
        if (end.updateUpperBound(lct, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateMinDuration(int minDuration, ICause cause) throws ContradictionException {
        if (duration.updateLowerBound(minDuration, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateMaxDuration(int maxDuration, ICause cause) throws ContradictionException {
        if (duration.updateUpperBound(maxDuration, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateDuration(int minDuration, int maxDuration, ICause cause) throws ContradictionException {
        if (duration.updateBounds(minDuration, maxDuration, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateEst(int est, ICause cause, Reason reason) throws ContradictionException {
        if (start.updateLowerBound(est, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateLst(int lst, ICause cause, Reason reason) throws ContradictionException {
        if (start.updateUpperBound(lst, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateEct(int ect, ICause cause, Reason reason) throws ContradictionException {
        if (end.updateLowerBound(ect, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateLct(int lct, ICause cause, Reason reason) throws ContradictionException {
        if (end.updateUpperBound(lct, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateMinDuration(int minDuration, ICause cause, Reason reason) throws ContradictionException {
        if (duration.updateLowerBound(minDuration, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateMaxDuration(int maxDuration, ICause cause, Reason reason) throws ContradictionException {
        if (duration.updateUpperBound(maxDuration, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean updateDuration(int minDuration, int maxDuration, ICause cause, Reason reason) throws ContradictionException {
        if (duration.updateBounds(minDuration, maxDuration, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean instantiateStartAt(int t, ICause cause) throws ContradictionException {
        if (start.instantiateTo(t, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean instantiateEndAt(int t, ICause cause) throws ContradictionException {
        if (end.instantiateTo(t, cause)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean instantiateStartAt(int t, ICause cause, Reason reason) throws ContradictionException {
        if (start.instantiateTo(t, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean instantiateEndAt(int t, ICause cause, Reason reason) throws ContradictionException {
        if (end.instantiateTo(t, cause, reason)) {
            propagate();
            return true;
        }
        return false;
    }

    public boolean mayBePerformed() {
        return true;
    }

    public boolean mustBePerformed() {
        return true;
    }

    public boolean forceToBePerformed(ICause cause) throws ContradictionException {
        return false;
    }

    public boolean forceToBePerformed(ICause cause, Reason reason) throws ContradictionException {
        return false;
    }

    public boolean forceToBeOptional(ICause cause) throws ContradictionException {
        ContradictionException ex = new ContradictionException();
        ex.set(cause, null, "forcing Task to be optional");
        throw ex;
    }

    public boolean forceToBeOptional(ICause cause, Reason reason) throws ContradictionException {
        ContradictionException ex = new ContradictionException();
        ex.set(cause, null, "forcing Task to be optional");
        throw ex;
    }

    public Task getMirror() {
        if (mirror == null) {
            mirror = new Task(end.neg().intVar(), duration, start.neg().intVar());
        }
        return mirror;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        insidePropagation = true;
        if (model.getSolver().getNodeCount() == 0) { // Only at root node
            updateMinDuration(0, this);
        }
        boolean hasFiltered;
        final boolean lcg = getModel().getSolver().isLCG();
        do {
            hasFiltered = false;
            if (mayBePerformed()) {
                if (lcg) {
                    hasFiltered = updateEst(
                            end.getLB() - duration.getUB(),
                            this,
//                            Reason.r(end.getMinLit(), duration.getMaxLit())
                            Reason.r(end.getLELit(end.getLB() - 1), duration.getGELit(duration.getUB() + 1))
                    );
                    hasFiltered |= updateLst(
                            end.getUB() - duration.getLB(),
                            this,
//                            Reason.r(end.getMaxLit(), duration.getMinLit())
                            Reason.r(end.getGELit(end.getUB() + 1), duration.getLELit(duration.getLB() - 1))
                    );

                    hasFiltered |= updateEct(
                            start.getLB() + duration.getLB(),
                            this,
//                            Reason.r(start.getMinLit(), duration.getMinLit())
                            Reason.r(start.getLELit(start.getLB() - 1), duration.getLELit(duration.getLB() - 1))
                    );
                    hasFiltered |= updateLct(
                            start.getUB() + duration.getUB(),
                            this,
//                            Reason.r(start.getMaxLit(), duration.getMaxLit())
                            Reason.r(start.getGELit(start.getUB() + 1), duration.getGELit(duration.getUB() + 1))
                    );

                    hasFiltered |= updateMinDuration(
                            end.getLB() - start.getUB(),
                            this,
//                            Reason.r(end.getMinLit(), start.getMaxLit())
                            Reason.r(end.getLELit(end.getLB() - 1), start.getGELit(start.getUB() + 1))
                    );
                    hasFiltered |= updateMaxDuration(
                            end.getUB() - start.getLB(),
                            this,
//                            Reason.r(end.getMaxLit(), start.getMinLit())
                            Reason.r(end.getGELit(end.getUB() + 1), start.getLELit(start.getLB() - 1))
                    );
                } else {
                    hasFiltered = updateEst(end.getLB() - duration.getUB(), this);
                    hasFiltered |= updateLst(end.getUB() - duration.getLB(), this);

                    hasFiltered |= updateEct(start.getLB() + duration.getLB(), this);
                    hasFiltered |= updateLct(start.getUB() + duration.getUB(), this);

                    hasFiltered |= updateMinDuration(end.getLB() - start.getUB(), this);
                    hasFiltered |= updateMaxDuration(end.getUB() - start.getLB(), this);
                }
                // TODO : add filtering for enumerated domains
            }
        } while (hasFiltered);
        insidePropagation = false;
    }

    @Override
    public ESat isEntailed() {
        if (start.isInstantiated() && duration.isInstantiated() && end.isInstantiated()) {
            return ESat.eval(start.getValue() + duration.getValue() == end.getValue());
        } else {
            return ESat.UNDEFINED;
        }
    }

    @Override
    public String toString() {
        return "Task[" +
                "start=" + start +
                ", duration=" + duration +
                ", end=" + end +
                ']';
    }

    @Override
    public void forEachIntVar(Consumer<IntVar> action) {
        action.accept(start);
        action.accept(duration);
        action.accept(end);
    }

    private void propagate() throws ContradictionException {
        if (!insidePropagation) {
            propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        }
    }
}
