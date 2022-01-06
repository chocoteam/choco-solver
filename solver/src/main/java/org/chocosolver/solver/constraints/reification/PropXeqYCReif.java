/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * A propagator dedicated to express in a compact way: (x = y + c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXeqYCReif extends Propagator<IntVar> {

    int cste;

    public PropXeqYCReif(IntVar x, IntVar y, int c, BoolVar b) {
        super(new IntVar[]{x, y, b}, PropagatorPriority.TERNARY, false);
        this.cste = c;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars[2].getLB() == 1) {
            if (vars[0].isInstantiated()) {
                vars[1].instantiateTo(vars[0].getValue() - cste, this);
                setPassive();
            } else if (vars[1].isInstantiated()) {
                vars[0].instantiateTo(vars[1].getValue() + cste, this);
                setPassive();
            }else{
                //noinspection StatementWithEmptyBody
                while (vars[0].updateLowerBound(vars[1].getLB() + cste, this) | vars[1].updateLowerBound(vars[0].getLB() - cste, this)) ;
                //noinspection StatementWithEmptyBody
                while (vars[0].updateUpperBound(vars[1].getUB() + cste, this) | vars[1].updateUpperBound(vars[0].getUB() - cste, this)) ;
                if(vars[0].hasEnumeratedDomain() && vars[1].hasEnumeratedDomain()){
                    int ub = vars[0].getUB();
                    for (int val = vars[0].getLB(); val <= ub; val = vars[0].nextValue(val)) {
                        if (!vars[1].contains(val - cste)) {
                            vars[0].removeValue(val, this);
                        }
                    }
                    ub = vars[1].getUB();
                    for (int val = vars[1].getLB(); val <= ub; val = vars[1].nextValue(val)) {
                        if (!vars[0].contains(val + cste)) {
                            vars[1].removeValue(val, this);
                        }
                    }
                }
            }
        } else if (vars[2].getUB() == 0) {
            if (vars[0].isInstantiated()) {
                if (vars[1].removeValue(vars[0].getValue() - cste, this) || !vars[1].contains(vars[0].getValue() - cste)) {
                    setPassive();
                }
            } else if (vars[1].isInstantiated()) {
                if (vars[0].removeValue(vars[1].getValue() + cste, this) || !vars[0].contains(vars[1].getValue() + cste)) {
                    setPassive();
                }
            } else if (vars[0].getUB() < (vars[1].getLB() + cste) || (vars[1].getUB() + cste) < vars[0].getLB()) {
                setPassive();
            }
        } else {
            if (vars[0].isInstantiated()) {
                if (vars[1].isInstantiated()) {
                    if (vars[0].getValue() == vars[1].getValue() + cste) {
                        vars[2].instantiateTo(1, this);
                    } else {
                        vars[2].instantiateTo(0, this);
                    }
                    setPassive();
                } else if (!vars[1].contains(vars[0].getValue() - cste)) {
                    vars[2].instantiateTo(0, this);
                    setPassive();
                }
            } else {
                if (vars[1].isInstantiated()) {
                    if (!vars[0].contains(vars[1].getValue() + cste)) {
                        vars[2].instantiateTo(0, this);
                        setPassive();
                    }
                } else {
                    if (vars[0].getLB() > vars[1].getUB() + cste
                            || vars[0].getUB() < vars[1].getLB() + cste) {
                        vars[2].instantiateTo(0, this);
                        setPassive();
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (vars[2].isInstantiatedTo(1)) {
                return ESat.eval(vars[0].getValue() == vars[1].getValue() + cste);
            } else {
                return ESat.eval(vars[0].getValue() != vars[1].getValue() + cste);
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        Propagator.defaultExplain(this, p, explanation);
    }

    @Override
    public String toString() {
        return "(" + vars[0].getName() + " = " + vars[1].getName() + (cste !=0?" + "+cste:"")+") <=> " + vars[2].getName();
    }
}
