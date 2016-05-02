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
package org.chocosolver.solver.expression.relational;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import java.util.Map;

/**
 * relational expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public interface ReExpression {

    /**
     * List of available operator for relational expression
     */
    enum Operator {
        /**
         * less than
         */
        LT {
            @Override
            boolean eval(int i1, int i2) {
                return i1 < i2;
            }
        },
        /**
         * less than or equal to
         */
        LE {
            @Override
            boolean eval(int i1, int i2) {
                return i1 <= i2;
            }
        },
        /**
         * greater than
         */
        GE {
            @Override
            boolean eval(int i1, int i2) {
                return i1 >= i2;
            }
        },
        /**
         * greater than or equal to
         */
        GT {
            @Override
            boolean eval(int i1, int i2) {
                return i1 > i2;
            }
        },
        /**
         * not equal to
         */
        NE {
            @Override
            boolean eval(int i1, int i2) {
                return i1 != i2;
            }
        },
        /**
         * equal to
         */
        EQ {
            @Override
            boolean eval(int i1, int i2) {
                return i1 == i2;
            }
        };

        abstract boolean eval(int i1, int i2);
    }

    /**
     * Post the decomposition of this expression in the solver
     */
    default void post() {
        decompose().post();
    }


    /**
     * @return the topmost constraint representing the expression. If needed, a call to this method
     * creates additional variables and posts additional constraints.
     */
    Constraint decompose();

    /**
     * @return a TABLE constraint with the
     */
    Constraint extension();

    boolean eval(int[] values, Map<IntVar, Integer> map);
}
