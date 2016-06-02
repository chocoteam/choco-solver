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

package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Jean-Guillaume Fages
 */
public class BinPackingTest {

	@DataProvider(name = "params")
	public Object[][] data1D(){
		// indicates whether to use explanations or not
		List<Object[]> elt = new ArrayList<>();
		elt.add(new Object[]{true});
		elt.add(new Object[]{false});
		return elt.toArray(new Object[elt.size()][1]);
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void test(boolean decomp) {
		Model model = new Model();
		int[] itemSize = new int[]{2,3,1};
		IntVar[] itemBin = model.intVarArray(3,-1,1);
		IntVar[] binLoad = model.intVarArray(3,-5,5);
		int offset = 0;
		if(decomp){
			bpDecomposition(itemBin,itemSize,binLoad,offset).post();
		}else{
			model.binPacking(itemBin,itemSize,binLoad,offset).post();
		}
		while(model.getSolver().solve()){
			assertTrue(itemBin[0].getValue()>=offset);
			assertTrue(itemBin[1].getValue()>=offset);
			assertTrue(binLoad[0].getValue()>=0);
			assertTrue(binLoad[1].getValue()>=0);
			assertEquals(binLoad[2].getValue(),0);
		}
		assertEquals(6, model.getSolver().getSolutionCount());
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void test2(boolean decomp) {
		Model model = new Model();
		int[] itemSize = new int[]{2,3,1};
		IntVar[] itemBin = model.intVarArray(3,-1,1);
		IntVar[] binLoad = model.intVarArray(3,-5,5);
		int offset = -1;
		if(decomp){
			bpDecomposition(itemBin,itemSize,binLoad,offset).post();
		}else{
			model.binPacking(itemBin,itemSize,binLoad,offset).post();
		}
		while(model.getSolver().solve()){
			assertTrue(itemBin[0].getValue()>=offset);
			assertTrue(itemBin[1].getValue()>=offset);
			assertTrue(binLoad[0].getValue()>=0);
			assertTrue(binLoad[1].getValue()>=0);
			assertTrue(binLoad[2].getValue()>=0);
		}
		assertEquals(24, model.getSolver().getSolutionCount());
	}

	@Test(groups="1s", timeOut=60000, dataProvider = "params")
	public void test3(boolean decomp) {
		Model model = new Model();
		int[] itemSize = new int[]{2,3,1};
		IntVar[] itemBin = model.intVarArray(3,-1,1);
		IntVar[] binLoad = model.intVarArray(3,-5,5);
		int offset = 1;
		if(decomp){
			bpDecomposition(itemBin,itemSize,binLoad,offset).post();
		}else{
			model.binPacking(itemBin,itemSize,binLoad,offset).post();
		}
		System.out.println(model.getSolver().isSatisfied());
		model.getSolver().solve();
		assertEquals(0, model.getSolver().getSolutionCount());
	}

	private static Constraint bpDecomposition(IntVar[] itemBin, int[] itemSize, IntVar[] binLoad, int offset){
		int nbBins = binLoad.length;
		int nbItems = itemBin.length;
		Model s = itemBin[0].getModel();
		BoolVar[][] xbi = s.boolVarMatrix("xbi", nbBins, nbItems);
		int sum = 0;
		for (int is : itemSize) {
			sum += is;
		}
		// constraints
		Constraint[] bpcons = new Constraint[nbItems + nbBins + 1];
		for (int i = 0; i < nbItems; i++) {
			bpcons[i] = s.boolsIntChanneling(ArrayUtils.getColumn(xbi, i), itemBin[i], offset);
		}
		for (int b = 0; b < nbBins; b++) {
			bpcons[nbItems + b] = s.scalar(xbi[b], itemSize, "=", binLoad[b]);
		}
		bpcons[nbItems + nbBins] = s.sum(binLoad, "=", sum);
		return Constraint.merge("BinPacking",bpcons);
	}
}
