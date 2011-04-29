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

package choco.kernel.common.util.tools;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 3 juil. 2009
 * Since : Choco 2.1.0
 * Update : Choco 2.1.0
 *
 * Provides some short and usefull methods to deal with String object
 * and pretty print of IPretty objects.
 *
 */
public class StringUtils {
    private StringUtils() {}

    /**
	 * Pads out a string upto padlen with pad chars
	 *
	 * @param str    string to be padded
	 * @param padlen length of pad (+ve = pad on right, -ve pad on left)
	 * @param pad    character
	 * @return padded string
	 */
	public static String pad(String str, int padlen, String pad) {
        final StringBuilder padding = new StringBuilder(32);
		final int len = Math.abs(padlen) - str.length();
		if (len < 1) {
			return str;
		}
		for (int i = 0; i < len; ++i) {
			padding.append(pad);
		}
		return (padlen < 0 ? padding.append(str).toString() : padding.insert(0,str).toString());
	}

}
