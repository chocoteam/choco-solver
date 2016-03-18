/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * @author Jean-Guillaume Fages
 * @since 03/04/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints.nary.nValue.amnv.differences;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.Variable;

/**
 * automatic detection of binary disequalities and allDifferent constraints
 */
public class AutoDiffDetection implements D {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    public static boolean DYNAMIC_ADDITIONS = false;

    private Variable[] scope;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AutoDiffDetection(Variable[] scope) {
        this.scope = scope;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean mustBeDifferent(int i1, int i2) {
        // automatic detection of binary disequalities and allDifferent constraints
        if (DYNAMIC_ADDITIONS || scope[i1].getEnvironment().getWorldIndex() <= 1) {
            for (Propagator p : scope[i1].getPropagators())
                if (p.isActive())
                    if (p.getClass().getName().contains("PropNotEqualX_Y") || p.getClass().getName().contains("PropAllDiff"))
                        for (Variable v : p.getVars())
                            if (v == scope[i2])
                                return true;
        }
        return false;
    }

}
