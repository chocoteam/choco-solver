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
package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Define a COUNT constraint setting size{forall v in lvars | v = occval} <= or >= or = occVar
 * assumes the occVar variable to be the last of the variables of the constraint:
 * vars = [lvars | occVar]
 * with  lvars = list of variables for which the occurence of occval in their domain is constrained
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class PropCount extends Propagator<IntVar> {

    /**
     * Store the number of variables which can still take the occurence value
     */
    public final IStateInt nbPossible;

    /**
     * Store the number of variables which are instantiated to the occurence value
     */
    public final IStateInt nbSure;

    public final boolean constrainOnInfNumber;    // >=
    public final boolean constrainOnSupNumber;    // <=

    //a table of variables that contain the occurrence value in their
    //initial domain.
    public final IntVar[] relevantVar;

    public int nbListVars;

    private final int occval;

    private final int ovIdx;

    protected final RemProc rem_proc;

    /**
     * Constructor,
     * Define an occurence constraint setting size{forall v in lvars | v = occval} <= or >= or = occVar
     * assumes the occVar variable to be the last of the variables of the constraint:
     * vars = [lvars | occVar]
     * with  lvars = list of variables for which the occurence of occval in their domain is constrained
     *
     * @param value checking value
     * @param vars   variables -- last one is LIMIT
     * @param onInf  if true, constraint insures size{forall v in lvars | v = occval} <= occVar
     * @param onSup  if true, constraint insure size{forall v in lvars | v = occval} >= occVar
     */
    public PropCount(int value, IntVar[] vars, boolean onInf, boolean onSup, Solver solver,
                     Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(vars, solver, intVarPropagatorConstraint, PropagatorPriority.LINEAR, false);
        this.occval = value;
        this.ovIdx = vars.length - 1;
        this.constrainOnInfNumber = onInf;
        this.constrainOnSupNumber = onSup;
        this.nbListVars = ovIdx;
        nbPossible = environment.makeInt(0);
        nbSure = environment.makeInt(0);
        int cpt = 0;
        for (int i = 0; i < ovIdx; i++) {
            if (vars[i].contains(this.occval)) {
                nbPossible.add(1);
                cpt++;
            }
        }
        relevantVar = new IntVar[cpt];
        cpt = 0;
        for (int i = 0; i < ovIdx; i++) {
            if (vars[i].contains(this.occval)) {
                relevantVar[cpt] = vars[i];
                cpt++;
            }
        }
        rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == vars.length-1) {
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
        } else {
            return EventType.INT_ALL_MASK();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nbSure = 0, nbPossible = 0;
        for (int i = 0; i < (nbListVars); i++) {
            if (vars[i].contains(occval)) {
                nbPossible++;
                if (vars[i].instantiatedTo(occval)) {
                    nbSure++;
                }
            }
        }

        this.nbSure.set(nbSure);
        this.nbPossible.set(nbPossible);
        checkNbPossible();
        checkNbSure();

    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int vIdx, int mask) throws ContradictionException {
        if (vIdx == ovIdx) {
            if (EventType.isInstantiate(mask) || EventType.isInclow(mask)) {
                //assumption : we only get the bounds events on the occurrence variable
                checkNbPossible();
            }
            if (EventType.isInstantiate(mask) || EventType.isDecupp(mask)) {
                //assumption : we only get the bounds events on the occurrence variable
                checkNbSure();
            }
        } else {
            if (EventType.isInstantiate(mask)) {
                //assumption : we only get the inst events on all variables except the occurrence variable
                if (vars[vIdx].getValue() == occval) {
                    nbSure.add(1);
                    checkNbSure();
                }
            }
            //assumption : we only get the inst events on all variables except the occurrence variable
            eventRecorder.getDeltaMonitor(vars[vIdx]).forEach(rem_proc.set(vIdx), EventType.REMOVE);
            checkNbPossible();
        }

    }

    @Override
    public ESat isEntailed() {
        int nbPos = 0;
        int nbSur = 0;
        for (int i = 0; i < vars.length-1; i++) {
            if (vars[i].contains(occval)) {
                nbPos++;
                if (vars[i].instantiated() && vars[i].getValue() == occval)
                    nbSur++;
            }
        }
        if (constrainOnInfNumber && constrainOnSupNumber) {
            if (vars[nbListVars].instantiated()) {
                if (nbPos == nbSur && nbPos == vars[nbListVars].getValue())
                    return ESat.TRUE;
            } else {
                if (nbPos < vars[nbListVars].getLB() ||
                        nbSur > vars[nbListVars].getUB())
                    return ESat.FALSE;
            }
        } else if (constrainOnInfNumber) {
            if (nbPos >= vars[nbListVars].getUB())
                return ESat.TRUE;
            if (nbPos < vars[nbListVars].getLB())
                return ESat.FALSE;
        } else {
            if (nbPos <= vars[nbListVars].getLB())
                return ESat.TRUE;
            if (nbPos > vars[nbListVars].getUB())
                return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("occur([");
        for (int i = 0; i < vars.length - 2; i++) {
            s.append(vars[i]).append(",");
        }
        s.append(vars[vars.length - 2]).append("], ").append(occval).append(")");
        if (constrainOnInfNumber && constrainOnSupNumber)
            s.append(" = ");
        else if (constrainOnInfNumber)
            s.append(" >= ");
        else
            s.append(" <= ");
        s.append(vars[ovIdx]);
        return s.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void checkNbPossible() throws ContradictionException {
        if (constrainOnInfNumber) {
            vars[nbListVars].updateUpperBound(nbPossible.get(), this, true);
            if (vars[nbListVars].instantiatedTo(nbPossible.get())) {
                for (int i = 0; i < relevantVar.length; i++) {
                    //for (IntDomainVar aRelevantVar : relevantVar) {
                    IntVar aRelevantVar = relevantVar[i];
                    if (aRelevantVar.contains(occval) && !aRelevantVar.instantiated()) {
                        //nbSure.add(1); // must be dealed by the event listener not here !!
                        aRelevantVar.instantiateTo(occval,  this, true);
                    }
                }
            }
        }
    }

    public void checkNbSure() throws ContradictionException {
        if (constrainOnSupNumber) {
            vars[nbListVars].updateLowerBound(nbSure.get(), this, true);
            if (vars[nbListVars].instantiatedTo(nbSure.get())) {
                for (int i = 0; i < relevantVar.length; i++) {
//                for (IntDomainVar aRelevantVar : relevantVar) {
                    IntVar aRelevantVar = relevantVar[i];
                    if (aRelevantVar.contains(occval) && !aRelevantVar.instantiated()) {
                        //nbPossible.add(-1);
                        aRelevantVar.removeValue(occval,  this, true);
                    }
                }
            }
        }
    }

    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropCount p;
        private int idxVar;

        public RemProc(PropCount p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            if (i == p.occval) {
                p.nbPossible.add(-1);
            }
        }
    }

}
