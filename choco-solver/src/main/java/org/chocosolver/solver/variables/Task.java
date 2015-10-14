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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 04/02/13
 * Time: 15:48
 */

package org.chocosolver.solver.variables;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;

/**
 * Container representing a task:
 * It ensures that: start + duration = end
 *
 * @author Jean-Guillaume Fages
 * @since 04/02/2013
 */
public class Task {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar start, duration, end;
    private IVariableMonitor update;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Container representing a task:
     * It ensures that: start + duration = end
     *
     * @param s start variable
     * @param d duration variable
     * @param e end variable
     */
    public Task(IntVar s, IntVar d, IntVar e) {
        start = s;
        duration = d;
        end = e;
        if (s.hasEnumeratedDomain() || d.hasEnumeratedDomain() || e.hasEnumeratedDomain()) {
            update = new TaskMonitorEnum(s, d, e);
        } else {
            update = new TaskMonitorBound(s, d, e);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * Applies BC-filtering so that start + duration = end
     *
     * @throws ContradictionException
     */
    public void ensureBoundConsistency() throws ContradictionException {
        update.onUpdate(start, IntEventType.REMOVE);
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    public IntVar getStart() {
        return start;
    }

    public void setStart(IntVar start) {
        this.start = start;
    }

    public IntVar getDuration() {
        return duration;
    }

    public void setDuration(IntVar duration) {
        this.duration = duration;
    }

    public IntVar getEnd() {
        return end;
    }

    public void setEnd(IntVar end) {
        this.end = end;
    }


    private class TaskMonitorEnum implements IVariableMonitor<IntVar> {

        IntVar S, D, E;

        public TaskMonitorEnum(IntVar S, IntVar D, IntVar E) {
            this.S = S;
            this.D = D;
            this.E = E;
            S.addMonitor(this);
            D.addMonitor(this);
            E.addMonitor(this);
        }

        @Override
        public void onUpdate(IntVar var, IEventType evt) throws ContradictionException {
            boolean fixpoint = true;
            while (fixpoint) {
                // start
                fixpoint = S.updateLowerBound(E.getLB() - D.getUB(), this);
                fixpoint |= S.updateUpperBound(E.getUB() - D.getLB(), this);
                // end
                fixpoint |= E.updateLowerBound(S.getLB() + D.getLB(), this);
                fixpoint |= E.updateUpperBound(S.getUB() + D.getUB(), this);
                // duration
                fixpoint |= D.updateLowerBound(E.getLB() - S.getUB(), this);
                fixpoint |= D.updateUpperBound(E.getUB() - S.getLB(), this);
            }
        }

        @Override
        public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
            boolean nrules = false;
            if (var == S) {
                if (evt == IntEventType.INCLOW) {
                    nrules = ruleStore.addLowerBoundRule(E);
                    nrules|=ruleStore.addUpperBoundRule(D);
                } else if (evt == IntEventType.DECUPP) {
                    nrules = ruleStore.addUpperBoundRule(E);
                    nrules|=ruleStore.addLowerBoundRule(D);
                } else {
                    throw new SolverException("TaskMonitor exception");
                }
            } else if (var == E) {
                if (evt == IntEventType.INCLOW) {
                    nrules = ruleStore.addLowerBoundRule(S);
                    nrules|=ruleStore.addLowerBoundRule(D);
                } else if (evt == IntEventType.DECUPP) {
                    nrules = ruleStore.addUpperBoundRule(S);
                    nrules|=ruleStore.addUpperBoundRule(D);
                } else {
                    throw new SolverException("TaskMonitor exception");
                }
            } else if (var == D) {
                if (evt == IntEventType.INCLOW) {
                    nrules = ruleStore.addLowerBoundRule(E);
                    nrules|=ruleStore.addUpperBoundRule(S);
                } else if (evt == IntEventType.DECUPP) {
                    nrules = ruleStore.addLowerBoundRule(S);
                    nrules|=ruleStore.addUpperBoundRule(E);
                } else {
                    throw new SolverException("TaskMonitor exception");
                }
            }
            return nrules;
        }
    }

    private class TaskMonitorBound implements IVariableMonitor<IntVar> {

        IntVar S, D, E;

        public TaskMonitorBound(IntVar S, IntVar D, IntVar E) {
            this.S = S;
            this.D = D;
            this.E = E;

            S.addMonitor(this);
            D.addMonitor(this);
            E.addMonitor(this);
        }

        @Override
        public void onUpdate(IntVar var, IEventType evt) throws ContradictionException {
            // start
            S.updateBounds(E.getLB() - D.getUB(), E.getUB() - D.getLB(), this);
            // end
            E.updateBounds(S.getLB() + D.getLB(), S.getUB() + D.getUB(), this);
            // duration
            D.updateBounds(E.getLB() - S.getUB(), E.getUB() - S.getLB(), this);
        }

        @Override
        public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
            boolean nrules = false;
            if (var == S) {
                if (evt == IntEventType.INCLOW) {
                    nrules = ruleStore.addLowerBoundRule(E);
                    nrules|=ruleStore.addUpperBoundRule(D);
                } else if (evt == IntEventType.DECUPP) {
                    nrules = ruleStore.addUpperBoundRule(E);
                    nrules|=ruleStore.addLowerBoundRule(D);
                } else {
                    throw new SolverException("TaskMonitor exception");
                }
            } else if (var == E) {
                if (evt == IntEventType.INCLOW) {
                    nrules = ruleStore.addLowerBoundRule(S);
                    nrules|=ruleStore.addLowerBoundRule(D);
                } else if (evt == IntEventType.DECUPP) {
                    nrules = ruleStore.addUpperBoundRule(S);
                    nrules|=ruleStore.addUpperBoundRule(D);
                } else {
                    throw new SolverException("TaskMonitor exception");
                }
            } else if (var == D) {
                if (evt == IntEventType.INCLOW) {
                    nrules = ruleStore.addLowerBoundRule(E);
                    nrules|=ruleStore.addUpperBoundRule(S);
                } else if (evt == IntEventType.DECUPP) {
                    nrules = ruleStore.addLowerBoundRule(S);
                    nrules|=ruleStore.addUpperBoundRule(E);
                } else {
                    throw new SolverException("TaskMonitor exception");
                }
            }
            return nrules;
        }
    }
}
