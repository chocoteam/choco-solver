package solver.search.strategy.enumerations.values.heuristics.zeroary;

import gnu.trove.THashMap;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;

public class UnsafeEnum extends HeuristicVal {
    int from;
    int delta;
    int to;
    int idx;

    private UnsafeEnum(Action action){
        super(action);
    }

    public UnsafeEnum(int from, int delta, int to) {
        super();
        this.from = from;
        this.idx = from;
        this.delta = delta;
        this.to = to;
    }

    public UnsafeEnum(int from, int delta, int to, Action action) {
        super(action);
        this.from = from;
        this.idx = from;
        this.delta = delta;
        this.to = to;
    }

    public boolean hasNext() {
        return delta > 0 ? idx <= to : idx >= to;
    }

    public int next() {
        int _from = idx;
        idx += delta;
        return _from;
    }

    public void remove() {
        throw new UnsupportedOperationException("UnsafeEnum.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        idx = from;
    }

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            UnsafeEnum duplicata = new UnsafeEnum(this.action);
            duplicata.from = this.from;
            duplicata.delta = this.delta;
            duplicata.to = this.to;
            duplicata.idx = this.idx;
            map.put(this, duplicata);
            return duplicata;
        }
    }
}
