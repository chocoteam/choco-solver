package choco.proba;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 28/11/11
 */
public class OneAllDiffBenchProbas extends AbstractBenchProbas {

    Instance inst;

    public OneAllDiffBenchProbas(int n, AllDifferent.Type type, int frequency, boolean active,
                                 BufferedWriter out, int seed, boolean isProba) throws IOException {
        super(new Solver(), n, type, frequency, active, out, seed, isProba);
    }

    @Override
    void solveProcess() {
        this.solver.findAllSolutions();
    }

    @Override
    void buildProblem(int size, boolean proba) {
        this.inst = new Instance(size, 0, size - 1, seed, 0, solver);
        this.vars = inst.generate(Distribution.UNIFORM_INTERVALS);
        this.allVars = vars;
        this.cstrs = new Constraint[]{new AllDifferent(vars, solver, type)};
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // Representation d'une instance pour la contrainte AllDifferent
    // Incluant differentes methodes de generation des domaines des variables
    //////////////////////////////////////////////////////////////////////////////////////////
    class Instance {
        int n; // nb vars
        int min, max; // plus petite et plus grande valeur possible pour un domaine
        int size;

        Solver solver;
        IntVar[] vars;
        int[][] domains;
        Set<Integer> unionDoms;

        Distribution type;
        Random r;

        Instance(int n, int min, int max, long seed, int size, Solver solver) {
            this.n = n;
            this.min = min;
            this.max = max;
            this.r = new Random(seed);
            this.size = size;
            this.solver = solver;
        }

        public IntVar[] generate(Distribution type) {
            this.type = type;
            this.domains = new int[n][];
            this.unionDoms = new HashSet<Integer>();
            this.vars = new IntVar[n];
            switch (type) {
                case ALL_EQUAL_SETS:
                    domains = allEqualSizeSets();
                    break;
                case ALL_EQUAL_INTERVALS:
                    domains = allEqualSizeIntervals(size);
                    break;
                case ALL_EQUAL_INTERVALS_SIZEFREE:
                    domains = allEqualSizeIntervalsSizefree();
                    break;
                case UNIFORM_SETS:
                    domains = uniformallyDistributedSets();
                    break;
                case SIZE_UNIFORM_INTERVALS:
                    domains = sizeUniformallyDistributedIntervals();
                    break;
                case LBOUND_SIZE_INTERVALS:
                    domains = lBoundUniformallyDistributedIntervals();
                    break;
                case LRBOUND_INTERVALS:
                    domains = boundsUniformallyDistributedIntervals();
                    break;
                case UNIFORM_INTERVALS:
                    domains = uniformallyDistributedIntervals();
                    break;
            }
            buildVars();
            return vars;
        }

        //////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////// Methodes privees internes a la classe //////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        private void buildVars() {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < domains[i].length; j++) {
                    unionDoms.add(domains[i][j]);
                }
                vars[i] = VariableFactory.enumerated("x" + i, domains[i], solver);
            }
        }

        // generate n randomized domains in the following way :
        // build max-min values between [min,max], store the result in values[]
        // choose n domains of fixed size s, randomly choosen in [1;sizeMax]
        // each domain is provided by shuffling and cutting values[] to its the first s_th elements

        private int[][] allEqualSizeSets() {
            int sizeMax = max - min + 1;
            int[] values = getValues(sizeMax);
            int s = Math.max(1, r.nextInt(sizeMax));
            domains = new int[n][];
            for (int v = 0; v < n; v++) {
                domains[v] = cutAndSort(s, randomPermutations(values));
            }
            return domains;
        }

        // generate n randomized domains in the following way :
        // each domain is an interval
        // choose n domains of fixed size s,
        // we randomly choose a lower bound between [min;max] according to s

        private int[][] allEqualSizeIntervals(int size) {
            int sizeMax = max - min + 1;
            int[] values = getValues(sizeMax);
            domains = new int[n][];
            if (size == sizeMax) {
                for (int v = 0; v < n; v++) {
                    domains[v] = new int[size];
                    System.arraycopy(values, 0, domains[v], 0, size);
                }
            } else {
                for (int v = 0; v < n; v++) {
                    int inf = r.nextInt(sizeMax - size + 1);
                    domains[v] = new int[size];
                    System.arraycopy(values, inf, domains[v], 0, size);
                }
            }
            return domains;
        }

        private int[][] allEqualSizeIntervalsSizefree() {
            int sizeMax = max - min + 1;
            int[] values = getValues(sizeMax);
            domains = new int[n][];
            if (size == sizeMax) {
                for (int v = 0; v < n; v++) {
                    domains[v] = new int[sizeMax];
                    System.arraycopy(values, 0, domains[v], 0, sizeMax);
                }
            } else {
                int size = r.nextInt(sizeMax) + 1;
                for (int v = 0; v < n; v++) {
                    int inf = r.nextInt(sizeMax - size + 1);
                    domains[v] = new int[size];
                    System.arraycopy(values, inf, domains[v], 0, size);
                }
            }
            return domains;
        }
        // generate n randomized domains in the following way :
        // build max-min values between [min,max], store the result in values[]
        // for each domain, we randomly choose a size s,
        // the domain values are choose by shuffling and cutting values[] to its first s-th elements

        private int[][] uniformallyDistributedSets() {
            int sizeMax = max - min + 1;
            int[] values = getValues(sizeMax);
            domains = new int[n][];
            for (int v = 0; v < n; v++) {
                int s = Math.max(1, r.nextInt(sizeMax));
                domains[v] = cutAndSort(s, randomPermutations(values));
            }
            return domains;
        }

        // generate n randomized domains in the following way :
        // each domain is an interval
        // for each domain, we randomly choose a size s,
        // a lower bound is randomly choosen between [min;max] according to s

        private int[][] sizeUniformallyDistributedIntervals() {
            int sizeMax = max - min + 1;
            int[] values = getValues(sizeMax);
            domains = new int[n][];
            for (int v = 0; v < n; v++) {
                int s = r.nextInt(sizeMax) + 1;
                int inf = r.nextInt(sizeMax - s + 1);
                domains[v] = new int[s];
                System.arraycopy(values, inf, domains[v], 0, s);
            }
            return domains;
        }

        private int[][] lBoundUniformallyDistributedIntervals() {
            int sizeMax = max - min + 1;
            int[] values = getValues(sizeMax);
            domains = new int[n][];
            for (int v = 0; v < n; v++) {
                int inf = r.nextInt(sizeMax);
                int s = r.nextInt(sizeMax - inf) + 1;
                domains[v] = new int[s];
                System.arraycopy(values, inf, domains[v], 0, s);
            }
            return domains;
        }

        private int[][] boundsUniformallyDistributedIntervals() {
            int sizeMax = max - min + 1;
            int[] values = getValues(sizeMax);
            domains = new int[n][];
            for (int v = 0; v < n; v++) {
                int b1 = r.nextInt(sizeMax);
                int b2 = r.nextInt(sizeMax);
                int inf = Math.min(b1, b2);
                int sup = Math.max(b1, b2);
                domains[v] = new int[sup - inf + 1];
                System.arraycopy(values, inf, domains[v], 0, sup - inf + 1);
            }
            return domains;
        }

        private int[][] uniformallyDistributedIntervals() {
            int sizeMax = max - min + 1;
            int[] values = getValues(sizeMax);
            int[][] allPossibleDoms = new int[(sizeMax * (sizeMax - 1)) / 2][];
            int k = 0;
            for (int l = 2; l <= sizeMax; l++) {
                for (int a = 0; a <= sizeMax - l; a++) {
                    allPossibleDoms[k] = new int[l];
                    System.arraycopy(values, a, allPossibleDoms[k], 0, l);
                    k++;
                }
            }
            domains = new int[n][];
            for (int v = 0; v < n; v++) {
                int indice = r.nextInt((sizeMax * (sizeMax - 1)) / 2);
                domains[v] = new int[allPossibleDoms[indice].length];
                domains[v] = allPossibleDoms[indice];
            }
            return domains;
        }


        private int[] getValues(int sizeMax) {
            int[] values = new int[sizeMax];
            for (int k = 0; k < sizeMax; k++) {
                values[k] = k + min;
            }
            return values;
        }

        private int[] randomPermutations(int[] tab) {
            int l = tab.length;
            for (int i = 0; i < l; i++) {
                int j = r.nextInt(l);
                int tmp = tab[i];
                tab[i] = tab[j];
                tab[j] = tmp;
            }
            return tab;
        }

        private int[] cutAndSort(int size, int[] tab) {
            int[] tmp = new int[size];
            System.arraycopy(tab, 0, tmp, 0, size);
            Arrays.sort(tmp);
            return tmp;
        }
    }

    enum Distribution {
        ALL_EQUAL_SETS, ALL_EQUAL_INTERVALS, ALL_EQUAL_INTERVALS_SIZEFREE, UNIFORM_SETS, SIZE_UNIFORM_INTERVALS, LBOUND_SIZE_INTERVALS, LRBOUND_INTERVALS, UNIFORM_INTERVALS
    }
}
