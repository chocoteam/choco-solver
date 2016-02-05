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
package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Jean-Guillaume Fages
 * @since 22/04/15
 * Created by IntelliJ IDEA.
 */
public class ImpactTest {

	@Test(groups="10s", timeOut=60000)
	public void testCostas() {
		Model s1 = costasArray(7,false);
		Model s2 = costasArray(7,true);

		s1.findAllSolutions();
		System.out.println(s1.getMeasures().getSolutionCount());

		s2.findAllSolutions();

		System.out.println(s2.getMeasures().getSolutionCount());
		Assert.assertEquals(s1.getMeasures().getSolutionCount(), s2.getMeasures().getSolutionCount());
	}

	private Model costasArray(int n, boolean impact){
		Model model = ProblemMaker.makeCostasArrays(n);
		IntVar[] vectors = (IntVar[]) model.getHook("vectors");
		model.set(ISF.domOverWDeg(vectors, 0));
		SMF.limitTime(model, 20000);

		if(impact){
			model.set(ISF.impact(vectors, 0));
		}else{
			model.set(ISF.domOverWDeg(vectors, 0));
		}
		return model;
	}
}
