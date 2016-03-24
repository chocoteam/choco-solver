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
import org.chocosolver.solver.Model;

import org.chocosolver.solver.variables.IntVar;


import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * <br/>
 * bin_packing(int: c, array[int] of var int: bin, array[int] of int: w)
 * @author Jean-Guillaume Fages
 * @since 04/06/2013
 */
public class BinPackingBuilder implements IBuilder {

    @Override
    public void build(Model model, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
		int c = exps.get(0).intValue();
		IntVar[] item_bin = exps.get(1).toIntVarArray(model);
		int[] item_size = exps.get(2).toIntArray();
		int min = MAX_VALUE / 2;
		int max = MIN_VALUE / 2;
		for (int i = 0; i < item_bin.length; i++) {
			min = min(min, item_bin[i].getLB());
			max = max(max, item_bin[i].getUB());
		}
		IntVar[] loads = model.intVarArray("TMPload", max - min + 1, 0, c, true);
		model.binPacking(item_bin, item_size, loads, min).post();
	}
}
