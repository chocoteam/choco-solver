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

import org.chocosolver.solver.variables.IntVar;

/**
 * This interface and its subclasses make possible to declare and manage
 * expressions for hybrid tuples.
 * <br/>
 * An expression can be seen as a restriction on the value a variable can take like:
 * <code>eq(2)</code>, <code>gt(1)</code> or <code>eq(col(0).add(1))</code>.
 * <br/>
 * The variable is not explicitly declare, only the position of the variable in the
 * hybrid table constraint is required to get its expression.
 * That's why the expression <code>col(i)</code> exists, where <code>i</code>
 * refers to the index of the column/variable in the table constraint.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/02/2023
 */
public interface HReExpression {

    /**
     * Supported operators
     */
    enum Op {
        EQ,
        NQ,
        GE,
        LE,
        COL,
        STAR
    }

    /**
     * @param prop the hybrid table propagator as caller
     * @param idx  index of the variable in prop
     * @return <i>true</i> if this expression can be satisfied
     * for the current domain of the variable at position <code>idx</code>,
     * that is at least one value is consistent with the expression.
     * Return <code>false</code> otherwise.
     */
    boolean canBeSatisfied(PropHybridTable prop, int idx);

    /**
     * For a given variable, at position <code>idx</code> in <code>prop</code>,
     * this method gets all values supported by this expression.
     *
     * @param prop the hybrid table propagator caller
     * @param idx  index of the variable in vars
     */
    void supportFor(PropHybridTable prop, int idx);

    /**
     * Manage expression like: <code>&lt;op&gt; i</code> where
     * <code>&lt;op&gt;</code> is an operator among {=,&ne;, &ge; &gt;, &le;, &lt;} and
     * <code>i</code> is an integer.
     *
     * @implNote The * expression (<code>any()</code>) is also defined with this class, the value set to <code>i</code>
     * is always 0. The <code>col(i)</code> expression is partially managed with this class.
     */
    class SimpleHReExpression implements HReExpression {

        Op op;
        int a;

        public SimpleHReExpression(Op op, int a) {
            this.a = a;
            this.op = op;
        }

        @Override
        public boolean canBeSatisfied(PropHybridTable prop, int idx) {
            IntVar var = prop.getVar(idx);
            switch (op) {
                case STAR:
                    return true;
                case EQ:
                    return var.contains(a);
                case NQ:
                    return !var.isInstantiatedTo(a);
                case GE:
                    return var.getUB() >= a;
                case LE:
                    return var.getLB() <= a;
                case COL: {
                    if (a == idx) return true;
                    for (int v = prop.getVar(a).getLB(); v <= prop.getVar(a).getUB(); v = prop.getVar(a).nextValue(v)) {
                        if (var.contains(v)) return true;
                    }
                    return false;
                }
                default:
                    return false;
            }
        }

        @Override
        public void supportFor(PropHybridTable prop, int idx) {
            PropHybridTable.StrHVar var = prop.getSVar(idx);
            switch (op) {
                case STAR:
                    var.supportForAll();
                    break;
                case EQ:
                    var.supportFor(a);
                    break;
                case NQ: {
                    int ub = var.var.getUB();
                    for (int val = var.var.getLB(); val <= ub; val = var.var.nextValue(val)) {
                        if (val != a) {
                            var.supportFor(val);
                        }
                    }
                }
                break;
                case GE: {
                    int ub = var.var.getUB();
                    for (int val = var.var.nextValue(a - 1); val <= ub; val = var.var.nextValue(val)) {
                        var.supportFor(val);
                    }
                }
                break;
                case LE: {
                    int ub = var.var.previousValue(a + 1);
                    for (int val = var.var.getLB(); val <= ub; val = var.var.nextValue(val)) {
                        var.supportFor(val);
                    }
                }
                break;
                case COL: {
                    if (a == idx) var.supportForAll();
                    int ub = var.var.getUB();
                    for (int val = var.var.getLB(); val <= ub; val = var.var.nextValue(val)) {
                        if (prop.getVar(a).contains(val)) {
                            var.supportFor(val);
                        }
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException();

            }
        }
    }

    /**
     * Class dedicated to <code>col(i)</code> expression where <code>i</code>
     * refers to the position of the column/variable in the tuple.
     */
    class ColHReExpression extends SimpleHReExpression implements HArExpression {

        public ColHReExpression(HReExpression.Op op, int a) {
            super(op, a);
        }

        @Override
        public boolean contains(PropHybridTable prop, int value) {
            return prop.getVar(a).contains(value);
        }


        @Override
        public boolean isInstantiated(PropHybridTable prop) {
            return prop.getVar(a).isInstantiated();
        }

        @Override
        public int getValue(PropHybridTable prop) {
            if (!isInstantiated(prop)) {
                throw new IllegalStateException("getValue() can be only called on instantiated variable.");
            }
            return prop.getVar(a).getValue();
        }

        @Override
        public int getLB(PropHybridTable prop) {
            return prop.getVar(a).getLB();
        }

        @Override
        public int getUB(PropHybridTable prop) {
            return prop.getVar(a).getUB();
        }
    }

    /**
     * Class to manage expression like <code>&lt;op&gt; i</code> where
     * <code>&lt;op&gt;</code> is an operator among {=,&ne;, &ge; &gt;, &le;, &lt;} and
     * <code>e</code> is an expression.
     */
    class ComplexHReExpression implements HReExpression {

        Op op;
        HArExpression e;

        public ComplexHReExpression(Op op, HArExpression e) {
            this.e = e;
            this.op = op;
        }

        @Override
        public boolean canBeSatisfied(PropHybridTable prop, int idx) {
            IntVar var = prop.getVar(idx);
            switch (op) {
                case EQ:
                    // at least one value intersects both domain
                    for (int v = var.getLB(); v <= var.getUB(); v = var.nextValue(v)) {
                        if (e.contains(prop, v)) {
                            return true;
                        }
                    }
                    return false;
                case NQ:
                    if (e.isInstantiated(prop)) {
                        return !var.contains(e.getValue(prop));
                    }
                    return true;
                case GE:
                    return var.getUB() >= e.getLB(prop);
                case LE:
                    return var.getLB() <= e.getUB(prop);
                default:
                    return false;
            }
        }

        @Override
        public void supportFor(PropHybridTable prop, int idx) {
            PropHybridTable.StrHVar svar = prop.getSVar(idx);
            IntVar var = svar.var;
            switch (op) {
                case EQ:
                    for (int v = var.getLB(); v <= var.getUB(); v = var.nextValue(v)) {
                        if (e.contains(prop, v)) {
                            svar.supportFor(v);
                        }
                    }
                    break;
                case NQ:
                    if (e.isInstantiated(prop)) {
                        int a = e.getValue(prop);
                        int ub = var.getUB();
                        for (int val = var.getLB(); val <= ub; val = var.nextValue(val)) {
                            if (val != a) {
                                svar.supportFor(val);
                            }
                        }
                    } else {
                        svar.supportForAll();
                    }
                    break;
                case GE: {
                    int a = e.getLB(prop);
                    int ub = var.getUB();
                    for (int val = var.nextValue(a - 1); val <= ub; val = var.nextValue(val)) {
                        svar.supportFor(val);
                    }
                }
                break;
                case LE: {
                    int a = e.getUB(prop);
                    int ub = var.previousValue(a + 1);
                    for (int val = var.getLB(); val <= ub; val = var.nextValue(val)) {
                        svar.supportFor(val);
                    }
                }
                break;
                default:
                    throw new UnsupportedOperationException();

            }
        }
    }

}
