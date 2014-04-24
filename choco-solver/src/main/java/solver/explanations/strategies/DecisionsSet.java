package solver.explanations.strategies;

import memory.IEnvironment;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.search.loop.SearchLoop;
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;

import java.io.Serializable;
import java.util.ArrayDeque;

/**
 * A specific type of decision which stores a set of decisions to apply.
 * This class is dedicated to explanation framework and is used for path reparation.
 * Decisions to apply "as is" and the one that should be refute, are stored, and the tree search is re-build
 * on a call to DecisionsSet.apply().
 */
public class DecisionsSet extends Decision<IntVar> implements Serializable {
    private final DynamicBacktracking dynamicBacktracking;
    private final ArrayDeque<Decision> decision_path; // list of decisions describing the decision path

    public DecisionsSet(DynamicBacktracking dynamicBacktracking) {
        this.dynamicBacktracking = dynamicBacktracking;
        this.decision_path = new ArrayDeque<Decision>(8);
    }

    public void clearDecisionPath() {
        decision_path.clear();
    }


    public void push(Decision dec) {
        decision_path.addFirst(dec);
    }

    public void setDecisionToRefute(Decision dec) {
        decision_path.addLast(dec);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                          Decision<IntVar> services                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public IntVar getDecisionVariable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getDecisionValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public void buildNext() {
        // nothing to do
    }

    @Override
    public void rewind() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply() throws ContradictionException {
        SearchLoop mSearchLoop = dynamicBacktracking.getSolver().getSearchLoop();
        IEnvironment environment = dynamicBacktracking.getSolver().getEnvironment();
        Decision dec;
        // retrieve the decision applied BEFORE the decision to refute, which is the last one in the decision_path
        Decision dec2ref = decision_path.getLast();

        Decision previous = dec2ref.getPrevious();
        int swi = dec2ref.getWorldIndex();
        //assert swi ==environment.getWorldIndex();

        // simulate open_node and rebuild decisions history
        dec = decision_path.pollFirst();
        dec.setPrevious(previous);
        dec.setWorldIndex(swi++);
        mSearchLoop.decision = dec;
        dec.buildNext();

        // then simulate down_branch
        dec.apply();
        mSearchLoop.smList.afterDownLeftBranch();
        previous = dec;

        // iterate over decisions
        while (!decision_path.isEmpty()) {

            // simulate open_node and rebuild decisions history
            dec = decision_path.pollFirst();
            dec.setPrevious(previous);
            dec.setWorldIndex(swi++);
            mSearchLoop.decision = dec;
            dec.buildNext();

            // then simulate down_branch
            mSearchLoop.smList.beforeDownLeftBranch();
            environment.worldPush();
            dec.apply();
            mSearchLoop.smList.afterDownLeftBranch();

            previous = dec;
        }
    }

    @Override
    public void setPrevious(Decision decision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Decision getPrevious() {
        return this;
    }

    @Override
    public void free() {
//            throw new UnsupportedOperationException();
    }

    @Override
    public void reverse() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Deduction getNegativeDeduction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Deduction getPositiveDeduction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return decision_path.getFirst().toString();
    }
}
