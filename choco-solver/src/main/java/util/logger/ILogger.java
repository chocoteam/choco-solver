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
 * The logger for Choco: simple and reduced.
 * <p/>
 * Created by cprudhom on 07/11/14.
 * Project: choco.
 */
public interface ILogger {

    /**
     * Enabled (if set to true) or disabled (if set to <code>false</code>) writing to the "standard" output stream
     *
     * @param isEnabled A boolean; if true, the "standard" output output stream will be available
     */
    public void enableInfo(boolean isEnabled);

    /**
     * @return <code>true</code> if "standard" output stream is enabled, <code>false</code> otherwise.
     */
    public boolean isInfoEnabled();

    /**
     * Writes a string to the "standard" output stream
     * and terminate the line automatically.
     *
     * @param s The <code>String</code> to be printed
     */
    public void info(String s);

    /**
     * Writes a formatted string to the "standard" output stream using the specified
     * format string and arguments and terminate the line automatically.
     *
     * @param format A format string as described in <a
     *               href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param args   Arguments referenced by the format specifiers in the format
     *               string.  If there are more arguments than format specifiers, the
     *               extra arguments are ignored.  The number of arguments is
     *               variable and may be zero.  The maximum number of arguments is
     *               limited by the maximum dimension of a Java array as defined by
     *               <cite>The Java&trade; Virtual Machine Specification</cite>.
     *               The behaviour on a
     *               <tt>null</tt> argument depends on the <a
     *               href="../util/Formatter.html#syntax">conversion</a>.
     */
    public void info(String format, Object... args);

    /**
     * Writes a formatted string to the "standard" output stream using the specified
     * format string and arguments and does not terminate the line automatically
     * (thus, adding '\n' is required).
     *
     * @param format A format string as described in <a
     *               href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param args   Arguments referenced by the format specifiers in the format
     *               string.  If there are more arguments than format specifiers, the
     *               extra arguments are ignored.  The number of arguments is
     *               variable and may be zero.  The maximum number of arguments is
     *               limited by the maximum dimension of a Java array as defined by
     *               <cite>The Java&trade; Virtual Machine Specification</cite>.
     *               The behaviour on a
     *               <tt>null</tt> argument depends on the <a
     *               href="../util/Formatter.html#syntax">conversion</a>.
     */
    public void infof(String format, Object... args);

    /**
     * Enabled (if set to true) or disabled (if set to false) writing to the "standard" error output stream
     *
     * @param isEnabled A boolean; if true, the "standard" error output output stream will be available
     */
    public void enableError(boolean isEnabled);

    /**
     * @return <code>true</code> if "standard" error output stream is enabled, <code>false</code> otherwise.
     */
    public boolean isErrorEnabled();


    /**
     * Writes a string to the "standard" error output stream
     * and terminate the line automatically.
     *
     * @param s The <code>String</code> to be printed
     */
    public void error(String s);

    /**
     * Writes a formatted string to the "standard" error output stream using the specified
     * format string and arguments and terminate the line automatically.
     *
     * @param format A format string as described in <a
     *               href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param args   Arguments referenced by the format specifiers in the format
     *               string.  If there are more arguments than format specifiers, the
     *               extra arguments are ignored.  The number of arguments is
     *               variable and may be zero.  The maximum number of arguments is
     *               limited by the maximum dimension of a Java array as defined by
     *               <cite>The Java&trade; Virtual Machine Specification</cite>.
     *               The behaviour on a
     *               <tt>null</tt> argument depends on the <a
     *               href="../util/Formatter.html#syntax">conversion</a>.
     */
    public void error(String format, Object... args);

    /**
     * Writes a formatted string to the "standard" error output stream using the specified
     * format string and arguments and does not terminate the line automatically
     * (thus, adding '\n' is required).
     *
     * @param format A format string as described in <a
     *               href="../util/Formatter.html#syntax">Format string syntax</a>
     * @param args   Arguments referenced by the format specifiers in the format
     *               string.  If there are more arguments than format specifiers, the
     *               extra arguments are ignored.  The number of arguments is
     *               variable and may be zero.  The maximum number of arguments is
     *               limited by the maximum dimension of a Java array as defined by
     *               <cite>The Java&trade; Virtual Machine Specification</cite>.
     *               The behaviour on a
     *               <tt>null</tt> argument depends on the <a
     *               href="../util/Formatter.html#syntax">conversion</a>.
     */
    public void errorf(String format, Object... args);

}
