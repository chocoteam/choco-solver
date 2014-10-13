/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 04/02/13
 * Time: 15:48
 */

package solver.variables;

import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.variables.events.IEventType;
import solver.variables.events.IntEventType;

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
            update = new IVariableMonitor() {
                @Override
                public void onUpdate(Variable var, IEventType evt) throws ContradictionException {
                    boolean fixpoint = true;
                    while (fixpoint) {
                        // start
                        fixpoint = start.updateLowerBound(end.getLB() - duration.getUB(), this);
                        fixpoint |= start.updateUpperBound(end.getUB() - duration.getLB(), this);
                        // end
                        fixpoint |= end.updateLowerBound(start.getLB() + duration.getLB(), this);
                        fixpoint |= end.updateUpperBound(start.getUB() + duration.getUB(), this);
                        // duration
                        fixpoint |= duration.updateLowerBound(end.getLB() - start.getUB(), this);
                        fixpoint |= duration.updateUpperBound(end.getUB() - start.getLB(), this);
                    }
                }

                @Override
                public void explain(Deduction d, Explanation e) {
                    throw new SolverException("A task cannot explain itself yet.");
                }

            };
        } else {
            update = new IVariableMonitor() {
                @Override
                public void onUpdate(Variable var, IEventType evt) throws ContradictionException {
                    // start
                    start.updateLowerBound(end.getLB() - duration.getUB(), this);
                    start.updateUpperBound(end.getUB() - duration.getLB(), this);
                    // end
                    end.updateLowerBound(start.getLB() + duration.getLB(), this);
                    end.updateUpperBound(start.getUB() + duration.getUB(), this);
                    // duration
                    duration.updateLowerBound(end.getLB() - start.getUB(), this);
                    duration.updateUpperBound(end.getUB() - start.getLB(), this);
                }

                @Override
                public void explain(Deduction d, Explanation e) {
                    throw new SolverException("A task cannot explain itself yet.");
                }

            };
        }
        start.addMonitor(update);
        duration.addMonitor(update);
        end.addMonitor(update);
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
}
