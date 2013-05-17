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
 * A factory to define solution pool (extending {@link ISolutionPool}).
 * A solution pool stores solutions found during the resolution process.
 * <p/>
 * There are 3 of them:
 * <br/>- {@link #NO_SOLUTION}: no solution is <b>stored</b> during the resolution process.
 * So, found solutions cannot be explored nor restored once the search ends.
 * <br/>- {@link #LAST_ONE}: stores the <b>last</b> solution found. Previous ones are erased.
 * <br/>- {@link #ALL}: stores all solutions found during the resolution process. If a objective value is declared,
 * the last solution corresponds to the best solution found so far.
 *
 * @author Arnaud Malapert
 * @author Charles Prud'homme
 * @see ISolutionPool
 * @since 19 juil. 2010
 */
public enum SolutionPoolFactory {

    NO_SOLUTION {
        @Override
        public ISolutionPool make() {
            return NoSolutionPool.SINGLETON;
        }
    },
    LAST_ONE {
        @Override
        public ISolutionPool make() {
            return new OneSolutionPool();
        }
    },
    ALL {
        @Override
        public ISolutionPool make() {
            return new InfiniteSolutionPool();
        }
    };

    public abstract ISolutionPool make();
}

final class NoSolutionPool implements ISolutionPool {

    protected final static NoSolutionPool SINGLETON = new NoSolutionPool();


    protected NoSolutionPool() {
        super();
    }


    @Override
    public List<Solution> asList() {
        return Collections.emptyList();
    }

    @Override
    public void recordSolution(Solver solver) {
    }

    @Override
    public Solution getBest() {
        return null;
    }

    @Override
    public void restoreBest() {
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public long size() {
        return 0;
    }


}

final class OneSolutionPool implements ISolutionPool {

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
        try {
            solution.replace(solver);
        } catch (NullPointerException ex) {
            solution = new Solution(solver);
        }
    }

    @Override
    public void restoreBest() {
        getBest().restore();
    }

    @Override
    public void clear() {
        solution = null;
    }

    @Override
    public boolean isEmpty() {
        return solution == null;
    }

    @Override
    public long size() {
        return solution == null ? 0 : 1;
    }
}


class InfiniteSolutionPool implements ISolutionPool {

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
    public boolean isEmpty() {
        return solutions.isEmpty();
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
    public void restoreBest() {
        getBest().restore();
    }

    @Override
    public long size() {
        return solutions.size();
    }
}