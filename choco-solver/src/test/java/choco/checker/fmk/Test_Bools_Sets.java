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

package org.chocosolver.choco.checker.fmk;

import org.chocosolver.solver.search.loop.SearchLoops;
import org.testng.annotations.Test;


/**
 * @author Jean-Guillaume Fages
 * @since 01/13
 */
public class Test_Bools_Sets {
    private SearchLoops slType; // search loop type default value

    public Test_Bools_Sets() {
        this.slType = SearchLoops.BINARY;
    }

    public Test_Bools_Sets(SearchLoops sl) {
        this.slType = slType;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test(groups = "10s")
    public void testBOOL_SUM() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                Correctness.checkCorrectness(Model.boolSum, n, -n / 2, 2 * n, seed, null);
            }
        }
    }

    @Test(groups = "1m")
    public void testSETS() {
        for (int i = 0; i < 10; i++) {
            long seed = System.currentTimeMillis();
            for (int n = 2; n < (1 << 5) + 1; n *= 2) {
                Correctness.checkCorrectness(Model.setUnion, n, -n / 2, 2 * n, seed, null);
                Correctness.checkCorrectness(Model.setInter, n, -n / 2, 2 * n, seed, null);
                Correctness.checkCorrectness(Model.setDisj, n, -n / 2, 2 * n, seed, null);
                Correctness.checkCorrectness(Model.setDiff, n, -n / 2, 2 * n, seed, null);
                Correctness.checkCorrectness(Model.setSubSet, n, -n / 2, 2 * n, seed, null);
                Correctness.checkCorrectness(Model.setAllEq, n, -n / 2, 2 * n, seed, null);
            }
        }
    }
}
