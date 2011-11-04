package solver.requests.conditions;

import choco.kernel.memory.IEnvironment;
import solver.constraints.probabilistic.propagators.nary.Union;
import solver.requests.ConditionnalRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 3 nov. 2011
 */
public class CondAllDiffBC extends AbstractCondition {

    protected Union unionset;
    protected IntVar[] vars;

    public CondAllDiffBC(IEnvironment environment, IntVar[] vars) {
        super(environment);
        this.vars = vars;
        this.unionset = new Union(vars, environment);
    }


    @Override
    boolean isValid() {
        return true;  // ici appeler le calcul de la proba : on retourne vrai avec une chance de 1-proba ?
    }

    @Override
    boolean alwaysValid() {
        return false; // on doit appeler la condition a chaque pas
    }

    @Override
    void update(ConditionnalRequest request, int evtMask) {
        if(EventType.isRemove(evtMask)){
            for (int i = request.fromDelta(); i <= request.toDelta(); i++) {
                unionset.remove(i);
            }
        }
    }

    /**
     * test for unionset => has to be executed in the search loop at the beginning of downBranch method
     *
     * @return true if unionset is ok
     */
    public boolean checkUnion() {
        int[] toCheck = unionset.getValues();
        Arrays.sort(toCheck);
        int[] computed = computeUnion();
        Arrays.sort(computed);
        if (toCheck.length != computed.length) {
            System.out.println(printTab("incr", toCheck));
            System.out.println("--------------------");
            System.out.println(printTab("comp", computed));
            return false;
        } else {
            int i = 0;
            while (i < toCheck.length && toCheck[i] == computed[i]) {
                i++;
            }
            if (i != toCheck.length) {
                System.out.println(printTab("incr", toCheck));
                System.out.println("--------------------");
                System.out.println(printTab("comp", computed));
                return false;
            } else {
                return true;
            }
        }
    }

    private int[] computeUnion() {
        Set<Integer> vals = new HashSet<Integer>();
        for (IntVar var : vars) {
            int ub = var.getUB();
            for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
                vals.add(i);
            }
        }
        int[] res = new int[vals.size()];
        int j = 0;
        for (Integer i : vals) {
            res[j++] = i;
        }
        return res;
    }

    private String printTab(String s, int[] tab) {
        String res = s + " : [";
        for (int aTab : tab) {
            res += aTab + ", ";
        }
        res = res.substring(0, res.length() - 2);
        res += "]";
        return res;
    }
}
