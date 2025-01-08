/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.hybrid;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.BitSet;

/**
 * Structure to compute support for a given variable to be used in {@link PropHybridTable}
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/04/2023
 */
public abstract class ASupport {

    /**
     * original var
     */
    IntVar var;
    /**
     * Store consistent values
     */
    final BitSet ac;
    /**
     * Current offset
     */
    int offset;
    /**
     * Count the number of value to remove
     */
    int cnt;

    ASupport() {
        this.ac = new BitSet();
    }

    final void setVar(IntVar var) {
        this.var = var;
        reset();
    }

    void reset() {
        ac.clear();
        offset = var.getLB();
        cnt = var.getDomainSize();
    }

    final IntVar getVar() {
        return var;
    }

    abstract void support(int value);

    abstract void supportAll();

    /**
     * Structure dedicated to {@link org.chocosolver.solver.constraints.extension.hybrid.ISupportable.Many}
     * where the intersection of the supports of all expression is required.
     */
    static class AndSupport extends ASupport {

        final BitSet and = new BitSet();

        @Override
        void reset() {
            super.reset();
            and.set(0, var.getDomainSize());
        }

        @Override
        void support(int value) {
            value -= offset;
            if(value > -1) {
                ac.set(value);
            }
        }

        @Override
        void supportAll() {
            ac.or(and);
        }

        void filter() {
            and.and(ac);
            ac.clear();
        }

        public void transferTo(ASupport v) {
            for (int i = and.nextSetBit(0); i > -1; i = and.nextSetBit(i + 1)) {
                v.support(i + offset);
            }
            reset();
        }
    }

    /**
     * Class that maintains, for a variable, the supported values
     */
    public static final class StrHVar extends ASupport {
        /**
         * index in the table
         */
        final int index;

        final IStateInt last_size;

        StrHVar(IEnvironment env, IntVar var_, int index_) {
            super();
            var = var_;
            last_size = env.makeInt(0);
            index = index_;
        }


        boolean removeUnsupportedValue(ICause cause) throws ContradictionException {
            boolean filter = false;
            if (var.hasEnumeratedDomain()) {
                for (int val = var.getLB(); cnt > 0 && val <= var.getUB(); val = var.nextValue(val)) {
                    if (!ac.get(val - offset)) {
                        filter |= var.removeValue(val, cause);
                        cnt--;
                    }
                }
            } else {
                int val = var.getLB();
                while (cnt > 0 && val <= var.getUB()) {
                    if (!ac.get(val - offset)) {
                        if (var.removeValue(val, cause)) {
                            filter = true;
                            cnt--;
                        } else break;
                    }
                    val = var.nextValue(val);
                }
                val = var.getUB();
                while (cnt > 0 && val >= var.getLB()) {
                    if (!ac.get(val - offset)) {
                        if (var.removeValue(val, cause)) {
                            filter = true;
                            cnt--;
                        } else break;
                    }
                    val = var.previousValue(val);
                }
            }
            return filter;
        }

        @Override
        void support(int value) {
            value -= offset;
            if (!ac.get(value)) {
                ac.set(value);
                cnt--;
            }
        }

        @Override
        void supportAll() {
            cnt = 0;
        }

        @Override
        public String toString() {
            return var.getName();
        }
    }
}
