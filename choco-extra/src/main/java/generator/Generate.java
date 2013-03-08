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
package generator;

import org.cojen.classfile.RuntimeClassFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/07/12
 */
public class Generate {

    public static void main(String[] args) throws IOException {
        File dir = createTempDir();
        String udir = System.getProperty("user.dir");
        CreateJarFile cjf = new CreateJarFile();
        cjf.init(udir + "/generated.jar");

        // make Queue + execute = while_one
        RuntimeClassFile cf = MakeQueue.createClassFile("generated", "QueueWhile", false, true);
        cf.defineClass();
        FileOutputStream fos = new FileOutputStream(dir + File.separator + "QueueWhile.class");
        cf.writeTo(fos);
        fos.close();
        cjf.add("generated", new File(dir + File.separator + "QueueWhile.class"));

        // make Queue + execute = one
        cf = MakeQueue.createClassFile("generated", "QueueOne", false, false);
        cf.defineClass();
        fos = new FileOutputStream(dir + File.separator + "QueueOne.class");
        cf.writeTo(fos);
        fos.close();
        cjf.add("generated", new File(dir + File.separator + "QueueOne.class"));

        /*// make Stack + execute = while_one
        cf = MakeQueue.createClassFile("generated", "StackWhile", false, true);
        cf.defineClass();
        fos = new FileOutputStream(dir + File.separator + "StackWhile.class");
        cf.writeTo(fos);
        fos.close();
        cjf.add("generated", new File(dir + File.separator + "StackWhile.class"));

        // make Queue + execute = one
        cf = MakeQueue.createClassFile("generated", "StackOne", false, false);
        cf.defineClass();
        fos = new FileOutputStream(dir + File.separator + "StackOne.class");
        cf.writeTo(fos);
        fos.close();
        cjf.add("generated", new File(dir + File.separator + "StackOne.class"));*/


        cjf.close();
    }

    public static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < 1000; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within "
                + 1000 + " attempts (tried "
                + baseName + "0 to " + baseName + (1000 - 1) + ')');
    }

}
