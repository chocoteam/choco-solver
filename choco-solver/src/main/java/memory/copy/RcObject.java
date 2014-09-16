/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package memory.copy;

import memory.IEnvironment;
import memory.IStateObject;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 22 juin 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class RcObject implements IStateObject, RecomputableElement {

    protected final EnvironmentCopying environment;
    protected int timeStamp;
    private Object currentObject;


    public RcObject(EnvironmentCopying env, Object obj) {
        environment = env;
        currentObject = obj;
        env.getObjectCopy().add(this);
        timeStamp = environment.getWorldIndex();
    }

    public Object get() {
        return currentObject;
    }

    public void set(Object y) {
        currentObject = y;
        timeStamp = environment.getWorldIndex();
    }

    /**
     * Modifies the value without storing the former value on the trailing stack.
     *
     * @param y      the new value
     * @param wstamp the stamp of the world in which the update is performed
     */

    public void _set(final Object y, final int wstamp) {
        currentObject = y;
        timeStamp = wstamp;
    }

    public IEnvironment getEnvironment() {
        return environment;
    }

    public Object deepCopy() {
        return currentObject;
    }

    public int getType() {
        return OBJECT;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return String.valueOf(currentObject.toString());
    }
}
