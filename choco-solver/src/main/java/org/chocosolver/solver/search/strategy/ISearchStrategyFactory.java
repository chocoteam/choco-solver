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
package org.chocosolver.solver.search.strategy;

import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.GreedyBranching;
import org.chocosolver.solver.search.strategy.strategy.LastConflict;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;

public interface ISearchStrategyFactory extends IRealStrategyFactory, ISetStrategyFactory, IIntStrategyFactory{

	// ************************************************************************************
	// GENERIC PATTERNS
	// ************************************************************************************

	/**
	 * Use the last conflict heuristic as a pluggin to improve a former search heuristic
	 * Should be set after specifying a search strategy.
	 * @return last conflict strategy
	 */
	default void useLastConflict() {
		_me().set(new LastConflict(_me().getModel(), _me().getStrategy(),1));
	}

	/**
	 * Make the input search strategy greedy, that is, decisions can be applied but not refuted.
	 * @param search a search heuristic building branching decisions
	 * @return a greedy form of search
	 */
	default AbstractStrategy greedySearch(AbstractStrategy search){
		return new GreedyBranching(search);
	}

	default AbstractStrategy sequencer(AbstractStrategy... searches){
		return new StrategiesSequencer(searches);
	}
}
