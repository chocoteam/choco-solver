/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;


import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.IntDelta;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.impl.siglit.SignedLiteral;
import org.chocosolver.util.iterators.*;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.Iterator;
import java.util.function.Consumer;

import static org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils.unionOf;

/**
 * Abstract class for defining integer views on integer variables
 * <br/>
 * "A view implements the same operations as a variable. A view stores a reference to a variable.
 * Invoking an operation on the view executes the appropriate operation on the view's variable."
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public abstract class IntView<I extends IntVar> extends AbstractView<I> implements IntVar {

    /**
     * Observed variable
     */
    protected final I var;

    /**
     * To store removed values
     */
    protected IntDelta delta;

    /**
     * Value iterator
     */
    protected DisposableValueIterator _viterator;

    /**
     * Range iterator
     */
    protected DisposableRangeIterator _riterator;

    /**
     * Value iterator allowing for(int i:this) loops
     */
    private final IntVarValueIterator _javaIterator = new IntVarValueIterator(this);

    /**
     * Signed Literal
     */
    protected SignedLiteral literal;

    /**
     * Create a view based on {@code var}
     * @param name name of the view
     * @param var observed variable
     */
    protected IntView(String name, I var) {
        super(name, var);
        this.var = var;
        this.delta = NoDelta.singleton;
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.INT;
    }

    public I getVariable() {
        return var;
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
    }

    @Override
    public int getRange() {
        return var.getRange();
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain();
    }

    @Override
    public boolean isInstantiated() {
        return getVariable().isInstantiated();
    }

	@Override
    public IDelta getDelta() {
        return var.getDelta();
    }

    @Override
    public void createDelta() {
        var.createDelta();
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        super.notifyPropagators(transformEvent(event), this);
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
            _viterator = new DisposableValueBoundIterator(this);
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
        if (_riterator == null || _riterator.isNotReusable()) {
            _riterator = new DisposableRangeBoundIterator(this);
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }

    @Override
    public Iterator<Integer> iterator() {
        _javaIterator.reset();
        return _javaIterator;
    }

    @Override
    public void forEachIntVar(Consumer<IntVar> action) {
        action.accept(var);
        action.accept(this);
    }

    @Override
    public void createLit(IntIterableRangeSet rootDomain) {
        if (this.literal != null) {
            throw new IllegalStateException("createLit(Implications) called twice");
        }
        this.literal = new SignedLiteral.Set(rootDomain);
    }

    @Override
    public SignedLiteral getLit() {
        if (this.literal == null) {
            throw new NullPointerException("getLit() called on null, a call to createLit(Implications) is required");
        }
        return this.literal;
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntVar pivot = explanation.readVar(p);
        IntVar other = (this == pivot ? getVariable() : this);
        IntIterableRangeSet dom = explanation.complement(other);
        other.unionLit(dom, explanation);
        dom = explanation.complement(pivot);
        unionOf(dom, explanation.readDom(p));
        pivot.intersectLit(dom, explanation);
    }
}
