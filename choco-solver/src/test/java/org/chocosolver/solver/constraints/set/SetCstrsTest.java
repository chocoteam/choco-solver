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
import org.chocosolver.solver.variables.VariableFactory;
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
		IntVar[] v2 = eqFilter("all_equal");
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
		IntVar x = VariableFactory.enumerated("x", 0, 10, s);
		IntVar y = VariableFactory.enumerated("y", 0, 10, s);
		// set view of A
		SetVar xset = VariableFactory.set("x as a set", 0, 10, s);
		SetVar yset = VariableFactory.set("y as a set", 0, 10, s);
		s.post(SCF.int_values_union(new IntVar[]{x},xset));
		s.post(SCF.int_values_union(new IntVar[]{y},yset));
		// X +9 <= Y or Y + 9 <= X
		SetVar Xleft = VariableFactory.set("", 0, 10, s);
		SetVar tmpLeft = VariableFactory.set("", 9, 19, s);
		s.post(SCF.offSet(Xleft,tmpLeft,9));
		SetVar Yleft = VariableFactory.set("", 0, 10, s);
		s.post(eq(tmpLeft, Yleft,mode));

		SetVar Yright = VariableFactory.set("",0, 10, s);
		SetVar tmpRight = VariableFactory.set("", 9, 19, s);
		s.post(SCF.offSet(Yright,tmpRight,9));
		SetVar Xright = VariableFactory.set("", 0, 10, s);
		s.post(eq(tmpRight, Xright,mode));

		//
		s.post(SCF.union(new SetVar[]{Xleft, Xright}, xset));
		s.post(SCF.union(new SetVar[]{Yleft,Yright},yset));
		// link to booleans
		BoolVar b1 = SCF.notEmpty(Yleft).reif();
		BoolVar b2 = SCF.notEmpty(Yright).reif();
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
			case "offset":return SCF.offSet(x, y, 0);
			default:
			case "all_equal":return SCF.all_equal(x, y);
		}
	}
}
