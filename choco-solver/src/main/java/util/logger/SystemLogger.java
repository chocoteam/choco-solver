/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package util.logger;

/**
 * Created by cprudhom on 07/11/14.
 * Project: choco.
 */
public class SystemLogger implements ILogger {

    /**
     * Is writing to the "standard" output stream enabled
     */
    protected boolean isInfoEnabled = true;

    /**
     * Is writing to the "standard" error output stream enabled
     */
    protected boolean isErrorEnabled = true;

    /**
     * Enabled (if set to true) or disabled (if set to <code>false</code>) writing to the "standard" output stream
     *
     * @param isEnabled A boolean; if true, the "standard" output output stream will be available
     */
    public void enableInfo(boolean isEnabled) {
        isInfoEnabled = isEnabled;
    }

    /**
     * @return <code>true</code> if "standard" output stream is enabled, <code>false</code> otherwise.
     */
    public boolean isInfoEnabled() {
        return isInfoEnabled;
    }

    /**
     * {@inheritDoc}
     * The stream is defined by {@link System#out}
     */
    public void info(String s) {
        System.out.println(s);
    }

    /**
     * {@inheritDoc}
     * The stream is defined by {@link System#out}
     */
    public void info(String format, Object... args) {
        System.out.format(format, args);
        System.out.print('\n');
    }

    /**
     * {@inheritDoc}
     * The stream is defined by {@link System#out}
     */
    public void infof(String format, Object... args) {
        System.out.format(format, args);
    }

    /**
     * Enabled (if set to true) or disabled (if set to false) writing to the "standard" error output stream
     *
     * @param isEnabled A boolean; if true, the "standard" error output output stream will be available
     */
    public void enableError(boolean isEnabled) {
        isErrorEnabled = isEnabled;
    }

    /**
     * @return <code>true</code> if "standard" error output stream is enabled, <code>false</code> otherwise.
     */
    public boolean isErrorEnabled() {
        return isErrorEnabled;
    }


    /**
     * {@inheritDoc}
     * The stream is defined by {@link System#err}
     */
    public void error(String s) {
        System.err.print(s);
    }

    /**
     * {@inheritDoc}
     * The stream is defined by {@link System#err}
     */
    public void error(String format, Object... args) {
        System.err.format(format, args);
        System.err.print('\n');
    }

    /**
     * {@inheritDoc}
     * The stream is defined by {@link System#err}
     */
    public void errorf(String format, Object... args) {
        System.err.format(format, args);
    }
}
