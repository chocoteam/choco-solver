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

package solver.search.strategy.enumerations.values;

import gnu.trove.map.hash.THashMap;
import solver.search.strategy.enumerations.values.comparators.Distance;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.heuristics.nary.Join;
import solver.search.strategy.enumerations.values.heuristics.nary.SeqN;
import solver.search.strategy.enumerations.values.heuristics.unary.DropN;
import solver.search.strategy.enumerations.values.heuristics.unary.Filter;
import solver.search.strategy.enumerations.values.heuristics.unary.FirstN;
import solver.search.strategy.enumerations.values.heuristics.zeroary.FastEnumVal;
import solver.search.strategy.enumerations.values.heuristics.zeroary.Random;
import solver.search.strategy.enumerations.values.heuristics.zeroary.UnsafeEnum;
import solver.search.strategy.enumerations.values.metrics.Const;
import solver.search.strategy.enumerations.values.metrics.Metric;
import solver.search.strategy.enumerations.values.predicates.Member;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/01/11
 */
public class HeuristicValFactory {

    HeuristicValFactory() {
    }


    /**
     * Sets the <b>inDomainMin</b> value iterator to the list of variable in parameter.
     * This iterator chooses the smallest value in the variable's domain
     *
     * @param vars list of variables declaring this value iterator
     */
    public static void indomainMin(IntVar... vars) {
        for (IntVar var : vars) {
            var.setHeuristicVal(HeuristicValFactory.enumVal(var, var.getLB(), 1, var.getUB()));
        }
    }

    /**
     * Sets the <b>inDomainMax</b> value iterator to the list of variable in parameter.
     * This iterator chooses the largest value in the variable's domain
     *
     * @param vars list of variables declaring this value iterator
     */
    public static void indomainMax(IntVar... vars) {
        for (IntVar var : vars) {
            var.setHeuristicVal(HeuristicValFactory.enumVal(var, var.getUB(), -1, var.getLB()));
        }
    }

    /**
     * Sets the <b>inDomainMiddle</b> value iterator to the list of variable in parameter.
     * This iterator chooses the closest value to the mean between the variable's domain current bounds
     *
     * @param vars list of variables declaring this value iterator
     */
    public static void indomainMiddle(IntVar... vars) {
        for (IntVar var : vars) {
            int middle = (var.getLB() + var.getUB()) / 2;
            var.setHeuristicVal(
                    new Join(new Distance(new Const(middle)),
                            HeuristicValFactory.enumVal(var, middle, -1, var.getLB()),
                            HeuristicValFactory.enumVal(var, middle + 1, 1, var.getUB()))
            );
        }
    }

    /**
     * @param vars list of variables declaring this value iterator
     */
    public static void indomainSplitMin(IntVar... vars) {
        for (IntVar var : vars) {
            int middle = (var.getLB() + var.getUB()) / 2;
            var.setHeuristicVal(
                    new SeqN(HeuristicValFactory.enumVal(var, middle, -1, var.getLB()),
                            HeuristicValFactory.enumVal(var, middle + 1, 1, var.getUB()))
            );
        }
    }

    /**
     * @param vars list of variables declaring this value iterator
     */
    public static void indomainSplitMax(IntVar... vars) {
        for (IntVar var : vars) {
            int middle = (var.getLB() + var.getUB()) / 2;
            var.setHeuristicVal(
                    new SeqN(HeuristicValFactory.enumVal(var, middle, 1, var.getUB()),
                            HeuristicValFactory.enumVal(var, middle-1, -1, var.getLB()))
            );
        }
    }

    /**
     * Sets the <b>inDomainRandom</b> value iterator to the list of variable in parameter.
     * This iterator chooses a random value in the variable's domain
     *
     * @param vars list of variables declaring this value iterator
     */
    public static void random(IntVar... vars) {
        for (IntVar v : vars) {
            v.setHeuristicVal(new Random(v));
        }
    }

    /**
     * Sets the <b>inDomainRandom</b> value iterator to the list of variable in parameter.
     * This iterator chooses a random value in the variable's domain
     *
     * @param seed seed for random
     * @param vars list of variables declaring this value iterator
     */
    public static void random(long seed, IntVar... vars) {
        for (IntVar v : vars) {
            v.setHeuristicVal(new Random(v, seed));
        }
    }

    /**
     * Sets the <b>inDomainRandom</b> value iterator to the list of variable in parameter.
     * This iterator chooses a random value in the variable's domain
     *
     * @param vars list of variables declaring this value iterator
     */
    public static void presetI(IntVar... vars) {
        for (IntVar v : vars) {
            v.setHeuristicVal(presetI(v));
        }
    }

    /**
     * Preset heuristic val for IntVar
     *
     * @param ivar a integer variable
     * @return an HeuristicVal
     */
    public static HeuristicVal presetI(IntVar ivar) {
        return fastenumVal(ivar);
    }

    /**
     * Build an heuristic val for RotateLeft  : SeqN(DropN(orig, metric), FirstN(orig, metric))
     * <br/>
     * BEWARE: metric already defined its own action (could be different from default action in DropN and FirstN)
     *
     * @param orig   sub heuristic val
     * @param metric number of element to rotate
     * @return {@link SeqN}
     */
    public static SeqN rotateLeft(HeuristicVal orig, Metric metric) {
        return new SeqN(new DropN(orig, metric), new FirstN(orig.duplicate(new THashMap<HeuristicVal, HeuristicVal>()), metric));
    }

    /**
     * Build an heuristic val for RotateLeft  : SeqN(DropN(orig, metric, action), FirstN(orig, metric, action)).
     * <br/>
     * BEWARE: metric already defined its own action (could be different from <code>action</code> in parameter)
     *
     * @param orig   sub heuristic val
     * @param metric number of element to rotate
     * @param action used for DropN and FirstN
     * @return {@link SeqN}
     */
    public static SeqN rotateLeft(HeuristicVal orig, Metric metric, Action action) {
        return new SeqN(
                new DropN(orig, metric, action),
                new FirstN(orig.duplicate(new THashMap<HeuristicVal, HeuristicVal>()), metric, action));
    }

    /**
     * Build an UnsafeEnum heuristic val from a domain
     * TODO : mieux commenter
     *
     * @param ivar variable to enumerate
     * @return {@link UnsafeEnum}
     */
    public static UnsafeEnum unsafeEnum(IntVar ivar) {
        return new UnsafeEnum(ivar.getLB(), 1, ivar.getUB());
    }

    /**
     * Build an UnsafeEnum heuristic val from a domain and an action
     * TODO : mieux commenter
     *
     * @param ivar   variable to enumerate
     * @param action action
     * @return {@link UnsafeEnum}
     */
    public static UnsafeEnum unsafeEnum(IntVar ivar, Action action) {
        return new UnsafeEnum(ivar.getLB(), 1, ivar.getUB(), action);
    }

    /**
     * Build an UnsafeEnum heuristic val from a domain
     * TODO : mieux commenter
     *
     * @param from  starting value
     * @param delta gap
     * @param to    ending value
     * @return {@link UnsafeEnum}
     */
    public static UnsafeEnum unsafeEnum(int from, int delta, int to) {
        return new UnsafeEnum(from, delta, to);
    }

    /**
     * Build an UnsafeEnum heuristic val from a domain and an action
     * TODO : mieux commenter
     *
     * @param from   starting value
     * @param delta  gap
     * @param to     ending value
     * @param action action
     * @return {@link UnsafeEnum}
     */
    public static UnsafeEnum unsafeEnum(int from, int delta, int to, Action action) {
        return new UnsafeEnum(from, delta, to, action);
    }

    /**
     * Build the following heuristic val: Filter(Member(domain), UnsafeEnum(domain))
     *
     * @param ivar variable to enumerate
     * @return a {@link Filter}
     */
    public static Filter enumVal(IntVar ivar) {
        return new Filter(new Member(ivar), unsafeEnum(ivar));
    }

    /**
     * Build the following heuristic val: FastEnumVal(ivar)
     *
     * @param ivar variable to enumerate
     * @return a {@link Filter}
     */
    public static HeuristicVal fastenumVal(IntVar ivar) {
        return new FastEnumVal(ivar);
    }

    /**
     * Build the following heuristic val: Filter(Member(domain, action), UnsafeEnum(domain, action)
     *
     * @param ivar   variable to enumerate
     * @param action action of Member and UnsafeEnum
     * @return a {@link Filter}
     */
    public static Filter enumVal(IntVar ivar, Action action) {
        return new Filter(new Member(ivar, action), unsafeEnum(ivar, action));
    }

    /**
     * Build the following heuristic val: FastEnumVal(ivar)
     *
     * @param ivar   variable to enumerate
     * @param action action to apply to FastEnumVal
     * @return a {@link Filter}
     */
    public static HeuristicVal fastenumVal(IntVar ivar, Action action) {
        return new FastEnumVal(ivar, action);
    }

    /**
     * Build the following heuristic val: Filter(Member(domain), UnsafeEnum(domain))
     *
     * @param ivar  domain of the variable
     * @param from  starting value
     * @param delta gap
     * @param to    ending value
     * @return a {@link Filter}
     */
    public static Filter enumVal(IntVar ivar, int from, int delta, int to) {
        return new Filter(new Member(ivar), unsafeEnum(from, delta, to));
    }

    /**
     * Build the following heuristic val: Filter(Member(domain, action), UnsafeEnum(domain, action)
     *
     * @param ivar   domain of the variable
     * @param from   starting value
     * @param delta  gap
     * @param to     ending value
     * @param action action of Member and UnsafeEnum
     * @return a {@link Filter}
     */
    public static Filter enumVal(IntVar ivar, int from, int delta, int to, Action action) {
        return new Filter(new Member(ivar, action), unsafeEnum(from, delta, to, action));
    }

    public static Random random(IntVar ivar, long seed) {
        return new Random(ivar, seed);
    }
}
