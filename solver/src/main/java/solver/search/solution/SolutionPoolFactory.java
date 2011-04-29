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

package solver.search.solution;

import solver.Solver;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <br/>
 *
 * @author Arnaud Malapert
 * @author Charles Prud'homme
 * @since 19 juil. 2010
 */
public class SolutionPoolFactory {

    private SolutionPoolFactory() {}

    public static ISolutionPool makeNoSolutionPool(){
        return NoSolutionPool.SINGLETON;
    }

    public static ISolutionPool makeOneSolutionPool(){
        return new OneSolutionPool();
    }

    public static ISolutionPool makeInfiniteSolutionPool(){
        return new InfiniteSolutionPool();
    }

    public static ISolutionPool makeSolutionPool(int capacity){
        switch (capacity){
            case 0:
                return makeNoSolutionPool();
            case 1:
                return makeOneSolutionPool();
            default:
                return makeInfiniteSolutionPool();
        }
    }
}

abstract class AbstractSolutionPool implements ISolutionPool {

	protected AbstractSolutionPool() {
		super();
	}

	@Override
	public final boolean isEmpty() {
		return size() == 0;
	}


	@Override
	public void clear() {}

}


final class NoSolutionPool extends AbstractSolutionPool {

	protected final static NoSolutionPool SINGLETON = new NoSolutionPool();



	protected NoSolutionPool() {
		super();
	}


	@Override
	public List<Solution> asList() {
		return Collections.emptyList();
	}

	@Override
	public void recordSolution(Solver solver) {}

    @Override
    public Solution getBest() {
        return null;
    }

    @Override
	public long size() {
		return 0;
	}


}

final class OneSolutionPool extends AbstractSolutionPool {

	private Solution solution;

	protected OneSolutionPool() {
		super();
	}

	@Override
	public List<Solution> asList() {
		return isEmpty() ? Collections.<Solution>emptyList() : Arrays.asList(solution);
	}

	@Override
	public Solution getBest() {
		return isEmpty() ? null : solution;
	}

	@Override
	public void recordSolution(Solver solver) {
        try{
            solution.replace(solver);
        }catch (NullPointerException ex){
            solution = new Solution(solver);
        }
	}

    @Override
    public long size() {
        return solution == null?0:1;
    }
}


class InfiniteSolutionPool extends AbstractSolutionPool {

	/**
	 * The historical record of solutions that were found
	 */
	protected final LinkedList<Solution> solutions = new LinkedList<Solution>();


	protected InfiniteSolutionPool() {
		super();
	}

	@Override
	public final List<Solution> asList() {
		return Collections.unmodifiableList(solutions);
	}

	@Override
	public void clear() {
		solutions.clear();
	}

	@Override
	public Solution getBest() {
		return solutions.peekFirst();
	}

	@Override
	public void recordSolution(Solver solver) {
		final Solution sol = new Solution(solver);
		solutions.addFirst(sol);
	}

    @Override
    public long size() {
        return solutions.size();
    }
}