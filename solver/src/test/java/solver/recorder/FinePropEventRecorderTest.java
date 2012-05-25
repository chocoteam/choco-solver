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
import solver.Cause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.ISchedulable;
import solver.propagation.IScheduler;
import solver.recorders.IEventRecorder;
import solver.recorders.coarse.CoarseEventRecorder;
import solver.recorders.fine.FinePropEventRecorder;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.delta.IDelta;
import solver.variables.delta.IDeltaMonitor;

import java.lang.reflect.Field;

import static org.easymock.EasyMock.*;


/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/02/12
 */
public class FinePropEventRecorderTest {

    Solver solver = null;
    IntVar iv1, iv2, iv3 = null;

    CoarseEventRecorder cer = null;
    IDelta d1, d2, d3 = null;
    IDeltaMonitor id1, id2, id3;
    Propagator p1;
    FinePropEventRecorder<IntVar> per = null;
    IScheduler s1 = null;


    @BeforeMethod
    public void setUp() throws Exception {
        solver = new Solver();
        p1 = EasyMock.createMock(Propagator.class);
        p1.addRecorder(EasyMock.<IEventRecorder>anyObject());
        // VAR 1
        iv1 = EasyMock.createMock(IntVar.class);
        iv1.getId();
        expectLastCall().andReturn(1).times(2);
        iv1.addMonitor(anyObject(IVariableMonitor.class));
        d1 = EasyMock.createMock(IDelta.class);
        id1 = EasyMock.createMock(IDeltaMonitor.class);
        expect(d1.createDeltaMonitor(p1)).andReturn(id1);
        iv1.getDelta();
        expectLastCall().andReturn(d1);
        // VAR 2
        iv2 = EasyMock.createMock(IntVar.class);
        iv2.getId();
        expectLastCall().andReturn(2);
        iv2.addMonitor(anyObject(IVariableMonitor.class));
        d2 = EasyMock.createMock(IDelta.class);
        id2 = EasyMock.createMock(IDeltaMonitor.class);
        expect(d2.createDeltaMonitor(p1)).andReturn(id2);
        iv2.getDelta();
        expectLastCall().andReturn(d2);
        // VAR 3
        iv3 = EasyMock.createMock(IntVar.class);
        iv3.getId();
        expectLastCall().andReturn(3);
        iv3.addMonitor(anyObject(IVariableMonitor.class));
        d3 = EasyMock.createMock(IDelta.class);
        id3 = EasyMock.createMock(IDeltaMonitor.class);
        expect(d3.createDeltaMonitor(p1)).andReturn(id3);
        iv3.getDelta();
        expectLastCall().andReturn(d3);
        cer = createMock(CoarseEventRecorder.class);

        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);

        per = new FinePropEventRecorder<IntVar>(new IntVar[]{iv1, iv2, iv3, iv1}, p1, new int[]{0, 1, 2, 3}, solver);

        verify(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
        reset(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);

        s1 = EasyMock.createMock(IScheduler.class);
        per.setScheduler(s1, 0);

        reset(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
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
        Assert.assertEquals(per.getVariables(), new IntVar[]{iv1, iv2, iv3, iv1});
        Assert.assertEquals(per.getPropagators(), new Propagator[]{p1});
        iv1.getId();
        expectLastCall().andReturn(1);
        iv2.getId();
        expectLastCall().andReturn(1);
        iv3.getId();
        expectLastCall().andReturn(1);
        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);

        Assert.assertEquals(per.getIdxInV(iv1), 0);
        Assert.assertEquals(per.getIdxInV(iv2), 0);
        Assert.assertEquals(per.getIdxInV(iv3), 0);

        verify(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
    }

    @Test
    public void testactivate() throws ContradictionException {
        iv1.activate(anyObject(IVariableMonitor.class));
        iv2.activate(anyObject(IVariableMonitor.class));
        iv3.activate(anyObject(IVariableMonitor.class));
        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
        per.activate(p1);
    }

    @Test
    public void testdesactivate() throws ContradictionException {
        iv1.desactivate(anyObject(IVariableMonitor.class));
        iv2.desactivate(anyObject(IVariableMonitor.class));
        iv3.desactivate(anyObject(IVariableMonitor.class));
        id1.clear();
        id2.clear();
        id3.clear();
        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
        per.desactivate(p1);
    }

    @Test
    public void testexecute() throws ContradictionException {
        // SCENARIO:
        // iv1, iv3 are updated
        iv1.getId();
        expectLastCall().andReturn(1);
        expect(p1.getPropagationConditions(0)).andReturn(60);
        expect(p1.getPropagationConditions(3)).andReturn(60);
        p1.decArity();
        expectLastCall().times(3);
        id1.clear();
        iv3.getId();
        expectLastCall().andReturn(3);
        expect(p1.getPropagationConditions(2)).andReturn(60);
        id3.clear();
        s1.schedule(EasyMock.<ISchedulable>anyObject());
        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);

        per.afterUpdate(iv1, EventType.INSTANTIATE, Cause.Null);
        per.afterUpdate(iv3, EventType.INSTANTIATE, Cause.Null);
        verify(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);

        reset(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
        id1.freeze();
        expect(p1.isActive()).andReturn(true);
        p1.propagate(per, 0, EventType.INSTANTIATE.getStrengthenedMask());
        p1.propagate(per, 3, EventType.INSTANTIATE.getStrengthenedMask());
        // and setPassive
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Invocation inv = LastControl.getCurrentInvocation();
                ((FinePropEventRecorder) inv.getArguments()[0]).desactivate(
                        (Propagator) LastControl.getCurrentInvocation().getMock());
                return null;
            }
        });
        id1.unfreeze();
        iv1.desactivate(per);
        id1.clear();
        iv2.desactivate(per);
        id2.clear();
        iv3.desactivate(per);
        id3.clear();

        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
        // RUN METHOD
        Assert.assertTrue(per.execute());
        verify(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
    }

    @Test
    public void testafterupdate() throws ContradictionException {
        iv1.getId();
        expectLastCall().andReturn(1);
        expect(p1.getPropagationConditions(0)).andReturn(60);
        expect(p1.getPropagationConditions(3)).andReturn(60);
        p1.decArity();
        expectLastCall().times(2);
        id1.clear();
        s1.schedule(EasyMock.<ISchedulable>anyObject());
        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);

        per.afterUpdate(iv1, EventType.INSTANTIATE, Cause.Null);
        verify(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
    }

    @Test
    public void testflush() throws ContradictionException {
        id1.clear();
        id2.clear();
        id3.clear();

        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
        per.flush();
        verify(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
    }

    @Test
    public void testvirtExec() throws ContradictionException {
        p1.incNbRecorderEnqued();
        iv1.getDelta();
        expectLastCall().andReturn(d1);
        d1.lazyClear();
        id1.unfreeze();
        iv2.getDelta();
        expectLastCall().andReturn(d2);
        d2.lazyClear();
        id2.unfreeze();
        iv3.getDelta();
        expectLastCall().andReturn(d3);
        d3.lazyClear();
        id3.unfreeze();
        s1.remove(per);

        replay(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);
        per.enqueue();
        per.virtuallyExecuted(p1);
        verify(iv1, iv2, iv3, cer, d1, d2, d3, p1, id1, id2, id3);

    }


}
