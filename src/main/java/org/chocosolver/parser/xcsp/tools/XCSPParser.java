/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
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
package org.chocosolver.parser.xcsp.tools;

import org.chocosolver.parser.Exit;
import org.chocosolver.parser.xcsp.tools.XConstraints.*;
import org.chocosolver.parser.xcsp.tools.XVariables.Array;
import org.chocosolver.parser.xcsp.tools.XVariables.Var;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class XCSPParser {

    private XParser parser;
    String currentFileName;

    public void parse(String input) {
        this.currentFileName = input;
        loadData();
        specifyVariables();
        specifyConstraints();
        specifyObjectives();
    }

    private void loadData() {
        try {
            parser = new XParser(currentFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void specifyVariables() {

        for (XVariables.Entry entry : parser.entriesOfVariables) {
            String id = entry.id;
            if (entry instanceof Var) {
                Var v = (Var) entry;
                if (v.degree > 0) {

                }
            } else {
                Array a = ((Array) entry);
                int[] sizes = a.size;
                switch (sizes.length) {
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    default:
                        Exit.log("Unknown case");
                        break;
                }
            }
        }
    }

    protected void specifyConstraints() {
        for (Entry entry : parser.entriesOfConstraints) {
            if (entry instanceof Ctr) {
                parseConstraint((Ctr) entry);
            } else if (entry instanceof Group) {
                parseGroup((Group) entry);
            } else Exit.log("Unknown case");
        }
    }

    protected void specifyObjectives() {
        for (XObjectives.Objective entry : parser.objectives) {
            if (entry.type == XEnums.TypeObjective.expression) {
                XNodeExpr node = ((XObjectives.ObjectiveExpr) entry).rootNode;
                if (node.type == XEnums.TypeExpr.VAR) {

                } else {
                    Exit.log("Unknown case");
                }
            } else {
                if (entry.type == XEnums.TypeObjective.sum) {
                } else if (entry.type == XEnums.TypeObjective.maximum) {
                } else if (entry.type == XEnums.TypeObjective.minimum) {
                } else
                    Exit.log("Unknown case");
            }
        }
    }


    private void parseConstraint(Ctr ctr) {
        Child[] childs = ctr.childs;
        switch (ctr.type) {
            case intension:
                break;
            case extension:
                break;
            case allDifferent:
                if (childs[0].type == XEnums.TypeChild.matrix) {
                } else {
                }
                break;
            case regular:
                break;
            case allEqual:
                break;
            case ordered:
                break;
            case sum:
                break;
            case channel:
                break;
            case lex:
                break;
            case noOverlap:
                break;
            default:
                Exit.log("Unknown case");
                break;
        }
    }

    private void parseGroup(Group group) {
        if (group.template instanceof Ctr) {
            Ctr ctrTemplate = (Ctr) group.template;
            Child[] childs = ctrTemplate.childs;

            switch (ctrTemplate.type) {
                case intension:
                    break;
                case extension:
                    break;
                case allDifferent:
                    break;
                case regular:
                    break;
                case sum:
                    break;
                case ordered:
                    break;
                default:
                    Exit.log("Unknown case");
                    break;
            }
        } else if (group.template instanceof Logic && ((Logic) group.template).type == XEnums.TypeCtr.not) {
            Entry child = ((Logic) group.template).components[0];
            if (child instanceof Ctr && ((Ctr) child).type == XEnums.TypeCtr.allEqual) {

            } else {
                Exit.log("Unknown case");
            }
        } else {
            Exit.log("Unknown case");
        }
    }
}
