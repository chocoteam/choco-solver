/**
 * Copyright (c) 1999-2011, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.search.strategy.enumerations.comparators;

import solver.search.strategy.enumerations.IntVal;
import solver.search.strategy.enumerations.comparators.values.InDomainMax;
import solver.search.strategy.enumerations.comparators.values.InDomainMin;
import solver.search.strategy.enumerations.sorters.FirstFail;
import solver.search.strategy.enumerations.sorters.MostConstrained;
import solver.search.strategy.enumerations.sorters.Random;
import solver.search.strategy.enumerations.sorters.Smallest;
import solver.variables.IntVar;

/**
 *
 * TODO: efficient random: does not use the sorting algorithm
 * TODO: MostConstrained: count requests instead of constraints
 * TODO: MostConstrained: what about entailed constraints?
 *
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/10
 */
public class ComparatorFactory {

    protected ComparatorFactory() {
    }

    //****************************************************************************************************************//

    public static final FirstFail first_fail = new FirstFail();

    public static final Smallest smallest = new Smallest();

    public static final Random<IntVar> random_var = new Random<IntVar>();

    public static final MostConstrained most_constrained = new MostConstrained();

    //****************************************************************************************************************//

    public static final InDomainMin indomain_min = new InDomainMin();

    public static final InDomainMax indomain_max = new InDomainMax();

    public static final Random<IntVal> random_val = new Random<IntVal>();
}
