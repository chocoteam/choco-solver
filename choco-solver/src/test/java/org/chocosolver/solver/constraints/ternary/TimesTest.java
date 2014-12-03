/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/02/11
 */
public class TimesTest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return vx * vy == vz ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Solver solver) {
        return IntConstraintFactory.times(vars[0], vars[1], vars[2]);
    }

	@Test(groups = "1s")
	public void testJL() {
	    Solver s = new Solver();
	    IntVar a = VF.enumerated("a", 0, 3, s);
	    IntVar b = VF.enumerated("b", -3, 3, s);

	    IntVar z = VF.enumerated("z", 3, 4, s);
	    s.post(ICF.arithm(z, "=", 3));
	    Constraint c = ICF.times(a, b, z);
	    s.post(c);
		try {
			s.propagate();
			Assert.assertFalse(a.contains(0));
			for (Propagator p : c.getPropagators()) {
				p.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
			}
			Assert.assertFalse(a.contains(0));
		}catch (ContradictionException e){
			Assert.assertFalse(true);
		}
	}
}
