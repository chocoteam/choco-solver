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
package solver.recorder;

import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.recorders.coarse.CoarseEventRecorder;
import solver.recorders.fine.var.VarEventRecorder;
import solver.variables.IntVar;


/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/02/12
 */
public class VarEventRecorderTest {

    Solver solver = null;
    IntVar iv1 = null;

    CoarseEventRecorder cer = null;
    Propagator p1 = null;
    Propagator p2 = null;
    Propagator p3 = null;
    VarEventRecorder<IntVar> ver = null;


   /* @BeforeMethod
    public void setUp() throws Exception {
        solver = new Solver();
        iv1 = createMock(IntVar.class);
        iv1.addMonitor(anyObject(IVariableMonitor.class));
        cer = createMock(CoarseEventRecorder.class);
        p1 = createMock(Propagator.class);
        expect(p1.getId()).andReturn(1).times(2);
        p1.addRecorder(anyObject(IEventRecorder.class));
        p2 = createMock(Propagator.class);
        p2.getId();
        expectLastCall().andReturn(2);
        p2.addRecorder(anyObject(IEventRecorder.class));
        p3 = createMock(Propagator.class);
        p3.getId();
        expectLastCall().andReturn(3);
        p3.addRecorder(anyObject(IEventRecorder.class));

        replay(iv1, cer, p1, p2, p3);

        ver = new VarEventRecorder<IntVar>(iv1, new Propagator[]{p1, p1, p2, p3}, solver);
        resetToDefault(iv1, cer, p1, p2, p3);
    }

    private <E, T> E get(String name, Class clazz, T inst) {
        Field ffirstAP;
        try {
            ffirstAP = clazz.getDeclaredField(name);
            ffirstAP.setAccessible(true);
            return (E) ffirstAP.get(inst);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T, O> void set(String name, Class clazz, T inst, O o) {
        Field ffirstAP;
        try {
            ffirstAP = clazz.getDeclaredField(name);
            ffirstAP.setAccessible(true);
            ffirstAP.set(inst, o);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testbasics() {
        Assert.assertEquals(ver.getVariables(), new IntVar[]{iv1});
        Assert.assertEquals(ver.getPropagators(), new Propagator[]{p1, p2, p3});
        Assert.assertEquals(ver.getIdxInV(iv1), 0);
    }

    @Test(expectedExceptions = SolverException.class)
    public void testexecute() throws ContradictionException {
        ver.execute();
    }

    @Test
    public void testactdes() throws ContradictionException {
        IStateInt firstAP = get("firstAP", VarEventRecorder.class, ver);
        IStateInt firstPP = get("firstPP", VarEventRecorder.class, ver);
        Propagator[] props = get("propagators", VarEventRecorder.class, ver);
        int[] propIdx = get("propIdx", VarEventRecorder.class, ver);

        Assert.assertEquals(firstAP.get(), 3);
        Assert.assertEquals(firstPP.get(), 3);
        Assert.assertEquals(props, new Propagator[]{p1, p2, p3});
        Assert.assertEquals(propIdx, new int[]{0, 1, 2});
        // prepare iv1 to receive a call to activate(ver)
        reset(iv1, cer, p1, p2, p3);
        iv1.activate(ver);
        p1.getId();
        expectLastCall().andReturn(1);
        replay(iv1, cer, p1, p2, p3);

        ver.activate(p1);

        Assert.assertEquals(firstAP.get(), 2);
        Assert.assertEquals(firstPP.get(), 3);
        Assert.assertEquals(props, new Propagator[]{p1, p2, p3});
        Assert.assertEquals(propIdx, new int[]{2, 1, 0});
        verify(iv1, cer, p1, p2, p3);
        reset(iv1, cer, p1, p2, p3);
        p2.getId();
        expectLastCall().andReturn(2);
        replay(iv1, cer, p1, p2, p3);

        ver.activate(p2);

        Assert.assertEquals(firstAP.get(), 1);
        Assert.assertEquals(firstPP.get(), 3);
        Assert.assertEquals(props, new Propagator[]{p1, p2, p3});
        Assert.assertEquals(propIdx, new int[]{2, 1, 0});
        verify(iv1, cer, p1, p2, p3);
        reset(iv1, cer, p1, p2, p3);
        p2.getId();
        expectLastCall().andReturn(2);
        replay(iv1, cer, p1, p2, p3);

        ver.desactivate(p2);

        Assert.assertEquals(firstAP.get(), 1);
        Assert.assertEquals(firstPP.get(), 2);
        Assert.assertEquals(props, new Propagator[]{p1, p2, p3});
        Assert.assertEquals(propIdx, new int[]{2, 0, 1});
        verify(iv1, cer, p1, p2, p3);
        reset(iv1, cer, p1, p2, p3);
        p1.getId();
        expectLastCall().andReturn(1);
        replay(iv1, cer, p1, p2, p3);

        ver.desactivate(p1);

        Assert.assertEquals(firstAP.get(), 1);
        Assert.assertEquals(firstPP.get(), 1);
        Assert.assertEquals(props, new Propagator[]{p1, p2, p3});
        Assert.assertEquals(propIdx, new int[]{2, 0, 1});
        verify(iv1, cer, p1, p2, p3);
        reset(iv1, cer, p1, p2, p3);
        replay(iv1, cer, p1, p2, p3);
    }

    @Test
    public void testafterupdate() throws ContradictionException {
        reset(iv1, cer, p1, p2, p3);
        iv1.activate(ver);
        p1.getId();
        expectLastCall().andReturn(1);
        p2.getId();
        expectLastCall().andReturn(2);
        replay(iv1, cer, p1, p2, p3);
        ver.activate(p1);
        ver.activate(p2);
        verify(iv1, cer, p1, p2, p3);
        reset(iv1, cer, p1, p2, p3);

        set("coarseER", Propagator.class, p2, cer);
        p2.decArity();
        p2.forcePropagate(EventType.FULL_PROPAGATION);
        replay(iv1, cer, p1, p2, p3);

        ver.onUpdate(iv1, EventType.INSTANTIATE, p1);

        verify(iv1, cer, p1, p2, p3);
    }

    @Test
    public void testenqueue() throws ContradictionException {
        reset(iv1, cer, p1, p2, p3);
        iv1.activate(ver);
        p1.getId();
        expectLastCall().andReturn(1);
        p2.getId();
        expectLastCall().andReturn(2);
        replay(iv1, cer, p1, p2, p3);
        ver.activate(p1);
        ver.activate(p2);
        verify(iv1, cer, p1, p2, p3);
        reset(iv1, cer, p1, p2, p3);

        p1.incNbRecorderEnqued();
        p2.incNbRecorderEnqued();
        replay(iv1, cer, p1, p2, p3);

        ver.enqueue();

        verify(iv1, cer, p1, p2, p3);
    }

    @Test
    public void testdequeue() throws ContradictionException {
        reset(iv1, cer, p1, p2, p3);
        iv1.activate(ver);
        p1.getId();
        expectLastCall().andReturn(1);
        p2.getId();
        expectLastCall().andReturn(2);
        replay(iv1, cer, p1, p2, p3);
        ver.activate(p1);
        ver.activate(p2);
        verify(iv1, cer, p1, p2, p3);
        reset(iv1, cer, p1, p2, p3);

        p1.decNbRecrodersEnqued();
        p2.decNbRecrodersEnqued();
        replay(iv1, cer, p1, p2, p3);

        ver.deque();

        verify(iv1, cer, p1, p2, p3);
    }

    @Test
    public void testvirtExec() throws ContradictionException {
        reset(iv1, cer, p1, p2, p3);
        iv1.activate(ver);
        p1.getId();
        expectLastCall().andReturn(1);
        p2.getId();
        expectLastCall().andReturn(2);
        replay(iv1, cer, p1, p2, p3);
        ver.activate(p1);
        ver.activate(p2);
        verify(iv1, cer, p1, p2, p3);
        reset(iv1, cer, p1, p2, p3);

        replay(iv1, cer, p1, p2, p3);

        ver.virtuallyExecuted(p1);

        verify(iv1, cer, p1, p2, p3);
    }
*/

}
