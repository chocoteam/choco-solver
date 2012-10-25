/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package parser.flatzinc.ast.ext;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.variables.Variable;

import java.util.ArrayList;

/**
 * A pair (var,prop)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/10/12
 */
public class Pair {

    public Variable var;
    public Propagator prop;
    public int idxVinP;

    public Pair(Variable var, Propagator prop, int idxVinP) {
        this.var = var;
        this.prop = prop;
        this.idxVinP = idxVinP;
    }

    public static ArrayList<Pair> populate(Solver solver) {
        ArrayList<Pair> pairs = new ArrayList<Pair>();
        Constraint[] cstrs = solver.getCstrs();
        for (int i = 0; i < cstrs.length; i++) {
            Constraint c = cstrs[i];
            Propagator[] props = c.propagators;
            for (int j = 0; j < props.length; j++) {
                Propagator prop = props[j];
                Variable[] vars = prop.getVars();
                for (int k = 0; k < vars.length; k++) {
                    pairs.add(new Pair(vars[i], props[j], k));
                }
            }
        }
        return pairs;
    }

    public static void remove(ArrayList<Pair> orig, ArrayList toRemove) {
        for (int i = 0; i < toRemove.size(); i++) {
            Object o = toRemove.get(i);
            if (o instanceof Pair) {
                orig.remove(o);
            } else {
                remove(orig, (ArrayList) o);
            }
        }
    }
}
