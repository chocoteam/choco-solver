/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package solver.constraints.nary.globalcardinality;

import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.constraints.Propagator;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;

import java.util.ArrayList;
import java.util.List;

/**
 * Global Cardinality constraint
 *
 * @author Hadrien Cambazard, Charles Prud'homme, Jean-Guillaume Fages
 * @since 16/06/11
 */
public class GlobalCardinality extends Constraint {

    public GlobalCardinality(IntVar[] vars, int[] values, IntVar[] cards) {
        super("GCC", createProp(vars, values, cards));
    }

	private static Propagator createProp(IntVar[] vars, int[] values, IntVar[] cards) {
		assert values.length == cards.length;
		TIntIntHashMap map = new TIntIntHashMap();
		int idx = 0;
		for (int v : values) {
			if (!map.containsKey(v)) {
				map.put(v, idx);
				idx++;
			} else {
				throw new UnsupportedOperationException("ERROR: multiple occurrences of value: " + v);
			}
		}
		return new PropFastGCC(vars, values, map, cards);
	}

    public static Constraint[] reformulate(IntVar[] vars, IntVar[] card, Solver solver) {
        List<Constraint> cstrs = new ArrayList<Constraint>();
        for (int i = 0; i < card.length; i++) {
            IntVar cste = VF.fixed(i, solver);
            BoolVar[] bs = VF.boolArray("b_" + i, vars.length, solver);
            for (int j = 0; j < vars.length; j++) {
                Constraint cs = LCF.ifThenElse(bs[j], ICF.arithm(vars[j], "=", cste), ICF.arithm(vars[j], "!=", cste));
                cstrs.add(cs);
            }
            cstrs.add(ICF.sum(bs, card[i]));
        }
        return cstrs.toArray(new Constraint[cstrs.size()]);
    }
}
