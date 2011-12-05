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

package solver.variables.delta;

import choco.kernel.common.util.procedure.IntProcedure;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.AbstractSearchLoop;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.variables.graph.GraphVar;

public class GraphDelta implements IGraphDelta {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GDelta nodeEnf, nodeRem, arcEnf, arcRem;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public GraphDelta(Solver solver) {
		nodeEnf = new GDelta();
		nodeRem = new GDelta();
		arcEnf = new GDelta();
		arcRem = new GDelta();
		solver.getSearchLoop().plugSearchMonitor(new WorldObserver());
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void forEach(IntProcedure proc, int from, int to) throws ContradictionException {
		throw new UnsupportedOperationException();
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	@Override
	public IntDelta getNodeRemovalDelta() {
		return nodeRem;
	}

	@Override
	public IntDelta getNodeEnforcingDelta() {
		return nodeEnf;
	}

	@Override
	public IntDelta getArcRemovalDelta() {
		return arcRem;
	}

	@Override
	public IntDelta getArcEnforcingDelta() {
		return arcEnf;
	}

	@Override
	public void clear() {
		nodeEnf.last = 0;
		nodeRem.last = 0;
		arcEnf.last = 0;
		arcRem.last = 0;
	}
	
	//***********************************************************************************
	// WORLD OBSERVER
	//***********************************************************************************

	/**Enables lazy clear*/
	private class WorldObserver extends VoidSearchMonitor implements ISearchMonitor{
		@Override
		public void beforeDownLeftBranch() {
			clear();
		}
		@Override
		public void beforeDownRightBranch() {
			clear();
		}
	}

	//***********************************************************************************
	// DELTA
	//***********************************************************************************

	private class GDelta implements IntDelta{

		int[] rem;
		int last;

		private GDelta() {
			rem = new int[16];
		}

		private int[] ensureCapacity(int idx, int[] values) {
			if (idx >= values.length) {
				int[] tmp = new int[idx * 3 / 2 + 1];
				System.arraycopy(values, 0, tmp, 0, idx);
				return tmp;
			}
			return values;
		}

		public void add(int value) {
			rem = ensureCapacity(last, rem);
			rem[last++] = value;
		}

		@Override
		public int get(int idx){
			return rem[idx];
		}

		@Override
		public int size() {
			return last;
		}

		@Override
		public void forEach(IntProcedure proc, int from, int to) throws ContradictionException {
			for (int i = from; i < to; i++) {
				proc.execute(rem[i]);
			}
		}
	}
}
