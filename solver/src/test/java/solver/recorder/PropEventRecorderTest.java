/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.recorder;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.propagation.IScheduler;
import solver.propagation.PropagationEngine;
import solver.recorders.fine.prop.PropEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.lang.reflect.Field;

import static org.easymock.EasyMock.*;


/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/02/12
 */
public class PropEventRecorderTest {

    Solver solver = null;
    IntVar iv1, iv2, iv3 = null;

    Propagator p1;
    PropEventRecorder<IntVar> per = null;
    IScheduler s1 = null;
    PropagationEngine engine;


    @BeforeMethod
    public void setUp() throws Exception {
        solver = new Solver();
        engine = new PropagationEngine(solver.getEnvironment());
        solver.set(engine);

        p1 = EasyMock.createMock(Propagator.class);
        expect(p1.getId()).andReturn(0).times(2);
        // VAR 1
        iv1 = EasyMock.createMock(IntVar.class);
        iv1.getId();
        expectLastCall().andReturn(1).times(4);
        // VAR 2
        iv2 = EasyMock.createMock(IntVar.class);
        iv2.getId();
        expectLastCall().andReturn(2).times(3);
        // VAR 3
        iv3 = EasyMock.createMock(IntVar.class);
        iv3.getId();
        expectLastCall().andReturn(3).times(3);

        replay(iv1, iv2, iv3, p1);

        per = new PropEventRecorder<IntVar>(new IntVar[]{iv1, iv2, iv3, iv1}, p1, solver, engine);
        engine.addEventRecorder(per);
        verify(iv1, iv2, iv3, p1);
        reset(iv1, iv2, iv3, p1);

        s1 = EasyMock.createMock(IScheduler.class);
        per.setScheduler(s1, 0);

        reset(iv1, iv2, iv3, p1);
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
        Assert.assertEquals(per.getVariables(), new IntVar[]{iv1, iv2, iv3});
        Assert.assertEquals(per.getPropagators(), new Propagator[]{p1});
        iv1.getId();
        expectLastCall().andReturn(1);
        iv2.getId();
        expectLastCall().andReturn(1);
        iv3.getId();
        expectLastCall().andReturn(1);
        replay(iv1, iv2, iv3, p1);

        Assert.assertEquals(per.getIdx(iv1), 0);
        Assert.assertEquals(per.getIdx(iv2), 0);
        Assert.assertEquals(per.getIdx(iv3), 0);

        verify(iv1, iv2, iv3, p1);
    }

    @Test
    public void testactivate() throws ContradictionException {
        replay(iv1, iv2, iv3, p1);
        per.activate(p1);
    }

    @Test
    public void testdesactivate() throws ContradictionException {
        replay(iv1, iv2, iv3, p1);
        per.desactivate(p1);
    }

    @Test(expectedExceptions = SolverException.class)
    public void testexecute() throws ContradictionException {
        // RUN METHOD
        Assert.assertTrue(per.execute());
    }

    @Test
    public void testafterupdate() throws ContradictionException {
        set("solver", Propagator.class, p1, solver);
        expect(p1.getId()).andReturn(0);
        expectLastCall().andReturn(EventType.FULL_PROPAGATION.mask);
        replay(iv1, iv2, iv3, p1, s1);

        per.afterUpdate(iv1.getId(), EventType.INSTANTIATE, Cause.Null);
        verify(iv1, iv2, iv3, p1);
    }

    @Test
    public void testflush() throws ContradictionException {
        replay(iv1, iv2, iv3, p1);
        per.flush();
        verify(iv1, iv2, iv3, p1);
    }

    @Test
    public void testvirtExec() throws ContradictionException {
        p1.incNbPendingEvt();
        replay(iv1, iv2, iv3, p1);
        per.enqueue();
        per.virtuallyExecuted(p1);
        verify(iv1, iv2, iv3, p1);

    }


}
