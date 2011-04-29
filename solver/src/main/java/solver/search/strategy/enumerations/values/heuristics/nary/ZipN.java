package solver.search.strategy.enumerations.values.heuristics.nary;

import gnu.trove.THashMap;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;

public class ZipN extends NaryHeuristicVal<HeuristicVal> {

    int current = 0;

    private ZipN(Action action) {
        super(action);
    }

    public ZipN(HeuristicVal[] subs) {
        super(subs);
    }

    public ZipN(HeuristicVal[] subs, Action action) {
        super(subs, action);
    }

    public ZipN(HeuristicVal left, HeuristicVal right) {
        this(new HeuristicVal[]{left, right});
    }

    public ZipN(HeuristicVal left, HeuristicVal right, Action action) {
        this(new HeuristicVal[]{left, right}, action);
    }


    public boolean hasNext() {
        for (HeuristicVal h : subs) {
            if (h.hasNext()) {
                return true;
            }
        }
        return false;
    }

    public int next() {
        while (!subs[current].hasNext()) {
            current = (current + 1) % subs.length;
        }
        int result = subs[current].next();
        current = (current + 1) % subs.length;
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException("ZipN.remove not implemented");
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
            ZipN duplicata = new ZipN(this.action);
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

