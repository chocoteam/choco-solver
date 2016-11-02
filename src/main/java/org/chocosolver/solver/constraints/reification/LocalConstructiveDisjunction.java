/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.reification;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 25/02/2016.
 */
public class LocalConstructiveDisjunction extends Constraint {
    /**
     * Make a new constraint defined as a set of given propagators
     *
     * @param constraints set of constraints in disjunction
     */
    public LocalConstructiveDisjunction(Constraint... constraints) {
        super("LocalConstructiveDisjunction", createProps(constraints));
    }

    private static Propagator[] createProps(Constraint... constraints) {
        Propagator<IntVar>[][] propagators = new Propagator[constraints.length][];
        TIntObjectHashMap<IntVar> map1 = new TIntObjectHashMap<>();
        for (int i = 0; i < constraints.length; i++) {
            propagators[i] = constraints[i].getPropagators().clone();
            for (int j = 0; j < propagators[i].length; j++) {
                Propagator<IntVar> prop = propagators[i][j];
                prop.setReifiedSilent();
                for (int k = 0; k < prop.getNbVars(); k++) {
                    map1.put(prop.getVar(k).getId(), prop.getVar(k));
                }
            }
        }
        int[] keys = map1.keys();
        Arrays.sort(keys);
        IntVar[] allvars = new IntVar[keys.length];
        int k = 0;
        for (int i = 0; i < keys.length; i++) {
            allvars[k++] = map1.get(keys[i]);
        }
        IntVar[] vars = Arrays.copyOf(allvars, k);
        assert vars.length > 0;
        return ArrayUtils.append(new Propagator[]{new PropLocalConDis(vars, propagators)},
                ArrayUtils.flatten(propagators));
    }

    @Override
    public ESat isSatisfied() {
        return propagators[0].isEntailed();
    }
}
