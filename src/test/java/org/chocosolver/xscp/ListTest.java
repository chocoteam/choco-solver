/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.xscp;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.chocosolver.parser.xcsp.BaseXCSPListener;
import org.chocosolver.parser.xcsp.XCSP;
import org.chocosolver.pf4cs.SetUpException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * <p>
 * Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 07/06/2017.
 */
public class ListTest {

    private static String ROOT= "/Users/cprudhom/Sources/XCSP/instances";

    private static String XLS= "xcsp" + File.separator + "testInstances.xls";

    @DataProvider
    public static Object[][] cspList() throws IOException {
        ClassLoader cl = ListTest.class.getClassLoader();
        String file = cl.getResource(XLS).getFile();
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        HSSFCell cell;
        int rows; // No of rows
        rows = sheet.getPhysicalNumberOfRows();

        int cols = 0;
        Object[][] instances = new Object[rows-1][2];
        for(int r = 1; r < rows; r++) {
            row = sheet.getRow(r);
            if(row != null) {
//                cols = sheet.getRow(r).getPhysicalNumberOfCells();
                int nbs = 0;
                if(row.getCell(1).toString().trim().equals("SAT")){
                    if(row.getCell(2) == null){
                        nbs = Integer.MAX_VALUE;
                    }else {
                        nbs = (int) row.getCell(2).getNumericCellValue();
                    }
                }
                instances[r - 1] = new Object[]{row.getCell(0).toString().trim(), nbs};
            }
        }
        return instances;
    }


    @Test(groups="ignored", timeOut=300000, dataProvider = "cspList")
    public void testCSP(String file, int nbSol) throws SetUpException {
        XCSP xscp = new XCSP();
        xscp.addListener(new BaseXCSPListener(xscp));
        xscp.setUp(ROOT+ File.separator+ file,
                (nbSol < Integer.MAX_VALUE ?"-a":"-stat"),
                "-stat"/*, "-cs"*/);
        xscp.createSolver();
        try {
            xscp.buildModel();
        }catch (RuntimeException e){
            Assert.fail();
        }
        xscp.configureSearch();
//        xscp.getModel().getSolver().limitTime("100s");
        xscp.solve();
        if(nbSol == Integer.MAX_VALUE){
            Assert.assertTrue(xscp.getModel().getSolver().getSolutionCount() > 0);
        }else {
            Assert.assertEquals(xscp.getModel().getSolver().getSolutionCount(), nbSol);
        }
    }

    @DataProvider
    public static Object[][] copList() throws IOException {
        ClassLoader cl = ListTest.class.getClassLoader();
        String file = cl.getResource(XLS).getFile();
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(1);
        HSSFRow row;
        HSSFCell cell;
        int rows; // No of rows
        rows = sheet.getPhysicalNumberOfRows();

        int cols = 0;
        Object[][] instances = new Object[rows-1][2];
        for(int r = 1; r < rows; r++) {
            row = sheet.getRow(r);
            if(row != null) {
//                cols = sheet.getRow(r).getPhysicalNumberOfCells();
                instances[r - 1] = new Object[]{row.getCell(0).toString().trim(),
                        (int) row.getCell(1).getNumericCellValue()};
            }
        }
        return instances;
    }


    @Test(groups="ignored", timeOut=300000, dataProvider = "copList")
    public void testCOP(String file, int opt) throws SetUpException {
        XCSP xscp = new XCSP();
        xscp.addListener(new BaseXCSPListener(xscp));
        xscp.setUp(ROOT+ File.separator+ file, "-stat"/*, "-cs"*/);
        xscp.createSolver();
        try {
            xscp.buildModel();
        }catch (RuntimeException e){
            Assert.fail();
        }
        xscp.configureSearch();
//        xscp.getModel().getSolver().limitTime("100s");
        xscp.solve();
        Assert.assertTrue(xscp.getModel().getSolver().getSolutionCount() > 0);
        Assert.assertEquals(xscp.getModel().getSolver().getBestSolutionValue(), opt);
    }

}
