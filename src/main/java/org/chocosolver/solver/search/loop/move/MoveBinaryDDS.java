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
package org.chocosolver.solver.search.loop.move;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;

/**
 * A move dedicated to run an Depth-bounded Discrepancy Search[1] (DDS) with binary decisions.
 * <p>
 * [1]:T. Walsh, Depth-bounded Discrepancy Search, IJCAI-97.
 * <p>
 * <p>
 * Note that the depth is not maintained since it is useful only when max discrepancy is greater than max depth,
 * which should not happen.
 * Created by cprudhom on 07/10/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 3.3.1
 */
public class MoveBinaryDDS extends MoveBinaryLDS {

    /**
     * Create a DFS with binary decisions
     *
     * @param strategy    how (binary) decisions are selected
     * @param discrepancy maximum discrepancy
     * @param environment backtracking environment
     */
    public MoveBinaryDDS(AbstractStrategy strategy, int discrepancy, IEnvironment environment) {
        super(strategy, discrepancy, environment);
    }

    @Override
    public boolean extend(Solver solver) {
        boolean extended = false;
        Decision current = strategy.getDecision();
        if (current != null) { // null means there is no more decision
            solver.getDecisionPath().pushDecision(current);
            solver.getEnvironment().worldPush();
            if (dis.get() == 1) {
                solver.getDecisionPath().getLastDecision().buildNext();
            }
            dis.add(-1);
            extended = true;
        }
        return extended;
    }
}
