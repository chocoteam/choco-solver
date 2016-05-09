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
package org.chocosolver.solver.constraints.nary.allen;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;

import static org.chocosolver.solver.constraints.nary.allen.AllenRelationMats.*;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 11/01/2016.
 */
public class AllenRelationMatsTest {

    @Test(groups="1s")
    public void testExtrel(){
        for(int extrel = 1; extrel <= 13; extrel++){
            int intrel = 13 - extrel;
            int cintrel = converse[intrel];
            ai[] boi = sequence_o[intrel];
            ai[] bli = sequence_l[intrel];
            ai[] boj = sequence_o[cintrel];
            ai[] blj = sequence_l[cintrel];
            System.out.printf("%d, %d, %s, %s, %s, %s\n", intrel, cintrel,
                    Arrays.toString(boi), Arrays.toString(bli), Arrays.toString(boj), Arrays.toString(blj));
        }
    }



    @Test(groups = "5m", timeOut = 900000)
    public void testAll() {
        Random rnd = new Random();
        int[][] domains;
        for (int k = 0; k < 2_000_000; k++) {
            System.out.printf("Seed (%d)\n", k);
            rnd.setSeed(k);
            domains = DomainBuilder.buildFullDomains(4, 0, 15, rnd, rnd.nextDouble(), rnd.nextBoolean());
            Model model = new Model();
            IntVar[] vars = new IntVar[8];
            vars[0] = model.intVar("o_i", domains[0]);
            vars[1] = model.intVar("l_i", domains[1]);
            vars[2] = model.intVar("o_j", domains[2]);
            vars[3] = model.intVar("l_j", domains[3]);
            vars[4] = model.intVar("o2_i", domains[0]);
            vars[5] = model.intVar("l2_i", domains[1]);
            vars[6] = model.intVar("o2_j", domains[2]);
            vars[7] = model.intVar("l2_j", domains[3]);
            IntVar re = model.intVar("REL", 1, 13, false);
            IntVar re2 = model.intVar("REL", 1, 13, false);
            for (int i = 1; i < 14; i++) {
                System.out.printf("%d\n", i);
                model.getEnvironment().worldPush();
                try {
                    re.instantiateTo(i, Cause.Null);
                    re2.instantiateTo(i, Cause.Null);
                } catch (ContradictionException e1) {
                    e1.printStackTrace();
                }
                AllenRelationMats ar = new AllenRelationMats(re, vars[0], vars[1], vars[2], vars[3], Cause.Null);
                AllenRelationMats ar2 = new AllenRelationMats(re2, vars[4], vars[5], vars[6], vars[7], Cause.Null);
                try {
                    ar.filter();
                    ar.check(); // check all remaining values
                    {
                        // check all removed values
                        for (int j = 0; j < 4; j++) {
                            for (int k1 = 0; k1 < domains[j].length; k1++) {
                                if (!vars[j].contains(domains[j][k1])) {
                                    model.getEnvironment().worldPush();
                                    vars[j + 4].instantiateTo(domains[j][k1], Cause.Null);
                                    try {
                                        ar2.filter();
                                        org.testng.Assert.fail();
                                    } catch (ContradictionException cex) {
                                    }
                                    model.getEnvironment().worldPop();
                                }
                            }
                        }
                    }
                } catch (ContradictionException cex) {
                    // todo: check
                }
                model.getEnvironment().worldPop();
            }
        }
    }

}