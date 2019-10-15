/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.writer;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.writer.constraints.ConstraintWriter;
import org.chocosolver.writer.variables.VariableWriter;

import java.io.IOException;

/**
 * A class to export ("write") a model
 *
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 20/09/2017.
 */
public abstract class ModelWriter {

    /**
     * Utility class to write each variable
     */
    final VariableWriter variableWriter;

    /**
     * Utility class to write each variable
     */
    final ConstraintWriter constraintWriter;

    public ModelWriter(VariableWriter variableWriter, ConstraintWriter constraintWriter) {
        this.variableWriter = variableWriter;
        this.constraintWriter = constraintWriter;
    }

    /**
     * Write a 'model' into a stream
     *
     * @param model a model
     */
    public void write(Model model) throws IOException {
        if (model.getSettings().enableSAT()) {
            throw new UnsupportedOperationException("ModelWriter does not support clauses stored in third-party SAT Solver." +
                    "Consider overriding Settings.enableSAT() to return 'false' to fix the problem.");
        }
        beginModel();
        writeName(model.getName());
        if (model.getNbRealVar() > 0) {
            writeModelPrecision(model.getPrecision());
        }
        beginVariables();
        for (Variable v : model.getVars()) {
            variableWriter.write(v);
        }
        endVariables();
        beginConstraints();
        for (Constraint c : model.getCstrs()) {
            constraintWriter.write(c);
        }
        endConstraints();
        if (model.getObjective() != null) {
            writeObjective(model.getObjective().getName(), model.getObjective().getId(),
                    model.getResolutionPolicy() == ResolutionPolicy.MAXIMIZE);
        }
        endModel();
    }

    /**
     * Begins encoding a model. A call to this method must be paired with a call to {@link
     * #endModel()}.
     */
    public abstract void beginModel() throws IOException;

    /**
     * Encodes the model name
     *
     * @param name current model name
     */
    public abstract void writeName(String name) throws IOException;

    /**
     * Encodes the current model precision (only when there is at least one real variable)
     *
     * @param precision range precision
     */
    public abstract void writeModelPrecision(double precision) throws IOException;

    /**
     * Begins encoding the set of variables. A call to this method must be paired with a call to
     * {@link #endVariables()}.
     */
    public abstract void beginVariables() throws IOException;

    /**
     * Ends encoding the current set of variables.
     */
    public abstract void endVariables() throws IOException;

    /**
     * Begins encoding the set of constraints. A call to this method must be paired with a call to
     * {@link #endConstraints()}.
     */
    public abstract void beginConstraints() throws IOException;

    /**
     * Ends encoding the current set of constraints.
     */
    public abstract void endConstraints() throws IOException;

    /**
     * Encodes the objective variable and policy
     *
     * @param objName  name of the objective variable
     * @param objId    id of the objective variable
     * @param maximize 'true' if maximization criterion, 'false' if minimization criterion
     */
    public abstract void writeObjective(String objName, int objId, boolean maximize) throws IOException;

    /**
     * Write 'end' model instruction
     */
    public abstract void endModel() throws IOException;

}
