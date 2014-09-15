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
package solver.constraints.reification;

import memory.structure.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.Variable;
import util.ESat;

/**
 * A specific propagator which posts constraint on condition.
 * <br/>
 * The user gives the condition, then <br/>
 * if the condition returns ESat.TRUE, then posts temporarily the first propagator,<br/>
 * if the condition returns Esat.FALSE, then it posts temporarily the second propagator,<br/>
 * Otherwise wait for the condition to be fully (un)satisfied.
 *
 * @author Charles Prud'homme
 * @since 06/02/2014
 */
public abstract class PropConditionnal extends Propagator<Variable> {
    private static Logger LOGGER = LoggerFactory.getLogger("propagator");

    Constraint[] condTrue;
    Constraint[] condFalse;

    /**
     * @param vars2observe set of variables to observe, their modifications triggers the condition checking
     * @param condTrue     the constraint to post if the condition is satisfied
     * @param condFalse    the constraint to post if the condition is not satisfied
     */
    public PropConditionnal(Variable[] vars2observe, Constraint[] condTrue, Constraint[] condFalse) {
        super(vars2observe, PropagatorPriority.VERY_SLOW, false);
        this.condTrue = condTrue.clone();
        this.condFalse = condFalse.clone();
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ESat condition = checkCondition();
        if (condition == ESat.TRUE) {
            setPassive();
            for (Constraint cstr : condTrue) {
                postTemp(cstr);
            }
        } else if (condition == ESat.FALSE) {
            setPassive();
            for (Constraint cstr : condFalse) {
                postTemp(cstr);
            }
        }
    }

    private void postTemp(final Constraint c) throws ContradictionException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("post " + c.toString());
        }
        solver.postTemp(c);
        // the constraint has been added during the resolution.
        // it should be removed on backtrack -> create a new undo operation
        solver.getEnvironment().save(new Operation() {
            @Override
            public void undo() {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("unpost " + c.toString());
                }
                solver.unpost(c);
            }
        });
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    /**
     * Check a specific condition.<br/>
     * If the condition returns ESat.TRUE, then posts temporarily the first propagator,<br/>
     * If the condition returns Esat.FALSE, then it posts temporarily the second propagator,<br/>
     * Otherwise wait for the condition to be fully (un)satisfied.
     *
     * @return Esat
     */
    public abstract ESat checkCondition();
}
