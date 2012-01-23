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

package solver.constraints.nary;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.nary.PropNoSubtour;
import solver.variables.IntVar;
import solver.variables.Variable;
import java.util.BitSet;

/**
 * NoSubtour constraint (see Pesant or Caseau&Laburthe)
 * for making a hamiltonian circuit
 */
public class NoSubTours extends IntConstraint<IntVar> {

    public NoSubTours(IntVar[] vars, Solver solver) {
        super(vars, solver);
		setPropagators(new PropNoSubtour(vars, solver, this));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
		int n = vars.length;
		BitSet visited = new BitSet(n);
		int i = 0;
		int size = 1;
		while(size!=n){
			size++;
			i = tuple[i];
			if(visited.get(i)){
				return ESat.FALSE;
			}
			visited.set(i);
		}
		if(i==0){
			return ESat.TRUE;
		}else{
			return ESat.FALSE;
		}
    }

    @Override
    public ESat isSatisfied() {
        return isEntailed();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("NoSubTour({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            Variable var = vars[i];
            sb.append(var);
        }
        sb.append("})");
        return sb.toString();
    }
}
