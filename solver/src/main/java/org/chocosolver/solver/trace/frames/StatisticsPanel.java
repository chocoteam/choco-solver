/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.trace.frames;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.SearchState;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A simple dashboard to show resolution statistics on a frame during resolution.
 *
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 21/11/2017.
 */
public class StatisticsPanel extends JPanel {

    private static String[] fieldnames = {
            "Variables",
            "Constraints",
            "Objective",
            "Solutions",
            "Nodes",
            "Fails",
            "Backtracks",
            "Backjumps",
            "Restarts",
            "Fixpoints",
            "Depth",
            "Time (sec.)", // time in second
            "Nodes/sec.", // node per second
            "Fails/sec.", // node per second
            "Fixpoints/sec.", // node per second
            "Mem. usage (MB)",
    };
    @SuppressWarnings("unchecked")
    private static Function<Solver, String>[] fieldvalues = (Function<Solver, String>[]) new Function[]{
            (Function<Solver, String>) solver -> Long.toString(solver.getModel().getNbVars()),
            (Function<Solver, String>) solver -> Long.toString(solver.getModel().getNbCstrs()),
            (Function<Solver, String>) solver ->
                    (solver.hasObjective() ? solver.getBestSolutionValue().toString() : "--"),
            (Function<Solver, String>) solver -> Long.toString(solver.getSolutionCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getNodeCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getFailCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getBackTrackCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getBackjumpCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getRestartCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getFixpointCount()),
            (Function<Solver, String>) solver -> Long.toString(solver.getCurrentDepth()),
            (Function<Solver, String>) solver ->
                    toHHmmss((long) (solver.getTimeCount() * 1000)),
            (Function<Solver, String>) solver ->
                    String.format("%.2f", (solver.getNodeCount() / solver.getTimeCount())),
            (Function<Solver, String>) solver ->
                    String.format("%.2f", (solver.getFailCount() / solver.getTimeCount())),
            (Function<Solver, String>) solver ->
                    String.format("%.2f", (solver.getFixpointCount() / solver.getTimeCount())),
            (Function<Solver, String>) solver -> String.format("%d", getUsedMemInBytes()),
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
     * Chart for objective function
     */
    private XYChart chart;
    /**
     * Panel that hosts {@link #chart}
     */
    private XChartPanel chartpanel;
    /**
     * List required by {@link #chart}, for X coordinate
     */
    private final List<Number> time;
    /**
     * List required by {@link #chart}, for Y coordinate
     */
    private final List<Number> obj;
    /**
     * Chart menu
     */
    private JMenu menuChart;
    /**
     * Chart option, modified thanks to {@link #menuChart}
     */
    private byte chartOptions;

    /**
     * Create a simple dashboard that show statistics from 'solver' every 'duration' milliseconds
     *
     * @param solver   to extract statistics from
     * @param duration frequency rate, in milliseconds
     */
    public StatisticsPanel(Solver solver, long duration, JFrame mainFrame) {
        this.solver = solver;

        setLayout(new BorderLayout());

        int length = fieldnames.length;
        textFields = new JTextField[length];
        JLabel[] labels = new JLabel[length];
        for (int i = 0; i < length; i++) {
            textFields[i] = new JTextField(8);
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

        // Create Chart
        time = new ArrayList<>();
        obj = new ArrayList<>();

        chartOptions = 0b11;
        makeMenu(mainFrame);


        printStatistics();
        //noinspection InfiniteLoopStatement
        // Create Chart
        /**
         * Thread that updates data
         */
        Thread printer = new Thread(() -> {
            alive = true;
            try {
                Thread.sleep(5);
                //noinspection InfiniteLoopStatement
                do {
                    if (solver.getSearchState().equals(SearchState.RUNNING)) {
                        printStatistics();
                        if (solver.hasObjective()) {
                            if ((chartOptions & 0b10) != 0) {
                                time.add(solver.getTimeCount());
                                obj.add(solver.getBestSolutionValue());
                            }
                            if (chart == null) {
                                // Create Chart
                                chart = QuickChart
                                    .getChart("Objective", "Time (sec)", "Objective value",
                                        solver.getObjectiveManager().getObjective().getName(), time,
                                        obj);
                                chart.getStyler().setChartBackgroundColor(leftPane.getBackground());
                                chartpanel = new XChartPanel<>(chart);
                                add(chartpanel);
                                mainFrame.pack();
                            }
                            if ((chartOptions & 0b01) != 0) {
                                chart.updateXYSeries(
                                    solver.getObjectiveManager().getObjective().getName(), time,
                                    obj, null);
                                chartpanel.revalidate();
                                chartpanel.repaint();
                            }
                        } else {
                            menuChart.setEnabled(false);
                        }
                    }
                    Thread.sleep(duration);
                } while (alive);
            } catch (InterruptedException ignored) {
            }
        });
        printer.setDaemon(true);
        printer.start();
    }

    private void makeMenu(JFrame mainFrame) {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();

        JMenu mainMenu = new JMenu("Menu");
        mainMenu.setMnemonic(KeyEvent.VK_M);
        menuBar.add(mainMenu);
        mainFrame.setJMenuBar(menuBar);

        menuChart = new JMenu("Chart");
        menuChart.setMnemonic(KeyEvent.VK_C);
        menuChart.add(makeShowHideItem(mainFrame));
        menuChart.add(makeDisconnectConnectItem(mainFrame));
        mainMenu.add(menuChart);
    }

    private JMenuItem makeDisconnectConnectItem(JFrame mainFrame) {
        JMenuItem item = new JMenuItem("Disconnect",
                KeyEvent.VK_D);
        item.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, InputEvent.ALT_MASK));
        item.addActionListener(e -> {
            if ((chartOptions & 0b10) != 0) {
                this.remove(chartpanel);
            }
            chartOptions = 0b00;
            time.clear();
            obj.clear();
            mainFrame.pack();
        });
        return item;
    }

    private JMenuItem makeShowHideItem(JFrame mainFrame) {
        JMenuItem item = new JMenuItem("Hide/Show",
                KeyEvent.VK_H);
        item.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, InputEvent.ALT_MASK));
        item.addActionListener(e -> {
            if ((chartOptions & 0b10) != 0) {
                this.remove(chartpanel);
                chartOptions ^= 0b10;
            } else {
                this.add(chartpanel);
                chartOptions = 0b11;
            }
            mainFrame.pack();
        });
        return item;
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
        for (int i = 0; i < textFields.length; i++) {
            textFields[i].setText(fieldvalues[i].apply(solver));
        }
    }

    private static String toHHmmss(long etime) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(etime),
                TimeUnit.MILLISECONDS.toMinutes(etime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(etime) % TimeUnit.MINUTES.toSeconds(1));
    }

    private static long getUsedMemInBytes(){
        long usedHeapMemoryAfterLastGC = 0;
        List<MemoryPoolMXBean> memoryPools = new ArrayList<>(ManagementFactory.getMemoryPoolMXBeans());
        for (MemoryPoolMXBean memoryPool : memoryPools) {
            if (memoryPool.getType().equals(MemoryType.HEAP)) {
                MemoryUsage poolCollectionMemoryUsage = memoryPool.getUsage();
                usedHeapMemoryAfterLastGC += poolCollectionMemoryUsage.getUsed();
            }
        }
        return(long)(usedHeapMemoryAfterLastGC / 1e6);
    }
}
