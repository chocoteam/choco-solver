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

package solver;

/**
 * Test save for sqrt
 */
public class SqrtTests {

//	@Test(groups = "1s")
//	public void testEnum() {
//		Random random = new Random();
//		for (int seed = 0; seed < 2000; seed++) {
//			random.setSeed(seed);
//			int[][] dom = DomainBuilder.buildFullDomains(1, -random.nextInt(15), random.nextInt(15),
//					random, random.nextDouble(), random.nextBoolean());
//			Solver view = viewE(dom, seed);
//			Solver cons = consE(dom, seed);
//			view.findAllSolutions();
//			cons.findAllSolutions();
//			Assert.assertEquals(view.getMeasures().getSolutionCount(), cons.getMeasures().getSolutionCount());
//		}
//	}
//
//	private Solver viewE(int[][] dom, int seed) {
//		Solver solver = new Solver();
//		IntVar A = VariableFactory.enumerated("A", dom[0], solver);
//		IntVar B = new SqrView(A, solver);
//		solver.post(IntConstraintFactory.arithm(B, ">", 0));
////        SearchMonitorFactory.log(solver, true, true);
//		solver.set(IntStrategyFactory.random(new IntVar[]{A, B}, seed));
//		return solver;
//	}
//
//	private Solver consE(int[][] dom, int seed) {
//		Solver solver = new Solver();
//		IntVar A = VariableFactory.enumerated("A", dom[0], solver);
//		TIntHashSet values = new TIntHashSet();
//		for (int i = 0; i < dom[0].length; i++) {
//			values.add(dom[0][i] * dom[0][i]);
//		}
//		int[] dom2 = values.toArray();
//		Arrays.sort(dom2);
//		IntVar B = VariableFactory.enumerated("X", dom2, solver);
//		solver.post(IntConstraintFactory.square(B, A));
//		solver.post(IntConstraintFactory.arithm(B, ">", 0));
////        SearchMonitorFactory.log(solver, true, true);
//		solver.set(IntStrategyFactory.random(new IntVar[]{A, B}, seed));
//		return solver;
//	}
//
//	@Test(groups = "1s")
//	public void testBound() {
//		Random random = new Random();
//		for (int seed = 2; seed < 2000; seed++) {
//			random.setSeed(seed);
//			int[][] dom = DomainBuilder.buildFullDomains(1, -random.nextInt(15), random.nextInt(15));
//			Solver view = viewB(dom, seed);
//			Solver cons = consB(dom, seed);
//			view.findAllSolutions();
//			cons.findAllSolutions();
//			Assert.assertEquals(view.getMeasures().getSolutionCount(), cons.getMeasures().getSolutionCount(), "seed:" + seed);
//		}
//	}
//
//	private Solver viewB(int[][] dom, int seed) {
//		Solver solver = new Solver();
//		int n = dom[0].length - 1;
//		IntVar A = VariableFactory.bounded("A", dom[0][0], dom[0][n], solver);
//		IntVar B = new SqrView(A, solver);
//		solver.post(IntConstraintFactory.arithm(B, ">", 0));
//		SearchMonitorFactory.log(solver, true, true);
//		solver.set(IntStrategyFactory.random(new IntVar[]{A, B}, seed));
//		return solver;
//	}
//
//	private Solver consB(int[][] dom, int seed) {
//		Solver solver = new Solver();
//		int n = dom[0].length - 1;
//		IntVar A = VariableFactory.bounded("A", dom[0][0], dom[0][n], solver);
//		int[] dom2 = new int[2];
//		dom2[0] = dom[0][n] < 0 ? dom[0][n] * dom[0][n] : 0;
//		dom2[1] = Math.max(dom[0][0] * dom[0][0], dom[0][n] * dom[0][1]);
//		IntVar B = VariableFactory.bounded("B", dom2[0], dom2[1], solver);
//		solver.post(IntConstraintFactory.square(B, A));
//		solver.post(IntConstraintFactory.arithm(B, ">", 0));
//		SearchMonitorFactory.log(solver, true, true);
//		solver.set(IntStrategyFactory.random(new IntVar[]{A, B}, seed));
//		return solver;
//	}
//	@Test(groups = "30s")
//	public void test1e() {
//		// Z = X^2
//		for (int seed = 0; seed < 99999; seed++) {
//			Solver ref = new Solver();
//			Solver solver = new Solver();
//			{
//				IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
//				IntVar z = VariableFactory.enumerated("z", 0, 4, ref);
//				ref.post(IntConstraintFactory.times(x, x, z));
//				ref.set(IntStrategyFactory.random(new IntVar[]{x, z}, seed));
//			}
//			{
//				IntVar z = VariableFactory.enumerated("z", 0, 4, solver);
//				IntVar x = new SqrView(z, solver);
//				solver.set(IntStrategyFactory.random(new IntVar[]{x, z}, seed));
//			}
//			check(ref, solver, seed, false, true);
//		}
//	}
//
//	public static void check(Solver ref, Solver solver, long seed, boolean strict, boolean solveAll) {
//		//        SearchMonitorFactory.log(ref, true, true);
//		//        SearchMonitorFactory.log(solver, true, true);
//		if (solveAll) {
//			ref.findAllSolutions();
//			solver.findAllSolutions();
//		} else {
//			//            System.out.printf("%s\n", ref.toString());
//			ref.findSolution();
//			//            System.out.printf("%s\n", solver.toString());
//			solver.findSolution();
//		}
//		Assert.assertEquals(solver.getMeasures().getSolutionCount(),
//				ref.getMeasures().getSolutionCount(), "solutions (" + seed + ")");
//		//        System.out.printf("%d : %d vs. %d  -- ", seed, ref.getMeasures().getNodeCount(),
//		//                solver.getMeasures().getNodeCount());
//		if (strict) {
//			Assert.assertEquals(solver.getMeasures().getNodeCount(), ref.getMeasures().getNodeCount(), "nodes (" + seed + ")");
//		} else {
//			Assert.assertTrue(ref.getMeasures().getNodeCount() >=
//					solver.getMeasures().getNodeCount(), seed + "");
//		}
//		//        System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
//		//                solver.getMeasures().getTimeCount(),
//		//                ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
//	}
//
//	@Test
//	public void testSqr1() {
//		Solver solver = new Solver();
//		IntVar var = new SqrView(VariableFactory.enumerated("b", new int[]{-2, 0, 1, 4}, solver), solver);
//		DisposableValueIterator vit = var.getValueIterator(true);
//		Assert.assertTrue(vit.hasNext());
//		Assert.assertEquals(0, vit.next());
//		Assert.assertTrue(vit.hasNext());
//		Assert.assertEquals(1, vit.next());
//		Assert.assertTrue(vit.hasNext());
//		Assert.assertEquals(4, vit.next());
//		Assert.assertTrue(vit.hasNext());
//		Assert.assertEquals(16, vit.next());
//		Assert.assertFalse(vit.hasNext());
//	}
//
//	@Test
//	public void testSqr2() {
//		Solver solver = new Solver();
//		IntVar var = new SqrView(VariableFactory.enumerated("b", new int[]{-2, 0, 1, 4}, solver), solver);
//		DisposableValueIterator vit = var.getValueIterator(false);
//		Assert.assertTrue(vit.hasPrevious());
//		Assert.assertEquals(16, vit.previous());
//		Assert.assertTrue(vit.hasPrevious());
//		Assert.assertEquals(4, vit.previous());
//		Assert.assertTrue(vit.hasPrevious());
//		Assert.assertEquals(1, vit.previous());
//		Assert.assertTrue(vit.hasPrevious());
//		Assert.assertEquals(0, vit.previous());
//		Assert.assertFalse(vit.hasPrevious());
//	}
//
//	@Test
//	public void testSqr3() {
//		Solver solver = new Solver();
//		IntVar var = new SqrView(VariableFactory.enumerated("b", new int[]{-2, 0, 1, 4}, solver), solver);
//		DisposableRangeIterator vit = var.getRangeIterator(true);
//		Assert.assertTrue(vit.hasNext());
//		Assert.assertEquals(0, vit.min());
//		Assert.assertEquals(1, vit.max());
//		vit.next();
//		Assert.assertTrue(vit.hasNext());
//		Assert.assertEquals(4, vit.min());
//		Assert.assertEquals(4, vit.max());
//		vit.next();
//		Assert.assertTrue(vit.hasNext());
//		Assert.assertEquals(16, vit.min());
//		Assert.assertEquals(16, vit.max());
//		vit.next();
//		Assert.assertFalse(vit.hasNext());
//	}
//
//	@Test
//	public void testSqr4() {
//		Solver solver = new Solver();
//		IntVar var = new SqrView(VariableFactory.enumerated("b", new int[]{-2, 0, 1, 4}, solver), solver);
//		DisposableRangeIterator vit = var.getRangeIterator(false);
//		Assert.assertTrue(vit.hasPrevious());
//		Assert.assertEquals(16, vit.min());
//		Assert.assertEquals(16, vit.max());
//		vit.previous();
//		Assert.assertTrue(vit.hasPrevious());
//		Assert.assertEquals(4, vit.min());
//		Assert.assertEquals(4, vit.max());
//		vit.previous();
//		Assert.assertTrue(vit.hasPrevious());
//		Assert.assertEquals(0, vit.min());
//		Assert.assertEquals(1, vit.max());
//		vit.previous();
//		Assert.assertFalse(vit.hasPrevious());
//	}
//
//	@Test(groups = "1s")
//	public void test1() {
//		Solver solver = new Solver();
//
//		IntVar X = VariableFactory.enumerated("X", -4, 12, solver);
//		IntVar Z = new SqrView(X, solver);
//
//		try {
//			//            solver.propagate();
//			Assert.assertFalse(Z.instantiated());
//			Assert.assertEquals(Z.getLB(), 0);
//			Assert.assertEquals(Z.getUB(), 144);
//			Assert.assertTrue(Z.contains(9));
//			Assert.assertEquals(Z.nextValue(9), 16);
//			Assert.assertEquals(Z.nextValue(18), 25);
//			Assert.assertEquals(Z.nextValue(143), 144);
//			Assert.assertEquals(Z.nextValue(145), Integer.MAX_VALUE);
//			Assert.assertEquals(Z.previousValue(145), 144);
//			Assert.assertEquals(Z.previousValue(144), 121);
//			Assert.assertEquals(Z.previousValue(118), 100);
//			Assert.assertEquals(Z.previousValue(-1), Integer.MIN_VALUE);
//
//			Z.updateLowerBound(9, Cause.Null);
//			Assert.assertEquals(X.getLB(), -4);
//			Assert.assertEquals(X.getUB(), 12);
//
//			Z.updateUpperBound(100, Cause.Null);
//			Assert.assertEquals(X.getUB(), 10);
//			Assert.assertEquals(X.getLB(), -4);
//
//			Z.removeValue(16, Cause.Null);
//			Assert.assertFalse(X.contains(-4));
//			Assert.assertFalse(X.contains(4));
//
//			Z.removeInterval(36, 64, Cause.Null);
//			Assert.assertFalse(X.contains(6));
//			Assert.assertFalse(X.contains(7));
//			Assert.assertFalse(X.contains(8));
//
//			Assert.assertEquals(X.getDomainSize(), 5);
//			Assert.assertEquals(Z.getDomainSize(), X.getDomainSize());
//
//			Z.instantiateTo(25, Cause.Null);
//			Assert.assertTrue(X.instantiated());
//			Assert.assertEquals(X.getValue(), 5);
//
//		} catch (ContradictionException ex) {
//			Assert.fail();
//		}
//	}
//
//	@Test(groups = "10s")
//	public void testIt1() {
//		Random random = new Random();
//		for (int seed = 0; seed < 200; seed++) {
//			random.setSeed(seed);
//			Solver solver = new Solver();
//			int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
//			IntVar o = VariableFactory.bounded("o", domains[0][0], domains[0][domains[0].length - 1], solver);
//			IntVar v = new SqrView(o, solver);
//			DisposableValueIterator vit = v.getValueIterator(true);
//			while (vit.hasNext()) {
//				int va = (int) Math.sqrt(vit.next());
//				Assert.assertTrue(o.contains(va) || o.contains(-va), "seed:" + seed);
//			}
//			vit.dispose();
//
//			vit = v.getValueIterator(false);
//			while (vit.hasPrevious()) {
//				int va = (int) Math.sqrt(vit.previous());
//				Assert.assertTrue(o.contains(va) || o.contains(-va), "seed:" + seed);
//			}
//			vit.dispose();
//
//			DisposableRangeIterator rit = v.getRangeIterator(true);
//			while (rit.hasNext()) {
//				int min = (int) Math.sqrt(rit.min());
//				int max = (int) Math.sqrt(rit.max());
//
//				Assert.assertTrue(o.contains(min) || o.contains(-min), "seed:" + seed);
//				Assert.assertTrue(o.contains(max) || o.contains(-max), "seed:" + seed);
//				rit.next();
//			}
//			rit.dispose();
//
//			rit = v.getRangeIterator(false);
//			while (rit.hasPrevious()) {
//				int min = (int) Math.sqrt(rit.min());
//				int max = (int) Math.sqrt(rit.max());
//
//				Assert.assertTrue(o.contains(min) || o.contains(-min), "seed:" + seed);
//				Assert.assertTrue(o.contains(max) || o.contains(-max), "seed:" + seed);
//				rit.previous();
//			}
//			rit.dispose();
//		}
//	}
//
//	@Test(groups = "10s")
//	public void testIt2() {
//		Random random = new Random();
//		for (int seed = 0; seed < 200; seed++) {
//			random.setSeed(seed);
//			Solver solver = new Solver();
//			int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
//			IntVar o = VariableFactory.enumerated("o", domains[0], solver);
//			IntVar v = new SqrView(o, solver);
//			DisposableValueIterator vit = v.getValueIterator(true);
//			while (vit.hasNext()) {
//				int va = (int) Math.sqrt(vit.next());
//				Assert.assertTrue(o.contains(va) || o.contains(-va), "seed:" + seed);
//			}
//			vit.dispose();
//
//			vit = v.getValueIterator(false);
//			while (vit.hasPrevious()) {
//				int va = (int) Math.sqrt(vit.previous());
//				Assert.assertTrue(o.contains(va) || o.contains(-va), "seed:" + seed);
//			}
//			vit.dispose();
//
//			DisposableRangeIterator rit = v.getRangeIterator(true);
//			while (rit.hasNext()) {
//				int min = (int) Math.sqrt(rit.min());
//				int max = (int) Math.sqrt(rit.max());
//
//				Assert.assertTrue(o.contains(min) || o.contains(-min), "seed:" + seed);
//				Assert.assertTrue(o.contains(max) || o.contains(-max), "seed:" + seed);
//				rit.next();
//			}
//			rit.dispose();
//
//			rit = v.getRangeIterator(false);
//			while (rit.hasPrevious()) {
//				int min = (int) Math.sqrt(rit.min());
//				int max = (int) Math.sqrt(rit.max());
//
//				Assert.assertTrue(o.contains(min) || o.contains(-min), "seed:" + seed);
//				Assert.assertTrue(o.contains(max) || o.contains(-max), "seed:" + seed);
//				rit.previous();
//			}
//			rit.dispose();
//		}
//	}
}
