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
package solver.constraints.nary.among;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * GCCAT:
 * NVAR is the number of variables of the collection VARIABLES that take their value in VALUES.
 * <br/><a href="http://www.emn.fr/x-info/sdemasse/gccat/Camong.html">gccat among</a>
 * <br/>
 * Propagator :
 * C. Bessiere, E. Hebrard, B. Hnich, Z. Kiziltan, T. Walsh,
 * Among, common and disjoint Constraints
 * CP-2005
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class Among extends IntConstraint {

    private final int[] values;

    public Among(IntVar limit, IntVar[] vars, int[] values, Solver solver) {
        super(ArrayUtils.append(vars, new IntVar[]{limit}), solver);
        TIntHashSet setValues = new TIntHashSet(values);
        this.values = setValues.toArray();
        Arrays.sort(this.values);
        setPropagators(new PropAmongGAC(this.vars, values));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        TIntSet _values = new TIntHashSet(values);
        int nb = 0;
        for (int i = 0; i < tuple.length - 1; i++) {
            if (_values.contains(tuple[i])) {
                nb++;
            }
        }
        return ESat.eval(nb == tuple[tuple.length - 1]);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("AMONG([");
        for (int i = 0; i < vars.length - 2; i++) {
            s.append(vars[i]).append(",");
        }
        s.append(vars[vars.length - 2]).append("], ").append(Arrays.toString(values)).append(")");
        s.append(" = ");
        s.append(vars[vars.length - 1]);
        return s.toString();
    }
}
