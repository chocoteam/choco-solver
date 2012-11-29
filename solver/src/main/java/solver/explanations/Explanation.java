/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.explanations;

import gnu.trove.set.hash.TIntHashSet;
import solver.constraints.propagators.Propagator;
import solver.propagation.queues.CircularQueue;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * An explanation is the combination of two sets: a set of deduction and a set of propagators.
 * The deductions are stored in a list, and the uniqueness of elements is ensured during the add operation.
 * This allows fast iteration over elements.
 * The propagators are stored in the same way.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 12:54:50
 * An explanation
 */

public class Explanation extends Deduction {
    public static Explanation SYSTEM = new Explanation();

    List<Deduction> deductions;
    private TIntHashSet did;
    private List<Propagator> propagators;
    private TIntHashSet pid;

    private static CircularQueue<Explanation> stock = new CircularQueue<Explanation>(8);

    public static Explanation build() {
        if (stock.isEmpty()) {
            Explanation e = new Explanation();
            if (ExplanationEngine.LOGGER.isInfoEnabled()) {
                ExplanationEngine.LOGGER.info("::EXP*:: " + e.toString());
            }
            return e;
        } else {
            Explanation e = stock.pollFirst();
            if (ExplanationEngine.LOGGER.isInfoEnabled()) {
                ExplanationEngine.LOGGER.info("::EXP!:: " + e.toString());
            }
            return e;
        }
    }

    private static void free(Explanation explanation) {
        if (stock.size() < 16) {
            stock.addLast(explanation);
        }
        explanation.reset();
        if (ExplanationEngine.LOGGER.isInfoEnabled()) {
            ExplanationEngine.LOGGER.info("::EXP?:: " + explanation.toString());
        }
    }

    private Explanation() {
        super(Type.Exp);
    }

    private Explanation(Propagator p) {
        this();
        this.add(p);
    }


    /**
     * Add a new explanation to this.
     * Extract the deductions and the propagators from expl, and add them to this.
     *
     * @param expl explanation to add
     */
    public void add(Explanation expl) {
        int nbd = expl.nbDeductions();
        int nbp = expl.nbPropagators();
        if (nbd > 0 || nbp > 0) {
            // 1. add all deductions of expl
            for (int i = 0; i < nbd; i++) {
                add(expl.getDeduction(i));
            }

            // 2. add all propagators of expl
            for (int i = 0; i < nbp; i++) {
                add(expl.getPropagator(i));
            }
        } else {
            free(expl);
        }
    }

    /**
     * Add a propagator to the set of propagators of this
     *
     * @param p propagator to add
     */
    public void add(Propagator p) {
        if (this.propagators == null) {
            this.propagators = new ArrayList<Propagator>(4);
            this.pid = new TIntHashSet(4);
        }
        if (this.pid.add(p.getId())) {
            this.propagators.add(p);
            if (ExplanationEngine.LOGGER.isInfoEnabled()) {
                ExplanationEngine.LOGGER.info("::UPD:: " + this.toString());
            }
        }
    }

    /**
     * Add a new deduction to the set of deductions of this
     *
     * @param d deduction to add
     */
    public void add(Deduction d) {
        if (d.mType == Type.Exp) {
            add((Explanation) d);
            //throw new UnsupportedOperationException("ARG");
        } else {
            if (this.deductions == null) {
                this.deductions = new ArrayList<Deduction>(4);
                this.did = new TIntHashSet();
            }
            if (this.did.add(d.id)) {
                this.deductions.add(d);
                if (ExplanationEngine.LOGGER.isInfoEnabled()) {
                    ExplanationEngine.LOGGER.info("::UPD:: " + this.toString());
                }
            }
        }
    }

    /**
     * Remove a deduction from the set of deductions of this.
     *
     * @param d deduction to remove
     */
    public void remove(Deduction d) {
        this.deductions.remove(d);
        this.did.remove(d.id);
    }


    public boolean contain(Deduction d) {
        return deductions != null && did.contains(d.id);
    }

    /**
     * Reset internal strucutre, forget all deductions and propagators.
     */
    public void reset() {
        if (this.propagators != null) {
            this.propagators.clear();
            this.pid.clear();
        }
        if (this.deductions != null) {
            this.deductions.clear();
            this.did.clear();
        }
    }

    /**
     * Return the size of deduction set
     *
     * @return number of deductions
     */
    public int nbDeductions() {
        return deductions == null ? 0 : deductions.size();
    }

    /**
     * Return the i^th deduction contains in this.
     * Deductions are stored in a list, their uniqueness is ensured during the add operation.
     * This allows simple iteration over deductions of an explanation.
     *
     * @param i index of the deduction
     * @return the deduction at rank i
     */
    public Deduction getDeduction(int i) {
        return deductions.get(i);
    }

    /**
     * Return the size of propagator set
     *
     * @return number of propagators
     */
    public int nbPropagators() {
        return propagators == null ? 0 : propagators.size();
    }

    /**
     * Return the i^th propagators contains in this.
     * Propagators are stored in a list, their uniqueness is ensured during the add operation.
     * This allows simple iteration over propagators of an explanation.
     *
     * @param i index of the propagator
     * @return the propagator at rank i
     */
    public Propagator getPropagator(int i) {
        return propagators.get(i);
    }

    /**
     * Retrieve the most recent world to backtrack to, with respect to the variable assignment stored in this.
     * This is usefull for the <b>dynamic backtracking</b>.
     *
     * @param explainer the engine
     * @return the world index to backtrack to
     */
    public int getMostRecentWorldToBacktrack(ExplanationEngine explainer) {
        int topworld = 0;
        if (this.deductions != null) {
            for (int d = 0; d < deductions.size(); d++) {
                Deduction dec = deductions.get(d);
                if (dec.mType == Type.VarAss) {
                    Variable va = ((VariableAssignment) dec).var;
                    int val = ((VariableAssignment) dec).val;
                    int world = explainer.getWorldIndex(va, val);
                    if (world > topworld) {
                        topworld = world;
                    }
                }
            }
        }
        return 1 + (explainer.solver.getEnvironment().getWorldIndex() - topworld);
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder("E_" + id);


        bf.append(" D: ");
        if (this.deductions != null && !this.deductions.isEmpty()) {
            bf.append("(" + this.deductions.size() + ") ");
            for (Deduction d : this.deductions) {
                bf.append(d).append(", ");
            }

            bf.delete(bf.lastIndexOf(","), bf.length() - 1);
        }

        bf.append(" ; P:");
        if (this.propagators != null) {
            bf.append("(" + this.propagators.size() + ") ");
            for (Propagator p : this.propagators) {

                bf.append(p).append(", ");
            }
            bf.delete(bf.lastIndexOf(","), bf.length() - 1);
        }

        return bf.toString();
    }
}
