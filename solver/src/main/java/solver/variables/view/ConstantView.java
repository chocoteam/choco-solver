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

package solver.variables.view;

import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.requests.IRequest;
import solver.requests.list.IRequestList;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.heuristics.zeroary.Empty;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.NoDelta;
import solver.variables.domain.CsteDomain;
import solver.variables.domain.IIntDomain;

/**
 * A IntVar with one domain value.
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations",
 * C. Schulte and G. Tack
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public class ConstantView extends AbstractVariable implements IntVar {

    protected final int constante;
    protected final String name;
    protected final IIntDomain domain;
    protected final Solver solver;

    protected int uniqueID;

    private DisposableValueIterator _viterator;

    private DisposableRangeIterator _riterator;

    public ConstantView(String name, int constante, Solver solver) {
        super(name, solver);
        this.name = name;
        this.solver = solver;
        this.constante = constante;
        this.domain = new CsteDomain(constante);
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        if (value == constante) {
            this.contradiction(cause, "unique value removal");
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= constante && constante <= to) {
            this.contradiction(cause, "unique value removal");
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        if (value != constante) {
            this.contradiction(cause, "outside domain instantitation");
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        if (value > constante) {
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        if (value < constante) {
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean contains(int value) {
        return constante == value;
    }

    @Override
    public boolean instantiatedTo(int value) {
        return constante == value;
    }

    @Override
    public int getValue() {
        return constante;
    }

    @Override
    public int getLB() {
        return constante;
    }

    @Override
    public int getUB() {
        return constante;
    }

    @Override
    public int getDomainSize() {
        return 1;
    }

    @Override
    public int nextValue(int v) {
        if (v < constante) {
            return constante;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int previousValue(int v) {
        if (v > constante) {
            return constante;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return true;
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        //useless
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return Empty.get();
    }

    @Override
    public void desactivate(IRequest request) {
        //useless
    }

    @Override
    public boolean instantiated() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addRequest(IRequest request) {
        //useless
    }

    @Override
    public void deleteRequest(IRequest request) {
        //useless
    }

    @Override
    public IRequestList getRequests() {
        return null;
    }

    @Override
    public int nbConstraints() {
        //who cares?
        return 0;
    }

    @Override
    public Explanation explain(int what) {
        return null;
    }

    @Override
    public int nbRequests() {
        return 0;
    }

    @Override
    public void updatePropagationConditions(Propagator propagator, int idxInProp) {
    }

    @Override
    public void deletePropagator(Propagator observer) {
    }

    @Override
    public void notifyPropagators(EventType eventType, ICause o) throws ContradictionException {
    }

    @Override
    public void attachPropagator(Propagator propagator, int idxInProp) {
    }

    @Override
    public String toString() {
        return name + "=" + String.valueOf(constante);
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public Solver getSolver() {
        return solver;
    }

    @Override
    public int getType() {
        return Variable.INTEGER;
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                boolean _next;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    _next = true;
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    _next = true;
                }

                @Override
                public boolean hasNext() {
                    return _next;
                }

                @Override
                public boolean hasPrevious() {
                    return _next;
                }

                @Override
                public int next() {
                    _next = false;
                    return constante;
                }

                @Override
                public int previous() {
                    _next = false;
                    return constante;
                }

            };
        }
        if (bottomUp) {
            _viterator.bottomUpInit();
        } else {
            _viterator.topDownInit();
        }
        return _viterator;
    }

    @Override
    public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
        if (_riterator == null || !_riterator.isReusable()) {
            _riterator = new DisposableRangeIterator() {
                boolean _next;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    _next = true;
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    _next = true;
                }

                @Override
                public boolean hasNext() {
                    return _next;
                }

                @Override
                public boolean hasPrevious() {
                    return _next;
                }

                @Override
                public void next() {
                    _next = false;
                }

                @Override
                public void previous() {
                    _next = false;
                }

                @Override
                public int min() {
                    return constante;
                }

                @Override
                public int max() {
                    return constante;
                }
            };
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }
}
