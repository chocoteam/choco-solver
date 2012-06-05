/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package solver.propagation.generator;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.propagation.IPropagationEngine;
import solver.propagation.generator.predicate.Predicate;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.recorders.fine.FineArcEventRecorder;
import solver.recorders.fine.FinePropEventRecorder;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static solver.propagation.generator.PrimitiveTools.validate;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/03/12
 */
public class PCons implements Generator<AbstractFineEventRecorder> {

    final List<AbstractFineEventRecorder> eventRecorders;

    public PCons(IPropagationEngine propagationEngine, Constraint... constraints) {
        this(propagationEngine, constraints, PArc.NOV);
    }

    public PCons(IPropagationEngine propagationEngine, Propagator... propagators) {
        this(propagationEngine, propagators, PArc.NOV);
    }

    public PCons(IPropagationEngine propagationEngine, Constraint[] constraints, Predicate[] validations) {
        super();
        eventRecorders = new ArrayList<AbstractFineEventRecorder>();
        if (constraints.length > 0) {
            Solver solver = constraints[0].getSolver();
            propagationEngine.prepareWM(solver);
            for (int i = 0; i < constraints.length; i++) {
                Propagator[] propagators = constraints[i].propagators;
                for (int j = 0; j < propagators.length; j++) {
                    Propagator propagator = propagators[j];
                    if (validations.length == 0 || validate(propagator, validations)) {
                        make(propagator, validations, propagationEngine, solver);
                    }
                }
            }
        }
    }

    public PCons(IPropagationEngine propagationEngine, Propagator[] propagators, Predicate[] validations) {
        super();
        eventRecorders = new ArrayList<AbstractFineEventRecorder>();
        if (propagators.length > 0) {
            Solver solver = propagators[0].getSolver();
            propagationEngine.prepareWM(solver);
            for (int j = 0; j < propagators.length; j++) {
                Propagator propagator = propagators[j];
                make(propagator, validations, propagationEngine, solver);
            }
        }
    }

    private void make(Propagator prop, Predicate[] validations, IPropagationEngine propagationEngine, Solver solver) {
        int pidx = prop.getId();
        Variable[] variables = prop.getVars().clone();
        int nbv = prop.getNbVars();
        int i = 0;
        int[] pindices = new int[nbv];
        for (int j = 0; j < nbv; j++) {
            Variable var = variables[j];
            int vidx = var.getId();
            if (propagationEngine.isMarked(vidx, pidx, j)) {
                if (validations.length == 0 || validate(var, validations)) {
                    propagationEngine.clearWatermark(vidx, pidx, j);
                    variables[i] = var;
                    pindices[i++] = j;
                }
            }
        }
        AbstractFineEventRecorder er = null;
        if (i == 1) { // in that case, there is only one variable, an Arc is a better alternative
            er = new FineArcEventRecorder(variables[0], prop, pindices[0], solver, propagationEngine);
        } else if (i < nbv) { // if some variables has been removed -- connectected previously
            er = new FinePropEventRecorder(Arrays.copyOfRange(variables, 0, i), prop,
                    Arrays.copyOfRange(pindices, 0, i), solver, propagationEngine);
        } else {
            er = new FinePropEventRecorder(variables, prop, pindices, solver, propagationEngine);
        }
        eventRecorders.add(er);
        propagationEngine.addEventRecorder(er);
    }

    @Override
    public AbstractFineEventRecorder[] getElements() {
        return eventRecorders.toArray(new AbstractFineEventRecorder[eventRecorders.size()]);
    }
}
