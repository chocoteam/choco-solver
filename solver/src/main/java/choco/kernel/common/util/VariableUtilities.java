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
package choco.kernel.common.util;

import solver.variables.BoolVar;
import solver.variables.IntVar;

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
        LinkedHashSet<BoolVar> nonRedundantBs = new LinkedHashSet<BoolVar>();
        for (int j = 0; j < v.length; j++) {
            if (!nonRedundantBs.contains(v[j])) {
                nonRedundantBs.add(v[j]);
            }
        }
        return nonRedundantBs.toArray(new BoolVar[nonRedundantBs.size()]);
    }

    public static IntVar[] nonReundantVars(IntVar[] v) {
        LinkedHashSet<IntVar> nonRedundantBs = new LinkedHashSet<IntVar>();
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
}
