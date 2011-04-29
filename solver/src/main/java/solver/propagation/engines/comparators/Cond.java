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
package solver.propagation.engines.comparators;

import solver.propagation.engines.comparators.predicate.Predicate;
import solver.views.IView;

import java.io.Serializable;
import java.util.Comparator;

public class Cond implements Comparator<IView>, Serializable {
    Predicate p;
    Comparator<IView> c1;
    Comparator<IView> c2;

    public Cond(Predicate p, Comparator<IView> c1, Comparator<IView> c2) {
        this.p = p;
        this.c1 = c1;
        this.c2 = c2;
    }

    public int compare(IView v1, IView v2) {
        boolean b1 = p.eval(v1);
        boolean b2 = p.eval(v2);
        if (b1 && b2) {
            return c1.compare(v1, v2);
        }
        if (!b1 && !b2) {
            return c2.compare(v1, v2);
        }
        if (b1 && !b2) {
            return -1;
        }
        //if (!b1 && b2) {
        return 1;
        //}
    }

    public String toString() {
        return "Cond(" + p + "," + c1 + "," + c2 + ")";
    }
}
