/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package solver.explanations.strategies.jumper;

import gnu.trove.list.array.TIntArrayList;
import solver.explanations.BranchingDecision;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.strategies.IDecisionJumper;

import java.util.Random;

/**
 * A decision jumper chooses randomly the number of jumps to do, among decisions involved in the explanation
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/02/13
 */
public class RandomDecisionJumper implements IDecisionJumper {

    Random random;
    TIntArrayList worlds;

    /**
     * Create a random decision jumper
     *
     * @param seed the seed for randomness
     */
    public RandomDecisionJumper(long seed) {
        random = new Random(seed);
        this.worlds = new TIntArrayList();
    }

    @Override
    public int compute(Explanation explanation, int currentWorldIndex) {
        worlds.clear();
        if (explanation.nbDeductions() > 0) {
            for (int d = 0; d < explanation.nbDeductions(); d++) {
                Deduction dec = explanation.getDeduction(d);
                if (dec.getmType() == Deduction.Type.DecLeft) {
                    worlds.add(((BranchingDecision) dec).getDecision().getWorldIndex() + 1);
                }
            }
        }
        int world = worlds.isEmpty() ? 0 : worlds.get(random.nextInt(worlds.size()));
        return 1 + (currentWorldIndex - world);
    }
}
