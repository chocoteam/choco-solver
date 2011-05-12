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

import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.requests.list.IRequestList;
import solver.search.loop.monitors.ISearchMonitor;
import solver.variables.IntVar;

/**
 * Naive implementation of
 * "Boosting systematic search by weighting constraints"
 * F.Boussemart, F.Hemery, C.Lecoutre and L.Sais
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/05/11
 */
public final class DomOverWDeg extends AbstractSorter<IntVar> implements ISearchMonitor {

    protected DomOverWDeg() {
    }

    private int weight(IntVar v) {
        IRequestList requests = v.getRequests();
        int w = 0;
        int card = requests.cardinality();
        for (int i = 0; i < card; i++) {
            Propagator prop = requests.get(i).getPropagator();
            if (prop.arity() > 1) {
                w += prop.getFails();
            }
        }
        return w;
    }


    @Override
    public int compare(IntVar o1, IntVar o2) {
        int w1 = weight(o1);
        int w2 = weight(o2);
        int s1 = o1.getDomainSize();
        int s2 = o2.getDomainSize();
        int d1 = o1.nbRequests();
        int d2 = o2.nbRequests();
        return (s1 * w2 * d2) - (s2 * w1 * d1);
    }

    @Override
    public void onContradiction() {
        if (ContradictionException.EXCEPTION.c != null) {
            ContradictionException.EXCEPTION.c.incFail();
        }
    }

    @Override
    public void beforeDownLeftBranch() {
    }

    @Override
    public void beforeDownRightBranch() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void beforeInitialize() {
    }

    @Override
    public void afterInitialize() {
    }

    @Override
    public void beforeInitialPropagation() {
    }

    @Override
    public void afterInitialPropagation() {
    }

    @Override
    public void beforeOpenNode() {
    }

    @Override
    public void afterOpenNode() {
    }

    @Override
    public void onSolution() {
    }

    @Override
    public void afterDownLeftBranch() {
    }

    @Override
    public void afterDownRightBranch() {
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
    }

    @Override
    public void beforeClose() {
    }

    @Override
    public void afterClose() {
    }
}
