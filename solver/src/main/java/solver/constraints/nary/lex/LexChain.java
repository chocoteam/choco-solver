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
package solver.constraints.nary.lex;

import common.ESat;
import common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.nary.lex.PropLexChain;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/08/11
 */
public class LexChain extends IntConstraint<IntVar> {

    private final boolean strict;
    private final int n, x;

    public LexChain(boolean strict, Solver solver, IntVar[]... vars) {
        super(ArrayUtils.flatten(vars), solver);
        this.strict = strict;
        this.n = vars[0].length;
        this.x = vars.length;
        setPropagators(new PropLexChain(vars, strict));
    }

    public LexChain(IntVar[][] vars, boolean strict, Solver solver) {
        super(ArrayUtils.flatten(vars), solver);
        this.strict = strict;
        this.n = vars[0].length;
        this.x = vars.length;
        setPropagators(new PropLexChain(vars, strict));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        return ESat.eval(checkTuple(0, tuple));
    }

    /**
     * check the feasibility of a tuple, recursively on each pair of consecutive vectors.
     * Compare vector xi with vector x(i+1):
     * return false if xij > x(i+1)j or if (strict && xi=x(i+1)), and checkTuple(i+1, tuple) otherwise.
     *
     * @param i     the index of the first vector to be considered
     * @param tuple the instantiation [[x11,..,x1n],[x21..x2n],..,[xk1..xkn]] to be checked
     * @return true iff lexChain(xi,x(i+1)) && lexChain(x(i+1),..,xk)
     */
    private boolean checkTuple(int i, int[] tuple) {
        if (i == x - 1) return true;
        int index = n * i;
        for (int j = 0; j < n; j++, index++) {
            if (tuple[index] > tuple[index + n])
                return false;
            if (tuple[index] < tuple[index + n])
                return checkTuple(i + 1, tuple);
        }
        return (!strict) && checkTuple(i + 1, tuple);
    }
}
