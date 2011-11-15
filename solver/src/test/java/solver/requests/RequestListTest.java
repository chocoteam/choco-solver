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

package solver.requests;

import choco.kernel.ESat;
import choco.kernel.common.util.objects.IList;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.list.RequestListBuilder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/02/11
 */
public class RequestListTest {

    private static class MockRequest extends AbstractRequest {

        Propagator p;
        int vidx;
        int mask;

        private MockRequest(Propagator p, int vidx) {
            super(p, null, vidx);
            this.p = p;
            this.vidx = vidx;
        }

        @Override
        public void filter() throws ContradictionException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(EventType e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(IntProcedure proc) throws ContradictionException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(IntProcedure proc, int from, int to) throws ContradictionException {
            throw new UnsupportedOperationException();
        }
    }

    private static class MockPropagator extends Propagator {

        int propCond = 0;

        protected MockPropagator(Solver solver, int propCond) {
            super(new IntVar[0], solver, null, PropagatorPriority.UNARY, false);
            this.propCond = propCond;
        }

        @Override
        public void propagate() throws ContradictionException {
        }

        @Override
        public void propagateOnRequest(IRequest iRequest, int idxVarInProp, int mask) throws ContradictionException {
        }

        @Override
        public int getPropagationConditions(int vIdx) {
            return propCond;
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }
    }

    @Test(groups = "1s")
    public void testArrayList() {
        Solver solver = new Solver();
        IEnvironment env = solver.getEnvironment();
        RequestListBuilder._DEFAULT = 0;
        IList<MockRequest> list = RequestListBuilder.preset(env, IRequest.IN_VAR);

        MockRequest[] requests = new MockRequest[3];
        for (int i = 0; i < requests.length; i++) {
            requests[i] = new MockRequest(new MockPropagator(solver, 2), i);
            list.add(requests[i], false);
        }

        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 3);

        env.worldPush();
        list.setPassive(requests[0]);
        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 2);

        env.worldPush();
        list.setPassive(requests[2]);
        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 1);

        env.worldPop();
        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 2);

        env.worldPop();
        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 3);
    }

    @Test(groups = "1s")
    public void testHalfBcktList() {
        Solver solver = new Solver();
        IEnvironment env = solver.getEnvironment();
        RequestListBuilder._DEFAULT = 1;
        IList<MockRequest> list = RequestListBuilder.preset(env, IRequest.IN_VAR);

        MockRequest[] requests = new MockRequest[3];
        for (int i = 0; i < requests.length; i++) {
            requests[i] = new MockRequest(new MockPropagator(solver, 2), i);
            list.add(requests[i], false);
        }

        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 3);

        env.worldPush();
        list.setPassive(requests[0]);
        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 2);

        env.worldPush();
        list.setPassive(requests[2]);
        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 1);

        env.worldPop();
        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 2);

        env.worldPop();
        Assert.assertEquals(requests[0].getIndex(IRequest.IN_VAR), 2);
        Assert.assertEquals(requests[1].getIndex(IRequest.IN_VAR), 0);
        Assert.assertEquals(requests[2].getIndex(IRequest.IN_VAR), 1);
        Assert.assertEquals(list.size(), 3);
        Assert.assertEquals(list.cardinality(), 3);
    }
}
