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
package solver.constraints.nary.globalcardinality;

import common.ESat;
import common.util.tools.ArrayUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.propagators.nary.globalcardinality.PropFastGCC;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Global Cardinality constraint
 *
 * @author Hadrien Cambazard, Charles Prud'homme, Jean-Guillaume Fages
 * @since 16/06/11
 */
public class GlobalCardinality extends IntConstraint<IntVar> {

    private final int nbvars;
    private final int[] values;
    private final TIntIntHashMap map;

    public GlobalCardinality(IntVar[] vars, int[] values, IntVar[] cards, Solver solver) {
        super(ArrayUtils.append(vars, cards), solver);
        assert values.length == cards.length;
        this.nbvars = vars.length;
        this.map = new TIntIntHashMap();
        this.values = values;
        int idx = 0;
        for (int v : values) {
            if (!map.containsKey(v)) {
                map.put(v, idx);
                idx++;
            } else {
                throw new UnsupportedOperationException("ERROR: multiple occurrences of value: " + v);
            }
        }
        this.setPropagators(new PropFastGCC(vars, values, map, cards));
    }

    public static Constraint[] reformulate(IntVar[] vars, IntVar[] card, Solver solver) {
        List<Constraint> cstrs = new ArrayList<Constraint>();
        for (int i = 0; i < card.length; i++) {
            IntVar cste = VariableFactory.fixed(i, solver);
            BoolVar[] bs = VariableFactory.boolArray("b_" + i, vars.length, solver);
            for (int j = 0; j < vars.length; j++) {
                cstrs.add(IntConstraintFactory.reified(bs[j], IntConstraintFactory.arithm(vars[j], "=", cste), IntConstraintFactory.arithm(vars[j], "!=", cste)));
            }
            cstrs.add(IntConstraintFactory.sum(bs, card[i]));
        }
        return cstrs.toArray(new Constraint[cstrs.size()]);
    }


    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("GlobalCardinality(<");
        buf.append(vars[0]);
        for (int i = 1; i < nbvars; i++) {
            buf.append(',').append(vars[i]);
        }
        buf.append(">,<");
        buf.append(":").append(vars[nbvars]);
        for (int i = 1; i < vars.length - nbvars; i++) {
            buf.append(',').append(i).append(":").append(vars[nbvars + i]);
        }
        buf.append(">)");
        return new String(buf);
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int n2 = values.length;
        int[] occ = new int[n2];
        for (int i = 0; i < nbvars; i++) {
            if (map.containsKey(tuple[i])) {
                occ[map.get(tuple[i])]++;
            }
        }
        for (int i = 0; i < n2; i++) {
            if (tuple[i + nbvars] != occ[i]) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }

    //	// need to fix propagators before using this
    //	public static enum Consistency {
    //		AC, AC_ON_CARDS, BC
    //	}
    //		switch (consistency) {
    //			case AC:
    //				setPropagators(new PropGCC_AC_Cards_Fast(vars, values, cards, this, solver));
    //				return;
    //			case AC_ON_CARDS:
    //				setPropagators(new PropGCC_AC_Cards_AC(vars, values, cards, this, solver));
    //				return;
    //			case BC:
    //			default:
    //				//CPRU  double to simulate idempotency
    //				setPropagators(new PropBoundGlobalCardinality(vars, cards, values[0], values[values.length - 1], solver, this),
    //						new PropBoundGlobalCardinality(vars, cards, values[0], values[values.length - 1], solver, this));
    //
    //		}
    //	/**
    //	 * Each values VALUES[i] should be taken exactly OCCURRENCES[i] variables of VARS.
    //	 * <br/>
    //	 * The level of consistency should be chosen among BC and AC.
    //	 * <p/>
    //	 * <b>BC</b>: ensures Bound Consistency,
    //	 * <br/><b>AC</b>: ensures Arc Consistency.
    //	 *
    //	 * @param VARS        collection of variables
    //	 * @param VALUES      collection of constrained values
    //	 * @param OCCURRENCES collection of cardinality variables
    //	 * @param CLOSED      restricts domains of VARS to VALUES if set to true
    //	 * @param CONSISTENCY consistency level, among {"BC", "AC"}
    //	 */
    //	public static GlobalCardinality global_cardinality(IntVar[] VARS, int[] VALUES, IntVar[] OCCURRENCES, boolean CLOSED,
    //													   String CONSISTENCY) {
    //		Solver solver = VARS[0].getSolver();
    //
    //		TIntObjectHashMap<IntVar> map = new TIntObjectHashMap<IntVar>(VALUES.length);
    //		for (int i = 0; i < VALUES.length; i++) {
    //			map.put(VALUES[i], OCCURRENCES[i]);
    //		}
    //
    //		int n = VARS.length;
    //		Arrays.sort(VALUES);
    //		int min = VALUES[0];
    //		int max = VALUES[VALUES.length - 1];
    //
    //		for (int v = 0; v < VARS.length; v++) {
    //			IntVar var = VARS[v];
    //			if (min > var.getLB()) {
    //				min = var.getLB();
    //			}
    //			if (max < var.getUB()) {
    //				max = var.getUB();
    //			}
    //		}
    //
    //		IntVar[] cards = new IntVar[max - min + 1];
    //		int[] values = new int[max - min + 1];
    //		for (int i = min; i <= max; i++) {
    //			values[i - min] = i;
    //			if (map.containsKey(i)) {
    //				cards[i - min] = map.get(i);
    //			} else {
    //				if (CLOSED) {
    //					cards[i - min] = VariableFactory.fixed(0, solver);
    //				} else {
    //					cards[i - min] = VariableFactory.bounded(StringUtils.randomName(), 0, n, solver);
    //				}
    //			}
    //		}
    //		return new GlobalCardinality(VARS, values, cards, GlobalCardinality.Consistency.valueOf(CONSISTENCY), solver);
    //
    //	}
}
