/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.util.ESat;

/**
 * A constraint to deal with constructive disjunction
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 26/01/2016.
 */
public class ConDisConstraint extends Constraint{

    /**
     * Unique constructive disjunction propagator
     */
    private final PropConDis condissol;

    /**
     * A constraint to deal with constructive disjunction (unique in a model instance)
     * @param model declaring model
     */
    public ConDisConstraint(Model model) {
        super("CondisConstraint",new PropConDis(model));
        condissol = (PropConDis) propagators[0];
    }

    @Override
    public ESat isSatisfied() {
        ESat so = ESat.UNDEFINED;
        for (Propagator propagator : propagators) {
            so = propagator.isEntailed();
            if (!so.equals(ESat.TRUE)) {
                return so;
            }
        }
        return so;
    }

    public PropConDis getPropCondis() {
        return condissol;
    }

}
