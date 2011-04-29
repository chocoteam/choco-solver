package solver.search.strategy.enumerations.values.heuristics.nary;

import gnu.trove.THashMap;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;

public class SeqN extends NaryHeuristicVal<HeuristicVal> {

    int current = 0;

    private SeqN(Action action) {
        super(action);
    }

    public SeqN(HeuristicVal[] subs) {
        super(subs);
    }

    public SeqN(HeuristicVal left, HeuristicVal right) {
        this(new HeuristicVal[]{left, right});
    }

    public SeqN(HeuristicVal[] subs, Action action) {
        super(subs, action);
    }

    public SeqN(HeuristicVal left, HeuristicVal right, Action action) {
        this(new HeuristicVal[]{left, right}, action);
    }

    public boolean hasNext() {
        for (int i = current; i < subs.length; i++) {
            current = i;
            if (subs[i].hasNext()) {
                return true;
            }
        }
        return false;
    }

    public int next() {
        return subs[current].next();
    }

    public void remove() {
        throw new UnsupportedOperationException("SeqN.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        current = 0;
    }

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            SeqN duplicata = new SeqN(this.action);
            duplicata.current = current;
            duplicata.subs = new HeuristicVal[subs.length];
            for (int i = 0; i < subs.length; i++) {
                duplicata.subs[i] = this.subs[i].duplicate(map);
            }
            map.put(this, duplicata);
            return duplicata;
        }
    }
}
