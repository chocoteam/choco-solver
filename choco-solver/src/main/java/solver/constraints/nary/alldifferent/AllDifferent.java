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

package solver.constraints.nary.alldifferent;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.binary.PropNotEqualX_Y;
import solver.variables.IntVar;
import solver.variables.Variable;
import util.ESat;

/**
 * Ensures that all variables from VARS take a different value.
 * The consistency level should be chosen among "BC", "AC" and "DEFAULT".
 */
public class AllDifferent extends Constraint<IntVar> {

    public static enum Type {
        AC, BC, weak_BC, NEQS, DEFAULT
    }

    public AllDifferent(IntVar[] vars, Solver solver) {
        this(vars, solver, Type.BC);
    }

    public AllDifferent(IntVar[] vars, Solver solver, Type type) {
        super(vars, solver);
        setPropagators(createPropagators(vars, type));
    }

    public static Propagator<IntVar>[] createPropagators(IntVar[] VARS, Type consistency) {
        switch (consistency) {
            case NEQS: {
                int s = VARS.length;
                int k = 0;
                Propagator[] props = new Propagator[(s * s - s) / 2];
                for (int i = 0; i < s - 1; i++) {
                    for (int j = i + 1; j < s; j++) {
                        props[k++] = new PropNotEqualX_Y(VARS[i], VARS[j]);
                    }
                }
                return props;
            }
            case AC:
                return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffAC_Fast(VARS)};
            case BC:
                return new Propagator[]{new PropAllDiffBC(VARS, false)};
            case weak_BC:
                return new Propagator[]{new PropAllDiffBC(VARS, true)};
            case DEFAULT:
            default:
                // adds a Probabilistic AC (only if at least some variables have an enumerated domain)
                boolean enumDom = false;
                for (int i = 0; i < VARS.length && !enumDom; i++) {
                    if (VARS[i].hasEnumeratedDomain()) {
                        enumDom = true;
                    }
                }
                if (enumDom) {
                    return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffBC(VARS, false), new PropAllDiffAC_adaptive(VARS, 0)};
                } else {
                    return new Propagator[]{new PropAllDiffInst(VARS), new PropAllDiffBC(VARS, false)};
                }
        }
    }

    @Override
    public ESat isEntailed() {
		boolean allInst = true;
        for (IntVar v : vars) {
            if (v.instantiated()) {
                int vv = v.getValue();
                for (IntVar w : vars) {
                    if (w != v && w.instantiated() && vv == w.getValue()) {
						return ESat.FALSE;
                    }
                }
            } else {
				allInst = false;
            }
        }
		if(allInst){
			return ESat.TRUE;
		}else{
			return ESat.UNDEFINED;
		}
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("AllDifferent({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            Variable var = vars[i];
            sb.append(var);
        }
        sb.append("})");
        return sb.toString();
    }
}
