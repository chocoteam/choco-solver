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

package parser.flatzinc;

import gnu.trove.map.hash.THashMap;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import parser.flatzinc.ast.Datas;
import parser.flatzinc.ast.Exit;
import solver.Solver;
import solver.propagation.DSLEngine;
import solver.propagation.generator.Arc;
import solver.propagation.generator.PropagationStrategy;
import solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import solver.propagation.hardcoded.TwoBucketPropagationEngine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class ParseAndSolveExt extends ParseAndSolve {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        new ParseAndSolveExt().doMain(args);
    }

    @Override
    public void buildParser(InputStream is, Solver mSolver, Datas datas) {
        try {
            // Create an input character stream from standard in
            ANTLRInputStream input = new ANTLRInputStream(is);
            // Create an ExprLexer that feeds from that stream
            FlatzincFullExtLexer lexer = new FlatzincFullExtLexer(input);
            // Create a stream of tokens fed by the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // Create a parser that feeds off the token stream
            FlatzincFullExtParser parser = new FlatzincFullExtParser(tokens);
            // Begin parsing at rule prog, get return value structure
            FlatzincFullExtParser.flatzinc_ext_model_return r = parser.flatzinc_ext_model();

            // WALK RESULTING TREE
            CommonTree t = (CommonTree) r.getTree(); // get tree from parser
            // Create a tree node stream from resulting tree
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
            FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes); // create a tree parser
            walker.flatzinc_model(mSolver, datas);                 // launch at start rule prog
        } catch (IOException io) {
            Exit.log(io.getMessage());
        } catch (RecognitionException re) {
            Exit.log(re.getMessage());
        }
    }

    @Override
    protected void makeEngine(Solver solver, Datas datas) {

        switch (eng) {
            case 1:
                solver.set(new TwoBucketPropagationEngine(solver));
                break;
            case 2:
                solver.set(new SevenQueuesPropagatorEngine(solver));
                break;
            case 4:
            case 5:
            case 6:
                DSLEngine pe = new DSLEngine(solver);
                ArrayList<Arc> pairs = Arc.populate(solver);
                THashMap<String, ArrayList> groups = new THashMap<String, ArrayList>(1);
                groups.put("All", pairs);
                String st;

                switch (eng) {
                    case 4:
                        st = "All as queue(wone) of {each prop as list(for)};";
                        break;
                    case 5:
                        st = "All as queue(wone) of {each var as list(for)};";
                        break;
                    case 6:
                        st = "All as list(wone) of {each prop.prioDyn as queue(one) of {each prop as list(for)}};";
                        break;
                    default:
                        st = "";
                }
                try {
                    // Create an input character stream from standard in
                    InputStream in = new ByteArrayInputStream(st.getBytes());
                    // Create an input character stream from standard in
                    ANTLRInputStream input = new ANTLRInputStream(in);
                    // Create an ExprLexer that feeds from that stream
                    FlatzincFullExtLexer lexer = new FlatzincFullExtLexer(input);
                    // Create a stream of tokens fed by the lexer
                    CommonTokenStream tokens = new CommonTokenStream(lexer);
                    // Create a parser that feeds off the token stream
                    FlatzincFullExtParser parser = new FlatzincFullExtParser(tokens);
                    // Begin parsing at rule prog, get return value structure
                    FlatzincFullExtParser.structure_return r = parser.structure();
                    CommonTree t = (CommonTree) r.getTree();
                    CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
                    FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes);
                    walker.mSolver = solver;
                    walker.datas = datas;
                    walker.groups = groups;
                    PropagationStrategy ps = walker.structure(pe);
                    pe.set(ps);
                    solver.set(pe);
                } catch (IOException re) {
                    throw new FZNException("cannot parse engine!");
                } catch (RecognitionException re) {
                    throw new FZNException("cannot parse engine!");
                }
                break;
        }
    }

}
