package solver.search.strategy.enumerations.values.heuristics.zeroary;

import gnu.trove.THashMap;
import gnu.trove.TIntArrayList;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;

public class List extends HeuristicVal {

    TIntArrayList elts;
    int idx;

    private List(Action action) {
        super(action);
    }

    public List(int... i) {
        super();
        elts = new TIntArrayList(i);
    }

    public List(Action action, int... i) {
        super(action);
        elts = new TIntArrayList(i);
    }

    public boolean hasNext() {
        return idx != elts.size();
    }

    public int next() {
        return elts.get(idx++);
    }

    public void remove() {
        throw new UnsupportedOperationException("List.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        idx = 0;
    }

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            List duplicata = new List(action);
            duplicata.elts = this.elts;
            duplicata.idx = this.idx;
            map.put(this, duplicata);
            return duplicata;
        }
    }
}
