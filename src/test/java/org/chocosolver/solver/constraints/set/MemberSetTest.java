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
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;

import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 27/11/14
 */
public class MemberSetTest {

    private static final int[] facto;

    static {
        facto = new int[20];
        facto[0] = facto[1] = 1;
        for (int i = 2; i < 20; i++) {
            facto[i] = i * facto[i - 1];
        }
    }


    private int sizeInterseaction(int[] d1, int[] d2) {
        int count = 0;
        int comb = 1;
        int size = d1.length - 1;
        for (int i = 0; i < size; i++) {
            comb += (facto[size] / (facto[i] * facto[size - i]));
        }
        for (int i = 0; i < d1.length; i++) {
            for (int j = 0; j < d2.length; j++) {
                if (d1[i] == d2[j]) count++;
            }
        }
        return count * comb;
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL253_enum() throws NoSuchFieldException, ContradictionException, IllegalAccessException {
        // Issue #253
        Random random = new Random();
        int[][] doms;
        for (int k = 0; k < 20; k++) {
            random.setSeed(k);
            for (int d = 0; d < 11; d++) {
                for (int h = 0; h < 2; h++) {
                    doms = buildFullDomains(2, -4, 5, random, d / 10.d, h == 0);
                    Model model = new Model();
                    SetVar s = model.setVar("s", new int[]{}, doms[0]);
                    IntVar i = model.intVar("i", doms[1]);
                    model.member(i, s).post();
                    //IOutputFactory.showSolutions(solver);
                    long nbSolutions = 0;
                    while (model.solve()) {
                        nbSolutions++;
                    }
                    assertEquals(nbSolutions, sizeInterseaction(doms[0], doms[1]), Arrays.toString(doms[0]) + " - " + Arrays.toString(doms[1]));
                }
            }
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testJL253_bound() throws NoSuchFieldException, ContradictionException, IllegalAccessException {
        // Issue #253
        Random random = new Random();
        int[][] doms;
        for (int k = 0; k < 20; k++) {
            random.setSeed(k);
            for (int d = 0; d < 11; d++) {
                for (int h = 0; h < 2; h++) {
                    doms = buildFullDomains(2, -4, 5, random, d / 10.d, h == 0);
                    // fill doms[1]
                    int lb = doms[1][0];
                    int ub = doms[1][doms[1].length - 1];
                    doms[1] = new int[ub - lb + 1];
                    for (int j = 0; j < doms[1].length; j++) {
                        doms[1][j] = lb + j;
                    }
                    Model model = new Model();
                    SetVar s = model.setVar("s", new int[]{}, doms[0]);
                    IntVar i = model.intVar("i", doms[1][0], doms[1][doms[1].length - 1], true);
                    model.member(i, s).post();
                    //IOutputFactory.showSolutions(solver);
                    long nbSolutions = 0;
                    while (model.solve()) {
                        nbSolutions++;
                    }
                    assertEquals(nbSolutions, sizeInterseaction(doms[0], doms[1]), Arrays.toString(doms[0]) + " - " + Arrays.toString(doms[1]));
                }
            }
        }
    }
}
