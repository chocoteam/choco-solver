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

package org.chocosolver.solver.constraints.nary.nValue.amnv.rules;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffBC;
import org.chocosolver.solver.constraints.nary.nValue.amnv.mis.F;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

import java.util.BitSet;

/**
 * R4 filtering rule (AllDifferent propagation)
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class R4 implements R {

	private AlgoAllDiffBC filter;

	public void filter(IntVar[] vars, UndirectedGraph graph, F heur, Propagator aCause) throws ContradictionException{
		int n = vars.length-1;
		BitSet mis = heur.getMIS();
		if(mis.cardinality()==vars[n].getUB()){
			IntVar[] vs = new IntVar[mis.cardinality()];
			int idx = 0;
			for(int x=mis.nextSetBit(0);x>=0;x=mis.nextSetBit(x+1)){
				vs[idx++] = vars[x];
			}
			if(filter==null)filter=new AlgoAllDiffBC(aCause);
			filter.reset(vs);
			filter.filter();
		}
	}
}
