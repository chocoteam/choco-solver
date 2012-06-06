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

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.internal.Invocation;
import org.easymock.internal.LastControl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IScheduler;
import solver.recorders.fine.FineVarEventRecorder;
import solver.recorders.fine.VarEventRecorder;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.delta.IDelta;
import solver.variables.delta.IIntDeltaMonitor;

import java.lang.reflect.Field;

import static org.easymock.EasyMock.*;


/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/02/12
 */
public class FineVarEventRecorderTest {

    Solver solver = null;
    IntVar iv1 = null;

    IDelta d1 = null;
    IIntDeltaMonitor id1, id2, id3, id4, id5;
    Propagator p1, p2, p3, p4, p5;
    VarEventRecorder<IntVar> ver = null;
    IScheduler s1 = null;


    @BeforeMethod
    public void setUp() throws Exception {
        solver = new Solver();
        // delta
        d1 = createMock(IDelta.class);
        // int var
        iv1 = createMock(IntVar.class);
        iv1.getDelta();
        expectLastCall().andReturn(d1);
        iv1.addMonitor(anyObject(FineVarEventRecorder.class));

        // first proapagator
        p1 = createMock(Propagator.class);
        expect(p1.getId()).andReturn(1).times(2);
        p1.addRecorder(anyObject(FineVarEventRecorder.class));
        id1 = createMock(IIntDeltaMonitor.class);
        iv1.monitorDelta(p1);
        expectLastCall().andReturn(id1);
        // snd propagator
        p2 = createMock(Propagator.class);
        p2.getId();
        expectLastCall().andReturn(2);
        p2.addRecorder(anyObject(FineVarEventRecorder.class));
        id2 = createMock(IIntDeltaMonitor.class);
        iv1.monitorDelta(p2);
        expectLastCall().andReturn(id2);
        // third propagator
        p3 = createMock(Propagator.class);
        p3.getId();
        expectLastCall().andReturn(3);
        p3.addRecorder(anyObject(FineVarEventRecorder.class));
        id3 = createMock(IIntDeltaMonitor.class);
        iv1.monitorDelta(p3);
        expectLastCall().andReturn(id3);
        // fourth propagator
        p4 = createMock(Propagator.class);
        p4.getId();
        expectLastCall().andReturn(4);
        p4.addRecorder(anyObject(FineVarEventRecorder.class));
        id4 = createMock(IIntDeltaMonitor.class);
        iv1.monitorDelta(p4);
        expectLastCall().andReturn(id4);
        // fifth propagator
        p5 = createMock(Propagator.class);
        p5.getId();
        expectLastCall().andReturn(5);
        p5.addRecorder(anyObject(FineVarEventRecorder.class));
        id5 = createMock(IIntDeltaMonitor.class);
        iv1.monitorDelta(p5);
        expectLastCall().andReturn(id5);

        replay(iv1, d1, p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);

        ver = new FineVarEventRecorder<IntVar>(iv1, new Propagator[]{p1, p1, p2, p3, p4, p5}, new int[]{0, 5, 1, 0, 2, 1}, solver);

        verify(iv1, d1, p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
        reset(iv1, d1, p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);

        iv1.activate(anyObject(IVariableMonitor.class));
        p1.getId();
        expectLastCall().andReturn(1);
        p2.getId();
        expectLastCall().andReturn(2);
        p3.getId();
        expectLastCall().andReturn(3);
        p4.getId();
        expectLastCall().andReturn(4).times(2);
        id4.clear();
        replay(iv1, d1, p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);

        ver.activate(p1);
        ver.activate(p2);
        ver.activate(p3);
        ver.activate(p4);
        ver.desactivate(p4);

        s1 = EasyMock.createMock(IScheduler.class);
        ver.setScheduler(s1, 0);

        resetToDefault(iv1, d1, p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
    }

    private <E, T> E get(String name, Class clazz, T inst) {
        Field field;
        try {
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return (E) field.get(inst);
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
        Assert.assertEquals(ver.getPropagators(), new Propagator[]{p1, p2, p3, p4, p5});
        Assert.assertEquals(ver.getIdxInV(iv1), 0);
    }

    @Test
    public void testexecute() throws ContradictionException {
        // Scenario :
        // p1, p2, p3 are active
        // p4 is passive
        // p5 is not yet active
        // p2 has triggered an event on iv1
        // ver is executed : p1 and p3 should be propagate.
        // Bonus : p1 is passive after propagate
        // <--START PREPARING-->
        p1.getId();
        expectLastCall().andReturn(1);
        p1.getPropagationConditions(0);
        expectLastCall().andReturn(EventType.INSTANTIATE.mask);
        p1.getPropagationConditions(5);
        expectLastCall().andReturn(EventType.INSTANTIATE.mask);
        p1.decArity();
        expectLastCall().times(2);
        id1.clear();
        p3.getId();
        expectLastCall().andReturn(3);
        p3.getPropagationConditions(0);
        expectLastCall().andReturn(EventType.INSTANTIATE.mask);
        p3.decArity();
        id3.clear();
        replay(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
        ver.afterUpdate(iv1, EventType.INSTANTIATE, p2);
        verify(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
        reset(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
        // <--END PREPARING-->

        // PROPAGATOR 1
        p1.getId();
        expectLastCall().andReturn(1).times(2);
        p1.isActive();
        expectLastCall().andReturn(true);
        p1.propagate(ver, 0, EventType.INSTANTIATE.getStrengthenedMask());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Invocation inv = LastControl.getCurrentInvocation();
                ((FineVarEventRecorder) inv.getArguments()[0]).desactivate(
                        (Propagator) LastControl.getCurrentInvocation().getMock());
                return null;
            }
        });
        p1.isPassive();
        expectLastCall().andReturn(true);
        // DELTA MONITOR 1
        id1.freeze();
        id1.clear();
        id1.unfreeze();
        // PROPAGATOR 2
        p2.getId();
        expectLastCall().andReturn(2);
        // PROPAGATOR 3
        p3.getId();
        expectLastCall().andReturn(3);
        p3.isActive();
        expectLastCall().andReturn(true);
        p3.propagate(ver, 0, EventType.INSTANTIATE.getStrengthenedMask());
        // DELTA MONITOR 1
        id3.freeze();
        id3.unfreeze();


        replay(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);

        // RUN METHOD
        ver.execute();

        verify(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);

    }

    @Test
    public void testafterupdate() throws ContradictionException {
        // Scenario :
        // p1, p2, p3 are active
        // p4 is passive
        // p5 is not yet active
        // p1 trigger an event on iv1
        // RESULT : p2 and p3 should be informed
        p2.getId();
        expectLastCall().andReturn(2);
        p2.getPropagationConditions(1);
        expectLastCall().andReturn(EventType.INSTANTIATE.mask);
        p2.decArity();
        id2.clear();
        p3.getId();
        expectLastCall().andReturn(3);
        p3.getPropagationConditions(0);
        expectLastCall().andReturn(EventType.INSTANTIATE.mask);
        p3.decArity();
        id3.clear();
        s1.schedule(ver);
        replay(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);

        ver.afterUpdate(iv1, EventType.INSTANTIATE, p1);

        verify(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
        int[] masks = get("evtmasks", FineVarEventRecorder.class, ver);
        Assert.assertEquals(masks[0], EventType.VOID.mask);
        Assert.assertEquals(masks[1], EventType.INSTANTIATE.getStrengthenedMask());
        Assert.assertEquals(masks[2], EventType.INSTANTIATE.getStrengthenedMask());
        Assert.assertEquals(masks[3], EventType.VOID.mask);
        Assert.assertEquals(masks[4], EventType.VOID.mask);
    }

    @Test
    public void testflush() throws ContradictionException {
        int[] masks = {1, 1, 1, 1, 1};
        set("evtmasks", FineVarEventRecorder.class, ver, masks);

        p1.getId();
        expectLastCall().andReturn(1);
        id1.clear();
        p2.getId();
        expectLastCall().andReturn(2);
        id2.clear();
        p3.getId();
        expectLastCall().andReturn(3);
        id3.clear();
        replay(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
        ver.flush();
        verify(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
        masks = get("evtmasks", FineVarEventRecorder.class, ver);
        Assert.assertEquals(masks[0], 0);
        Assert.assertEquals(masks[1], 0);
        Assert.assertEquals(masks[2], 0);
        Assert.assertEquals(masks[3], 1);
        Assert.assertEquals(masks[4], 1);
    }

    @Test
    public void testvirtExec() throws ContradictionException {
        // scenario :
        // p1, p2, p3 are active
        // p4 is passive
        // p5 is not yet active
        // p1, p2 then p3 are virtually executed
        // RESULTS : ver is removed from scheduler
        iv1.getDelta();
        expectLastCall().andReturn(d1).times(3);
        d1.lazyClear();
        expectLastCall().times(3);
        p1.incNbRecorderEnqued();
        p1.getId();
        expectLastCall().andReturn(1).times(1);
        id1.unfreeze();
        p2.incNbRecorderEnqued();
        p2.getId();
        expectLastCall().andReturn(2).times(1);
        id2.unfreeze();
        p3.incNbRecorderEnqued();
        p3.getId();
        expectLastCall().andReturn(3).times(1);
        id3.unfreeze();

        replay(iv1, d1, p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);

        ver.enqueue();
        int[] masks = {1, 1, 1, 1, 1};
        set("evtmasks", FineVarEventRecorder.class, ver, masks);

        ver.virtuallyExecuted(p1);
        ver.virtuallyExecuted(p2);
        ver.virtuallyExecuted(p3);

        verify(p1, p2, p3, p4, p5, id1, id2, id3, id4, id5);
    }


}
