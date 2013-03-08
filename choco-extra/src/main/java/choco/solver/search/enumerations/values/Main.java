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

package choco.solver.search.enumerations.values;

public class Main {
    public static void main(String[] args) {
        // properties of the DSL
        // - do not loose value
        // - do not duplicate value
        // - do not invent value
        // - computation always terminates
        // - computation complexity does not depend on the domain size

        // - three kinds of operations:
        //   - unsplitter
        //   - sorter
        //   - splitter
        //   - a fourth kind? sizer

        //id(9).enumerate();
        //id(9).reverse().enumerate();
        //id(9).unconcat().get(0).enumerate();
        //id(9).unconcat().get(1).enumerate();
//		id(9).unconcat().reverse().concat().enumerate();
//		id(9).unconcat().applyReverseAt(1).concat().enumerate();
        // rotate left
//		id(9).split().reverse().concat().enumerate();
        // queens
        id(9).unconcat().applyReverseAt(0).zip().enumerate();
        //id(9).unconcat(4).mapReverse().concat().enumerate();
//		id(9).unzip().concat().enumerate();

        // TO DO:
        // - test (debug and limitation)
        // - true higher order (Map, ApplyAt, Repeat)
        // - memory for Sorting
        // - split (filter on a predicate)
        // - SortBy f1 f2 f3 ...
        // - random
        // - take a bit set as a parameter
        // - take the set of the solver as a parameter
        // - program choice Var ; choice Val
    }

    static ValueIterator<Integer> id(int n) {
        return new Id(n);
    }
}