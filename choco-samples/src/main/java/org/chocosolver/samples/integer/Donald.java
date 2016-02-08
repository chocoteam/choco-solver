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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**
 * A verbal arithmetic puzzle:
 * <br/>
 * &#32;&#32;&#32;D&#32;O&#32;N&#32;A&#32;L&#32;D<br/>
 * +&#32;G&#32;E&#32;R&#32;A&#32;L&#32;D<br/>
 * ========<br/>
 * &#32;&#32;&#32;R&#32;O&#32;B&#32;E&#32;R&#32;T<br/>
 * <br/>
 * Attribute a different value to each letter, such that the equation is correct.
 *
 * @author Charles Prud'homme
 * @since 03/08/11
 */
public class Donald extends AbstractProblem {

    IntVar d, o, n, a, l, g, e, r, b, t;
    IntVar[] letters;

    @Override
    public void buildModel() {
        model = new Model("Donald");
        d = model.intVar("d", 1, 9, true);
        o = model.intVar("o", 0, 9, true);
        n = model.intVar("n", 0, 9, true);
        a = model.intVar("a", 0, 9, true);
        l = model.intVar("l", 0, 9, true);
        g = model.intVar("g", 1, 9, true);
        e = model.intVar("e", 0, 9, true);
        r = model.intVar("r", 1, 9, true);
        b = model.intVar("b", 0, 9, true);
        t = model.intVar("t", 0, 9, true);
        letters = new IntVar[]{d, o, n, a, l, g, e, r, b, t};

        model.allDifferent(letters, "BC").post();
        model.scalar(new IntVar[]{d, o, n, a, l, d,
                g, e, r, a, l, d,
                r, o, b, e, r, t}, new int[]{100000, 10000, 1000, 100, 10, 1,
                100000, 10000, 1000, 100, 10, 1,
                -100000, -10000, -1000, -100, -10, -1,
        }, "=", 0).post();


    }

    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        model.solve();
    }

    @Override
    public void prettyOut() {
        System.out.println("donald + gerald = robert ");
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < letters.length; i++) {
            st.append(String.format("%s : %d\n\t", letters[i].getName(), letters[i].getValue()));
        }
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new Donald().execute(args);
    }
}
