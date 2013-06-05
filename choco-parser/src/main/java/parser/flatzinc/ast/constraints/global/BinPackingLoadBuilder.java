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

package parser.flatzinc.ast.constraints.global;

import parser.flatzinc.ast.Datas;
import parser.flatzinc.ast.constraints.IBuilder;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.Expression;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.variables.IntVar;
import util.tools.ArrayUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * <br/>
 * bin_packing_load(array[int] of var int: load, array[int] of var int: bin, array[int] of int: w)
 * @author Jean-Guillaume Fages
 * @since 04/06/2013
 */
public class BinPackingLoadBuilder implements IBuilder {

    @Override
    public Constraint[] build(Solver solver, String name, List<Expression> exps, List<EAnnotation> annotations, Datas datas) {
		IntVar[] loads = exps.get(0).toIntVarArray(solver);
		IntVar[] item_bin = exps.get(1).toIntVarArray(solver);
		int[] item_size = exps.get(2).toIntArray();
		LinkedList<Constraint> addCons = new LinkedList();
		for(int i=0; i<item_bin.length; i++){
			if(item_bin[i].getLB()<1){
				addCons.add(ICF.arithm(item_bin[i],">=",1));
			}
			if(item_bin[i].getUB()>loads.length){
				addCons.add(ICF.arithm(item_bin[i],"<=",loads.length));
			}
		}
		if(addCons.size()>0){
			return ArrayUtils.append(ICF.bin_packing(item_bin, item_size, loads, 1), (Constraint[])addCons.toArray());
		}else{
			return ICF.bin_packing(item_bin, item_size, loads, 1);
		}
    }
}
