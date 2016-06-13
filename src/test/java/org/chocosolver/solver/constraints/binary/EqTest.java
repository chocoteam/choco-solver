/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.util.ESat.TRUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/06/13
 */
public class EqTest {

    @DataProvider(name = "params")
    public Object[][] data1D(){
        // indicates whether to use explanations or not
        List<Object[]> elt = new ArrayList<>();
        elt.add(new Object[]{true});
        elt.add(new Object[]{false});
        return elt.toArray(new Object[elt.size()][1]);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void test1(boolean exp) {
        Model s = new Model();
        IntVar two1 = s.intVar(2);
        IntVar two2 = s.intVar(2);
        s.arithm(two1, "=", two2).post();
        if(exp){
            s.getSolver().setCBJLearning(false,false);
        }
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }


    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void test2(boolean exp) {
        Model s = new Model();
        IntVar three = s.intVar(3);
        IntVar two = s.intVar(2);
        s.arithm(three, "-", two, "=", 1).post();
        if(exp){
            s.getSolver().setCBJLearning(false,false);
        }
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void test3(boolean exp) {
        Model s = new Model();
        IntVar three = s.intVar(3);
        IntVar two = s.intVar(2);
        s.arithm(three, "=", two, "+", 1).post();
        if(exp){
            s.getSolver().setCBJLearning(false,false);
        }
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }
}
