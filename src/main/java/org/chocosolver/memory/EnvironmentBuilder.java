/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.memory;

import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.memory.trailing.trail.*;
import org.chocosolver.memory.trailing.trail.chunck.*;
import org.chocosolver.memory.trailing.trail.flatten.*;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeBoolTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeDoubleTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeIntTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeLongTrail;

import static org.chocosolver.memory.trailing.EnvironmentTrailing.*;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 10/05/2016.
 */
public class EnvironmentBuilder {

    /**
     * Trail to consider to manage doubles
     */
    private IStoredDoubleTrail dt;
    /**
     * Trail to consider to manage booleans
     */
    private IStoredBoolTrail bt;
    /**
     * Trail to consider to manage integers
     */
    private IStoredIntTrail it;
    /**
     * Trail to consider to manage longs
     */
    private IStoredLongTrail lt;
    /**
     * Trail to consider to manage operations
     */
    private IOperationTrail ot;


    /**
     * Set the int trail.
     * @param t the trail to use
     * @return {@code this}
     */
    public EnvironmentBuilder setTrail(IStoredIntTrail t) {
        it = t;
        return this;
    }

    /**
     * Set the long trail.
     * @param t the trail to use
     * @return {@code this}
     */
    public EnvironmentBuilder setTrail(IStoredLongTrail t) {
        lt = t;
        return this;
    }

    /**
     * Set the double trail.
     * @param t the trail to use
     * @return {@code this}
     */
    public EnvironmentBuilder setTrail(IStoredDoubleTrail t) {
        dt = t;
        return this;
    }

    /**
     * Set the bool trail.
     * @param t the trail to use
     * @return {@code this}
     */
    public EnvironmentBuilder setTrail(IStoredBoolTrail t) {
        bt = t;
        return this;
    }

    /**
     * Set the operation trail.
     * @param t the trail to use
     * @return {@code this}
     */
    public EnvironmentBuilder setTrail(IOperationTrail t) {
        ot = t;
        return this;
    }

    /**
     * Build the environment
     * @return the resulting environment
     */
    public EnvironmentTrailing build(){
        EnvironmentTrailing env = new EnvironmentTrailing();
        if (bt == null) {
            bt = new StoredBoolTrail(NBUPATES, NBWORLDS, LOADFACTOR);
        }

        if (it == null) {
            it = new StoredIntTrail(NBUPATES, NBWORLDS, LOADFACTOR);
        }

        if (dt == null) {
            dt = new StoredDoubleTrail(NBUPATES, NBWORLDS, LOADFACTOR);
        }

        if (lt == null) {
            lt = new StoredLongTrail(NBUPATES, NBWORLDS, LOADFACTOR);
        }

        if (ot == null) {
            ot = new OperationTrail(NBUPATES, NBWORLDS, LOADFACTOR);
        }

        env.setBoolTrail(bt);
        env.setIntTrail(it);
        env.setDoubleTrail(dt);
        env.setOperationTrail(ot);
        env.setLongTrail(lt);
        return env;
    }

    /**
     * Build a chunk environment
     * @return {@code this}
     */
    public EnvironmentBuilder fromChunk(){
        setTrail(new ChunckedBoolTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        setTrail(new ChunckedIntTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        setTrail(new ChunckedDoubleTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        setTrail(new ChunckedLongTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        setTrail(new ChunckedOperationTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        return this;
    }

    /**
     * Build an unsafe environment (operations are flat)
     * @return {@code this}
     */
    public EnvironmentBuilder fromUnsafe(){
        setTrail(new UnsafeBoolTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        setTrail(new UnsafeIntTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        setTrail(new UnsafeDoubleTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        setTrail(new UnsafeLongTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        setTrail(new OperationTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        return this;
    }

    /**
     * Build a flat environment
     * @return the resulting environment
     */
    public EnvironmentBuilder fromFlat(){
        // default implementation is flat
        // Creates trails on the fly
        return this;
    }
}