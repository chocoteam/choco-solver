/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.automata.FA;

import dk.brics.automaton.*;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.util.tools.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Mar 15, 2010
 * Time: 12:53:23 PM
 */
public class FiniteAutomaton implements IAutomaton {


    protected final static TIntIntHashMap charFromIntMap = new TIntIntHashMap(16, .5f, -1, -1);
    protected final static TIntIntHashMap intFromCharMap = new TIntIntHashMap(16, .5f, -1, -1);

    static {
        int delta = 0;
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            while ((char) (i + delta) == '"' || (char) (i + delta) == '{' || (char) (i + delta) == '}' || (char) (i + delta) == '<' ||
                    (char) (i + delta) == '>' || (char) (i + delta) == '[' || (char) (i + delta) == ']' ||
                    (char) (i + delta) == '(' || (char) (i + delta) == ')') delta++;
            charFromIntMap.put(i, i + delta);
            intFromCharMap.put(i + delta, i);
        }

    }

    public static int getIntFromChar(char c) {
        return intFromCharMap.get(c);
    }

    public static char getCharFromInt(int i) {
        int c = charFromIntMap.get(i);
        if (c > -1) {
            return (char) charFromIntMap.get(i);
        } else {
            throw new SolverException("Unknwon value \"" + i + "\". Note that only integers in [" +
                    (int)Character.MIN_VALUE + "," +(int)(Character.MAX_VALUE) + "] are allowed by FiniteAutomaton.");
        }
    }


    protected Automaton representedBy;

    protected TObjectIntHashMap<State> stateToIndex;
    protected ArrayList<State> states;
    protected TIntHashSet alphabet;
    protected int nbStates;


    public FiniteAutomaton() {
        this.representedBy = new Automaton();
        this.stateToIndex = new TObjectIntHashMap<>();
        this.states = new ArrayList<>();

        this.alphabet = new TIntHashSet();

    }

    /**
     * Create a finite automaton based on a regular expression.
     * The regexp accepts digits  and numbers, in [0,65535].
     * However, to distinguish a number from a suite of digits, the former must be surrounded by '<' and '>'.
     * For instance, "12<34>" stands for a '1' (digit), followed by a '2' (digit) followed by a '34' (number).
     *
     * @param regexp
     */
    public FiniteAutomaton(String regexp) {
        this();
        String correct = StringUtils.toCharExp(regexp);
        this.representedBy = new RegExp(correct).toAutomaton();
        for (State s : representedBy.getStates()) {
            for (Transition t : s.getTransitions()) {
                for (char c = t.getMin(); c <= t.getMax(); c++) {
                    alphabet.add(getIntFromChar(c));
                }
            }
        }
        syncStates();
    }

    public FiniteAutomaton(FiniteAutomaton other) {
        perfectCopy(other);
    }

    private void perfectCopy(FiniteAutomaton other) {
        this.representedBy = new Automaton();
        this.states = new ArrayList<>();
        this.stateToIndex = new TObjectIntHashMap<>();
        this.alphabet = new TIntHashSet();
        this.nbStates = other.nbStates;
        for (int i = 0; i < other.nbStates; i++) {
            State s = new State();
            this.states.add(s);
            this.stateToIndex.put(s, i);
            if (other.isFinal(i))
                s.setAccept(true);
            if (other.getInitialState() == i)
                this.representedBy.setInitialState(s);
        }
        List<int[]> transitions = other.getTransitions();
        for (int[] t : transitions) {
            this.addTransition(t[0], t[1], t[2]);
        }

    }

    private FiniteAutomaton(Automaton a, TIntHashSet alphabet) {
        this();
        fill(a, alphabet);

    }


    public static int max(TIntHashSet hs) {
        int max = Integer.MIN_VALUE;
        for (TIntIterator it = hs.iterator(); it.hasNext(); ) {
            int n = it.next();
            if (n > max)
                max = n;
        }
        return max;
    }

    private static int min(TIntHashSet hs) {
        int min = Integer.MAX_VALUE;
        for (TIntIterator it = hs.iterator(); it.hasNext(); ) {
            int n = it.next();
            if (n < min)
                min = n;
        }
        return min;
    }

    public void fill(Automaton a, TIntHashSet alphabet) {

        int max = max(alphabet);
        int min = min(alphabet);


        this.setDeterministic(a.isDeterministic());

        HashMap<State, State> m = new HashMap<>();
        Set<State> states = a.getStates();
        for (State s : states) {
            this.addState();
            State ns = this.states.get(this.states.size() - 1);
            m.put(s, ns);

        }
        for (State s : states) {
            State p = m.get(s);
            int source = stateToIndex.get(p);
            p.setAccept(s.isAccept());
            if (a.getInitialState().equals(s))
                representedBy.setInitialState(p);
            for (Transition t : s.getTransitions()) {
                int tmin = getIntFromChar(t.getMin());
                int tmax = getIntFromChar(t.getMax());
                State dest = m.get(t.getDest());
                int desti = stateToIndex.get(dest);
                int minmax = Math.min(max, tmax);
                for (int i = Math.max(min, tmin); i <= minmax; i++) {
                    if (alphabet.contains(i))
                        this.addTransition(source, desti, i);
                }
            }

        }
    }

    public int getNbStates() {
        return nbStates;
    }

    public int getNbSymbols() {
        return alphabet.size();
    }

    public int addState() {
        int idx = states.size();
        State s = new State();
        states.add(s);
        stateToIndex.put(s, idx);
        nbStates++;
        return idx;
    }


    public void removeSymbolFromAutomaton(int symbol) {
        char c = getCharFromInt(symbol);
        ArrayList<Triple> triples = new ArrayList<>();
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            for (Transition t : s.getTransitions()) {

                if (t.getMin() <= c && t.getMax() >= c) {
                    triples.add(new Triple(i, stateToIndex.get(t.getDest()), symbol));
                }
            }
            for (Triple t : triples) {
                this.deleteTransition(t.a, t.b, t.c);
            }
            triples.clear();
        }
        alphabet.remove(symbol);
        //this.representedBy.reduce();
        // this.syncStates();
    }

    public void addTransition(int source, int destination, int... symbols) {
        for (int symbol : symbols) {
            try {
                checkState(source, destination);
            } catch (StateNotInAutomatonException e) {
//                LOGGER.warn("Unable to addTransition : " + e);
            }
            alphabet.add(symbol);
            State s = states.get(source);
            State d = states.get(destination);
            s.addTransition(new Transition(getCharFromInt(symbol), d));
        }
    }

    public void deleteTransition(int source, int destination, int symbol) {
        try {
            checkState(source, destination);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to delete transition : " + e);
        }
        State s = states.get(source);
        State d = states.get(destination);
        Set<Transition> transitions = s.getTransitions();
        Set<Transition> nTrans = new HashSet<>();
        char c = getCharFromInt(symbol);
        Iterator<Transition> it = transitions.iterator();
        for (; it.hasNext(); ) {
            Transition t = it.next();
            if (t.getDest().equals(d) && t.getMin() <= c && t.getMax() >= c) {
                it.remove();

                if (t.getMin() == c && c < t.getMax()) {
                    nTrans.add(new Transition((char) (c + 1), t.getMax(), d));
                } else if (t.getMin() > c && c == t.getMax()) {
                    nTrans.add(new Transition(t.getMin(), (char) (c - 1), d));
                } else if (t.getMin() < c && c < t.getMax()) {
                    nTrans.add(new Transition(t.getMin(), (char) (c - 1), d));
                    nTrans.add(new Transition((char) (c + 1), t.getMax(), d));
                }
            }
        }
        transitions.addAll(nTrans);
    }

    private void checkState(int... state) throws StateNotInAutomatonException {
        int sz = states.size();
        for (int s : state)
            if (s >= sz) {
                throw new StateNotInAutomatonException(s);
            }
    }

    public int delta(int source, int symbol) throws NonDeterministicOperationException {
        if (!representedBy.isDeterministic()) {
            throw new NonDeterministicOperationException();
        }
        try {
            checkState(source);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to perform delta lookup, state not in automaton : " + e);
        }
        State s = states.get(source);
        State d = s.step(getCharFromInt(symbol));
        if (d != null) {
            return stateToIndex.get(d);
        } else
            return -1;

    }

    private HashSet<State> nexts = new HashSet<>();

    public void delta(int source, int symbol, TIntHashSet states) {
        try {
            checkState(source);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable perform delta lookup, state not in automaton : " + e);
        }
        State s = this.states.get(source);
        nexts.clear();
        s.step(getCharFromInt(symbol), nexts);
        for (State to : nexts) {
            states.add(stateToIndex.get(to));
        }
    }


    public TIntArrayList getOutSymbols(int source) {
        try {
            checkState(source);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to get outgoing transition, state not in automaton : " + e);
        }
        TIntHashSet set = new TIntHashSet();
        State s = states.get(source);
        for (Transition t : s.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                set.add(getIntFromChar(c));
            }
        }
        return new TIntArrayList(set.toArray());

    }

    private TIntHashSet tmpSet = new TIntHashSet();

    public int[] getOutSymbolsArray(int source) {
        try {
            checkState(source);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to get outgoing transition, state not in automaton : " + e);
        }
        tmpSet.clear();
        State s = states.get(source);
        for (Transition t : s.getTransitions()) {
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                tmpSet.add(getIntFromChar(c));
            }
        }
        return tmpSet.toArray();

    }

    public void addToAlphabet(int a) {
        alphabet.add(a);
    }

    public void removeFromAlphabet(int a) {
        alphabet.remove(a);
    }

    public int getInitialState() {
        State s = representedBy.getInitialState();
        if (s == null)
            return -1;
        else
            return stateToIndex.get(s);
    }

    public boolean isFinal(int state) {
        try {
            checkState(state);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to check if this state is final : " + e);
        }
        return states.get(state).isAccept();
    }

    public void setInitialState(int state) {
        try {
            checkState(state);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to set initial state, state is not in automaton : " + e);
        }
        representedBy.setInitialState(states.get(state));
    }

    public void setFinal(int state) {
        try {
            checkState(state);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to set final state, state is not in automaton : " + e);
        }
        states.get(state).setAccept(true);
    }

    public void setFinal(int... states) {
        for (int s : states) setFinal(s);
    }

    public void setNonFinal(int state) {
        try {
            checkState(state);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to set non final state, state is not in automaton : " + e);
        }
        states.get(state).setAccept(false);
    }

    public void setNonFInal(int... states) {
        for (int s : states) setNonFinal(s);
    }

    public boolean run(int[] word) {
        StringBuilder b = new StringBuilder();
        for (int i : word) {
            char c = getCharFromInt(i);
            b.append(c);
        }
        return representedBy.run(b.toString());
    }

    public Automaton makeBricsAutomaton() {
        return representedBy.clone();
    }

    public IAutomaton repeat() {
        return new FiniteAutomaton(this.representedBy.repeat(), alphabet);
    }

    public IAutomaton repeat(int min) {
        return new FiniteAutomaton(this.representedBy.repeat(min), alphabet);
    }

    public IAutomaton repeat(int min, int max) {
        return new FiniteAutomaton(this.representedBy.repeat(min, max), alphabet);
    }

    public void minimize() {
        this.representedBy.minimize();
        syncStates();
    }

    private void syncStates() {
        this.alphabet.clear();
        this.states.clear();
        this.stateToIndex.clear();
        int idx = 0;
        for (State s : representedBy.getStates()) {
            states.add(s);
            stateToIndex.put(s, idx++);
            for (Transition t : s.getTransitions()) {
                for (char c = t.getMin(); c <= t.getMax(); c++) {
                    alphabet.add(getIntFromChar(c));
                }
            }
        }
        nbStates = states.size();
    }

    public void reduce() {
        this.representedBy.reduce();
        syncStates();
    }

    public void removeDeadTransitions() {
        this.representedBy.removeDeadTransitions();
        syncStates();
    }

    public FiniteAutomaton union(FiniteAutomaton otherI) {
        Automaton union = this.representedBy.union(otherI.representedBy);
        TIntHashSet alphabet = new TIntHashSet(this.alphabet.toArray());
        alphabet.addAll(otherI.alphabet.toArray());
        return new FiniteAutomaton(union, alphabet);

    }

    public FiniteAutomaton intersection(IAutomaton otherI) {
        FiniteAutomaton other = (FiniteAutomaton) otherI;
        Automaton inter = this.representedBy.intersection(other.representedBy);
        TIntHashSet alphabet = new TIntHashSet();
        for (int a : this.alphabet.toArray()) {
            if (other.alphabet.contains(a))
                alphabet.add(a);
        }
        return new FiniteAutomaton(inter, alphabet);
    }

    public FiniteAutomaton complement(TIntHashSet alphabet) {
        Automaton comp = this.representedBy.complement();
        return new FiniteAutomaton(comp, alphabet);
    }

    public FiniteAutomaton complement() {
        return complement(alphabet);
    }


    public FiniteAutomaton concatenate(FiniteAutomaton otherI) {
        Automaton conc = this.representedBy.concatenate(otherI.representedBy);
        TIntHashSet alphabet = new TIntHashSet(this.alphabet.toArray());
        alphabet.addAll(otherI.alphabet.toArray());
        return new FiniteAutomaton(conc, alphabet);
    }

    public void addEpsilon(int source, int destination) {
        try {
            checkState(source, destination);
        } catch (StateNotInAutomatonException e) {
//            LOGGER.warn("Unable to add epsilon transition, a state is not in the automaton : " + e);

        }
        State s = states.get(source);
        State d = states.get(destination);


        ArrayList<StatePair> pairs = new ArrayList<>();
        pairs.add(new StatePair(s, d));
        this.representedBy.addEpsilons(pairs);
    }

    public boolean isDeterministic() {
        return this.representedBy.isDeterministic();
    }

    public void setDeterministic(boolean deterministic) {
        this.representedBy.setDeterministic(deterministic);
    }

    public TIntHashSet getFinalStates() {
        TIntHashSet finals = new TIntHashSet();
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).isAccept())
                finals.add(i);
        }
        return finals;
    }


    public void toDotty(String f) {
        String s = this.toDot();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(f)));
            bw.write(s);
            bw.close();
        } catch (IOException e) {
//            System.err.println("Unable to write dotty file " + f);
        }
    }

    public String toDot() {
        StringBuilder b = new StringBuilder("digraph Automaton {\n");
        b.append("  rankdir = LR;\n");
        Set<State> states = this.representedBy.getStates();
        // setStateNumbers(states);
        for (State s : states) {
            int idx = stateToIndex.get(s);
            b.append("  ").append(idx);
            if (s.isAccept())
                b.append(" [shape=doublecircle];\n");
            else
                b.append(" [shape=circle];\n");
            if (s == this.representedBy.getInitialState()) {
                b.append("  initial [shape=plaintext,label=\"\"];\n");
                b.append("  initial -> ").append(idx).append("\n");
            }
            for (Transition t : s.getTransitions()) {
                b.append("  ").append(idx);
                appendDot(t, b);
            }
        }
        return b.append("}\n").toString();
    }

    private void appendDot(Transition t, StringBuilder b) {
        int destIdx = stateToIndex.get(t.getDest());

        b.append(" -> ").append(destIdx).append(" [label=\"");
        b.append("{");
        b.append(getIntFromChar(t.getMin()));
        if (t.getMin() != t.getMax()) {
            for (char c = (char) (t.getMin() + 1); c <= t.getMax(); c++) {
                b.append(",");
                b.append(getIntFromChar(c));
            }
        }
        b.append("}");
        b.append("\"]\n");
    }

    public TIntHashSet getAlphabet() {
        return alphabet;
    }

    public List<int[]> getTransitions() {
        List<int[]> transitions = new ArrayList<>();
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            for (Transition t : s.getTransitions()) {
                int dest = stateToIndex.get(t.getDest());
                for (char c = t.getMin(); c <= t.getMax(); c++) {
                    int symbol = getIntFromChar(c);
                    transitions.add(new int[]{i, dest, symbol});
                }
            }
        }
        return transitions;
    }


    public List<int[]> getTransitions(int state) {
        List<int[]> transitions = new ArrayList<>();
        for (Transition t : states.get(state).getTransitions()) {
            int dest = stateToIndex.get(t.getDest());
            for (char c = t.getMin(); c <= t.getMax(); c++) {
                int symbol = getIntFromChar(c);
                transitions.add(new int[]{state, dest, symbol});
            }
        }
        return transitions;
    }

    public ArrayList<int[]> _removeSymbolFromAutomaton(int alpha) {
        char c = getCharFromInt(alpha);
        TIntHashSet setOfRemoved = new TIntHashSet();
        ArrayList<Triple> triples = new ArrayList<>();
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            for (Transition t : s.getTransitions()) {

                if (t.getMin() <= c && t.getMax() >= c) {
                    triples.add(new Triple(i, stateToIndex.get(t.getDest()), alpha));
                    setOfRemoved.add(i);
                }
            }
            for (Triple t : triples) {
                this.deleteTransition(t.a, t.b, t.c);
            }
            triples.clear();

        }
        alphabet.remove(alpha);
        // this.removeDeadTransitions();
        ArrayList<int[]> couple = new ArrayList<>();
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            for (Transition t : s.getTransitions()) {
                int dest = stateToIndex.get(t.getDest());
                if (setOfRemoved.contains(dest)) {
                    for (char d = t.getMin(); d <= t.getMax(); d++)
                        couple.add(new int[]{i, getIntFromChar(d)});
                }

            }


        }
        return couple;


        //this.representedBy.reduce();
        // this.syncStates();


    }

    public FiniteAutomaton clone() throws CloneNotSupportedException {
        FiniteAutomaton auto = (FiniteAutomaton) super.clone();
        auto.representedBy = new Automaton();
        auto.states = new ArrayList<>();
        auto.stateToIndex = new TObjectIntHashMap<>();
        auto.alphabet = new TIntHashSet();
        auto.nbStates = this.nbStates;
        for (int i = 0; i < this.nbStates; i++) {
            State s = new State();
            auto.states.add(s);
            auto.stateToIndex.put(s, i);
            if (this.isFinal(i))
                s.setAccept(true);
            if (this.getInitialState() == i)
                auto.representedBy.setInitialState(s);
        }
        List<int[]> transitions = this.getTransitions();
        for (int[] t : transitions) {
            auto.addTransition(t[0], t[1], t[2]);
        }
        return auto;
    }


    @Override
    public String toString() {
        return representedBy.toString();
    }
}
