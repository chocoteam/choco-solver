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
package solver.propagation.generator.predicate;

import gnu.trove.set.hash.TIntHashSet;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/03/12
 */
public class InCstrSet extends Predicate<Propagator> {

    final TIntHashSet allowIndices;

    public InCstrSet(Constraint... constraints) {
        super(false, true);
        allowIndices = new TIntHashSet();
        add(constraints);
    }

    public void add(Constraint[] constraints) {
        for (int i = 0; i < constraints.length; i++) {
            Propagator[] propagators = constraints[i].propagators;
            for (int j = 0; j < propagators.length; j++) {
                Propagator propagator = propagators[j];
                allowIndices.add(propagator.getId());
            }
        }
    }

    @Override
    public boolean isValid(Propagator element) {
        return allowIndices.contains(element.getId());
    }
}
