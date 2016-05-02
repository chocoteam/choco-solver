/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;



/**
 * A simple class that provides a method to check if a given
 * tuple is valid i.e. if it is ok regarding the current domain
 * of the variables
 */
public class ValidityChecker implements IntComparator {

    //variables sorted from the minimum domain to the max
    protected IntVar[] vars;
    public int[] sortedidx;
    protected int arity;
    protected ArraySort sorter;

    public ValidityChecker(int ari, IntVar[] vars) {
        arity = ari;
        this.vars = new IntVar[arity];
        sortedidx = new int[arity];
        sorter = new ArraySort(arity, false, true);
        for (int i = 0; i < vars.length; i++) {
            this.vars[i] = vars[i];
            sortedidx[i] = i;
        }
    }

    public final int getPosition(int idx) {
        return idx;
    }

    /**
     * Sort the variable to speedup the check
     */
    public void sortvars() {
        for (int i = 0; i < arity; i++) {
            sortedidx[i] = i;
        }
        sorter.sort(sortedidx, arity, this);
    }

    // Is tuple valide ?
    public boolean isValid(int[] tuple) {
        for (int i = 0; i < arity; i++)
            if (!vars[sortedidx[i]].contains(tuple[sortedidx[i]])) return false;
        return true;
    }

    @Override
    public int compare(int i1, int i2) {
        return vars[sortedidx[i1]].getDomainSize() - vars[sortedidx[i2]].getDomainSize();
    }
}
