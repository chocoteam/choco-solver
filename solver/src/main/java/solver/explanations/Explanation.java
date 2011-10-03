/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.explanations;

import solver.constraints.propagators.Propagator;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 12:54:50
 * An explanation
 */
public class Explanation extends Deduction {
    public static final int DOM = 1;
    public static final int LB = 2;
    public static final int UB = 3;

    Set<Deduction> deductions;
    Set<Propagator> contraintes;

    public Explanation(Set<Deduction> p, Set<Propagator> e ) {
        this.deductions = p;
        this.contraintes = e;
    }

    public void add(Explanation expl) {
        if (this.deductions == null) this.deductions = expl.deductions;
        else if (expl.deductions != null) this.deductions.addAll(expl.deductions);
        if (this.contraintes == null) this.contraintes = expl.contraintes;
        else if (expl.contraintes != null) this.contraintes.addAll(expl.contraintes);
    }

    public void add(Propagator p) {
        if (this.contraintes == null) {
            this.contraintes = new HashSet<Propagator>();
        }
        this.contraintes.add(p);
    }

    public void add(Deduction d) {
        if (d instanceof Explanation) {
            add((Explanation) d);
        }
        else {
            if (this.deductions == null) {
                this.deductions = new HashSet<Deduction>();
            }
//            System.out.println("adding to expl d:" + d + " here? " + this.deductions.contains(d));
            this.deductions.add(d);
        }

    }

    public void reset() {
        this.contraintes = null;
        this.deductions = null;
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();


        bf.append("D: ");
        if (this.deductions != null)  {
            bf.append("(" + this.deductions.size() + ") ");
            for (Deduction d: this.deductions) {
                bf.append(d).append(", ");
            }

            bf.delete(bf.lastIndexOf(","), bf.length() - 1);
         }

        bf.append(" // P:");
        if (this.contraintes != null) {
            bf.append("(" + this.contraintes.size() + ") ");
            for (Propagator p: this.contraintes) {

                bf.append(p).append(", ");
            }
          bf.delete(bf.lastIndexOf(","), bf.length() - 1);
         }
        return bf.toString();
    }
}
