/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;

import java.lang.ref.SoftReference;
import java.util.Arrays;

/**
 * A class to explain a modification.
 * A reason is always associated with one or more literals.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/09/2023
 */
public abstract class Reason {

    // 0 : no manager, high GC, low RAM
    // 1 : reason manager, low GC, high RAM
    // 2 : reason manager with chunks, (should be) low GC, low RAM
    private final static int VERSION = Integer.getInteger("reaMan", 3);
    private static final int CHUNK_SHIFT = 16;
    public static IManager manager = null;
    /**
     * An undefined reason.
     * This reason is static and should not be modified.
     */
    static final Clause UNDEF = new ArrayClause(new int[]{0});
    /**
     * A thread-local clause to explain a modification with one literal.
     * This clause is static and can be reused in the same thread.
     */
    private final static ThreadLocal<Clause> short_expl_2 = ThreadLocal.withInitial(() -> new ArrayClause(new int[]{0, 0}));
    /**
     * A thread-local clause to explain a propagation with two literals.
     * This clause is static and can be reused in the same thread.
     */
    private final static ThreadLocal<Clause> short_expl_3 = ThreadLocal.withInitial(() -> new ArrayClause(new int[]{0, 0, 0}));

    /**
     * TODO: not really a good idea to build this in that way...
     *
     * @param environment
     */
    public static void makeManager(IEnvironment environment) {
        System.out.println("%% VERSION="+ VERSION);
        switch (VERSION) {
            case 1:
                Reason.manager = new ReasonManager(environment);
                break;
            case 2:
            case 3:
                Reason.manager = new ReasonManagerChunk(environment, VERSION == 2);
                break;
            default:
                Reason.manager = new NoManager();
                break;
        }
    }

    /**
     * Create an undefined reason.
     *
     * @return an undefined static reason
     * @implSpec In practice, this reason is static and thus should not be modified.
     */
    public static Clause undef() {
        return UNDEF;
    }

    /**
     * Create a reason from a single literal
     *
     * @param p a literal
     * @return a reason
     */
    public static Reason r(int p) {
        return manager.r(p);
    }

    /**
     * Create a reason from two literals
     *
     * @param p a literal
     * @param q a literal
     * @return a reason
     */
    public static Reason r(int p, int q) {
        return manager.r(p, q);
    }

    /**
     * Create a reason from one or more literals.
     * <p>If more than 2 literals are given, the literal at index 0 should be left empty for the asserting literal.
     * </p>
     *
     * @param ps other literals
     * @return a reason
     * @implSpec if length of ps is strictly greater than 2,
     * then the literal at index 0 should be left empty for the asserting literal
     */
    public static Reason r(int... ps) {
        if (ps.length == 1) {
            return r(ps[0]);
        } else if (ps.length == 2) {
            return r(ps[0], ps[1]);
        } else if (ps.length > 2) {
            assert ps[0] == 0 : "The first literal should be left empty for the asserting literal";
            return manager.r(ps);
        } else {
            return Reason.UNDEF;
        }
    }

    /**
     * Create a reason from one or more literals.
     * <p>If more than 2 literals are given, the literal at index 0 should be left empty for the asserting literal.
     * </p>
     *
     * @param ps other literals
     * @return a reason
     * @implSpec if length of ps is strictly greater than 2,
     * then the literal at index 0 should be left empty for the asserting literal
     */
    public static Reason r(TIntArrayList ps) {
        if (ps.size() == 1) {
            return r(ps.getQuick(0));
        } else if (ps.size() == 2) {
            return r(ps.getQuick(0), ps.getQuick(1));
        } else if (ps.size() > 2) {
            assert ps.getQuick(0) == 0 : "The first literal should be left empty for the asserting literal";
            return manager.r(ps);
        } else {
            return Reason.UNDEF;
        }
    }

    /**
     * Gather a reason with a new literal.
     *
     * @param r a reason
     * @param p a literal
     * @return a new reason with p added to the literals of r
     */
    public static Reason gather(Reason r, int p) {
        if (r instanceof Clause) {
            Clause cl = (Clause) r;
            int[] ps = new int[cl.size() + 1];
            for (int i = 0; i < cl.size(); i++) {
                ps[i] = cl._g(i);
            }
            ps[cl.size()] = p;
            return Reason.r(ps);
        } else if (r instanceof Reason1) {
            return Reason.r(((Reason1) r).d1, p);
        } else if (r instanceof Reason2) {
            int[] ps = new int[4];
            ps[0] = 0;// leave space for the asserting literal
            ps[1] = ((Reason2) r).d1;
            ps[2] = ((Reason2) r).d2;
            ps[3] = p;
            return Reason.r(ps);
        } else if (r instanceof IndexBasedReason) {
            return manager.r((IndexBasedReason) r, p);
        } else {
            return Reason.r(p);
        }
    }

    /**
     * Extract the conflict clause from the reason.
     *
     * @return a clause
     */
    abstract Clause getConflict();

    /**
     * A reason with a single literal
     */
    final static class Reason1 extends Reason {
        int d1;

        private Reason1 set(int d1) {
            this.d1 = d1;
            return this;
        }

        @Override
        public Clause getConflict() {
            Clause c = short_expl_2.get();
            c._s(1, d1);
            return c;
        }

        @Override
        public String toString() {
            return "lits: 0 ∨ " + d1;
        }
    }

    /**
     * A reason with two literals
     */
    final static class Reason2 extends Reason {
        int d1;
        int d2;

        private Reason2 set(int d1, int d2) {
            this.d1 = d1;
            this.d2 = d2;
            return this;
        }

        @Override
        public Clause getConflict() {
            Clause c = short_expl_3.get();
            c._s(1, d1);
            c._s(2, d2);
            return c;
        }

        @Override
        public String toString() {
            return "lits: 0 ∨ " + d1 + " ∨ " + d2;
        }
    }

    final static class IndexBasedReason extends Reason {
        int index;

        public void setIndex(int idx) {
            this.index = idx;
        }

        @Override
        Clause getConflict() {
            IManager.short_expl.index = index;
            return IManager.short_expl;
        }

        @Override
        public String toString() {
            StringBuilder st = new StringBuilder();
            st.append("lits: ");
            st.append(manager.getVal(index + 1));
            for (int i = 2; i <= manager.getVal(index); i++) {
                st.append(" ∨ ").append(manager.getVal(index + i));
            }
            return st.toString();
        }
    }

    public interface IManager {

        ReasonClause short_expl = new ReasonClause();

        int getVal(int at);

        void setVal(int at, int val);

        Reason r(int d1);

        Reason r(int d1, int d2);

        Reason r(int[] ds);

        Reason r(TIntArrayList ds);

        Reason r(IndexBasedReason reason, int p);
    }

    public final static class NoManager implements IManager {

        @Override
        public int getVal(int at) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setVal(int at, int val) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Reason r(int d1) {
            return new Reason1().set(d1);
        }

        @Override
        public Reason r(int d1, int d2) {
            return new Reason2().set(d1, d2);
        }

        @Override
        public Reason r(int[] ds) {
            return new ArrayClause(ds);
        }

        @Override
        public Reason r(TIntArrayList ds) {
            return new ArrayClause(ds);
        }

        @Override
        public Reason r(IndexBasedReason reason, int p) {
            throw new UnsupportedOperationException();
        }
    }


    public final static class ReasonManager implements IManager {
        private int start;
        private int current;
        private final IStateInt currentIndex;
        private final IStateInt currentReason;
        private int[] buffer = new int[1024];
        private IndexBasedReason[] reasons = new IndexBasedReason[256];

        public ReasonManager(IEnvironment environment) {
            this.currentIndex = environment.makeInt(0);
            this.currentReason = environment.makeInt(0);
        }

        @Override
        public int getVal(int at) {
            return buffer[at];
        }

        @Override
        public void setVal(int at, int val) {
            buffer[at] = val;
        }

        private Reason getOrMakeReason(int ci) {
            int cr = currentReason.get();
            if (cr >= reasons.length) {
                reasons = Arrays.copyOf(reasons, (int) (reasons.length * 1.5));
            }
            IndexBasedReason reason = reasons[cr];
            if (reason == null) {
                reason = new IndexBasedReason();
                reasons[cr] = reason;
            }
            reason.setIndex(ci);
            currentReason.set(cr + 1);
            //System.out.printf("%s%n", reason);
            return reason;
        }

        private void ensureCapacity(int startIndex, int size) {
            while (startIndex + size > buffer.length) {
                buffer = Arrays.copyOf(buffer, (int) (buffer.length * 1.5));
            }
        }

        public Reason r(int d1) {
            int ci = currentIndex.get();
            ensureCapacity(ci, 3);
            buffer[ci] = 2;
            buffer[ci + 1] = 0;
            buffer[ci + 2] = d1;
            currentIndex.add(3);

            return getOrMakeReason(ci);
        }

        public Reason r(int d1, int d2) {
            int ci = currentIndex.get();
            ensureCapacity(ci, 4);
            buffer[ci] = 3;
            buffer[ci + 1] = 0;
            buffer[ci + 2] = d1;
            buffer[ci + 3] = d2;
            currentIndex.add(4);

            return getOrMakeReason(ci);
        }

        public Reason r(int[] ds) {
            assert ds[0] == 0;
            int ci = currentIndex.get();
            ensureCapacity(ci, ds.length + 1);
            buffer[ci] = ds.length;
            //buffer[ci + 1] = 0; useless since ds[0] == 0
            System.arraycopy(ds, 0, buffer, ci + 1, ds.length);
            currentIndex.add(ds.length + 1);

            return getOrMakeReason(ci);
        }

        public Reason r(TIntArrayList ds) {
            int ci = currentIndex.get();
            ensureCapacity(ci, ds.size() + 1);
            buffer[ci] = ds.size();
            //buffer[ci + 1] = 0; useless since ds[0] == 0
            for (int i = 0; i < ds.size(); i++) {
                buffer[ci + 1 + i] = ds.getQuick(i);
            }
            currentIndex.add(ds.size() + 1);

            return getOrMakeReason(ci);
        }

        public Reason r(IndexBasedReason reason, int p) {
            int idx = reason.index;
            int size = buffer[idx];
            int ci = currentIndex.get();
            ensureCapacity(ci, size + 1);

            buffer[ci] = size + 1;
            System.arraycopy(buffer, idx + 1, buffer, ci + 1, size);
            buffer[ci + 1 + size] = p;
            currentIndex.add(size + 1);
            return getOrMakeReason(ci);
        }

        public void open() {
            this.start = currentIndex.get();
            this.current = start + 1;
        }

        public void write(int d) {
            if (d > 0) {
                ensureCapacity(current, 1);
                buffer[++current] = d;
            }
        }

        public Reason close() {
            buffer[start] = current - start;
            return getOrMakeReason(start);
        }
    }

    public final static class ReasonManagerChunk implements IManager {
        // 65536 entiers par bloc (soit 256 Ko par bloc)
        private static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
        private static final int CHUNK_MASK = CHUNK_SIZE - 1;
        private final IStateInt currentIndex;
        private int[][] buffer = new int[16][]; // Tableau de blocs
        private int chunksCount = 0;
        private SoftReference<Object> canary = new SoftReference<>(new Object());

        private final IStateInt currentReason;
        private IndexBasedReason[] reasons = new IndexBasedReason[256];
        private final boolean fullIndexed;

        public ReasonManagerChunk(IEnvironment environment, boolean fullIndexed) {
            this.currentIndex = environment.makeInt(0);
            addChunk();
            this.currentReason = environment.makeInt(0);
            this.fullIndexed = fullIndexed;
        }

        private void addChunk() {
            if (chunksCount == buffer.length) {
                buffer = Arrays.copyOf(buffer, (int) (buffer.length * 1.5));
            }
            buffer[chunksCount++] = new int[CHUNK_SIZE];
        }

        private int ensureCapacity(int sizeNeeded) {
            //assert sizeNeeded < CHUNK_SIZE : "cannot allocate more than " + CHUNK_SIZE + ", but " + sizeNeeded + " required";
            int ci = currentIndex.get();
            // 1. ALIGNEMENT OPTIONNEL
            // On ne tente d'aligner que si la raison peut tenir dans UN SEUL chunk.
            // Si elle est plus grande, l'alignement est inutile (elle sera fragmentée de toute façon).
            if (sizeNeeded <= CHUNK_SIZE) {
                if ((ci >> CHUNK_SHIFT) != (ci + sizeNeeded - 1) >> CHUNK_SHIFT) {
                    // Erreur de chevauchement : On passe au début du bloc suivant
                    ci = (ci + CHUNK_SIZE) & ~CHUNK_MASK;
                    // On met à jour le solver pour ne pas perdre cet espace
                    currentIndex.set(ci);
                } else if (canary.get() == null) {
                    // current buffer used:
                    int bi = (ci >> CHUNK_SHIFT);
                    Arrays.fill(buffer, bi + 1, chunksCount, null);
                    chunksCount = bi + 1;
                    canary = new SoftReference<>(new Object());
                }
            }

            // 3. On vérifie si on a assez de blocs physiques alloués
            // lastIndex est la position du dernier entier qu'on va écrire
            int lastIndex = ci + sizeNeeded - 1;

            // Tant que l'index requis dépasse la capacité totale (nombre de blocs * taille)
            while ((lastIndex >> CHUNK_SHIFT) >= chunksCount) {
                addChunk();
            }
            return ci;
        }

        // Méthode d'écriture sécurisée
        public void setVal(int currentIndex, int value) {
            buffer[currentIndex >> CHUNK_SHIFT][currentIndex & CHUNK_MASK] = value;
        }

        // Méthode de lecture pour IndexBasedReason
        public int getVal(int currentIndex) {
            return buffer[currentIndex >> CHUNK_SHIFT][currentIndex & CHUNK_MASK];
        }

        private Reason getOrMakeReason(int ci) {
            int cr = currentReason.get();
            if (cr >= reasons.length) {
                reasons = Arrays.copyOf(reasons, (int) (reasons.length * 1.5));
            }
            IndexBasedReason reason = reasons[cr];
            if (reason == null) {
                reason = new IndexBasedReason();
                reasons[cr] = reason;
            }
            reason.setIndex(ci);
            currentReason.set(cr + 1);
            //System.out.printf("%s%n", reason);
            return reason;
        }

        public Reason r(int d1) {
            if (fullIndexed) {
                int ci = ensureCapacity(3);
                setVal(ci, 2);                   // size
                setVal(ci + 1, 0);    // reserved
                setVal(ci + 2, d1);         // d1
                currentIndex.add(3);
                return getOrMakeReason(ci);
            } else {
                return new Reason1().set(d1);
            }
        }

        public Reason r(int d1, int d2) {
            if (fullIndexed) {
                int ci = ensureCapacity(4);
                setVal(ci, 3);                  // size
                setVal(ci + 1, 0);  // reserved
                setVal(ci + 2, d1);       // d1
                setVal(ci + 3, d2);       // d2
                currentIndex.add(4);
                return getOrMakeReason(ci);
            } else {
                return new Reason2().set(d1, d2);
            }
        }

        private void writeArray(int targetIndex, int[] ds) {
            int remaining = ds.length;
            int srcPos = 0;
            int currentTarget = targetIndex;

            while (remaining > 0) {
                int blockIdx = currentTarget >> CHUNK_SHIFT;
                int offset = currentTarget & CHUNK_MASK;

                // Combien d'espace reste-t-il dans le chunk actuel ?
                int spaceInCurrentChunk = CHUNK_SIZE - offset;

                // On copie soit tout ce qui reste, soit ce qui tient dans le chunk
                int amountToCopy = Math.min(remaining, spaceInCurrentChunk);

                System.arraycopy(ds, srcPos, buffer[blockIdx], offset, amountToCopy);

                remaining -= amountToCopy;
                srcPos += amountToCopy;
                currentTarget += amountToCopy;
            }
        }

        public Reason r(int[] ds) {
            assert ds[0] == 0;
            int size = ds.length;
            int ci = ensureCapacity(size + 1);
            setVal(ci, size);                     // size
            // setVal(ci + 1, 0);  useless, see assert
            //System.arraycopy(ds, 0, buffer[(ci + 1) >> CHUNK_SHIFT], (ci & CHUNK_MASK) + 1, size);
            writeArray(ci + 1, ds);
            currentIndex.add(size + 1);
            return getOrMakeReason(ci);
        }

        private void writeArray(int targetIndex, TIntArrayList ds) {
            int remaining = ds.size();
            int srcPos = 0;
            int currentTarget = targetIndex;

            while (remaining > 0) {
                int blockIdx = currentTarget >> CHUNK_SHIFT;
                int offset = currentTarget & CHUNK_MASK;

                // Combien d'espace reste-t-il dans le chunk actuel ?
                int spaceInCurrentChunk = CHUNK_SIZE - offset;

                // On copie soit tout ce qui reste, soit ce qui tient dans le chunk
                int amountToCopy = Math.min(remaining, spaceInCurrentChunk);
                int[] _buffer = buffer[blockIdx];
                for (int i = 0; i < amountToCopy; i++) {
                    _buffer[offset + i] = ds.getQuick(srcPos + i);
                }
                remaining -= amountToCopy;
                srcPos += amountToCopy;
                currentTarget += amountToCopy;
            }
        }

        public Reason r(TIntArrayList ds) {
            assert ds.getQuick(0) == 0;
            int size = ds.size();
            int ci = ensureCapacity(ds.size() + 1);
            setVal(ci, size);                     // size
            // setVal(ci + 1, 0);  useless, see assert
            writeArray(ci + 1, ds);
            currentIndex.add(ds.size() + 1);
            return getOrMakeReason(ci);
        }

        public Reason r(IndexBasedReason reason, int p) {
            int idx = reason.index;
            int size = getVal(idx);
            int ci = ensureCapacity(size + 2);
//            setVal(ci, size + 1);
//            for (int i = 0; i < size; i++) {
//                setVal(ci + 1 + i, getVal(idx + 1 + i));
//            }
//            setVal(ci + size + 1, p);
            int[] _buffer = buffer[(ci + 1) >> CHUNK_SHIFT];
            int start = (ci & CHUNK_MASK) + 1;
            for (int i = 0; i < size; i++) {
                _buffer[start + i] = getVal(idx + 1 + i);
            }
            _buffer[start + size] = p;
            currentIndex.add(size + 2);
            return getOrMakeReason(ci);
        }

    }

    static final class ReasonClause extends Clause {
        private int index;

        private void setIndex(int idx) {
            this.index = idx;
        }

        @Override
        public int size() {
            return manager.getVal(index);
        }

        @Override
        public double getActivity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setActivity(double activity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLBD() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLBD(int lbd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int _g(int i) {
            return manager.getVal(index + 1 + i);
        }

        @Override
        public void _s(int pos, int l) {
            manager.setVal(index + 1 + pos, l);
        }

        @Override
        public String toString(MiniSat sat) {
            return "";
        }
    }

}
