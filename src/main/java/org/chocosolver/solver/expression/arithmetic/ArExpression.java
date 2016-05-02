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
package org.chocosolver.solver.expression.arithmetic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.relational.BiReExpression;
import org.chocosolver.solver.expression.relational.ReExpression;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.MathUtils;

import java.util.Map;

/**
 *
 * arithmetic expression
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public interface ArExpression {

    /**
     * A default empty array
     */
    ArExpression[] NO_CHILD = new ArExpression[0];

    /**
     * Return the associated model
     *
     * @return a Model object
     */
    Model getModel();

    /**
     * List of available operator for arithmetic expression
     */
    enum Operator {
        /**
         * negation operator
         */
        NEG {
            @Override
            int eval(int i1) {
                return -i1;
            }

            @Override
            int eval(int i1, int i2) {
                throw new UnsupportedOperationException();
            }
        },
        /**
         * absolute operator
         */
        ABS {
            @Override
            int eval(int i1) {
                return Math.abs(i1);
            }

            @Override
            int eval(int i1, int i2) {
                throw new UnsupportedOperationException();
            }
        },
        /**
         * addition operator
         */
        ADD {
            @Override
            int eval(int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            int eval(int i1, int i2) {
                return MathUtils.safeAdd(i1, i2);
            }
        },
        /**
         * subtraction operator
         */
        SUB {
            @Override
            int eval(int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            int eval(int i1, int i2) {
                return MathUtils.safeSubstract(i1, i2);
            }
        },
        /**
         * multiplication operator
         */
        MUL {
            @Override
            int eval(int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            int eval(int i1, int i2) {
                return MathUtils.safeMultiply(i1, i2);
            }
        },
        /**
         * division operator
         */
        DIV {
            @Override
            int eval(int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            int eval(int i1, int i2) {
                return i1 / i2;
            }
        },
        /**
         * modulo operator
         */
        MOD {
            @Override
            int eval(int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            int eval(int i1, int i2) {
                return i1 % i2;
            }
        },
        /**
         * square operator
         */
        SQR {
            @Override
            int eval(int i1) {
                return MathUtils.safeMultiply(i1, i1);
            }

            @Override
            int eval(int i1, int i2) {
                throw new UnsupportedOperationException();
            }
        },
        /**
         * power operator
         */
        POW {
            @Override
            int eval(int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            int eval(int i1, int i2) {
                return MathUtils.pow(i1, i2);
            }
        },
        /**
         * min operator
         */
        MIN {
            @Override
            int eval(int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            int eval(int i1, int i2) {
                return Math.min(i1, i2);
            }
        },
        /**
         * max operator
         */
        MAX {
            @Override
            int eval(int i1) {
                throw new UnsupportedOperationException();
            }

            @Override
            int eval(int i1, int i2) {
                return Math.max(i1, i2);
            }
        };

        abstract int eval(int i1);

        abstract int eval(int i1, int i2);
    }

    /**
     * @return the arithmetic expression as an {@link IntVar}.
     * If necessary, it creates intermediary variable and posts intermediary constraints
     */
    IntVar intVar();

    /**
     * @return <tt>true</tt> if this expression is a leaf, ie a variable, <tt>false</tt> otherwise
     */
    default boolean isExpressionLeaf(){
        return false;
    }

    /**
     * @param values int values to evaluate
     * @param map mapping between variables of the topmost expression and position in <i>values</i>
     * @return an evaluation of this expression with a tuple
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    default int eval(int[] values, Map<IntVar, Integer> map){
        assert this instanceof IntVar;
        return values[map.get(this)];
    }


    /**
     * @return the child of this expression, or null if thid
     */
    default ArExpression[] getExpressionChild(){
        return NO_CHILD;
    }

    /**
     * @return return the expression "-x" where this is "x"
     */
    default ArExpression neg() {
        return new UnArExpression(ArExpression.Operator.NEG, this);
    }

    /**
     * @return return the expression "|x|" where this is "x"
     */
    default ArExpression abs() {
        return new UnArExpression(ArExpression.Operator.ABS, this);
    }

    /**
     * @param y an int
     * @return return the expression "x + y" where this is "x"
     */
    default ArExpression add(int y) {
        return new BiArExpression(ArExpression.Operator.ADD, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x + y" where this is "x"
     */
    default ArExpression add(ArExpression y) {
        return new BiArExpression(ArExpression.Operator.ADD, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "x - y" where this is "x"
     */
    default ArExpression sub(int y) {
        return new BiArExpression(ArExpression.Operator.SUB, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x - y" where this is "x"
     */
    default ArExpression sub(ArExpression y) {
        return new BiArExpression(ArExpression.Operator.SUB, this, y);
    }


    /**
     * @param y an int
     * @return return the expression "x * y" where this is "x"
     */
    default ArExpression mul(int y) {
        return new BiArExpression(ArExpression.Operator.MUL, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x * y" where this is "x"
     */
    default ArExpression mul(ArExpression y) {
        return new BiArExpression(ArExpression.Operator.MUL, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "x / y" where this is "x"
     */
    default ArExpression div(int y) {
        return new BiArExpression(ArExpression.Operator.DIV, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x / y" where this is "x"
     */
    default ArExpression div(ArExpression y) {
        return new BiArExpression(ArExpression.Operator.DIV, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "x % y" where this is "x"
     */
    default ArExpression mod(int y) {
        return new BiArExpression(ArExpression.Operator.MOD, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x % y" where this is "x"
     */
    default ArExpression mod(ArExpression y) {
        return new BiArExpression(ArExpression.Operator.MOD, this, y);
    }

    /**
     * @return return the expression "x^2" where this is "x"
     */
    default ArExpression sqr() {
        return new UnArExpression(ArExpression.Operator.SQR, this);
    }

    /**
     * @param y an int
     * @return return the expression "x + y" where this is "x"
     */
    default ArExpression pow(int y) {
        return new BiArExpression(ArExpression.Operator.POW, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x + y" where this is "x"
     */
    default ArExpression pow(ArExpression y) {
        return new BiArExpression(ArExpression.Operator.POW, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "min(x, y)" where this is "x"
     */
    default ArExpression min(int y) {
        return new BiArExpression(ArExpression.Operator.MIN, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "min(x, y)" where this is "x"
     */
    default ArExpression min(ArExpression y) {
        return new BiArExpression(ArExpression.Operator.MIN, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "max(x, y)" where this is "x"
     */
    default ArExpression max(int y) {
        return new BiArExpression(ArExpression.Operator.MAX, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "max(x, y)" where this is "x"
     */
    default ArExpression max(ArExpression y) {
        return new BiArExpression(ArExpression.Operator.MAX, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "|x - y|" where this is "x"
     */
    default ArExpression dist(int y) {
        return this.sub(y).abs();
    }

    /**
     * @param y an expression
     * @return return the expression "|x - y|" where this is "x"
     */
    default ArExpression dist(ArExpression y) {
        return this.sub(y).abs();
    }

    /**
     * @param y an int
     * @return return the expression "x < y" where this is "x"
     */
    default ReExpression lt(int y) {
        return new BiReExpression(ReExpression.Operator.LT, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x < y" where this is "x"
     */
    default ReExpression lt(ArExpression y) {
        return new BiReExpression(ReExpression.Operator.LT, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "x <= y" where this is "x"
     */
    default ReExpression le(int y) {
        return new BiReExpression(ReExpression.Operator.LE, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x <= y" where this is "x"
     */
    default ReExpression le(ArExpression y) {
        return new BiReExpression(ReExpression.Operator.LE, this, y);
    }

    /**
     * @param y an ibt
     * @return return the expression "x > y" where this is "x"
     */
    default ReExpression gt(int y) {
        return new BiReExpression(ReExpression.Operator.GT, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x > y" where this is "x"
     */
    default ReExpression gt(ArExpression y) {
        return new BiReExpression(ReExpression.Operator.GT, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "x >= y" where this is "x"
     */
    default ReExpression ge(int y) {
        return new BiReExpression(ReExpression.Operator.GE, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x >= y" where this is "x"
     */
    default ReExpression ge(ArExpression y) {
        return new BiReExpression(ReExpression.Operator.GE, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "x =/= y" where this is "x"
     */
    default ReExpression ne(int y) {
        return new BiReExpression(ReExpression.Operator.NE, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x =/= y" where this is "x"
     */
    default ReExpression ne(ArExpression y) {
        return new BiReExpression(ReExpression.Operator.NE, this, y);
    }

    /**
     * @param y an int
     * @return return the expression "x = y" where this is "x"
     */
    default ReExpression eq(int y) {
        return new BiReExpression(ReExpression.Operator.EQ, this, this.getModel().intVar(y));
    }

    /**
     * @param y an expression
     * @return return the expression "x = y" where this is "x"
     */
    default ReExpression eq(ArExpression y) {
        return new BiReExpression(ReExpression.Operator.EQ, this, y);
    }
}
