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
import org.chocosolver.memory.trailing.trail.chunck.*;
import org.chocosolver.memory.trailing.trail.flatten.*;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeBoolTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeDoubleTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeIntTrail;
import org.chocosolver.memory.trailing.trail.unsafe.UnsafeLongTrail;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 10/05/2016.
 */
public class EnvironmentBuilder {

    /**
     * Number of updates for int trail
     */
    private int nu_int = EnvironmentTrailing.NBUPATES;
    /**
     * Number of worlds for int trail
     */
    private int nw_int = EnvironmentTrailing.NBWORLDS;
    /**
     * Load factor for int trail
     */
    private double lf_int = 1.5;
    /**
     * Type for int trail
     */
    private EnvironmentTrailing.Type t_int = EnvironmentTrailing.Type.FLAT;

    /**
     * Number of updates for long trail
     */
    private int nu_long = EnvironmentTrailing.NBUPATES;
    /**
     * Number of worlds for long trail
     */
    private int nw_long = EnvironmentTrailing.NBWORLDS;
    /**
     * Load factor for long trail
     */
    private double lf_long = 1.5;
    /**
     * Type for long trail
     */
    private EnvironmentTrailing.Type t_long = EnvironmentTrailing.Type.FLAT;

    /**
     * Number of updates for bool trail
     */
    private int nu_bool = EnvironmentTrailing.NBUPATES;
    /**
     * Number of worlds for bool trail
     */
    private int nw_bool = EnvironmentTrailing.NBWORLDS;
    /**
     * Load factor for bool trail
     */
    private double lf_bool = 1.5;
    /**
     * Type for bool trail
     */
    private EnvironmentTrailing.Type t_bool = EnvironmentTrailing.Type.FLAT;

    /**
     * Number of updates for double trail
     */
    private int nu_double = EnvironmentTrailing.NBUPATES;
    /**
     * Number of worlds for double trail
     */
    private int nw_double = EnvironmentTrailing.NBWORLDS;
    /**
     * Load factor for double trail
     */
    private double lf_double = 1.5;
    /**
     * Type for double trail
     */
    private EnvironmentTrailing.Type t_double = EnvironmentTrailing.Type.FLAT;

    /**
     * Number of updates for operation trail
     */
    private int nu_ope = EnvironmentTrailing.NBUPATES;
    /**
     * Number of worlds for operation trail
     */
    private int nw_ope = EnvironmentTrailing.NBWORLDS;
    /**
     * Load factor for operation trail
     */
    private double lf_ope = 1.5;
    /**
     * Type for operation trail
     */
    private EnvironmentTrailing.Type t_ope = EnvironmentTrailing.Type.FLAT;


    /**
     * Number of default updates allowed for int trail
     * @param nu_int number of update
     * @return this builder
     */
    public EnvironmentBuilder setNbUpdatesForIntTrail(int nu_int) {
        this.nu_int = nu_int;
        return this;
    }

    /**
     * Number of default worlds allowed for int trail
     * @param nw_int number of worlds
     * @return this builder
     */
    public EnvironmentBuilder setNbWorldsForIntTrail(int nw_int) {
        this.nw_int = nw_int;
        return this;
    }

    /**
     * Set load factor for int trail resizing
     * @param lf_int load factor
     * @return this builder
     */
    public EnvironmentBuilder setLoadFactorForIntTrail(double lf_int) {
        this.lf_int = lf_int;
        return this;
    }

    /**
     * Set type for int trail
     * @param t_int type of trail
     * @return this builder
     */
    public EnvironmentBuilder setTypeForIntTrail(EnvironmentTrailing.Type t_int) {
        this.t_int = t_int;
        return this;
    }

    /**
     * Number of default updates allowed for long trail
     * @param nu_long number of update
     * @return this builder
     */
    public EnvironmentBuilder setNbUpdatesForLongTrail(int nu_long) {
        this.nu_long = nu_long;
        return this;
    }
    /**
     * Number of default worlds allowed for long trail
     * @param nw_long number of worlds
     * @return this builder
     */
    public EnvironmentBuilder setNbWorldsForLongTrail(int nw_long) {
        this.nw_long = nw_long;
        return this;
    }

    /**
     * Set load factor for long trail resizing
     * @param lf_long load factor
     * @return this builder
     */
    public EnvironmentBuilder setLoadFactorForLongTrail(double lf_long) {
        this.lf_long = lf_long;
        return this;
    }

    /**
     * Set type for long trail
     * @param t_long type of trail
     * @return this builder
     */
    public EnvironmentBuilder setTypeForLongTrail(EnvironmentTrailing.Type t_long) {
        this.t_long = t_long;
        return this;
    }

    /**
     * Number of default updates allowed for bool trail
     * @param nu_bool number of update
     * @return this builder
     */
    public EnvironmentBuilder setNbUpdatesForBoolTrail(int nu_bool) {
        this.nu_bool = nu_bool;
        return this;
    }

    /**
     * Number of default worlds allowed for bool trail
     * @param nw_bool number of worlds
     * @return this builder
     */
    public EnvironmentBuilder setNbWorldsForBoolTrail(int nw_bool) {
        this.nw_bool = nw_bool;
        return this;
    }

    /**
     * Set load factor for bool trail resizing
     * @param lf_bool load factor
     * @return this builder
     */
    public EnvironmentBuilder setLoadFactorForBoolTrail(double lf_bool) {
        this.lf_bool = lf_bool;
        return this;
    }

    /**
     * Set type for bool trail
     * @param t_bool type of trail
     * @return this builder
     */
    public EnvironmentBuilder setTypeForBoolTrail(EnvironmentTrailing.Type t_bool) {
        this.t_bool = t_bool;
        return this;
    }

    /**
     * Number of default updates allowed for double trail
     * @param nu_double number of update
     * @return this builder
     */
    public EnvironmentBuilder setNbUpdatesForDoubleTrail(int nu_double) {
        this.nu_double = nu_double;
        return this;
    }

    /**
     * Number of default worlds allowed for double trail
     * @param nw_double number of worlds
     * @return this builder
     */
    public EnvironmentBuilder setNbWorldsForDoubleTrail(int nw_double) {
        this.nw_double = nw_double;
        return this;
    }

    /**
     * Set load factor for double trail resizing
     * @param lf_double load factor
     * @return this builder
     */
    public EnvironmentBuilder setLoadFactorForDoubleTrail(double lf_double) {
        this.lf_double = lf_double;
        return this;
    }

    /**
     * Set type for double trail
     * @param t_double type of trail
     * @return this builder
     */
    public EnvironmentBuilder setTypeForDoubleTrail(EnvironmentTrailing.Type t_double) {
        this.t_double = t_double;
        return this;
    }

    /**
     * Number of default updates allowed for operation trail
     * @param nu_ope number of update
     * @return this builder
     */
    public EnvironmentBuilder setNbUpdatesForOperationTrail(int nu_ope) {
        this.nu_ope = nu_ope;
        return this;
    }

    /**
     * Number of default worlds allowed for operation trail
     * @param nw_ope number of worlds
     * @return this builder
     */
    public EnvironmentBuilder setNbWorldsForOperationTrail(int nw_ope) {
        this.nw_ope = nw_ope;
        return this;
    }

    /**
     * Set load factor for operation trail resizing
     * @param lf_ope load factor
     * @return this builder
     */
    public EnvironmentBuilder setLoadFactorForOperationTrail(double lf_ope) {
        this.lf_ope = lf_ope;
        return this;
    }

    /**
     * Set type for oepration trail
     * @param t_ope type of trail
     * @return this builder
     */
    public EnvironmentBuilder setTypeForOperationTrail(EnvironmentTrailing.Type t_ope) {
        this.t_ope = t_ope;
        return this;
    }

    /**
     * Build the environment
     * @return the resulting environment
     */
    public EnvironmentTrailing build(){
        EnvironmentTrailing env = new EnvironmentTrailing();
        switch (t_int){
            case FLAT:
                env.setIntTrail(new StoredIntTrail(nu_int, nw_int, lf_int));
                break;
            case UNSAFE:
                env.setIntTrail(new UnsafeIntTrail(nu_int, nw_int, lf_int));
                break;
            case CHUNCK:
                env.setIntTrail(new StoredIntChunckTrail(nw_int, lf_int));
                break;
        }
        switch (t_long){
            case FLAT:
                env.setLongTrail(new StoredLongTrail(nu_long, nw_long, lf_long));
                break;
            case UNSAFE:
                env.setLongTrail(new UnsafeLongTrail(nu_long, nw_long, lf_long));
                break;
            case CHUNCK:
                env.setLongTrail(new StoredLongChunckTrail(nw_long, lf_long));
                break;
        }
        switch (t_bool){
            case FLAT:
                env.setBoolTrail(new StoredBoolTrail(nu_bool, nw_bool, lf_bool));
                break;
            case UNSAFE:
                env.setBoolTrail(new UnsafeBoolTrail(nu_bool, nw_bool, lf_bool));
                break;
            case CHUNCK:
                env.setBoolTrail(new StoredBoolChunckTrail(nw_bool, lf_bool));
                break;
        }
        switch (t_double){
            case FLAT:
                env.setDoubleTrail(new StoredDoubleTrail(nu_double, nw_double, lf_double));
                break;
            case UNSAFE:
                env.setDoubleTrail(new UnsafeDoubleTrail(nu_double, nw_double, lf_double));
                break;
            case CHUNCK:
                env.setDoubleTrail(new StoredDoubleChunckTrail(nw_double, lf_double));
                break;
        }
        switch (t_ope){
            default:
            case FLAT:
                env.setOperationTrail(new OperationTrail(nu_ope, nw_ope, lf_ope));
                break;
            case CHUNCK:
                env.setOperationTrail(new OperationChunckTrail(nw_ope, lf_ope));
                break;
        }
        return env;
    }
}
