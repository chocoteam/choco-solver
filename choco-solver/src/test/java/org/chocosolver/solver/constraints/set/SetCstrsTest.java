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
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jean-Guillaume Fages
 * @since 22/01/16
 * Created by IntelliJ IDEA.
 */
public class SetCstrsTest {

	@Test(groups="1s", timeOut=60000)
	public static void testEq() {
		IntVar[] v1 = eqFilter("offset");
		IntVar[] v2 = eqFilter("allEqual");
		for(int i=0;i<v1.length;i++) {
			Assert.assertEquals(v1[i].getDomainSize(), v2[i].getDomainSize());
			for(int v=v1[i].getLB();v<=v1[i].getUB();v=v1[i].nextValue(v)){
				Assert.assertTrue(v2[i].contains(v));
			}
		}
		v1[0].getSolver().findAllSolutions();
		v2[0].getSolver().findAllSolutions();
		Assert.assertEquals(
				v1[0].getSolver().getMeasures().getSolutionCount(),
				v2[0].getSolver().getMeasures().getSolutionCount()
		);
	}

	public static IntVar[] eqFilter(String mode){
		Solver s = new Solver();
		IntVar x = s.intVar("x", 0, 10, false);
		IntVar y = s.intVar("y", 0, 10, false);
		// set view of A
		SetVar xset = s.setVar("x as a set", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9,10});
		SetVar yset = s.setVar("y as a set", new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9,10});
		s.post(s.union(new IntVar[]{x},xset));
		s.post(s.union(new IntVar[]{y},yset));
		// X +9 <= Y or Y + 9 <= X
		SetVar Xleft = s.setVar(new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9,10});
		SetVar tmpLeft = s.setVar(new int[]{}, new int[]{9,10,11,12,13,14,15,16,17,18,19});
		s.post(s.offSet(Xleft,tmpLeft,9));
		SetVar Yleft = s.setVar("",  new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9,10});
		s.post(eq(tmpLeft, Yleft,mode));

		SetVar Yright = s.setVar(new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9,10});
		SetVar tmpRight = s.setVar(new int[]{}, new int[]{9,10,11,12,13,14,15,16,17,18,19});
		s.post(s.offSet(Yright,tmpRight,9));
		SetVar Xright = s.setVar(new int[]{}, new int[]{0,1,2,3,4,5,6,7,8,9,10});
		s.post(eq(tmpRight, Xright,mode));

		//
		s.post(s.union(new SetVar[]{Xleft, Xright}, xset));
		s.post(s.union(new SetVar[]{Yleft,Yright},yset));
		// link to booleans
		BoolVar b1 = s.notEmpty(Yleft).reify();
		BoolVar b2 = s.notEmpty(Yright).reify();
		// ---
		SatFactory.addBoolOrArrayEqualTrue(new BoolVar[]{b1, b2});
		Chatterbox.showStatistics(s);
		Chatterbox.showSolutions(s);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		System.out.println(mode);
		System.out.println(x);
		System.out.println(y);
		System.out.println("%%%%%%");
		return new IntVar[]{x,y};
	}

	public static Constraint eq(SetVar x, SetVar y, String mode){
		switch (mode){
			case "offset":return x.getSolver().offSet(x, y, 0);
			default:
			case "allEqual":return x.getSolver().allEqual(x, y);
		}
	}
}
