/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 27/02/2017.
 */
public class CryptoTest {

    public static int R = 6;
    public static int block = 16;
    public static int quarter_block = 4;

    public static Tuples Xor_3_path = new Tuples(true);
    public static Tuples Xor_2_path = new Tuples(true);
    public static Tuples L_path = new Tuples(true);


    public static void Gen_path(){
        Xor_2_path.add(0, 0, 0);
        Xor_2_path.add(0, 1, 1);
        Xor_2_path.add(1, 0, 1);
        Xor_2_path.add(1, 1, 0);

        Xor_2_path.add(1, 1, 1);

        Xor_3_path.add(0, 0, 0, 0);
        Xor_3_path.add(0, 0, 1, 1);
        Xor_3_path.add(0, 1, 0, 1);
        Xor_3_path.add(0, 1, 1, 0);
        Xor_3_path.add(1, 0, 0, 1);
        Xor_3_path.add(1, 0, 1, 0);
        Xor_3_path.add(1, 1, 0, 0);
        Xor_3_path.add(1, 1, 1, 1);

        Xor_3_path.add(0, 1, 1, 1);
        Xor_3_path.add(1, 0, 1, 1);
        Xor_3_path.add(1, 1, 0, 1);
//    	Xor_3_path.add(1, 1, 1, 0);

        Gen_L_path();
    }

    public static void Gen_L_path(){
        int[][] temp_s = new int[256][8];
        for(int i = 0 ; i < 256; i ++){
            for(int j = 0; j < 8; j ++){
                temp_s[i][j] = 0;
            }
        }
        L_path.add(0, 0, 0, 0, 0, 0, 0, 0);
        for(int i = 0; i < 256; i ++){
            int temp = 0;
            for(int j = 0; j < 8; j++){
                temp_s[i][j] = i >> (7 - j) & 1;
                temp = temp + temp_s[i][j];
            }
            if(temp >= 5){
                L_path.add(temp_s[i]);
            }
        }
    }

    @DataProvider(name="table")
    public Object[][] tables() {
        return new String[][]{
                {"CT+"}, {"FC"}, {"GAC2001"}, {"GACSTR+"}, {"GAC2001+"}, {"GAC3rm+"}, {"GAC3rm"}, {"STR2+"}, {"MDD+"}
        };
    }
    
    @Test(groups="1s", timeOut=60000, dataProvider = "table")
    public void testCS71234(String table){
        //initialize the Tuples: Xor_3_path, Xor_2_path, L_path
        Gen_path();
        Settings settings = new DefaultSettings().setWarnUser(false);
        Model model = new Model(settings);

    	/*   declare all vars at round    */
        IntVar[][] input_at_round = model.intVarMatrix("in", R + 1, block, 0, 1);
        IntVar[][] before_S_at_round = model.intVarMatrix("befS", R, quarter_block, 0, 1);
        IntVar[][] after_L_at_round = model.intVarMatrix("aftL", R, quarter_block, 0, 1);

        IntVar[][] valid_patterns_L_at_round = new IntVar[R][quarter_block + quarter_block];
        IntVar[][][] valid_patterns_right_xor_at_round = new IntVar[R][quarter_block][3 + 1];
        IntVar[][][] valid_patterns_left_xor_at_round = new IntVar[R][quarter_block][2 + 1];

        /*   set my input constraint    */
        int[] coff = new int[block];
        for(int i = 0; i < block; i ++){
            coff[i] = 1;
        }
        model.scalar(input_at_round[0], coff, ">", 0).post();
//        model.scalar(input_at_round[0], coff, "<", 9).post();

        /*   set prapogation constraints at round    */
        for(int r = 0; r < R; r++){
        	/*   right xor 4 with : 3 bit in, 1 bit out   */
            for(int position = 0; position < quarter_block; position++){
                for(int i = 0; i < 3; i ++){
                    valid_patterns_right_xor_at_round[r][position][i] =
                            input_at_round[r][quarter_block * (i + 1) + position];
                }
                valid_patterns_right_xor_at_round[r][position][3] = before_S_at_round[r][position];
                model.table(valid_patterns_right_xor_at_round[r][position], Xor_3_path, table).post();
            }

        	/*   L 1 with : 4 bit in, 4 bit out    */
            for(int i = 0; i < quarter_block; i ++){
                valid_patterns_L_at_round[r][i] = before_S_at_round[r][i];
                valid_patterns_L_at_round[r][i + 4] = after_L_at_round[r][i];
            }
            model.table(valid_patterns_L_at_round[r], L_path, table).post();

        	/*   right xor 4 with : 2 bit in, 1 bit out    */
            for(int position = 0; position < quarter_block; position++){
                valid_patterns_left_xor_at_round[r][position][0] = input_at_round[r][position];
                valid_patterns_left_xor_at_round[r][position][1] = after_L_at_round[r][position];
                valid_patterns_left_xor_at_round[r][position][2] =
                        input_at_round[r + 1][quarter_block * 3 + position];

                model.table(valid_patterns_left_xor_at_round[r][position], Xor_2_path, table).post();
            }

        	/*   the shift between two round    */
            for(int i = 0; i < 3 * quarter_block; i ++){
                input_at_round[r + 1][i] = input_at_round[r][quarter_block + i];
            }

        }


        /*   collect domwdeg's vars    */
        IntVar[] vars = new IntVar[(R + 1) * block + R * quarter_block * 2];
        int cpt = 0;
        for(int i = 0; i < R + 1; i++){
            if(i < R){
                for(int j = 0; j < block; j ++){
                    vars[cpt++] = input_at_round[i][j];
                }
                for(int j = 0; j < quarter_block; j ++){
                    vars[cpt++] = before_S_at_round[i][j];
                }
                for(int j = 0; j < quarter_block; j ++){
                    vars[cpt++] = after_L_at_round[i][j];
                }
            }
            else if(i == R){
                for(int j = 0; j < block; j ++){
                    vars[cpt++] = input_at_round[i][j];
                }
            }
        }

    	/*   set OBJ to minimize    */
        IntVar OBJ = model.intVar("objective value", 0, 128 / 6);
        IntVar[] activeflag = new IntVar[R * quarter_block];
        for(int i = 0; i < R; i++){
            for(int j = 0; j < quarter_block; j ++){
                activeflag[i * quarter_block + j] = before_S_at_round[i][j];
            }
        }
        model.sum(activeflag, "=", OBJ).post();
        model.setObjective(Model.MINIMIZE, OBJ);

        Solver solver = model.getSolver();
        solver.setSearch(Search.domOverWDegSearch(vars));
        solver.setSearch(Search.lastConflict(solver.getSearch()));
        solver.findAllOptimalSolutions(OBJ, false);
        Assert.assertEquals(solver.getSolutionCount(), 60);
    }
    
}
