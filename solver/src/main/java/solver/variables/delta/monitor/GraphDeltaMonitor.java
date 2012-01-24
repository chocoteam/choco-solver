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
package solver.variables.delta.monitor;

import choco.kernel.common.util.procedure.IntProcedure;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.delta.GraphDelta;
import solver.variables.delta.IDeltaMonitor;
import solver.variables.delta.IGraphDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class GraphDeltaMonitor implements IDeltaMonitor<GraphDelta> {

	protected final GraphDelta delta;

	private int[] first, last; // references, in variable delta value to propagate, to un propagated values
	protected int[] frozenFirst, frozenLast; // same as previous while the recorder is frozen, to allow "concurrent modifications"

	public GraphDeltaMonitor(GraphDelta delta) {
		this.delta = delta;
		this.first = new int[4];
		this.last = new int[4];
		this.frozenFirst = new int[4];
		this.frozenLast = new int[4];
	}

	@Override
	public void freeze() {
		for (int i = 0; i < 4; i++) {
			this.frozenFirst[i] = first[i]; // freeze indices
			this.first[i] = this.frozenLast[i] = last[i] = delta.getSize(i);
		}
	}

	@Override
	public void unfreeze() {
		for (int i = 0; i < 4; i++) {
			this.first[i] = last[i] = delta.getSize(i);
		}
	}

	@Override
	public void clear() {
		for (int i = 0; i < 4; i++) {
			this.first[i] = last[i] = 0;
		}
	}

	@Override
	public void forEach(IntProcedure proc, EventType evt) throws ContradictionException {
		int type;
		switch (evt) {//Otherwise the recorder will do a snapshot of a delta that may have not been cleared yet
			case REMOVENODE:
				type = IGraphDelta.NR;
				for (int i = frozenFirst[type]; i < frozenLast[type]; i++) {
					proc.execute(delta.get(i, type));
				}
				break;
			case ENFORCENODE:
				type = IGraphDelta.NE;
				for (int i = frozenFirst[type]; i < frozenLast[type]; i++) {
					proc.execute(delta.get(i, type));
				}
				break;
			case REMOVEARC:
				type = IGraphDelta.AR;
				for (int i = frozenFirst[type]; i < frozenLast[type]; i++) {
					proc.execute(delta.get(i, type));
				}
				break;
			case ENFORCEARC:
				type = IGraphDelta.AE;
				for (int i = frozenFirst[type]; i < frozenLast[type]; i++) {
					proc.execute(delta.get(i, type));
				}
				break;
		}
	}
}
