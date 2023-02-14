/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.hybrid;

/**
 * Arithmetical expressions for hybrid tuples.
 * Make possible to extend an expression with operations like addition or subtraction.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/02/2023
 */
public interface HArExpression {

    /**
     * Supported operations
     */
    enum Op {
        PLUS,
        MINUS,
    }

    /**
     * @param prop the hybrid table propagator as caller
     * @return <code>true</code> if this expression is instantiated
     * (i.e., its domain contains a unique value), <code>false</code> otherwise.
     */
    boolean isInstantiated(PropHybridTable prop);

    /**
     * @param prop the hybrid table propagator as caller
     * @return the value this expression is instantiated to
     * @throws IllegalStateException when the expression is not instantiated
     */
    int getValue(PropHybridTable prop) throws IllegalStateException;

    /**
     * @param prop the hybrid table propagator as caller
     * @return the current lower bound of this expression
     */
    int getLB(PropHybridTable prop);

    /**
     * @param prop the hybrid table propagator as caller
     * @return the current upper bound of this expression
     */
    int getUB(PropHybridTable prop);

    /**
     * @param prop  the hybrid table propagator as caller
     * @param value the value to check
     * @return <code>true</code> if this expression contains the value <code>value</code>.
     */
    boolean contains(PropHybridTable prop, int value);

    /**
     * @param i a constant
     * @return a new expression <code>this + i</code>
     */
    default HArExpression add(int i) {
        return new BiHArExpression(this, Op.PLUS, i);
    }

    /**
     * @param i a constant
     * @return a new expression <code>this - i</code>
     */
    default HArExpression sub(int i) {
        return new BiHArExpression(this, Op.MINUS, i);
    }

    /**
     * A class to deal with expression like <code>e &lt;op&gt; i</code>
     * where <code>e</code> is an expression,
     * <code>&lt;op&gt;</code> is an operator among {+,-} and
     * <code>i</code> is an integer.
     */
    class BiHArExpression implements HArExpression {

        HArExpression a1;
        int a2;
        Op o;

        public BiHArExpression(HArExpression a1, Op o, int a2) {
            this.a1 = a1;
            this.o = o;
            this.a2 = a2;
        }

        @Override
        public boolean contains(PropHybridTable prop, int value) {
            switch (o) {
                case PLUS:
                    return a1.contains(prop, value - a2);
                case MINUS:
                    return a1.contains(prop, value + a2);
                default:
                    return false;
            }
        }

        @Override
        public boolean isInstantiated(PropHybridTable prop) {
            return a1.isInstantiated(prop);
        }

        @Override
        public int getValue(PropHybridTable prop) {
            int v = a1.getValue(prop);
            switch (o) {
                case PLUS:
                    v += a2;
                    break;
                case MINUS:
                    v -= a2;
                    break;
            }
            return v;
        }

        @Override
        public int getLB(PropHybridTable prop) {
            int v = a1.getLB(prop);
            switch (o) {
                case PLUS:
                    v += a2;
                    break;
                case MINUS:
                    v -= a2;
                    break;
            }
            return v;
        }

        @Override
        public int getUB(PropHybridTable prop) {
            int v = a1.getUB(prop);
            switch (o) {
                case PLUS:
                    v += a2;
                    break;
                case MINUS:
                    v -= a2;
                    break;
            }
            return v;
        }
    }

}
