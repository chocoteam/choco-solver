/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.view.integer.IntAffineView;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.function.Consumer;

import static org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils.unionOf;

/**
 * Container representing a task:
 * It ensures that: start + duration = end
 *
 * @author Jean-Guillaume Fages
 * @since 04/02/2013
 */
public class Task implements ICause {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final IntVar start;
    private final IntVar duration;
    private final IntVar end;
    private Constraint arithmConstraint;

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
        start = model.intVar(Math.max(est, ect - d), Math.min(lst, lct - d));
        duration = model.intVar(d);
        end = start.getModel().offset(start, d);
    }

    /**
     * Container representing a task:
     * It ensures that: start + duration = end, end being an offset view of start + duration.
     *
     * @param s start variable
     * @param d duration value
     */
    public Task(IntVar s, int d) {
        start = s;
        duration = start.getModel().intVar(d);
        end = start.getModel().offset(start, d);
    }

    /**
     * Container representing a task:
     * It ensures that: start + duration = end, end being an offset view of start + duration.
     *
     * @param s start variable
     * @param d duration value
     */
    public Task(IntVar s, IntVar d) {
        if (d.isInstantiated()) {
            start = s;
            duration = d;
            end = start.getModel().offset(start, d.getValue());
        } else {
            start = s;
            duration = d;
            end = start.getModel().intVar(s.getLB() + d.getLB(), s.getUB() + d.getUB());
            arithmConstraint = start.getModel().arithm(start, "+", duration, "=", end);
            arithmConstraint.post();
        }
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
        this(s, d, e, !d.isInstantiated() || !isOffsetView(s, d.getValue(), e));
    }

    private Task(IntVar s, IntVar d, IntVar e, boolean postArithm) {
        start = s;
        duration = d;
        end = e;
        if (postArithm) {
            arithmConstraint = start.getModel().arithm(start, "+", duration, "=", end);
            arithmConstraint.post();
        }
    }

    private static boolean isOffsetView(IntVar s, int d, IntVar e) {
        if(e instanceof IntAffineView) {
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

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * Applies BC-filtering so that start + duration = end
     *
     * @throws ContradictionException thrown if a inconsistency has been detected between start, end and duration
     */
    public void ensureBoundConsistency() throws ContradictionException {
        boolean hasFiltered;
        do {
            // start
            hasFiltered = start.updateBounds(end.getLB() - duration.getUB(), end.getUB() - duration.getLB(), this);
            // end
            hasFiltered |= end.updateBounds(start.getLB() + duration.getLB(), start.getUB() + duration.getUB(), this);
            // duration
            hasFiltered |= duration.updateBounds(end.getLB() - start.getUB(), end.getUB() - start.getLB(), this);
        } while (hasFiltered);
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

    public Constraint getArithmConstraint() {
        return arithmConstraint;
    }

    private static void doExplain(IntVar S, IntVar D, IntVar E,
                                  int p,
                                  ExplanationForSignedClause explanation) {
        IntVar pivot = explanation.readVar(p);
        IntIterableRangeSet dom;
        dom = explanation.complement(S);
        if (S == pivot) {
            unionOf(dom, explanation.readDom(p));
            S.intersectLit(dom, explanation);
        } else {
            S.unionLit(dom, explanation);
        }
        dom = explanation.complement(D);
        if (D == pivot) {
            unionOf(dom, explanation.readDom(p));
            D.intersectLit(dom, explanation);
        } else {
            D.unionLit(dom, explanation);
        }
        dom = explanation.complement(E);
        if (E == pivot) {
            unionOf(dom, explanation.readDom(p));
            E.intersectLit(dom, explanation);
        } else {
            E.unionLit(dom, explanation);
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
    public void explain(int p, ExplanationForSignedClause explanation) {
        doExplain(start, duration, end, p, explanation);
    }

    @Override
    public void forEachIntVar(Consumer<IntVar> action) {
        action.accept(start);
        action.accept(duration);
        action.accept(end);
    }
}
