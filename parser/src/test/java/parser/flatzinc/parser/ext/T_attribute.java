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
package parser.flatzinc.parser.ext;

import junit.framework.Assert;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.testng.annotations.Test;
import parser.flatzinc.FlatzincFullExtParser;
import parser.flatzinc.FlatzincFullExtWalker;
import parser.flatzinc.ast.ext.Attribute;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/10/12
 */
public class T_attribute extends GrammarExtTest {

    public Attribute par_type(FlatzincFullExtParser parser) throws RecognitionException {
        FlatzincFullExtParser.attribute_return r = parser.attribute();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes);
        return walker.attribute();
    }

    @Test
    public void test1() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("v.idx");
        Attribute a = par_type(fp);
        Assert.assertTrue(a == Attribute.VIDX);
    }

    @Test
    public void test2() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("c.idx");
        Attribute a = par_type(fp);
        Assert.assertTrue(a == Attribute.CIDX);
    }

    @Test
    public void test3() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("p.idx");
        Attribute a = par_type(fp);
        Assert.assertTrue(a == Attribute.PIDX);
    }

    @Test
    public void test4() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("v.cardinality");
        Attribute a = par_type(fp);
        Assert.assertTrue(a == Attribute.VCARD);
    }

    @Test
    public void test5() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("c.arity");
        Attribute a = par_type(fp);
        Assert.assertTrue(a == Attribute.CARITY);
    }

    @Test
    public void test6() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("p.priority");
        Attribute a = par_type(fp);
        Assert.assertTrue(a == Attribute.PPRIO);
    }

    @Test
    public void test7() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("p.arity");
        Attribute a = par_type(fp);
        Assert.assertTrue(a == Attribute.PARITY);
    }

    @Test
    public void test8() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("p.prioDyn");
        Attribute a = par_type(fp);
        Assert.assertTrue(a == Attribute.PPRIOD);
    }
}
