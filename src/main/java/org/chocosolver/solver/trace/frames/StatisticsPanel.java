/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license. See LICENSE file in the project root for full license
 * information.
 */
package org.chocosolver.solver.trace.frames;

import org.chocosolver.solver.Solver;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.swing.*;

/**
 * A simple dashboard to show resolution statistics on a frame during resolution.
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 21/11/2017.
 */
public class StatisticsPanel extends JPanel{

    private static String[] fieldnames = {
            "Variables",
            "Constraints",
            "Objective",
            "Solutions",
            "Nodes",
            "Fails",
            "Backtracks",
            "Restarts",
            "Time (sec.)", // time in second
            "Nodes/sec.", // node per second
    };
    @SuppressWarnings("unchecked")
    private static Function<Solver, String>[] fieldvalues = (Function<Solver, String>[]) new Function[]{
            (Function<Solver, String>) solver -> Long.toString(solver.getModel().getNbVars()),
            (Function<Solver, String>) solver -> Long.toString(solver.getModel().getNbCstrs()),
            (Function<Solver, String>) solver ->
                (solver.hasObjective()?solver.getBestSolutionValue().toString():"--"),
            (Function<Solver, String>) solver -> Long.toString(solver.getSolutionCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getNodeCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getFailCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getBackTrackCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getRestartCount()),
            (Function<Solver, String>) solver ->
                    toHHmmss((long)(solver.getTimeCount() * 1000)),
            (Function<Solver, String>) solver ->
                    String.format("%.2f",(solver.getNodeCount() / solver.getTimeCount())),
    };

    /**
     * A boolean to kill the printer when the resolution ends.
     */
    private volatile boolean alive;

    /**
     * Solver to extract statistics from
     */
    private final Solver solver;

    /**
     * Fields to update
     */
    private final JTextField[] textFields;

    /**
     * Create a simple dashboard that show statistics from 'solver' every 'duration' milliseconds
     * @param solver to extract statistics from
     * @param duration frequency rate, in milliseconds
     */
    public StatisticsPanel(Solver solver, long duration){
        this.solver = solver;

        setLayout(new BorderLayout());

        textFields = new JTextField[10];
        JLabel[] labels = new JLabel[10];
        for(int i = 0; i < 10; i++){
            textFields[i] = new JTextField(6);
            textFields[i].setEnabled(false);
            textFields[i].setHorizontalAlignment(SwingConstants.RIGHT);
            labels[i] = new JLabel(fieldnames[i] + ": ");
            labels[i].setLabelFor(textFields[i]);
        }

        //Lay out the text controls and the labels.
        JPanel textControlsPane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        textControlsPane.setLayout(gridbag);
        addLabelTextRows(labels, textFields, textControlsPane);

        c.gridwidth = GridBagConstraints.REMAINDER; //last
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        textControlsPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Resolution statistics"), BorderFactory
                        .createEmptyBorder(5, 5, 5, 5)));

        //Put everything together.
        JPanel leftPane = new JPanel(new BorderLayout());
        leftPane.add(textControlsPane, BorderLayout.PAGE_START);

        add(leftPane, BorderLayout.LINE_START);
        printStatistics();
        Thread printer = new Thread(() -> {
            alive = true;
            try {
                Thread.sleep(duration);
                //noinspection InfiniteLoopStatement
                do {
                    printStatistics();
                    Thread.sleep(duration);
                } while (alive);
            } catch (InterruptedException ignored) {
            }
        });
        printer.setDaemon(true);
        printer.start();
    }

    private void addLabelTextRows(JLabel[] labels, JTextField[] textFields, Container container) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        int numLabels = labels.length;

        for (int i = 0; i < numLabels; i++) {
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE; //reset to default
            c.weightx = 0.0; //reset to default
            container.add(labels[i], c);

            c.gridwidth = GridBagConstraints.REMAINDER; //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            container.add(textFields[i], c);
        }
    }

    private void printStatistics() {
        for(int i = 0 ; i < textFields.length; i++) {
            textFields[i].setText(fieldvalues[i].apply(solver));
        }
    }

    private static String toHHmmss(long etime){
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(etime),
                TimeUnit.MILLISECONDS.toMinutes(etime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(etime) % TimeUnit.MINUTES.toSeconds(1));
    }
}
