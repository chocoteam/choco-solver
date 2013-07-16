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

package solver.constraints.nary.cnf;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.BoolVar;
import util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 nov. 2010
 */
public class SatConstraint extends Constraint<BoolVar, PropSat> {

    final PropSat miniSat;

    public SatConstraint(Solver solver) {
        super(solver);
        miniSat = new PropSat(solver);
        setPropagators(miniSat);

    }

    @Override
    public ESat isSatisfied() {
        ESat so = ESat.UNDEFINED;
        for (int i = 0; i < propagators.length; i++) {
            so = propagators[i].isEntailed();
            if (!so.equals(ESat.TRUE)) {
                return so;
            }
        }
        return so;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append('(');
        for (int p = 0; p < propagators.length; p++) {
            st.append(propagators[p].toString()).append(") and (");
        }
        st.replace(st.length() - 6, st.length(), "");
        return st.toString();
    }

    public SatSolver getSatSolver() {
        return propagators[0].getSatSolver();
    }
}
