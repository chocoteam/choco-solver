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

package org.chocosolver.solver.search.strategy;

import org.chocosolver.solver.search.strategy.selectors.values.*;

/**
 * Small factory enabling an easier instantiation of the value selectors.
 * It gathers the {@link org.chocosolver.solver.variables.IntVar}, {@link org.chocosolver.solver.variables.RealVar}
 * and {@link org.chocosolver.solver.variables.SetVar} value selectors.
 */
public class ValueSelect {


    // ************************************************************************************
    // INTVAR VALUES SELECTORS
    // ************************************************************************************

    /**
     * Selects the upper bound of an IntVar
     *
     * <p>
     *     e.g. if an <code>IntVar</code> with values {1, 5, 8, 9}
     *     is chosen by the variable selector, it selects <code>9</code>
     * </p>
     * @return a value selector returning the upper bound
     */
    public static IntValueSelector intDomainMax() {
        return new IntDomainMax();
    }


    /**
     * Selects the lower bound of an IntVar
     *
     * <p>
     *     e.g. if an <code>IntVar</code> with values {1, 5, 8, 9}
     *     is chosen by the variable selector, it selects <code>1</code>
     * </p>
     * @return a value selector returning the lower bound
     */
    public static IntValueSelector intDomainMin() {
        return new IntDomainMin();
    }


    /**
     * Selects the median value of an IntVar
     *  <br/>
     * BEWARE: this should not be used within assignments and/or value removals if variables
     *  have a bounded domain
     * <p>
     *     e.g. if an <code>IntVar</code> with values {1, 5, 8, 9}
     *     is chosen by the variable selector, it selects <code>8</code> <br/><br/>
     *
 *         if an <code>IntVar</code> with values {2, 6, 7, 10, 12}
     *     is chosen by the variable selector, it selects <code>7</code>
     * </p>
     * @return a value selector returning the median value
     */
    public static IntValueSelector intDomainMedian() {
        return new IntDomainMedian();
    }


    /**
     * Picks randomly a value inside the domain of an IntVar
     *
     * </p>
     * @return a value selector returning a random value
     */
    public static IntValueSelector intDomainRandom(long seed) {
        return new IntDomainRandom(seed);
    }


    /**
     * Picks randomly either the lower bound or the upper bound of an IntVar
     *
     * <p>
     *     e.g. if an <code>IntVar</code> with values {1, 5, 8, 9}
     *     is chosen by the variable selector, it can select <code>1</code> or <code>9</code>
     * </p>
     * @return a value selector returning either the lower bound of the upper bound
     */
    public static IntValueSelector intDomainRandomBound(long seed) {
        return new IntDomainRandomBound(seed);
    }


    // ************************************************************************************
    // REALVAR VALUES SELECTORS
    // ************************************************************************************

    /**
     * Selects the upper bound of a RealVar
     *
     * <p>
     *     e.g. if an <code>RealVar</code> is defined between 2.0 and 8.0
     *     is chosen by the variable selector, it selects <code>8.0</code>
     * </p>
     * @return a value selector returning the upper bound
     */
    public static RealValueSelector realDomainMax() {
        return new RealDomainMax();
    }

    /**
     * Selects the lower bound of a RealVar
     *
     * <p>
     *     e.g. if an <code>RealVar</code> is defined between 2.0 and 8.0
     *     is chosen by the variable selector, it selects <code>2.0</code>
     * </p>
     * @return a value selector returning the upper bound
     */
    public static RealValueSelector realDomainMin() {
        return new RealDomainMin();
    }

    /**
     * Selects the middle value of a RealVar
     *
     * <p>
     *     e.g. if an <code>RealVar</code> is defined between 2.0 and 8.0
     *     is chosen by the variable selector, it selects <code>5.0</code>
     * </p>
     * @return a value selector returning the middle value
     */
    public static RealValueSelector realDomainMiddle() {
        return new RealDomainMiddle();
    }


    // ************************************************************************************
    // SETVAR VALUES SELECTORS
    // ************************************************************************************

    /**
     * Selects the first integer in the envelope and not in the kernel
     *
     * <p>
     *     e.g. if an <code>SetVar</code> with kernel {2, 5, 9} and envelope {2, 5, 8, 9, 10}
     *     is chosen by the variable selector, it selects <code>8</code>
     * </p>
     * @return a value selector returning the first integer in the envelope and not in the kernel
     */
    public static SetValueSelector setDomainMin() {
        return new SetDomainMin();
    }

}
