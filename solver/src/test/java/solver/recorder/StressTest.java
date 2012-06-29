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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.Queue;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.recorders.fine.arc.FineArcEventRecorder;
import solver.recorders.fine.prop.FinePropEventRecorder;
import solver.recorders.fine.var.FineVarEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.lang.reflect.Field;

import static org.easymock.EasyMock.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/06/12
 */
public class StressTest {

    int M = 9;

    Solver solver = null;
    IntVar iv1, iv2 = null;
    IPropagationEngine engine;
    AbstractFineEventRecorder[] fers;
    Propagator p1, p2;

    @BeforeMethod
    public void setUp() throws Exception {
        solver = new Solver();
        engine = new PropagationEngine(solver.getEnvironment());
        solver.nextId();
        solver.nextId();
        solver.nextId();
        solver.nextId();
        p1 = EasyMock.createMock("p1", Propagator.class);
        p2 = EasyMock.createMock("p2", Propagator.class);
        iv1 = EasyMock.createMock("v1", IntVar.class);
        iv2 = EasyMock.createMock("v2", IntVar.class);

        set("vars", Propagator.class, p1, new IntVar[]{iv1, iv2});
        set("vars", Propagator.class, p2, new IntVar[]{iv1, iv2});

        replay(iv1, iv2, p1, p2);
        verify(iv1, iv2, p1, p2);
        reset(iv1, iv2, p1, p2);
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
    public void arc() throws ContradictionException {
        expect(iv1.getId()).andReturn(1).times(4);
        expect(iv2.getId()).andReturn(3).times(4);
        expect(p1.getId()).andReturn(0).times(3);
        expect(p2.getId()).andReturn(2).times(3);
        replay(iv1, iv2, p1, p2);

        fers = new FineArcEventRecorder[4];
        fers[0] = new FineArcEventRecorder(iv1, p1, 0, solver, engine);
        engine.addEventRecorder(fers[0]);
        fers[1] = new FineArcEventRecorder(iv2, p1, 1, solver, engine);
        engine.addEventRecorder(fers[1]);
        fers[2] = new FineArcEventRecorder(iv1, p2, 1, solver, engine);
        engine.addEventRecorder(fers[2]);
        fers[3] = new FineArcEventRecorder(iv2, p2, 0, solver, engine);
        engine.addEventRecorder(fers[3]);

        engine.set(new Queue(fers));

        engine.activatePropagator(p1);
        engine.activatePropagator(p2);
        verify(iv1, iv2, p1, p2);
        reset(iv1, iv2, p1, p2);

        // for update
        expect(iv1.getId()).andReturn(1).times(M);
        expect(p2.getPropagationConditions(1)).andReturn(EventType.INSTANTIATE.mask).times(M);
        p2.incNbRecorderEnqued();
        expectLastCall().times(1);
        run(false);
        p2.decNbRecrodersEnqued();
        expectLastCall().times(1);
        replay(p2);
        engine.flush();
        reset(p2);


        // for update
        expect(iv1.getId()).andReturn(1).times(M);
        expect(p2.getPropagationConditions(1)).andReturn(EventType.INSTANTIATE.mask).times(M);
        p2.incNbRecorderEnqued();
        expectLastCall().times(M);
        p2.decNbRecrodersEnqued();
        expectLastCall().times(M);
        // for execute

        expect(p2.isActive()).andReturn(true).times(M);
        p2.propagate(fers[2], 1, EventType.INSTANTIATE.strengthened_mask);
        expectLastCall().times(M);
        run(true);
    }

    @Test
    public void prop() throws ContradictionException {
        expect(iv1.getId()).andReturn(1).times(21);
        expect(iv2.getId()).andReturn(3).times(21);
        expect(p1.getId()).andReturn(0).times(2);
        expect(p2.getId()).andReturn(2).times(2);
        replay(iv1, iv2, p1, p2);

        fers = new FinePropEventRecorder[2];
        fers[0] = new FinePropEventRecorder(new IntVar[]{iv1, iv2}, p1, new int[]{0, 1}, solver, engine);
        engine.addEventRecorder(fers[0]);
        fers[1] = new FinePropEventRecorder(new IntVar[]{iv2, iv1}, p2, new int[]{0, 1}, solver, engine);
        engine.addEventRecorder(fers[1]);

        engine.set(new Queue(fers));

        engine.activatePropagator(p1);
        engine.activatePropagator(p2);
        verify(iv1, iv2, p1, p2);
        reset(iv1, iv2, p1, p2);

        // for update
        expect(iv1.getId()).andReturn(1).times(M);
        expect(p2.getPropagationConditions(1)).andReturn(EventType.INSTANTIATE.mask).times(M);
        p2.incNbRecorderEnqued();
        expectLastCall().times(1);
        run(false);

        p2.decNbRecrodersEnqued();
        expectLastCall().times(1);
        replay(p2);
        engine.flush();
        reset(p2);

        // for update
        expect(iv1.getId()).andReturn(1).times(M);
        expect(p2.getPropagationConditions(1)).andReturn(EventType.INSTANTIATE.mask).times(M);
        // for execute
        p2.incNbRecorderEnqued();
        expectLastCall().times(M);
        p2.decNbRecrodersEnqued();
        expectLastCall().times(M);
        expect(p2.isActive()).andReturn(true).times(M);
        p2.propagate(fers[1], 1, EventType.INSTANTIATE.strengthened_mask);
        expectLastCall().times(M);
        run(true);

    }

    @Test
    public void var() throws ContradictionException {
        expect(iv1.getId()).andReturn(1).times(2);
        expect(iv2.getId()).andReturn(3).times(2);
        expect(p1.getId()).andReturn(0).times(7);
        expect(p2.getId()).andReturn(2).times(7);
        replay(iv1, iv2, p1, p2);

        fers = new FineVarEventRecorder[2];
        fers[0] = new FineVarEventRecorder(iv1, new Propagator[]{p1, p2}, new int[]{0, 1}, solver, engine);
        engine.addEventRecorder(fers[0]);
        fers[1] = new FineVarEventRecorder(iv2, new Propagator[]{p1, p2}, new int[]{1, 0}, solver, engine);
        engine.addEventRecorder(fers[1]);

        engine.set(new Queue(fers));

        engine.activatePropagator(p1);
        engine.activatePropagator(p2);
        verify(iv1, iv2, p1, p2);
        reset(iv1, iv2, p1, p2);

        // for update
        expect(iv1.getId()).andReturn(1).times(M);
        expect(p2.getId()).andReturn(2).times(M);
        expect(p2.getPropagationConditions(1)).andReturn(EventType.INSTANTIATE.mask).times(M);
        p1.incNbRecorderEnqued();
        expectLastCall().times(1);
        p2.incNbRecorderEnqued();
        expectLastCall().times(1);
        run(false);

        expect(p1.getId()).andReturn(0).times(M);
        expect(p2.getId()).andReturn(2).times(M);
        p1.decNbRecrodersEnqued();
        expectLastCall().times(1);
        p2.decNbRecrodersEnqued();
        expectLastCall().times(1);
        replay(p1, p2);
        engine.flush();
        reset(p1, p2);

        // for update
        expect(iv1.getId()).andReturn(1).times(M);
        expect(p2.getId()).andReturn(2).times(M);
        expect(p2.getPropagationConditions(1)).andReturn(EventType.INSTANTIATE.mask).times(M);
        // for execute
        expect(p1.getId()).andReturn(0).times(M);
        expect(p2.getId()).andReturn(2).times(M);
        p1.incNbRecorderEnqued();
        expectLastCall().times(M);
        p2.incNbRecorderEnqued();
        expectLastCall().times(M);
        p1.decNbRecrodersEnqued();
        expectLastCall().times(M);
        p2.decNbRecrodersEnqued();
        expectLastCall().times(M);
        expect(p2.isActive()).andReturn(true).times(M);
        p2.propagate(fers[0], 1, EventType.INSTANTIATE.strengthened_mask);
        expectLastCall().times(M);
        run(true);
    }

    private void run(boolean execute) throws ContradictionException {
        replay(iv1, iv2, p1, p2);
        long t = -System.nanoTime();
        for (int i = 0; i < M; i++) {
            engine.onVariableUpdate(iv1, EventType.INSTANTIATE, p1);
            if (execute) engine.propagate();
        }
        t += System.nanoTime();
        System.out.printf("%.3fms %s", t / 1000d / 1000d, execute?"\n":" ");
        verify(iv1, iv2, p1, p2);
        reset(iv1, iv2, p1, p2);
    }

    public static void main(String[] args) throws Exception {
        StressTest st = new StressTest();
        for (st.M = 10; st.M < 10000001; st.M *= 10) {
            System.out.println(st.M + " ******");
            st.setUp();
            st.arc();
            st.setUp();
            st.prop();
            st.setUp();
            st.var();
        }
    }

}
