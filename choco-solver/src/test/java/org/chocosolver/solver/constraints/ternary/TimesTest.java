/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

	@Test(groups="1s")
	public void testJL2(){
		for(int i = 1 ; i < 100001; i*=10) {
			System.out.printf("%d\n", 465 * i);
			Solver s = new Solver();
			IntVar i1 = VF.enumerated("i1", 0, 465 * i, s);
			IntVar i2 = VF.enumerated("i2", 0, 465 * i, s);
			s.post(ICF.times(i1, 465 * i, i2));
			s.findAllSolutions();
			Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);
		}
	}

	@Test(groups="1s")
	public void testJL3(){
		for(int i = 1 ; i < 1000001; i*=10) {
			System.out.printf("%d\n", 465 * i);
			Solver s = new Solver();
			IntVar i1 = VF.bounded("i1", 0, 465 * i, s);
			IntVar i2 = VF.bounded("i2", 0, 465 * i, s);
			s.post(ICF.times(i1, 465 * i, i2));
			s.findAllSolutions();
			Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);
		}
	}

	@Test(groups="1s")
	public void testJL4() {
		Solver s = new Solver();
		IntVar i1 = VF.enumerated("i1", 0, 465, s);
		IntVar i2 = VF.enumerated("i2", 0, 465 * 10000, s);
		s.post(ICF.times(i1, 10000, i2));
		s.findAllSolutions();
		Assert.assertEquals(s.getMeasures().getSolutionCount(), 466);
	}
	@Test(groups="1s")
	public void testJL5(){
		Solver s = new Solver();
		IntVar i1 = VF.bounded("i1", Integer.MIN_VALUE /10, Integer.MAX_VALUE /10, s);
		IntVar i2 = VF.bounded("i2", Integer.MIN_VALUE/10, Integer.MAX_VALUE/10, s);
		s.post(ICF.times(i1, 10000, i2));
		s.findAllSolutions();
		Assert.assertEquals(s.getMeasures().getSolutionCount(), Integer.MAX_VALUE/100000 * 2 + 1);
	}

}
