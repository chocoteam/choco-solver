/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;


import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;



/**
 * This interface describes services of smallest element which can act on variables.
 * As an example, propagator is a cause because it filters values from variable domain.
 * So do decision, objective manager, etc.
 * It has an impact on domain variables and so it can fails.
 * <p>
 *     Important: when the {@link ICause#why(org.chocosolver.solver.explanations.RuleStore, org.chocosolver.solver.variables.IntVar, org.chocosolver.solver.variables.events.IEventType, int)} method
 *     needs to evaluate the incoming event, one may be aware that in some cases (for instance, BoolVar), the original event can promoted.
 *     Hence, if a cause can only explain bound modifications, it should also either consider the INSTANTIATION or the strengthen mask.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 oct. 2010
 */
public interface ICause  {


    /**
     * Add new rules to the rule store
     *
     * @param ruleStore the rule store
     * @param var       the modified variable
     * @param evt       the undergoing event
     * @param value     the value (for REMOVE only)
     * @return true if at least one rule has been added to the rule store
     */
    default boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        throw new SolverException("Undefined why(...) method for " + this);
    }
}
