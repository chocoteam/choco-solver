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

package choco.serializable;

import org.testng.Assert;
import org.testng.annotations.Test;
import samples.nqueen.NQueenBinary;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 3 déc. 2010
 */
public class SerializableTest {

    private static File create() throws IOException {
        return File.createTempFile("SOLVER", ".ser");
    }


    private static File write(final Object o) throws IOException {
        final File file = create();
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(o);
        out.close();
        return file;
    }


    private static Object read(final File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        fis = new FileInputStream(file);
        in = new ObjectInputStream(fis);
        final Object o = in.readObject();
        in.close();
        return o;
    }

    @Test(groups = {"1s"})
    public void testEmptySolver() {
        Solver solver = new Solver();
        File file = null;
        try {
            file = write(solver);
        } catch (IOException e) {
            e.printStackTrace();
        }
        solver = null;
        Assert.assertNull(solver);
        try {
            solver = (Solver) read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(solver);
    }

    @Test(groups = {"1s"})
    public void testEngine() {
        IPropagationEngine eng = new PropagationEngine();
        File file = null;
        try {
            file = write(eng);
        } catch (IOException e) {
            e.printStackTrace();
        }
        eng = null;
        Assert.assertNull(eng);
        try {
            eng = (IPropagationEngine) read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(eng);
    }

    @Test(groups = {"1s"})
    public void testIntegerVariable() {
        Solver s = new Solver();
        IntVar var = VariableFactory.enumerated("v", 1, 10, s);
        File file = null;
        try {
            file = write(var);
        } catch (IOException e) {
            e.printStackTrace();
        }
        var = null;
        try {
            var = (IntVar) read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(var);
    }

    @Test(groups = {"1s"})
    public void testConstraint() {
        Solver s = new Solver();
        IntVar var = VariableFactory.enumerated("v", 1, 10, s);
        Constraint c = ConstraintFactory.eq(var, 0, s);
        File file = null;
        try {
            file = write(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        c = null;
        try {
            c = (Constraint) read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(c);
    }

    @Test(groups = {"1s"})
    public void testNQueen() {
        NQueenBinary pb = new NQueenBinary();
        pb.readArgs("-q", "8");
        pb.buildModel();
        pb.configureSolver();

        Solver s = pb.getSolver();

        File file = null;
        try {
            file = write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        s = null;
        try {
            s = (Solver) read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(s);
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 92, "nb sol incorrect");
    }

}
