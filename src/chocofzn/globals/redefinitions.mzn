% This file is intentionally empty since there are no standard redefinitions
% of FlatZinc built-ins.  The file has to exist because globals.mzn includes
% it.

predicate alldifferentChoco(array[int] of var int: x);
predicate alldifferentBut0Choco(array[int] of var int: x);

predicate amongChoco(var int: n, array[int] of var int: x, set of int: v);
predicate atleastChoco(int: n, array[int] of var int: x, int: v);
predicate atmostChoco(int: n, array[int] of var int: x, int: v);
predicate circuitChoco(array[int] of var int: x);
predicate count_eq_reif_choco(array[int] of var int: x, var int: y, var int: c, var bool: b);
predicate count_eqchoco(array[int] of var int: x, var int: y, var int: c);
predicate cumulativeChoco(array[int] of var int: s, array[int] of var int: d, array[int] of var int: r, var int: b);
predicate diffnChoco(array[int] of var int: x, array[int] of var int: y,array[int] of var int: dx, array[int] of var int: dy);
predicate exactlyChoco(int: n, array[int] of var int: x, int: v);
predicate globalCardinalityChoco(array[int] of var int: x,
                    array[int] of int: cover,
                    array[int] of var int: counts,
                    bool: closed);
predicate globalCardinalityLowUpChoco(array[int] of var int: x,
                    array[int] of int: cover,
                    array[int] of int: lbound,
                    array[int] of int: ubound,
                    bool: closed);
predicate inverseChoco(array[int] of var int: f,
                  array[int] of var int: invf);
predicate lex2Choco(array[int, int] of var int: x, bool: strict);
predicate lex_lessChoco(array[int] of var bool: x, array[int] of var bool: y, bool: strict);
predicate lex_lessChoco(array[int] of var int: x, array[int] of var int: y, bool: strict);
predicate maximumChoco(var int: m, array[int] of var int: x);
predicate minimumChoco(var int: m, array[int] of var int: x);
predicate nvalueChoco(var int: n, array[int] of var int: x);
predicate regularChoco(array[int] of var int: x, int: Q, int: S,
                    array[int,int] of int: d, int: q0, set of int: F);
predicate subcircuitChoco(array[int] of var int: x);
predicate tableChoco(array[int] of var bool: x, array[int, int] of bool: t);
predicate tableChoco(array[int] of var int: x, array[int, int] of int: t);
predicate bin_packingChoco(int: c, array[int] of var int: bin, array[int] of int: w);
predicate bin_packing_loadChoco(array[int] of var int: load, array[int] of var int: bin, array[int] of int: w);
predicate bin_packing_capaChoco(array[int] of int: c, array[int] of var int: bin, array[int] of int: w);