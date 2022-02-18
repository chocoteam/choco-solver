/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.learn;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.variables.IntVar;

import java.util.Optional;

/**
 * An abstract class for event recording utility.
 * <p>
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public abstract class AbstractEventObserver {

    /**
     * A default observer that does nothing on events
     */
    public static final AbstractEventObserver SILENT_OBSERVER = new AbstractEventObserver() {
    };

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
    }

    public void pushDecisionLevel(){}

    /**
     * Undo the last operation done
     */
    public void undo() {
    }

    public Optional<Implications> getGI(){
        return Optional.empty();
    }
}
