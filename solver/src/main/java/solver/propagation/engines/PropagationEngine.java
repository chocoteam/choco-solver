/**
 *  Copyright (c) 2010, Ecole des Mines de Nantes
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

package solver.propagation.engines;

import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.engines.comparators.IncrArityP;
import solver.propagation.engines.comparators.IncrPriorityP;
import solver.propagation.engines.comparators.predicate.Predicate;
import solver.propagation.engines.group.Group;
import solver.views.IView;

import java.util.Arrays;
import java.util.Comparator;

/**
 * An implementation of <code>IPropagationEngine</code>.
 * It deals with 2 main types of <code>IView</code>s.
 * Ones are intialized one (at the end of the list).
 * <p/>
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 28 oct. 2010
 */
public final class PropagationEngine implements IPropagationEngine {


    protected IView[] views = new IView[16];

    protected int offset = 0; // index of the last "common" view -- after that index, one can find initialization views

    protected int size = 0;

    protected IView lastPoppedView;

    protected Comparator<IView> comparator = IncrArityP.get();

    protected Policy policy = Policy.FIXPOINT;

    protected Group[] groups = new Group[8];

    protected IEngine engine;

    protected int nbGroup = 0;

    protected Deal deal = Deal.SEQUENCE;

    /**
     * Initialize this <code>IPropagationEngine</code> object with the array of <code>Constraint</code> and <code>Variable</code> objects.
     * It automatically pushes an event (call to <code>propagate</code>) for each constraints, the initial awake.
     */
    public void init() {
        IView[] tmp = views;
        views = new IView[size];
        System.arraycopy(tmp, 0, views, 0, size);

        Arrays.sort(views, offset, size, IncrPriorityP.get()); // first, sort initialization views
        IView view;
        int i;
        // initialization views are also enqued to be treated at initial propagation
        for (i = offset; i < size; i++) {
            view = views[i];
            view.setIndex(i);
            view.enqueue();
        }
        addGroup(new Group(Predicate.TRUE, comparator, policy));
        // first we set view to one group
        int j;
        for (i = 0; i < offset; i++) {
            lastPoppedView = views[i];
            j = 0;
            // look for the first right group
            while (!groups[j].getPredicate().eval(lastPoppedView)) {
                j++;
            }
            groups[j].addView(lastPoppedView);
        }
        // then intialize groups
        for (j = 0; j < nbGroup; j++) {
            groups[j].make();
        }


//        addGroup(new Group(Predicate.TRUE, comparator, policy));
//        Comparator<IView> _comp = comparator;
//        for (i = nbGroup - 2; i >= 0; i--) {
//            _comp = new Cond(groups[i].getPredicate(), groups[i].getComparator(), _comp);
//        }
//        Arrays.sort(views, 0, offset, _comp);
//        int from = 0;
//        int to = 0;
//        int idxInG = 0;
//        int g = 0;
//        while (to < offset) {
//            lastPoppedView = views[to];
//            if (!groups[g].getPredicate().eval(lastPoppedView)) {
//                groups[g].make(Arrays.copyOfRange(views, from, to), g);
//                g++;
//                from = to;
//                idxInG = 0;
//            }
//            lastPoppedView.setGroup(g);
//            lastPoppedView.setIndex(idxInG++);
//            to++;
//        }
//        groups[g].make(Arrays.copyOfRange(views, from, to), g);
//        nbGroup = ++g;

        switch (deal) {
            case SEQUENCE:
                engine = new WhileEngine(Arrays.copyOfRange(groups, 0, nbGroup));
                break;
            case QUEUE:
                engine = new OldestEngine(Arrays.copyOfRange(groups, 0, nbGroup));
                break;
        }


    }

    @Override
    public void addConstraint(Constraint constraint) {
        int nbV = 0;
        int nbI = 0;
        Propagator[] props = constraint.propagators;
        nbI += props.length;
        for (int p = 0; p < nbI; p++) {
            nbV += props[p].nbViews();
        }
        IView[] tmp = views;
        // ensure capacity
        while (views.length < size + (nbV + nbI)) {
            views = new IView[tmp.length * 2];
            System.arraycopy(tmp, 0, views, 0, size);
            tmp = views;
        }
        System.arraycopy(tmp, offset, views, offset + nbV, size - offset);

        int k = size + nbV;
        for (int p = 0; p < props.length; p++, k++) {
            Propagator prop = props[p];
            // "common" views
            for (int i = 0; i < prop.nbViews(); i++, offset++) {
                views[offset] = prop.getView(i);
                views[offset].setPropagationEngine(this);
            }
            // intitialization view
            views[k] = prop.getView(-1);
            views[k].setPropagationEngine(this);
        }
        size = k;
    }

    @Override
    public void addGroup(Group group) {
        if (groups.length == nbGroup) {
            Group[] tmp = groups;
            groups = new Group[nbGroup * 2];
            System.arraycopy(tmp, 0, groups, 0, nbGroup);
        }
        groups[nbGroup] = group;
        group.setIndex(nbGroup++);

    }

    @Override
    public void deleteGroups() {
        nbGroup = 0;
    }

    @Override
    public void setDeal(Deal deal) {
        this.deal = deal;
    }

    @Override
    public void setDefaultComparator(Comparator<IView> comparator) {
        this.comparator = comparator;
    }

    @Override
    public Comparator<IView> getDefaultComparator() {
        return comparator;
    }

    @Override
    public void setDefaultPolicy(Policy policy) {
        this.policy = policy;
    }

    @Override
    public void initialPropagation() throws ContradictionException {
        for (int i = offset; i < size; i++) {
            lastPoppedView = views[i];
            lastPoppedView.deque();
            lastPoppedView.filter();
        }
        engine.fixPoint();
    }


    @Override
    public void fixPoint() throws ContradictionException {
        engine.fixPoint();
    }

    @Override
    public void update(IView view) {
        engine.update(view);

    }

    @Override
    public void remove(IView view) {
        engine.remove(view);
    }

    @Override
    public void flushAll() {
        engine.flushAll();
    }

    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append(groups[0].toString());
        for (int i = 1; i < nbGroup; i++) {
            st.append(",").append(groups[i].toString());
        }
        return st.toString();
    }


    @Override
    public int getNbViews() {
        return size;
    }

    public long pushed() {
        long _p = 0;
        for (int i = nbGroup - 1; i >= 0; i--) {
            _p += groups[i].getReacher().pushed();
        }
        return _p;
    }

    public long popped() {
        long _p = 0;
        for (int i = nbGroup - 1; i >= 0; i--) {
            _p += groups[i].getReacher().popped();
        }
        return _p;
    }

    public long updated() {
        long _p = 0;
        for (int i = nbGroup - 1; i >= 0; i--) {
            _p += groups[i].getReacher().updated();
        }
        return _p;
    }

}
