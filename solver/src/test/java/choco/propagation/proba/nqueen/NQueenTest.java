/**
*  Copyright (c) 2010, Ecole des Mines de Nantes
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

package choco.propagation.proba.nqueen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.SearchLoops;

import static choco.propagation.proba.nqueen.NQueenModeler.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
public class NQueenTest {
    private static final Logger log = LoggerFactory.getLogger("test");


    public final static int NB_QUEENS_SOLUTION[] = {0, 0, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712, 365596};


    private int peType; // propagation type default value

    private int piType; // pilot type default value

    private int slType; // search loop type default value

    private int size;

    public NQueenTest() {
        this.peType = 0;
        this.piType = 0;
        this.slType = 0;
        this.size = 10;
    }

    public NQueenTest(int peType, int piType, int slType, int size) {
        this.peType = peType;
        this.piType = piType;
        this.slType = slType;
        this.size = size;
    }

    @BeforeTest(alwaysRun = true)
    private void beforeTest() {
        SearchLoops._DEFAULT = slType;
    }


    private int[][] buildFullDomains(int i) {
        int[][] domains = new int[i][i];
        for (int j = 0; j < i; j++) {
            for (int k = 0; k < i; k++) {
                domains[j][k] = k + 1;
            }
        }
        return domains;
    }

    private String parameters(){
        StringBuilder st = new StringBuilder();
        st.append("(s:").append(size);
        st.append(" pe:").append(peType);
        st.append(" pi:").append(piType);
        st.append(" sl:").append(slType);
        st.append(")");
        return st.toString();
    }

    private void assertIt(Solver s) {
        Assert.assertEquals(s.getMeasures().getSolutionCount(), NB_QUEENS_SOLUTION[size], "nb sol incorrect");
    }


    @Test(groups = "10m")
    public void testGlobalBinary() {
        log.info("modelBinaryGlobal {}", parameters());
        int[][] domains = buildFullDomains(size);
        Solver s = modelBinaryGlobal(size, domains);
        s.findAllSolutions();
        assertIt(s);
    }

    @Test(groups = "10m")
    public void testGlobal() throws ContradictionException {
        log.info("modelGlobal {}", parameters());
        int[][] domains = buildFullDomains(size);
        Solver s = modelGlobal(size, domains);
        s.findAllSolutions();
        assertIt(s);
    }

    @Test(groups = "10m")
    public void testGlobal2() {
        log.info("modelGlobal2 {}", parameters());
        int[][] domains = buildFullDomains(size);
        Solver s = modelGlobal2(size, domains);
        s.findAllSolutions();
        assertIt(s);
    }


    @Test(groups = "10m")
    public void testDualGlobal() {
        log.info("modelDualGlobal {}", parameters());
        int[][] domains = buildFullDomains(size);
        Solver s = modelDualGlobal(size, domains);
        s.findAllSolutions();
        assertIt(s);
    }


}
