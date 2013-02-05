package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.extension.nary.IterTuplesTable;
import solver.constraints.propagators.extension.nary.LargeRelation;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Model provided by Thierry Petit in its Choco version
 */
public class SortingChordsBenchProbas extends AbstractBenchProbas {

    // makeChords
    int[][] costs; // indexes are costs
    int nbnotesmax;

    // makeVars
    IntVar[] chords;
    IntVar[] costvars; // length-1
    IntVar obj;

    List<List<int[]>> tuples;

    // -----
    // build
    // -----
    protected static int[][] makeChords(int n, int notesmax, int seed) {
        Random r = new Random(seed);
        int[][] costs = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < costs[i].length; j++) {
                if (i == j) {
                    costs[i][j] = 0;
                } else {
                    costs[i][j] = ((Math.abs(r.nextInt())) % notesmax);
                    costs[j][i] = costs[i][j];
                }
            }
        }
        return costs;
    }

    public SortingChordsBenchProbas(int n, int notesmax, AllDifferent.Type type, int nbTests, int seed, boolean isProba) throws IOException {
        super(new Solver(), n, type, nbTests, seed, isProba);
        this.nbnotesmax = notesmax;

    }

    @Override
    void buildProblem(int size, boolean proba) {
        this.costs = makeChords(size, nbnotesmax, seed);
        makeVars();
        makeConstraints();
    }

    /*void configSearchStrategy() {
        this.solver.set(StrategyFactory.domwdegMindom(this.vars, this.solver, seed));
    }*/

    public void makeVars() {
        this.vars = new IntVar[size];
        this.allVars = new IntVar[size + size - 1 + 2];
        chords = new IntVar[size];
        costvars = new IntVar[size - 1];
        for (int i = 0; i < size; i++) {
            chords[i] = VariableFactory.enumerated("chords[" + i + "]", 0, size, solver);
            if (i < size - 1) {
                costvars[i] = VariableFactory.enumerated("costs[" + i + "]", 0, (nbnotesmax - 1), solver);
            }
        }
        obj = VariableFactory.enumerated("obj", 0, ((size - 1) * (nbnotesmax - 1)), solver);
        int i;
        for (i = 0; i < size; i++) {
            //System.out.println(vars.length + " -- " + i);
            vars[i] = allVars[i] = chords[i];
        }
        for (int k = 0; k < size - 1; k++, i++) {
            //System.out.println(vars.length + " -- " + i);
            allVars[i] = costvars[k]; // brancher sur les chords suffit
        }
        //System.out.println(vars.length + " -- " + i);
        allVars[i] = obj;  // on ne met pas l'objectif dans les vars de decision
    }

    public void makeConstraints() {
        this.cstrs = new Constraint[size - 1 + 2];
        int nbCstrs = 0;
        this.tuples = new ArrayList<List<int[]>>(size - 1);
        for (int i = 0; i < size - 1; i++) {
            IntVar[] vars = new IntVar[3];
            vars[0] = chords[i];
            vars[1] = chords[i + 1];
            vars[2] = costvars[i];
            List<int[]> tuples_i = new ArrayList<int[]>();
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    if (j != k) {
                        int[] tab = new int[3];
                        tab[0] = j;
                        tab[1] = k;
                        tab[2] = costs[j][k];
                        tuples_i.add(tab);
                    }
                }
            }
            int[] offsets = new int[3];
            int[] sizes = new int[3];
            for (int j = 0; j < 3; j++) {
                sizes[j] = vars[j].getUB() - vars[j].getLB() + 1;
                offsets[j] = vars[j].getLB();
            }
            LargeRelation relation = new IterTuplesTable(tuples_i, offsets, sizes);
            this.cstrs[nbCstrs++] = IntConstraintFactory.table(vars, relation, "AC32");
            this.tuples.add(tuples_i);
            //m.addConstraint(Choco.feasTupleAC(tuples, vars[0], vars[1], vars[2]));
        }
        this.cstrs[nbCstrs++] = new AllDifferent(chords, solver, type);
        /*int[] coeffs = new int[size];
        Arrays.fill(coeffs, 1);
        coeffs[size-1] = -1;
        IntVar[] sumVars = ArrayUtils.append(costvars,new IntVar[]{obj});  //*/
        //m.addConstraint(Choco.leq(Choco.sum(costvars), obj));
        this.cstrs[nbCstrs] = IntConstraintFactory.sum(costvars, "=", obj);
        //this.cstrs[nbCstrs] = Sum.leq(sumVars, coeffs, 0, solver);
    }

    /*@Override
    void solveProcess() {
        //System.out.printf("%s\n", solver.toString());
        //SearchMonitorFactory.log(solver,true,false);
        this.solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,obj);
        assert checkSolution() : solver.toString();
    } */

    boolean checkSolution() {
        for (int i = 0; i < chords.length; i++) {
            for (int j = 0; j < chords.length; j++) {
                if (i != j) {
                    if (chords[i].getValue() == chords[j].getValue()) {
                        return false;
                    }
                }
            }
        }
        int sum = 0;
        for (int i = 0; i < costvars.length; i++) {
            sum += costvars[i].getValue();
        }
        if (sum != obj.getValue()) {
            return false;
        }
        for (int i = 0; i < size - 1; i++) {
            int varVal1 = chords[i].getValue();
            int varVal2 = chords[i + 1].getValue();
            int varVal3 = costvars[i].getValue();
            List<int[]> tuples_i = tuples.get(i);
            for (int[] tuple : tuples_i) {
                if (varVal1 == tuple[0] && varVal2 == tuple[1] && varVal3 == tuple[2]) {
                    return true;
                }
            }
        }
        return false;
    }

    // ---------
    // Instances
    // ---------

    public static void min0(int seed) {
        int nbchords = 6;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min1(int seed) {
        int nbchords = 8;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min2(int seed) {
        int nbchords = 10;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min3(int seed) {
        int nbchords = 12;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min4(int seed) {
        int nbchords = 14;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min5(int seed) {
        int nbchords = 16;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min6(int seed) {
        int nbchords = 18;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min7(int seed) {
        int nbchords = 20;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min0b(int seed) {
        int nbchords = 6;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min1b(int seed) {
        int nbchords = 8;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min2b(int seed) {
        int nbchords = 10;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min3b(int seed) {
        int nbchords = 12;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min4b(int seed) {
        int nbchords = 14;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min5b(int seed) {
        int nbchords = 16;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min6b(int seed) {
        int nbchords = 18;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min7b(int seed) {
        int nbchords = 20;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    // last table 3

    public static void min3c(int seed) {
        int nbchords = 12;
        int maxnotes = 5;
        int numb = 1;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min5c(int seed) {
        int nbchords = 16;
        int maxnotes = 5;
        int numb = 1;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min7c(int seed) {
        int nbchords = 20;
        int maxnotes = 4;
        int numb = 1;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min3d(int seed) {
        int nbchords = 12;
        int maxnotes = 6;
        int numb = 1;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min5d(int seed) {
        int nbchords = 16;
        int maxnotes = 6;
        int numb = 1;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min7d(int seed) {
        int nbchords = 20;
        int maxnotes = 6;
        int numb = 1;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min3e(int seed) {
        int nbchords = 25;
        int maxnotes = 5;
        int numb = 1;
        int nbTests = 10;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void min5e(int seed) {
        int nbchords = 12;
        int maxnotes = 7;
        int numb = 1;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK10(int seed) {
        int nbchords = 6;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK11(int seed) {
        int nbchords = 8;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK12(int seed) {
        int nbchords = 10;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK13(int seed) {
        int nbchords = 12;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK14(int seed) {
        int nbchords = 14;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK15(int seed) {
        int nbchords = 16;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK16(int seed) {
        int nbchords = 18;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK17(int seed) {
        int nbchords = 20;
        int maxnotes = 4;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK10b(int seed) {
        int nbchords = 6;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK11b(int seed) {
        int nbchords = 8;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK12b(int seed) {
        int nbchords = 10;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK13b(int seed) {
        int nbchords = 12;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK14b(int seed) {
        int nbchords = 14;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK15b(int seed) {
        int nbchords = 16;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK16b(int seed) {
        int nbchords = 18;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }

    public static void minK17b(int seed) {
        int nbchords = 20;
        int maxnotes = 6;
        int numb = 2;
        int nbTests = 100;
        //minimize(seed, nbTests, nbchords, maxnotes, numb);
    }


}
