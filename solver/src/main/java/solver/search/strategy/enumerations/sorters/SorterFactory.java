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
package solver.search.strategy.enumerations.sorters;

import solver.Solver;
import solver.search.strategy.enumerations.sorters.metrics.*;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * A list of preset variable comparators (syntaxic sugar).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 10/05/11
 */
public class SorterFactory {

    private SorterFactory() {
    }

    /**
     * Variable are sorted wrt the declaring order
     *
     * @param variables array of variables
     * @param <V>       type of variable
     * @return input order sorter
     */
    public static <V extends IntVar> AbstractSorter<V> presetI(V[] variables) {
        return new Incr<V>(Input.<V>build(variables));
    }

    /**
     * Variable are sorted wrt the declaring order
     *
     * @param variables array of variables
     * @param <V>       type of variable
     * @return input order sorter
     */
    public static <V extends Variable> AbstractSorter<V> inputOrder(V[] variables) {
        return new Incr<V>(Input.<V>build(variables));
    }

    /**
     * Variables are sorted by increasing domain sizes
     *
     * @return minimum domain sorter
     */
    public static AbstractSorter<IntVar> minDomain() {
        return new Incr<IntVar>(DomSize.build());
    }

    /**
     * Variables are sorted by decreasing domain sizes
     *
     * @return maximum domain sorter
     */
    public static AbstractSorter<IntVar> maxDomain() {
        return new Decr<IntVar>(DomSize.build());
    }

    /**
     * Variables are sorted by increasing smallest values
     *
     * @return smallest value sorter
     */
    public static AbstractSorter<IntVar> smallest() {
        return new Incr<IntVar>(LowerBound.build());
    }

    /**
     * Variables are sorted by increasing largest values
     *
     * @return largest value sorter
     */
    public static AbstractSorter<IntVar> largest() {
        return new Incr<IntVar>(UpperBound.build());
    }

    /**
     * Variables are sorted by increasing difference between the 2 smallest values of their domains
     *
     * @return max regret sorter
     */
    public static AbstractSorter<IntVar> maxRegret() {
        return new Incr<IntVar>(LargestValues.build());
    }

    /**
     * Variables are sorted by increasing with the smallest value in its domain,
     * breaking ties using the number of constraints
     *
     * @return most constrained sorter
     */
    public static AbstractSorter<IntVar> mostConstrained() {
        return new Seq<IntVar>(new Incr<IntVar>(LowerBound.build()),
                new Incr(Degree.build()));
    }

    /**
     * Variables are sorted by increasing by number of constraints
     *
     * @return most constrained sorter
     */
    public static AbstractSorter<IntVar> occurrence() {
        return new Incr(Degree.build());
    }

    /**
     * Variables are sorted randomly
     *
     * @param seed intial seed
     * @return random sorter
     */
    public static AbstractSorter<IntVar> random(long seed) {
        return new Random<IntVar>(seed);
    }

    /**
     * Variables are sorted randomly
     *
     * @param random pseudorandom numbers generator
     * @return random sorter
     */
    public static AbstractSorter<IntVar> random(java.util.Random random) {
        return new Random<IntVar>(random);
    }

    /**
     * Variables are sorted randomly
     *
     * @return random sorter
     */
    public static AbstractSorter<IntVar> random() {
        return new Random<IntVar>();
    }

    /**
     * Naive implementation of
     * "Boosting systematic search by weighting constraints"
     * F.Boussemart, F.Hemery, C.Lecoutre and L.Sais
     *
     * @param solver declaring solver
     * @return dom/wdeg sorter
     */
    public static AbstractSorter<IntVar> domOverWDeg(Solver solver) {
        DomOverWDeg dd = new DomOverWDeg();
        solver.getSearchLoop().branchSearchMonitor(dd);
        return dd;
    }
}
