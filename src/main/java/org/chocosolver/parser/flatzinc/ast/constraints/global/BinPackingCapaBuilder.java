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

package org.chocosolver.parser.flatzinc.ast.constraints.global;

import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.constraints.IBuilder;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.chocosolver.parser.flatzinc.ast.expression.Expression;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * <br/>
 * bin_packing_capa(array[int] of int: c, array[int] of var int: bin, array[int] of int: w)
 *
 * @author Jean-Guillaume Fages
 * @since 04/06/2013
 */
public class BinPackingCapaBuilder implements IBuilder {

    @Override
    public void build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
        int[] c = exps.get(0).toIntArray();
        IntVar[] item_bin = exps.get(1).toIntVarArray(solver);
        int[] item_size = exps.get(2).toIntArray();
        LinkedList<Constraint> addCons = new LinkedList<>();
        for (int i = 0; i < item_bin.length; i++) {
            if (item_bin[i].getLB() < 1) {
                addCons.add(ICF.arithm(item_bin[i], ">=", 1));
            }
            if (item_bin[i].getUB() > c.length) {
                addCons.add(ICF.arithm(item_bin[i], "<=", c.length));
            }
        }
        IntVar[] loads = new IntVar[c.length];
        for (int i = 0; i < c.length; i++) {
            loads[i] = VF.bounded("load_" + i, 0, c[i], solver);
        }
        if (addCons.size() > 0) {
            solver.post(ArrayUtils.append(ICF.bin_packing(item_bin, item_size, loads, 1), (Constraint[]) addCons.toArray()));
        } else {
            solver.post(ICF.bin_packing(item_bin, item_size, loads, 1));
        }
    }
}
