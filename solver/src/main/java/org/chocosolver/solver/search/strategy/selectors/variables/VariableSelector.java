/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.solver.variables.Variable;



/**
 * A variable selector specifies which variable should be selected at a fix point. It is based specifications
 * (ex: smallest domain, most constrained, etc.).
 * <br/> Basically, the variable selected should not be already instantiated to a singleton (although it is allowed).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public interface VariableSelector<V extends Variable>  {

    /**
     * Provides access to the current selected variable among {@code variables}.
     * If there is no variable left, return {@code null}.
     *
     * @return the current selected variable if any, {@code null} otherwise.
     */
    V getVariable(V[] variables);

}
