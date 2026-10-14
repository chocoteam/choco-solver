---
title: "Description"
date: 2020-02-05T11:22:05+01:00
type: docs
weight: 51
description: >
  A description of the problem to model and solve.
---

Nonograms are a popular puzzles, which goes by different names in
different countries.

Models have to shade in squares in a grid so that blocks of consecutive
shaded squares satisfy constraints given for each row and column.

Constraints typically indicate the sequence of shaded blocks (e.g. 3,1,2
means that there is a block of 3, then a gap of unspecified size, a
block of length 1, another gap, and then a block of length 2).

See [Nonogram](http://www.csplib.org/Problems/prob012/) for more
details.

Input data
----------

We consider here the following input (in java):

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// sequence of shaded blocks
int[][][] BLOCKS =
        new int[][][]{{
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
        }, {
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
                {2}}};
{{< /tab >}}
{{< /tabpane >}}
