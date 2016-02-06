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
package org.chocosolver.samples;

import org.chocosolver.samples.integer.Partition;
import org.chocosolver.solver.Model;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/03/11
 */
public class PartitionTest {

    protected Model modeler(int size) {
        Partition pb;
        pb = new Partition();
        pb.readArgs("-n", Integer.toString(size));
        pb.createSolver();
        pb.buildModel();
        pb.configureSearch();
        return pb.getModel();
    }

    @Test(groups="5m", timeOut=300000)
    public void test4to14() {
        int[] size = {8, 12, 16, 20, 24, 28};
        int[] sols = {1, 1, 7, 24, 296, 1443};
//        int[] nodes = {3, 22, 189, 1739, 17889, 189944};

        for (int i = 0; i < size.length; i++) {
            Model sol = modeler(size[i]);
            while (sol.solve()) ;
            assertEquals(sol.getMeasures().getSolutionCount(), sols[i]);
//            Assert.assertEquals(sol.getMeasures().getNodeCount(), nodes[i]);
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test16to32() {
        int[] size = {32, 36, 40, 44, 48, 52, 56, 60, 64};
        int[] sols = {1, 1, 1, 1, 1, 1, 1, 1, 1};
//        int[] nodes = {633, 760, 2250, 6331, 19832, 19592, 60477, 139296, 180302};

        for (int i = 0; i < size.length; i++) {
            Model sol = modeler(size[i]);
            sol.solve();
            Assert.assertEquals(sol.getMeasures().getSolutionCount(), sols[i]);
//            Assert.assertEquals(sol.getMeasures().getNodeCount(), nodes[i]);
        }
    }

}
