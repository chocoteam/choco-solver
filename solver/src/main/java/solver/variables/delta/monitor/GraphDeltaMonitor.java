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
import choco.kernel.common.util.procedure.PairProcedure;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.delta.IGraphDelta;
import solver.variables.delta.IGraphDeltaMonitor;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class GraphDeltaMonitor implements IGraphDeltaMonitor {

	protected final IGraphDelta delta;

	protected int[] first, last; // references, in variable delta value to propagate, to un propagated values
	protected int[] frozenFirst, frozenLast; // same as previous while the recorder is frozen, to allow "concurrent modifications"
	protected ICause propagator;

    int timestamp = -1;
    final AbstractSearchLoop loop;

	public GraphDeltaMonitor(IGraphDelta delta, ICause propagator) {
		this.delta = delta;
        loop = delta.getSearchLoop();
		this.first = new int[4];
		this.last = new int[4];
		this.frozenFirst = new int[4];
		this.frozenLast = new int[4];
		this.propagator = propagator;
	}

	@Override
	public void freeze() {
        assert delta.timeStamped():"delta is not timestamped";
        lazyClear();
		for (int i = 0; i < 3; i++) {
			this.frozenFirst[i] = first[i]; // freeze indices
			this.first[i] = this.frozenLast[i] = last[i] = delta.getSize(i);
		}
		this.frozenFirst[3] = first[3]; // freeze indices
		this.first[3] = this.frozenLast[3] = last[3] = delta.getSize(IGraphDelta.AE_tail);
	}

	@Override
	public void unfreeze() {
        timestamp = loop.timeStamp;
		for (int i = 0; i < 3; i++) {
			this.first[i] = last[i] = delta.getSize(i);
		}
		this.first[3] = last[3] = delta.getSize(IGraphDelta.AE_tail);

		// VRAIMENT UTILE?
		delta.lazyClear();	// fix 27/07/12
		lazyClear();		// fix 27/07/12
	}

    public void lazyClear() {
        if (timestamp - loop.timeStamp != 0) {
            clear();
            timestamp = loop.timeStamp;
        }
    }

	@Override
	public void clear() {
		for (int i = 0; i < 4; i++) {
			this.first[i] = last[i] = 0;
		}
	}

	@Deprecated
	public void forEach(IntProcedure proc, EventType evt) throws ContradictionException {
		throw new UnsupportedOperationException("use forEachNode or forEachArc instead");
	}

    @Override
	public void forEachNode(IntProcedure proc, EventType evt) throws ContradictionException {
		int type;
		if(evt==EventType.REMOVENODE){
			type = IGraphDelta.NR;
			for (int i = frozenFirst[type]; i < frozenLast[type]; i++) {
				if(delta.getCause(i,type)!=propagator){
					proc.execute(delta.get(i, type));
				}
			}
		}else if(evt==EventType.ENFORCENODE){
			type = IGraphDelta.NE;
			for (int i = frozenFirst[type]; i < frozenLast[type]; i++) {
				if(delta.getCause(i,type)!=propagator){
					proc.execute(delta.get(i, type));
				}
			}
		}else{
			throw new UnsupportedOperationException();
		}
	}

    @Override
	public void forEachArc(PairProcedure proc, EventType evt) throws ContradictionException {
		if(evt==EventType.REMOVEARC){
			for (int i = frozenFirst[2]; i < frozenLast[2]; i++) {
				if(delta.getCause(i,IGraphDelta.AR_tail)!=propagator){
					proc.execute(delta.get(i, IGraphDelta.AR_tail),delta.get(i, IGraphDelta.AR_head));
				}
			}
		}else if(evt==EventType.ENFORCEARC){
			for (int i = frozenFirst[3]; i < frozenLast[3]; i++) {
				if(delta.getCause(i,IGraphDelta.AE_tail)!=propagator){
					proc.execute(delta.get(i, IGraphDelta.AE_tail),delta.get(i, IGraphDelta.AE_head));
				}
			}
		}else{
			throw new UnsupportedOperationException();
		}
	}
}
