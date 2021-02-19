/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.IOException;

import static java.lang.String.format;

/**
 * CSPLib prob012:<br/>
 * "Nonograms are a popular puzzles, which goes by different names in different countries.
 * Models have to shade in squares in a grid so that blocks of consecutive shaded squares satisfy constraints
 * given for each row and column.
 * Constraints typically indicate the sequence of shaded blocks (e.g. 3,1,2 means that there is a block of 3,
 * then a gap of unspecified size, a block of length 1, another gap, and then a block of length 2)."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/08/11
 */
public class Nonogram extends AbstractProblem {

    @Option(name = "-d", aliases = "--data", usage = "Nonogram data ID.", required = false)
    Data data = Data.rabbit;
    @Option(name = "-f", aliases = "--frame", usage = "open a frame.", required = false)
    boolean frame = false;

    BoolVar[][] vars;


    @Override
    public void buildModel() {
        model = new Model();
        int nR = data.getR().length;
        int nC = data.getC().length;
        vars = new BoolVar[nR][nC];
        for (int i = 0; i < nR; i++) {
            for (int j = 0; j < nC; j++) {
                vars[i][j] = model.boolVar(format("B_%d_%d", i, j));
            }
        }
        for (int i = 0; i < nR; i++) {
            dfa(vars[i], data.getR(i), model);
        }
        for (int j = 0; j < nC; j++) {
            dfa(ArrayUtils.getColumn(vars, j), data.getC(j), model);
        }

    }

    private void dfa(BoolVar[] cells, int[] rest, Model model) {
        StringBuilder regexp = new StringBuilder("0*");
        int m = rest.length;
        for (int i = 0; i < m; i++) {
            regexp.append('1').append('{').append(rest[i]).append('}');
            regexp.append('0');
            regexp.append(i == m - 1 ? '*' : '+');
        }
        IAutomaton auto = new FiniteAutomaton(regexp.toString());
        model.regular(cells, auto).post();
    }


    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        model.getSolver().solve();
        System.out.println(String.format("Nonogram -- %s", data.name()));
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < vars.length; i++) {
            st.append("\t");
            for (int j = 0; j < vars[i].length; j++) {
                st.append(vars[i][j].getValue() == 1 ? '#' : ' ');
            }
            st.append("\n");
        }
        System.out.println(st.toString());
        if (frame) {
            JFrame frame = new JFrame();
            frame.setTitle("NonoGram");
            frame.setBackground(Color.GRAY);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            Container contentPane = frame.getContentPane();
            Drawing d = new Drawing();
            contentPane.add(d);
            Dimension dim = d.getSize();
            frame.setSize(new Dimension((int) dim.getWidth(), (int) dim.getHeight() + 25));

            frame.setVisible(true);
        }
    }

    /**
     * Class to draw the solution on a JPanel.
     */
    private class Drawing extends JPanel {

        int px = 10;

        public Drawing() {
            this.setSize(new Dimension(vars.length * px, vars[0].length * px));
            this.setEnabled(true);
        }


        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < vars.length; i++) {
                for (int j = 0; j < vars[i].length; j++) {
                    //g.setColor(java.awt.Color.gray);
                    //g.fillRect(j * px, i * px, px, px);

                    if (vars[i][j].getValue() == 1)
                        g.setColor(java.awt.Color.black);
                    else
                        g.setColor(java.awt.Color.white);

                    g.fillRect(j * px, i * px, px, px);

                }
            }

        }
    }

    public static void main(String[] args) {
        new Nonogram().execute(args);
    }

    ////////////////////////////////////////// DATA ////////////////////////////////////////////////////////////////////
    enum Data {
        /**
         * From <a href="http://www.comp.lancs.ac.uk/~ss/nonogram/">http://www.comp.lancs.ac.uk/~ss/nonogram/</a>
         */
        rabbit(new int[][][]{
                {
                        {2},
                        {4, 2},
                        {1, 1, 4},
                        {1, 1, 1, 1},
                        {1, 1, 1, 1},
                        {1, 1, 1, 1},
                        {1, 1, 1, 1},
                        {1, 1, 1, 1},
                        {1, 2, 2, 1},
                        {1, 3, 1},
                        {2, 1},
                        {1, 1, 1, 2},
                        {2, 1, 1, 1},
                        {1, 2},
                        {1, 2, 1},
                },
                {
                        {3},
                        {3},
                        {10},
                        {2},
                        {2},
                        {8, 2},
                        {2},
                        {1, 2, 1},
                        {2, 1},
                        {7},
                        {2},
                        {2},
                        {10},
                        {3},
                        {2},
                }
        }),
        soccer(new int[][][]{
                {
                        {3},
                        {5},
                        {3, 1},
                        {2, 1},
                        {3, 3, 4},
                        {2, 2, 7},
                        {6, 1, 1},
                        {4, 2, 2},
                        {1, 1},
                        {3, 1},
                        {6},
                        {2, 7},
                        {6, 3, 1},
                        {1, 2, 2, 1, 1},
                        {4, 1, 1, 3},
                        {4, 2, 2},
                        {3, 3, 1},
                        {3, 3},
                        {3},
                        {2, 1}
                },
                {
                        {2},
                        {1, 2},
                        {2, 3},
                        {2, 3},
                        {3, 1, 1},
                        {2, 1, 1},
                        {1, 1, 1, 2, 2},
                        {1, 1, 3, 1, 3},
                        {2, 6, 4},
                        {3, 3, 9, 1},
                        {5, 3, 2},
                        {3, 1, 2, 2},
                        {2, 1, 7},
                        {3, 3, 2},
                        {2, 4},
                        {2, 1, 2},
                        {2, 2, 1},
                        {2, 2},
                        {1},
                        {1}}
        }),
        bar_code(new int[][][]{
                {{7, 1, 1, 7},
                        {1, 1, 1, 1, 1},
                        {1, 3, 1, 1, 1, 1, 3, 1},
                        {1, 3, 1, 1, 1, 3, 1},
                        {1, 3, 1, 1, 2, 1, 3, 1},
                        {1, 1, 3, 1, 1},
                        {7, 1, 1, 1, 7},
                        {1, 1,},
                        {3, 5, 1, 3, 1,},
                        {2, 1, 2, 7},
                        {1, 1, 4, 2, 4, 3},
                        {3, 1, 1, 4, 1},
                        {6, 1, 2, 2, 1},
                        {1, 1, 3, 3},
                        {7, 1, 1, 1, 3, 2},
                        {1, 1, 6, 2, 1,},
                        {1, 3, 1, 1, 1, 1, 1, 1,},
                        {1, 3, 1, 1, 1, 4, 1,},
                        {1, 3, 1, 1, 3, 2, 1},
                        {1, 1, 2, 2, 1, 1,},
                        {7, 5, 1, 2},
                },
                {{7, 4, 7},
                        {1, 1, 2, 2, 1, 1},
                        {1, 3, 1, 1, 3, 1, 3, 1},
                        {1, 3, 1, 1, 1, 1, 3, 1},
                        {1, 3, 1, 1, 2, 1, 3, 1},
                        {1, 1, 1, 1, 1, 1, 1},
                        {7, 1, 1, 1, 7},
                        {1, 2,},
                        {1, 3, 1, 5, 3},
                        {2, 1, 1, 1, 2},
                        {1, 1, 8, 3, 1, 1},
                        {2, 5, 1, 3},
                        {2, 2, 1, 1, 1, 7},
                        {1, 1, 1,},
                        {7, 3, 1, 1,},
                        {1, 1, 2, 6, 1},
                        {1, 3, 1, 2, 4, 3,},
                        {1, 3, 1, 1, 1, 2,},
                        {1, 3, 1, 3, 1,},
                        {1, 1, 2, 5, 2},
                        {7, 6, 1, 1},
                }

        }),
        /**
         * From http://www.icparc.ic.ac.uk/eclipse/examples/nono.ecl.txt, the
         * hardest instance.
         */
        p200(new int[][][]{
                {
                        {1, 1, 2, 2,},
                        {5, 5, 7,},
                        {5, 2, 2, 9,},
                        {3, 2, 3, 9,},
                        {1, 1, 3, 2, 7,},
                        {3, 1, 5,},
                        {7, 1, 1, 1, 3,},
                        {1, 2, 1, 1, 2, 1,},
                        {4, 2, 4,},
                        {1, 2, 2, 2,},
                        {4, 6, 2,},
                        {1, 2, 2, 1,},
                        {3, 3, 2, 1,},
                        {4, 1, 15,},
                        {1, 1, 1, 3, 1, 1,},
                        {2, 1, 1, 2, 2, 3,},
                        {1, 4, 4, 1,},
                        {1, 4, 3, 2,},
                        {1, 1, 2, 2,},
                        {7, 2, 3, 1, 1,},
                        {2, 1, 1, 1, 5,},
                        {1, 2, 5,},
                        {1, 1, 1, 3,},
                        {4, 2, 1,},
                        {3,},
                },
                {
                        {2, 2, 3,},
                        {4, 1, 1, 1, 4,},
                        {4, 1, 2, 1, 1,},
                        {4, 1, 1, 1, 1, 1, 1,},
                        {2, 1, 1, 2, 3, 5,},
                        {1, 1, 1, 1, 2, 1,},
                        {3, 1, 5, 1, 2,},
                        {3, 2, 2, 1, 2, 2,},
                        {2, 1, 4, 1, 1, 1, 1,},
                        {2, 2, 1, 2, 1, 2,},
                        {1, 1, 1, 3, 2, 3,},
                        {1, 1, 2, 7, 3,},
                        {1, 2, 2, 1, 5,},
                        {3, 2, 2, 1, 2,},
                        {3, 2, 1, 2,},
                        {5, 1, 2,},
                        {2, 2, 1, 2,},
                        {4, 2, 1, 2,},
                        {6, 2, 3, 2,},
                        {7, 4, 3, 2,},
                        {7, 4, 4,},
                        {7, 1, 4,},
                        {6, 1, 4,},
                        {4, 2, 2,},
                        {2, 1},
                }
        });
        final int[][][] data;

        Data(int[][][] data) {
            this.data = data;
        }

        int[][] getR() {
            return data[0];
        }

        int[] getR(int i) {
            return data[0][i];
        }

        int[][] getC() {
            return data[1];
        }

        int[] getC(int j) {
            return data[1][j];
        }
    }

    private static class GetImage {
        public int[][] data;
        private int px = 6;

        GetImage(String path) throws InterruptedException, IOException {
            Image image = new ImageIcon(path).getImage();
            this.handlepixels(image, 0, 0, image.getWidth(null), image.getHeight(null));
            print();
        }

        public void handlesinglepixel(int x, int y, int pixel) {
            int alpha = (pixel >> 24) & 0xff;
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = (pixel) & 0xff;
            System.out.printf("%d %d %d %d\n", alpha, red, green, blue);
            if (blue + red + green < 765) {
                data[x][y] = 1;
            } else {
                data[x][y] = 0;
            }
        }

        public void handlepixels(java.awt.Image img, int x, int y, int w, int h) {
            PixelGrabber pg = new PixelGrabber(img, x, y, w, h, false);
            try {
                pg.grabPixels();
            } catch (InterruptedException e) {
                System.err.println("interrupted waiting for pixels!");
                return;
            }
            if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                System.err.println("image fetch aborted or errored");
                return;
            }
            int[] pixels = (int[]) pg.getPixels();
            data = new int[h][w];
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    handlesinglepixel(x + i, y + j, pixels[j * w + i]);
                }
            }
        }

        private void print() {
            StringBuilder st = new StringBuilder();
            st.append("{");
            int[][] rdata = ArrayUtils.transpose(data);
            oneDimension(rdata, st);
            st.append("},\n{");
            oneDimension(data, st);
            st.append("}\n");
            System.out.printf("%s\n", st.toString());
        }

        private void oneDimension(int[][] rdata, StringBuilder st) {
            for (int i = 0; i < rdata.length; i += px) {
                st.append('{');
                int c = 0;
                for (int j = 0; j < rdata[i].length; j += px) {
                    if (rdata[i][j] == 1) {
                        c++;
                    } else {
                        if (c > 0) {
                            st.append(c).append(',');
                        }
                        c = 0;
                    }
                }
                if (c > 0) {
                    st.append(c);
                }
                st.append("},\n");
            }
        }

        public static void main(String[] args) throws InterruptedException, IOException {
            new GetImage("/Users/cprudhom/Desktop/logoChoco2.png");
        }
    }
}
