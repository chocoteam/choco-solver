/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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

package solver.constraints.extension.nary;

import gnu.trove.map.hash.TObjectIntHashMap;
import solver.variables.IntVar;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A simple class that provides a method to check if a given
 * tuple is valid i.e. if it is ok regarding the current domain
 * of the variables
 */
public class ValidityChecker implements Comparator<IntVar> {

    //variables sorted from the minimum domain to the max
    protected IntVar[] sortedvs;
    public int[] position;
    protected TObjectIntHashMap<IntVar> mapinit;
    protected int arity;

    public ValidityChecker(int ari, IntVar[] vars) {
        arity = ari;
        sortedvs = new IntVar[arity];
        mapinit = new TObjectIntHashMap<>(arity);
        position = new int[arity];
        for (int i = 0; i < vars.length; i++) {
            sortedvs[i] = vars[i];
            mapinit.put(vars[i], i);
            position[i] = i;
        }
    }

    public final int getPosition(int idx) {
        return position[idx];
    }

    /**
     * Sort the variable to speedup the check
     */
    public void sortvars() {
        Arrays.sort(sortedvs, this);
        for (int i = 0; i < arity; i++) {
            position[i] = mapinit.get(sortedvs[i]);
        }
    }

    // Is tuple valide ?
    public boolean isValid(int[] tuple) {
        for (int i = 0; i < arity; i++)
            if (!sortedvs[i].contains(tuple[position[i]])) return false;
        return true;
    }

    public boolean isValid(int[] tuple, int i) {
        return sortedvs[i].contains(tuple[position[i]]);
    }

    /**
     * Sort the variables by domain size
     */
    public int compare(IntVar o, IntVar o1) {
        return o.getDomainSize() - o1.getDomainSize();
    }


}