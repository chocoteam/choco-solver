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
package org.chocosolver.util;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/08/11
 */
public enum VariableUtilities {
    ;

    public static BoolVar[] nonReundantVars(BoolVar[] v) {
        LinkedHashSet<BoolVar> nonRedundantBs = new LinkedHashSet<>();
        for (int j = 0; j < v.length; j++) {
            if (!nonRedundantBs.contains(v[j])) {
                nonRedundantBs.add(v[j]);
            }
        }
        return nonRedundantBs.toArray(new BoolVar[nonRedundantBs.size()]);
    }

    public static IntVar[] nonReundantVars(IntVar[] v) {
        LinkedHashSet<IntVar> nonRedundantBs = new LinkedHashSet<>();
        for (int j = 0; j < v.length; j++) {
            if (!nonRedundantBs.contains(v[j])) {
                nonRedundantBs.add(v[j]);
            }
        }
        return nonRedundantBs.toArray(new IntVar[nonRedundantBs.size()]);
    }

    public static boolean emptyUnion(IntVar x, IntVar y) {
        if (x.getLB() <= y.getUB()
                && (y.getLB() <= x.getUB())) {
            if (!y.hasEnumeratedDomain() || !x.hasEnumeratedDomain()) {
                return false;
            } else {
                int ub = y.getUB();
                for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                    if (x.contains(val)) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return true;
        }
    }

    public static IntVar[] extractIntVar(Variable[] vars, boolean bool2) {
        ArrayList<IntVar> to = new ArrayList<IntVar>();
        for (int i = 0; i < vars.length; i++) {
            if ((vars[i].getTypeAndKind() & Variable.INT) != 0
                    || (bool2 && (vars[i].getTypeAndKind() & Variable.BOOL) != 0)) {
                to.add((IntVar) vars[i]);
            }
        }
        return to.toArray(new IntVar[to.size()]);
    }
}
