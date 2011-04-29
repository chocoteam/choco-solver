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
package solver.propagation.engines.group;

import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.Queue;
import solver.propagation.engines.comparators.predicate.Predicate;
import solver.views.IView;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A class to group views and define a policy to propagate.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/04/11
 */
public class Group {

    protected Comparator<IView> comparator;

    protected Predicate predicate;

    protected Policy policy = Policy.FIXPOINT;

    protected AFixpointReacher reacher;

    protected int index;

    protected IView[] views;
    protected int nbViews;

    public Group(Predicate predicate, Comparator<IView> comparator, Policy policy) {
        this.comparator = comparator;
        this.predicate = predicate;
        this.policy = policy;
        views = new IView[8];
        nbViews = 0;
    }

    public Comparator<IView> getComparator() {
        return comparator;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public final AFixpointReacher getReacher() {
        return reacher;
    }

    public void make() {
        views = Arrays.copyOf(views, nbViews);
        Arrays.sort(views, comparator);
        for (int i = 0; i < nbViews; i++) {
            views[i].setIndex(i);
        }
        if (comparator == Queue.get()) {
            reacher = new Oldest(nbViews);
        } else {
            switch (policy) {
                case ONE:
                    reacher = new One(views, comparator);
                    break;
                case ITERATE:
                    reacher = new Iterate(views, comparator);
                    break;
                case FIXPOINT:
                default:
                    reacher = new Fixpoint(views, comparator);
                    break;
            }
        }
    }

    public void addView(IView aView) {
        //ensure capacity
        if (nbViews + 1 > views.length) {
            IView[] tmp = views;
            int size = views.length * 2;
            views = new IView[size];
            System.arraycopy(tmp, 0, views, 0, nbViews);
        }
        views[nbViews++] = aView;
        aView.setGroup(index);
    }

    @Override
    public String toString() {
        return "[" + reacher.toString() + "]";
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
