/* ************************************************
*           _      _                             *
*          |  (..)  |                            *
*          |_ J||L _|         CHOCO solver       *
*                                                *
*     Choco is a java library for constraint     *
*     satisfaction problems (CSP), constraint    *
*     programming (CP) and explanation-based     *
*     constraint solving (e-CP). It is built     *
*     on a event-based propagation mechanism     *
*     with backtrackable structures.             *
*                                                *
*     Choco is an open-source software,          *
*     distributed under a BSD licence            *
*     and hosted by sourceforge.net              *
*                                                *
*     + website : http://choco.emn.fr            *
*     + support : choco@emn.fr                   *
*                                                *
*     Copyright (C) F. Laburthe,                 *
*                   N. Jussien    1999-2010      *
**************************************************/
package sandbox;


import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import solver.variables.IntVar;

import javax.swing.*;
import java.awt.*;

// ****************************************************************************
// * JFREECHART DEVELOPER GUIDE                                               *
// * The JFreeChart Developer Guide, written by David Gilbert, is available   *
// * to purchase from Object Refinery Limited:                                *
// *                                                                          *
// * http://www.object-refinery.com/jfreechart/guide.html                     *
// *                                                                          *
// * Sales are used to provide funding for the JFreeChart project - please    *
// * support us so that we can continue developing free software.             *
// ****************************************************************************

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28 oct. 2010
 */
public class ObjectiveChart extends ApplicationFrame implements solver.Observer<IntVar> {

    /**
     * The datasets.
     */
    private TimeSeriesCollection[] datasets;

    /**
     * The most recent value added to series 1.
     */
    private double[] lastValue = new double[2];

    Second lastml = new Second();

    /**
     * Constructs a new demonstration application.
     *
     * @param title     the frame title.
     * @param objective
     */
    public ObjectiveChart(final String title, IntVar objective) {

        super(title);

        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Time"));
        this.datasets = new TimeSeriesCollection[1];

        {
            this.lastValue[0] = objective.getLB();
            final TimeSeries series1 = new TimeSeries("Lower bound ");
            final TimeSeries series2 = new TimeSeries("Upper bound ");
            this.datasets[0] = new TimeSeriesCollection(series1);
            this.datasets[0].addSeries(series2);
            final NumberAxis rangeAxis = new NumberAxis(objective.getName());
            rangeAxis.setAutoRangeIncludesZero(false);
            final XYPlot subplot = new XYPlot(
                    this.datasets[0], null, rangeAxis, new XYDifferenceRenderer(Color.black, Color.white, false)
            );
            subplot.setBackgroundPaint(Color.lightGray);
            subplot.setDomainGridlinePaint(Color.white);
            subplot.setRangeGridlinePaint(Color.white);
            plot.add(subplot);
        }

        final JFreeChart chart = new JFreeChart("Objective chart", plot);
        chart.setBorderPaint(Color.black);
        chart.setBorderVisible(true);
        chart.setBackgroundPaint(Color.white);

        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        final ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        //axis.setFixedAutoRange(10000.0);  // 60 seconds

        final JPanel content = new JPanel(new BorderLayout());

        final ChartPanel chartPanel = new ChartPanel(chart);
        content.add(chartPanel);

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 470));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(content);

    }

    @Override
    public void update(IntVar variable, boolean cond) {
        Second newml = new Second();
        if (!newml.equals(lastml)) {
            this.lastValue[0] = variable.getLB();
            this.datasets[0].getSeries(0).add(newml, this.lastValue[0]);
            this.lastValue[1] = variable.getUB();
            this.datasets[0].getSeries(1).add(newml, this.lastValue[1]);
            lastml = newml;
        }
    }
}