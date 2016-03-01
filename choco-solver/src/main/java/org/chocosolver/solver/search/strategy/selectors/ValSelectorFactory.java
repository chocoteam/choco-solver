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
package org.chocosolver.solver.search.strategy.selectors;

import org.chocosolver.solver.search.strategy.selectors.values.*;

/**
 * Strategies over real variables
 * Just there to simplify strategies creation.
 * <br/>
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 */
public class ValSelectorFactory {

    // ************************************************************************************
    // GENERIC SELECTORS
    // ************************************************************************************

    // TODO

    // ************************************************************************************
    // INTVAR SELECTORS
    // ************************************************************************************

    /**
     * Selects the variable lower bound
     * @return a value selector
     */
    public static IntValueSelector minIntVal() {
        return new IntDomainMin();
    }

    /**
     * Selects a value at the middle between the variable lower and upper bounds
     * <p>
     * BEWARE: this should not be used within assignments and/or value removals if variables
     * have a bounded domain.
     *
     * @param floor the rounding policy: set to true, return the closest value less than or equal to the middle value
     *              set to false, return the closest value greater or equal to the middle value.
     *              Can lead to infinite loop when not correctly selected.
     * @return a value selector
     */
    public static IntValueSelector midIntVal(boolean floor) {
        return new IntDomainMiddle(floor);
    }

    /**
     * Selects the variable upper bound
     *
     * @return a value selector
     */
    public static IntValueSelector maxIntVal() {
        return new IntDomainMax();
    }

    /**
     * Selects randomly either the lower bound or the upper bound of the variable
     * Takes an arbitrary value in {LB,UB}
     *
     * @param SEED the seed for randomness
     * @return a value selector
     */
    public static IntValueSelector randomIntBound(long SEED) {
        return new IntDomainRandomBound(SEED);
    }

    /**
     * Selects randomly a value in the variable domain.
     * Takes an arbitrary value in [LB,UB]
     * <p>
     * BEWARE: this should not be used within assignments and/or value removals if variables
     * have a bounded domain.
     *
     * @param SEED the seed for randomness
     * @return a value selector
     */
    public static IntValueSelector randomIntVal(long SEED) {
        return new IntDomainRandom(SEED);
    }

    // ************************************************************************************
    // REALVAR SELECTORS
    // ************************************************************************************

    /**
     * Value selector for halving domains of real variables.
     * @return a value selector to split real variable domains.
     */
    public static RealValueSelector midRealVal() {
        return new RealDomainMiddle();
    }

    /**
     * @return a value selector to select real lower bounds. (use with caution)
     */
    public static RealValueSelector minRealVal() {
        return new RealDomainMin();
    }

    /**
     * @return a value selector to select real upper bounds. (use with caution)
     */
    public static RealValueSelector maxRealVal() {
        return new RealDomainMax();
    }

    // ************************************************************************************
    // SETVAR SELECTORS
    // ************************************************************************************

	/**
     * @return a value selector for SetVar to select the first int in UB\LB
     * (not necessarily the smallest one as set domains are not sorted)
     */
    public static SetValueSelector firstSetVal(){
        return new SetDomainMin();
    }
}
