/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.propagation.hardcoded.TwoBucketPropagationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 3 dec. 2010
 */
public class SerializableTest {

    private static File create() throws IOException {
        return File.createTempFile("SOLVER", ".ser");
    }


    private static File write(final Object o) throws IOException {
        final File file = create();
        FileOutputStream fos;
        ObjectOutputStream out;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(o);
        out.close();
        return file;
    }


    private static Object read(final File file) throws IOException, ClassNotFoundException {
        FileInputStream fis;
        ObjectInputStream in;
        fis = new FileInputStream(file);
        in = new ObjectInputStream(fis);
        final Object o = in.readObject();
        in.close();
        return o;
    }

    @Test(groups="1s", timeOut=60000)
    public void testEmptyModel() {
        Model model = new Model();
        File file = null;
        try {
            file = write(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
        model = null;
        try {
            model = (Model) read(file);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(model);
    }

    @Test(groups="1s", timeOut=60000)
    public void testEngine1() {
        IPropagationEngine eng = new TwoBucketPropagationEngine(new Model());
        File file = null;
        try {
            file = write(eng);
        } catch (IOException e) {
            e.printStackTrace();
        }
        eng = null;
        try {
            eng = (IPropagationEngine) read(file);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(eng);
    }

    @Test(groups="1s", timeOut=60000)
    public void testEngine2() {
        IPropagationEngine eng = new SevenQueuesPropagatorEngine(new Model());
        File file = null;
        try {
            file = write(eng);
        } catch (IOException e) {
            e.printStackTrace();
        }
        eng = null;
        try {
            eng = (IPropagationEngine) read(file);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(eng);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIntegerVariable() {
        Model s = new Model();
        IntVar var = s.intVar("v", 1, 10, false);
        File file = null;
        try {
            file = write(var);
        } catch (IOException e) {
            e.printStackTrace();
        }
        var = null;
        try {
            var = (IntVar) read(file);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(var);
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstraint() {
        Model s = new Model();
        IntVar var = s.intVar("v", 1, 10, false);
        Constraint c = s.arithm(var, "=", 0);
        File file = null;
        try {
            file = write(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        c = null;
        try {
            c = (Constraint) read(file);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(c);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQueen() {
        Model s = new Model();
        int n = 8;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("Q_" + i, 1, n, false);
        }


        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = s.arithm(vars[i], "!=", vars[j]);
                neq.post();
                s.arithm(vars[i], "!=", vars[j], "+", -k).post();
                s.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }


        File file = null;
        try {
            file = write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        s = null;
        try {
            s = (Model) read(file);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        assertNotNull(s);
        while (s.solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), 92, "nb sol incorrect");
    }

}
