/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.search.strategy.Search;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/06/2020
 */
public class RegParserTest {

    RegParser parser;

    @BeforeMethod(alwaysRun = true)
    public void before() {
        parser = new RegParser("test") {
            @Override
            public char getCommentChar() {
                return 0;
            }

            @Override
            public Settings createDefaultSettings() {
                return null;
            }

            @Override
            public void createSolver() {

            }

            @Override
            public Thread actionOnKill() {
                return null;
            }

            @Override
            public void buildModel() {

            }

            @Override
            public void solve() {

            }
        };
    }

    @Test(groups = "1s")
    public void testFile1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        try {
            p.parseArgument();
            Assert.fail();
        } catch (CmdLineException ignored) {
        }
        Assert.assertFalse(parser.free);
        p.parseArgument("/file");
    }

    @Test(groups = "1s")
    public void testLimit1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.limits.time, -1);
        p.parseArgument("-limit=[1]", "/file");
        Assert.assertEquals(parser.limits.time, 1);
        p.parseArgument("-limit=[1s]", "/file");
        Assert.assertEquals(parser.limits.time, 1000);
        p.parseArgument("-limit=[1m1s]", "/file");
        Assert.assertEquals(parser.limits.time, 61000);
        p.parseArgument("-limit=[1h1m1s]", "/file");
        Assert.assertEquals(parser.limits.time, 3661000);
    }

    @Test(groups = "1s")
    public void testLimit2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.limits.sols, -1);
        p.parseArgument("-limit=[2sols]", "/file");
        Assert.assertEquals(parser.limits.sols, 2);
    }

    @Test(groups = "1s")
    public void testLimit3() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.limits.runs, -1);
        p.parseArgument("-limit=[2runs]", "/file");
        Assert.assertEquals(parser.limits.runs, 2);
    }


    @Test(groups = "1s")
    public void testLimit4() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-limit=[2s,2sols,2runs]", "/file");
        Assert.assertEquals(parser.limits.time, 2000);
        Assert.assertEquals(parser.limits.sols, 2);
        Assert.assertEquals(parser.limits.runs, 2);
    }

    @Test(groups = "1s")
    public void testStat1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertFalse(parser.stat);
        p.parseArgument("-stat", "/file");
        Assert.assertTrue(parser.stat);
    }

    @Test(groups = "1s")
    public void testCsv1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertFalse(parser.csv);
        p.parseArgument("-csv", "/file");
        Assert.assertTrue(parser.csv);
    }

    @Test(groups = "1s")
    public void testVarh1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.varH, Search.VarH.DEFAULT);
        p.parseArgument("-f", "-varh", "input", "/file");
        Assert.assertEquals(parser.varH, Search.VarH.INPUT);
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testVarh2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-varh", "input", "/file");
    }

    @Test(groups = "1s")
    public void testVarl1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.valH, Search.ValH.DEFAULT);
        p.parseArgument("-f", "-valh", "max", "/file");
        Assert.assertEquals(parser.valH, Search.ValH.MAX);
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testVarl2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-varl", "max", "/file");
    }

    @Test(groups = "1s")
    public void test() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
    }


    @Test(groups = "1s")
    public void testFree1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertFalse(parser.free);
        p.parseArgument("-f", "/file");
        Assert.assertTrue(parser.free);
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testLc1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-lc", "2", "/file");
    }

    @Test(groups = "1s")
    public void testLc2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("/file");
        Assert.assertEquals(parser.lc, 1);
        p.parseArgument("-f", "-lc", "2", "/file");
        Assert.assertEquals(parser.lc, 2);
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testLc3() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-f", "-cos", "-lc", "2", "/file");
    }

    @Test(groups = "1s", expectedExceptions = CmdLineException.class)
    public void testCos1() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-cos", "/file");
        Assert.assertTrue(parser.cos);
    }

    @Test(groups = "1s")
    public void testCos2() throws CmdLineException {
        CmdLineParser p = new CmdLineParser(parser);
        p.parseArgument("-f", "-cos", "/file");
        Assert.assertTrue(parser.cos);
    }

}