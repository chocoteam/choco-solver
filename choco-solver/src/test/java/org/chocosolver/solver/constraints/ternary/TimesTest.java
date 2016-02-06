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

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.System.out;
import static org.chocosolver.solver.variables.events.PropagatorEventType.FULL_PROPAGATION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

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
    protected Constraint make(IntVar[] vars, Model model) {
        return model.times(vars[0], vars[1], vars[2]);
    }

	@Test(groups="1s", timeOut=60000)
	public void testJL() {
		Model s = new Model();
		IntVar a = s.intVar("a", 0, 3, false);
		IntVar b = s.intVar("b", -3, 3, false);

		IntVar z = s.intVar("z", 3, 4, false);
		s.arithm(z, "=", 3).post();
		Constraint c = s.times(a, b, z);
		c.post();
		try {
			s.propagate();
			assertFalse(a.contains(0));
			for (Propagator p : c.getPropagators()) {
				p.propagate(FULL_PROPAGATION.getMask());
			}
			assertFalse(a.contains(0));
		} catch (ContradictionException e) {
			assertFalse(true);
		}
	}

	@Test(groups="10s", timeOut=60000)
	public void testJL2(){
		for(int i = 1 ; i < 100001; i*=10) {
			out.printf("%d\n", 465 * i);
			Model s = new Model();
			IntVar i1 = s.intVar("i1", 0, 465 * i, false);
			IntVar i2 = s.intVar("i2", 0, 465 * i, false);
			s.times(i1, 465 * i, i2).post();
			while (s.solve()) ;
			assertEquals(s.getMeasures().getSolutionCount(), 2);
		}
	}

	@Test(groups="1s", timeOut=60000)
	public void testJL3(){
		for(int i = 1 ; i < 1000001; i*=10) {
			out.printf("%d\n", 465 * i);
			Model s = new Model();
			IntVar i1 = s.intVar("i1", 0, 465 * i, true);
			IntVar i2 = s.intVar("i2", 0, 465 * i, true);
			s.times(i1, 465 * i, i2).post();
			while (s.solve()) ;
			assertEquals(s.getMeasures().getSolutionCount(), 2);
		}
	}

	@Test(groups="10s", timeOut=60000)
	public void testJL4() {
		Model s = new Model();
		IntVar i1 = s.intVar("i1", 0, 465, false);
		IntVar i2 = s.intVar("i2", 0, 465 * 10000, false);
		s.times(i1, 10000, i2).post();
		while (s.solve()) ;
		assertEquals(s.getMeasures().getSolutionCount(), 466);
	}
	@Test(groups="1s", timeOut=60000)
	public void testJL5() {
		Model s = new Model();
		IntVar i1 = s.intVar("i1", MIN_VALUE / 10, MAX_VALUE / 10, true);
		IntVar i2 = s.intVar("i2", MIN_VALUE / 10, MAX_VALUE / 10, true);
		s.times(i1, 10000, i2).post();
		while (s.solve()) ;
		assertEquals(s.getMeasures().getSolutionCount(), MAX_VALUE / 100000 * 2 + 1);
	}

	@Test(groups="1s", timeOut=60000)
	public void testJL6() {
		Model s = new Model();
		s.set(new Settings() {
			@Override
			public boolean enableTableSubstitution() {
				return false;
			}
		});
		IntVar i1 = s.intVar("i1", new int[]{1, 55000});
		IntVar i2 = s.intVar("i2", new int[]{1, 55000});
		IntVar i3 = s.intVar("i3", new int[]{1, 55000});
		s.times(i1, i2, i3).post();
	}

    @Test(groups="1s", timeOut=60000)
    public void testJL7() {
		Model s = new Model();
		s.set(new Settings() {
			@Override
			public boolean enableTableSubstitution() {
				return true;
			}
		});
		IntVar i1 = s.intVar("i1", new int[]{1, 10000});
		IntVar i2 = s.intVar("i2", new int[]{1, 10000});
		IntVar i3 = s.intVar("i3", new int[]{1, 10000});
		s.times(i1, i2, i3).post();
	}

}
