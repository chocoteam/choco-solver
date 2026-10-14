---
title: "Code"
date: 2020-02-05T11:22:13+01:00
type: docs
math: "true"
weight: 53
description: >
  A bunch of code.
---

Building DFAs
-------------

Before describing the model, which is very compact, we will see how 
Deterministic Finite Automaton (DFA) can be build.

We will focus on a single sequence: {1, 2, 3}.

### Regexp way

The regular expression that encodes the sequence is
`"0*10+1{2}0+1{3}0*"`:

- `0*` the word can start with unbounded number of 0 (`*` means zero or
more times) 
- `10+` the first block of 1 is followed by at least one 0 (`+`
means one or more times) 
- `1{2}0+` deals with the second block of 2 (`a{n}` means `a`
occurs exactly `n` times) which is followed by at least one 0 
- `1{3}0*` the third -- and last -- block of size 3 is followed by zero or more 0.
Indeed, this the last block of the sequence, so there cannot be other 1
after but 0s are optional.

Starting and ending 0s are optional but it has to be defined in the
regexp, otherwise some valid words may be skipped.

{{% alert title="Caution" color="primary"%}}
In Choco-solver, DFAs only accept integer as character. 
`0*a+` is not a valid grammar, there is no conversion `Character` (java term) to `Integer`. But, numbers are allowed, not only digits. 
Indeed, some variables can take value greater than 9.
In that case, numbers are declared using the specific characters 
`<` and `>`. 
For example: `"0*<11><22>0*"` will accept words like `00112200` or `1122` but no `0120`.
{{%/alert%}}


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
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
{{< /tab >}}
{{< /tabpane >}}

### Constructive way

The constructive way requires to declare all states of the automaton and
links together with transitions. A transition corresponds to a character
in the word, and a state is *between* two characters of the word.

So there is a need of an initial state from which (through an outgoing
transition) the first character of the word will be provided. And at
least one final state to which (through an ingoing transition) the last
character of the word will be provided.

We note $s_i$ the initial state. The first character can either be a 0
or a 1, there will be two transitions outgoing from $s_i$. Then,
transition from $s_i$ producing 0 will go to $i_0$ (first transition).
And transition from $s_i$ producing 1 will go to $i_1$ (second
transition). $i_0$ points to itself providing 0 (third transition).
Outgoing transition from $i_1$ goes to $i_2$ and produces 0 (fourth
transition). Two transitions outgoes from $i_2$: one goes to itself
(fifth transition, producing 0), one goes to $i_3$ (sixth transition,
producing 1). $i_3$ goes to $i_4$ (seventh transition) and produce 1.
$i_4$ goes to $i_5$ (eighth transition) and produce 0. And so on. 

{{% pageinfo color="primary"%}}

![DFA for {1,2} sequence.](/images/tutos/dfa.png)

_Graph illustrating the DFA for the sequence {1, 2}. Generated with [Graphviz](http://www.graphviz.org/)._
{{%/pageinfo%}}




And here the code for building such a DFA for any sequence:

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
private void dfa2(BoolVar[] cells, int[] seq, Model model) {
    FiniteAutomaton auto = new FiniteAutomaton();
    int si = auto.addState();
    auto.setInitialState(si); // declare it as initial state
    int i0 = auto.addState();
    auto.addTransition(si, i0, 0); // first transition
    auto.addTransition(i0, i0, 0); // second transition
    int from = i0;
    int m = seq.length;
    for (int i = 0; i < m; i++) {
        int ii = auto.addState();
        // word can start with '1'
        if(i == 0){
            auto.addTransition(si, ii, 1);
        }
        auto.addTransition(from, ii, 1);
        from = ii;
        for(int j = 1; j < seq[i]; j++){
            int jj = auto.addState();
            auto.addTransition(from, jj, 1);
            from = jj;
        }
        int ii0 = auto.addState();
        auto.addTransition(from, ii0, 0);
        auto.addTransition(ii0, ii0, 0);
        // the word can end with '1' or '0'
        if(i == m - 1){
            auto.setFinal(from, ii0);
        }
        from = ii0;
    }
    model.regular(cells, auto).post();
}
{{< /tab >}}
{{< /tabpane >}}

{{%alert title="Info" color="primary"%}}
Any regexp can be transformed into a DFA and conversely.
But, most of the time the constructive way is more convenient.
{{%/alert%}}

The entire code
---------------

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// number of columns
int N = 15;
// number of rows
int M = 15;
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

Model model = new Model("Nonogram");
// Variables declaration
BoolVar[][] cells = model.boolVarMatrix("c", N, M);
// Constraint declaration
// one regular per row
for (int i = 0; i < M; i++) {
    dfa(cells[i], BLOCKS[0][i], model);
}
for (int j = 0; j < N; j++) {
    dfa(ArrayUtils.getColumn(cells, j), BLOCKS[1][j], model);
}
if(model.getSolver().solve()){
    for (int i = 0; i < cells.length; i++) {
        System.out.printf("\t");
        for (int j = 0; j < cells[i].length; j++) {
            System.out.printf(cells[i][j].getValue() == 1 ? "#" : " ");
        }
        System.out.printf("\n");
    }
}
{{< /tab >}}
{{< /tabpane >}}

Things to remember
------------------

-   Regular constraint constructs valid fix-sized words on the basis of
    a vocabulary and a grammar.
-   A deterministic finite automaton can either be build with a regular
    expression or step-by-step.
-   Regular constraints are very useful when patterns occur in
    solutions. For example, when dealing with shifts on a personnal
    scheduling problem: for example: "a nurse doesn't do a late night
    shift followed by a day shift the next day".