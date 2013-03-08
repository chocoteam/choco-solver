/*
 * Created by IntelliJ IDEA.
 * User: sofdem - sophie.demassey{at}emn.fr
 * Date: Jul 27, 2010 - 1:53:16 PM
 */

package samples.nsp;

import common.ESat;
import solver.Solver;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Basis of a Choco CPModel for the Nurse Scheduling Problem.
 * This class defined the model variables and the common utility methods (print, parse, etc.)
 * Constraints should be defined in a derived class
 *
 * @author Sophie Demassey
 */
public class NurseSchedulingProblem {

    /**
     * the instance data
     */
    protected NSData data;

    /**
     * the table [nbEmployees][nbDays] of assignment variables
     */
    protected IntVar[][] shifts;

    /**
     * the table [nbEmployees][nbActivities] of activity counter variables
     */
    protected IntVar[][] occurrences;

    /**
     * the table [nbActivities][nbDays] of activity cover variables
     */
    protected IntVar[][] covers;

    /**
     * a short description (the list of constraints) of the built model configuration
     */
    protected String description;

    /**
     * the configuration of the model to build defined as a map ConstraintType -> ChocoImplementation
     */
    protected Map<String, String> options;

    /**
     * build a Choco CP Model without constraints
     *
     * @param data the instance data
     */
    public NurseSchedulingProblem(NSData data, Solver solver) {
        this(data, "", solver);
    }

    /**
     * build a Choco CP Model under some constraint configuration
     *
     * @param data    the instance data
     * @param options the model configuration
     * @param solver
     */
    public NurseSchedulingProblem(NSData data, String options, Solver solver) {
        super();
        this.data = data;
        this.makeVariables(solver);
        this.description = "NSCPModel: ";
        this.parseOptions(options);
    }

    private void makeVariables(Solver solver) {
        this.shifts = VariableFactory.enumeratedMatrix("S", data.nbEmployees(), data.nbDays(), 0, data.nbActivities() - 1, solver);

        this.occurrences = new IntVar[data.nbEmployees()][data.nbActivities()];
        for (int e = 0; e < data.nbEmployees(); e++) {
            for (int a = 0; a < occurrences[e].length; a++) {
                occurrences[e][a] = VariableFactory.bounded("n" + data.getLiteral(a) + e, data.getCounterLB(e, a), data.getCounterUB(e, a), solver);
            }
        }

        this.covers = new IntVar[data.nbActivities()][];
        for (int a = 0; a < covers.length; a++) {
            this.covers[a] = VariableFactory.boundedArray("B", data.nbDays(), data.getCoverLB(a), data.getCoverUB(a), solver);
        }
    }

    /**
     * print the current solver solution if exists
     *
     * @param s the CPSolver
     */
    public String solutionToString(Solver s) {
        if (ESat.TRUE != s.isFeasible()) {
            return null;
        }
        StringBuffer buf = new StringBuffer(100);

        for (int e = 0; e < shifts.length; e++) {
            for (IntVar v : shifts[e]) {
                buf.append(data.getLiteral(v.getValue())).append(" ");
            }
            buf.append("% ");
            for (IntVar v : occurrences[e]) {
                buf.append((v == null) ? ". " : v.getValue() + " ");

            }
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * print the current solver solution if exists
     *
     * @param s the CPSolver
     */
    public void printSolution(Solver s) {
        if (ESat.TRUE != s.isFeasible()) {
            System.out.println("No solution found.");
            return;
        }
        for (int e = 0; e < shifts.length; e++) {
            for (IntVar v : shifts[e]) {
                System.out.print(data.getLiteral(v.getValue()) + " ");
            }
            System.out.print("% ");
            for (IntVar v : occurrences[e]) {
                System.out.print((v == null) ? ". " : v.getValue() + " ");

            }
            System.out.println("");
        }
    }

    /**
     * get the current solver solution as the assignment table if exists
     *
     * @param s the CPSolver
     * @return the current solver solution as the assignment table [nbEmployees][nbDays] if exists, null otherwise
     */
    public int[][] getSolution(Solver s) {
        if (ESat.TRUE != s.isFeasible()) {
            return null;
        }
        int[][] solution = new int[shifts.length][shifts[0].length];
        for (int e = 0; e < shifts.length; e++) {
            for (int t = 0; t < shifts[e].length; t++) {
                solution[e][t] = shifts[e][t].getValue();
            }
        }
        return solution;
    }

    /**
     * get a description of the model configuration
     *
     * @return the description (list of constraints)
     */
    public String getDescription() {
        return description + ".";
    }

    /**
     * check whether a constraint of a given type should be built according to the required model configuration
     *
     * @param ctrDesc the constraint type
     * @return true iff the constraint has to be built
     */
    protected boolean isSetConstraint(String ctrDesc) {
        return options.containsKey(ctrDesc);
    }

    /**
     * get the wanted Choco implementation of a constraint type according to the required model configuration
     *
     * @param ctrDesc the constraint type
     * @return the wanted Choco implementation
     */
    protected String getConstraintOption(String ctrDesc) {
        return options.get(ctrDesc);
    }

    private void parseOptions(String options) {
        this.options = new HashMap<String, String>();
        if (options == null) return;
        String[] opts = options.trim().split(" ");
        for (String opt : opts) {
            String[] x = opt.split("[\\[\\]]");
            String o = (x.length == 1) ? "" : x[1];
            this.options.put(x[0], o);
        }
    }

    public NSData getData() {
        return data;
    }

    public IntVar[][] getShifts() {
        return shifts;
    }

    public IntVar[][] getOccurrences() {
        return occurrences;
    }

    public IntVar[][] getCovers() {
        return covers;
    }
}
