package org.chocosolver.util.tools;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 30/01/2017.
 */
public class VariableUtilsTest {

    @Test(groups = "1s")
    public void testBoundsForDivision() throws Exception {
        int[] values = {-10,-2,-1,0,1,2,10};
        int n = values.length;
        for(int l1 = 0; l1 < n; l1++){
            for(int u1 = l1; u1 < n; u1++){
                for(int l2 = 0; l2 < n; l2++){
                    for(int u2 = l2; u2 < n; u2++){
                        Model model = new Model();
                        IntVar x = model.intVar(values[l1], values[u1]);
                        IntVar y = model.intVar(values[l2], values[u2]);
                        int[] bounds = VariableUtils.boundsForDivision( x, y);
                        IntVar z = model.intVar(bounds[0], bounds[1]);
                        model.div(x, y, z).post();
                        model.getSolver().findAllSolutions();
                        Assert.assertEquals(model.getSolver().getSolutionCount(),
                                listForDivision(values[l1],values[u1],values[l2],values[u2]).size());
                    }
                }
            }
        }
    }

    private List<Integer> listForDivision(int l1, int u1, int l2, int u2){
        List<Integer> values = new ArrayList<>();
        for(int i = l1; i <= u1; i++){
            for(int j = l2; j <= u2; j++){
                if(j!=0)values.add(i/j);
            }
        }
        return values;
    }

    @Test(groups = "1s")
    public void testBoundsForAddition() throws Exception {
        int[] values = {-10,-2,-1,0,1,2,10};
        int n = values.length;
        for(int l1 = 0; l1 < n; l1++){
            for(int u1 = l1; u1 < n; u1++){
                for(int l2 = 0; l2 < n; l2++){
                    for(int u2 = l2; u2 < n; u2++){
                        Model model = new Model();
                        IntVar x = model.intVar(values[l1], values[u1]);
                        IntVar y = model.intVar(values[l2], values[u2]);
                        int[] bounds = VariableUtils.boundsForAddition( x, y);
                        IntVar z = model.intVar(bounds[0], bounds[1]);
                        model.arithm(x, "+", y,"=", z).post();
                        model.getSolver().findAllSolutions();
                        Assert.assertEquals(model.getSolver().getSolutionCount(),
                                listForAddition(values[l1],values[u1],values[l2],values[u2]).size());
                    }
                }
            }
        }
    }

    private List<Integer> listForAddition(int l1, int u1, int l2, int u2){
        List<Integer> values = new ArrayList<>();
        for(int i = l1; i <= u1; i++){
            for(int j = l2; j <= u2; j++){
                values.add(i+j);
            }
        }
        return values;
    }

    @Test(groups = "1s")
    public void testBoundsForSubstraction() throws Exception {
        int[] values = {-10,-2,-1,0,1,2,10};
        int n = values.length;
        for(int l1 = 0; l1 < n; l1++){
            for(int u1 = l1; u1 < n; u1++){
                for(int l2 = 0; l2 < n; l2++){
                    for(int u2 = l2; u2 < n; u2++){
                        Model model = new Model();
                        IntVar x = model.intVar(values[l1], values[u1]);
                        IntVar y = model.intVar(values[l2], values[u2]);
                        int[] bounds = VariableUtils.boundsForSubstraction( x, y);
                        IntVar z = model.intVar(bounds[0], bounds[1]);
                        model.arithm(x, "-", y,"=", z).post();
                        model.getSolver().findAllSolutions();
                        Assert.assertEquals(model.getSolver().getSolutionCount(),
                                listForSubstraction(values[l1],values[u1],values[l2],values[u2]).size());
                    }
                }
            }
        }
    }

    private List<Integer> listForSubstraction(int l1, int u1, int l2, int u2){
        List<Integer> values = new ArrayList<>();
        for(int i = l1; i <= u1; i++){
            for(int j = l2; j <= u2; j++){
                values.add(i+j);
            }
        }
        return values;
    }

    @Test(groups = "1s")
    public void testBoundsForMultiplication() throws Exception {
        int[] values = {-10,-2,-1,0,1,2,10};
        int n = values.length;
        for(int l1 = 0; l1 < n; l1++){
            for(int u1 = l1; u1 < n; u1++){
                for(int l2 = 0; l2 < n; l2++){
                    for(int u2 = l2; u2 < n; u2++){
                        Model model = new Model();
                        IntVar x = model.intVar(values[l1], values[u1]);
                        IntVar y = model.intVar(values[l2], values[u2]);
                        int[] bounds = VariableUtils.boundsForMultiplication( x, y);
                        IntVar z = model.intVar(bounds[0], bounds[1]);
                        model.arithm(x, "*", y,"=", z).post();
                        model.getSolver().findAllSolutions();
                        Assert.assertEquals(model.getSolver().getSolutionCount(),
                                listForMultiplication(values[l1],values[u1],values[l2],values[u2]).size());
                    }
                }
            }
        }
    }

    private List<Integer> listForMultiplication(int l1, int u1, int l2, int u2){
        List<Integer> values = new ArrayList<>();
        for(int i = l1; i <= u1; i++){
            for(int j = l2; j <= u2; j++){
                values.add(i*j);
            }
        }
        return values;
    }

}