/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.memory.IStateBitSet;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
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
    public final IStateBitSet nbPossible;

    /**
     * Store the number of variables which are instantiated to the occurence value
     */
    public final IStateBitSet nbSure;

    public final boolean constrainOnInfNumber;    // >=
    public final boolean constrainOnSupNumber;    // <=

    public int nbListVars;

    private final int occval;

    private final int ovIdx;

    /**
     * Constructor,
     * Define an occurence constraint setting size{forall v in lvars | v = occval} <= or >= or = occVar
     * assumes the occVar variable to be the last of the variables of the constraint:
     * vars = [lvars | occVar]
     * with  lvars = list of variables for which the occurence of occval in their domain is constrained
     *
     * @param value checking value
     * @param vars  variables -- last one is LIMIT
     * @param onInf if true, constraint insures size{forall v in lvars | v = occval} <= occVar
     * @param onSup if true, constraint insure size{forall v in lvars | v = occval} >= occVar
     */
    public PropCount(int value, IntVar[] vars, boolean onInf, boolean onSup, Solver solver,
                     Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(vars, PropagatorPriority.LINEAR, false);
        this.occval = value;
        this.ovIdx = vars.length - 1;
        this.constrainOnInfNumber = onInf;
        this.constrainOnSupNumber = onSup;
        this.nbListVars = ovIdx;
        nbPossible = environment.makeBitSet(vars.length);
        nbSure = environment.makeBitSet(vars.length);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == vars.length - 1) {
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
        } else {
            return EventType.INT_ALL_MASK();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((EventType.FULL_PROPAGATION.mask & evtmask) != 0) {
            nbPossible.clear();
            nbSure.clear();
            for (int i = 0; i < (nbListVars); i++) {
                if (vars[i].contains(occval)) {
                    nbPossible.set(i);
                    if (vars[i].instantiatedTo(occval)) {
                        nbSure.set(i);
                    }
                }
            }
        }

        filter(true, 2);
    }

    protected void filter(boolean startWithPoss, int nbRules) throws ContradictionException {
        boolean run;
        int nbR = 0;
        do {
            if (startWithPoss) {
                run = checkNbPossible();
            } else {
                run = checkNbSure();
            }
            startWithPoss ^= true;
            nbR++;
        } while (run || nbR < nbRules);
    }

    @Override
    public void propagate(int vIdx, int mask) throws ContradictionException {
        if (vIdx == ovIdx) {
            if (EventType.isInstantiate(mask) || EventType.isInclow(mask)) {
                //assumption : we only get the bounds events on the occurrence variable
                filter(true, 1);
            }
            if (EventType.isInstantiate(mask) || EventType.isDecupp(mask)) {
                //assumption : we only get the bounds events on the occurrence variable
                filter(false, 1);
            }
        } else {
            int nbRule = 1;
            if (EventType.isInstantiate(mask)) {
                //assumption : we only get the inst events on all variables except the occurrence variable
                if (vars[vIdx].getValue() == occval) {
                    nbSure.set(vIdx);
                    nbRule++;
                }
            }
            //assumption : we only get the inst events on all variables except the occurrence variable
            if (nbPossible.get(vIdx) && !vars[vIdx].contains(occval)) {
                nbPossible.clear(vIdx);
            }
            filter(true, nbRule);
        }

    }

    @Override
    public ESat isEntailed() {
        int nbPos = 0;
        int nbSur = 0;
        for (int i = 0; i < vars.length - 1; i++) {
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

    public boolean checkNbPossible() throws ContradictionException {
        boolean hasChanged = false;
        if (constrainOnInfNumber) {
            int card = nbPossible.cardinality();
            hasChanged = vars[nbListVars].updateUpperBound(card, aCause);
            if (vars[nbListVars].instantiatedTo(card)) {
                for (int i = nbPossible.nextSetBit(0); i >= 0; i = nbPossible.nextSetBit(i + 1)) {
                    if (/*vars[i].contains(occval) && */!vars[i].instantiated()) {
                        hasChanged = true;
                        nbSure.set(i);
                        vars[i].instantiateTo(occval, aCause);
                    }
                }
            }
        }
        return hasChanged;
    }

    public boolean checkNbSure() throws ContradictionException {
        boolean hasChanged = false;
        if (constrainOnSupNumber) {
            int sure = nbSure.cardinality();
            hasChanged = vars[nbListVars].updateLowerBound(sure, aCause);
            if (vars[nbListVars].instantiatedTo(sure)) {
                for (int i = nbPossible.nextSetBit(0); i >= 0; i = nbPossible.nextSetBit(i + 1)) {
                    if (/*aRelevantVar.contains(occval) && */!vars[i].instantiated()) {
                        if (vars[i].removeValue(occval, aCause)) {
                            nbPossible.clear(i);
                            hasChanged = true;
                        }
                    }
                }
            }
        }
        return hasChanged;
    }
}
