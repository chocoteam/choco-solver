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

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

/**
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 21/12/2015.
 */
class RelationFactory {


    /**
     * Make a large relation from <i>tuples</i> and an array of IntVar.
     * @param tuples list of tuples
     * @param vars array of IntVar
     * @return a large relation
     */
    public static LargeRelation makeLargeRelation(Tuples tuples, IntVar[] vars) {
        long totalSize = 1;
        for (int i = 0; i < vars.length && (int)totalSize == totalSize; i++) { // to prevent from long overflow
            totalSize *= vars[i].getRange();
        }
        if ((int)totalSize != totalSize) {
            return new TuplesVeryLargeTable(tuples, vars);
        }
        if (totalSize / 8 > 50 * 1024 * 1024) {
            return new TuplesLargeTable(tuples, vars);
        }
        return new TuplesTable(tuples, vars);
    }


    /**
     * Make iterable relation from <i>tuples</i> and an array of IntVar.
     * @param tuples list of tuples
     * @param vars array of IntVar
     * @return an iterable relation
     */
    public static IterTuplesTable makeIterableRelation(Tuples tuples, IntVar[] vars) {
        return new IterTuplesTable(tuples, vars);
    }

    /**
     * Make list-based relation from <i>tuples</i> and an array of IntVar.
     * @param tuples list of tuples
     * @param vars array of IntVar
     * @return a lsit-based relation
     */
    public static TuplesList makeListBasedRelation(Tuples tuples, IntVar[] vars) {
        return new TuplesList(tuples, vars);
    }

}
