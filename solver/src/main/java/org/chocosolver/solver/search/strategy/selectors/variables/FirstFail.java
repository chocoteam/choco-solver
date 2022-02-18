/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**
 * <b>First fail</b> variable selector.
 * It chooses the leftmost variable with the smallest domain (instantiated variables are ignored).
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 2 juil. 2010
 */
public class FirstFail implements VariableSelector<IntVar>, VariableEvaluator<IntVar> {

    private final IStateInt lastIdx; // index of the last non-instantiated variable

    /**
     * <b>First fail</b> variable selector.
     * @param model reference to the model (does not define the variable scope)
     */
    public FirstFail(Model model){
        lastIdx = model.getEnvironment().makeInt(0);
    }
    
    
    @Override
    public IntVar getVariable(IntVar[] variables) {
        IntVar smallVar = null;
        int smallDSize = Integer.MAX_VALUE;
        // get and update the index of the first uninstantiated variable
        int idx = lastIdx.get();
        while(idx < variables.length && variables[idx].isInstantiated()) {
            idx++;
        }
        lastIdx.set(idx);
        //search for the leftmost variable with smallest domain
        while(idx < variables.length) {
            final int dsize = variables[idx].getDomainSize();
            if (dsize < smallDSize && dsize > 1) {
             // the variable is candidate for having the smallest domain
             //and  the variable is not instantiated 
                smallVar = variables[idx];
                smallDSize = dsize;
                // cannot be smaller than a boolean domain 
                if(dsize == 2) {break;}
            }
            idx++;
        }
        return smallVar;
    }
    
    @Override
    public double evaluate(IntVar variable) {
        return variable.getDomainSize();
    }
}
