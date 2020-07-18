% This file is intentionally empty since there are no standard redefinitions
% of FlatZinc built-ins.  The file has to exist because globals.mzn includes
% it.

predicate fzn_alldifferentChoco(array[int] of var int: x);
predicate fzn_alldifferentBut0Choco(array[int] of var int: x);

predicate fzn_amongChoco(var int: n, array[int] of var int: x, set of int: v);
predicate fzn_atleastChoco(int: n, array[int] of var int: x, int: v);
predicate fzn_atmostChoco(int: n, array[int] of var int: x, int: v);
predicate fzn_circuitChoco(array[int] of var int: x);
predicate fzn_count_eq_reif_choco(array[int] of var int: x, var int: y, var int: c, var bool: b);
predicate fzn_count_eqchoco(array[int] of var int: x, var int: y, var int: c);
predicate fzn_cumulativeChoco(array[int] of var int: s, array[int] of var int: d, array[int] of var int: r, var int: b);
predicate fzn_diffnChoco(array[int] of var int: x, array[int] of var int: y,array[int] of var int: dx, array[int] of var int: dy);
predicate fzn_distributeChoco(array[int] of var int: card,
                     array[int] of var int: value,
                     array[int] of var int: base);
predicate fzn_exactlyChoco(int: n, array[int] of var int: x, int: v);
predicate fzn_geostChoco(int : k, array[int,int] of int : rect_size , array[int,int] of int : rect_offset,
                array[int] of set of int : shape, array[int,int] of var int: x, array[int] of var int : kind);
predicate fzn_globalCardinalityChoco(array[int] of var int: x,
                    array[int] of int: cover,
                    array[int] of var int: counts,
                    bool: closed);
predicate fzn_globalCardinalityLowUpChoco(array[int] of var int: x,
                    array[int] of int: cover,
                    array[int] of int: lbound,
                    array[int] of int: ubound,
                    bool: closed);
predicate fzn_inverseChoco(array[int] of var int: f,
                  array[int] of var int: invf);
predicate fzn_knapsackChoco(array[int] of int: w, array[int] of int:p,
                 array[int] of var int:x, var int: W, var int: P);
predicate fzn_lex2Choco(array[int, int] of var int: x, bool: strict);
predicate fzn_lex_lessChoco(array[int] of var bool: x, array[int] of var bool: y, bool: strict);
predicate fzn_lex_lessChoco(array[int] of var int: x, array[int] of var int: y, bool: strict);
predicate fzn_maximumChoco(var int: m, array[int] of var int: x);
predicate fzn_memberChoco(array[int] of int: x, var int: y);
predicate fzn_memberVarChoco(array[int] of var int: x, var int: y);
predicate fzn_memberReifChoco(array[int] of int: x, var int: y, var bool: b);
predicate fzn_memberVarReifChoco(array[int] of var int: x, var int: y, var bool: b);
predicate fzn_minimumChoco(var int: m, array[int] of var int: x);
predicate fzn_nvalueChoco(var int: n, array[int] of var int: x);
predicate fzn_regularChoco(array[int] of var int: x, int: Q, int: S,
                    array[int,int] of int: d, int: q0, set of int: F);
predicate fzn_sortChoco(array[int] of var int: x, array[int] of var int: y);
predicate fzn_subcircuitChoco(array[int] of var int: x);
predicate fzn_tableChoco(array[int] of var bool: x, array[int, int] of bool: t);
predicate fzn_tableChoco(array[int] of var int: x, array[int, int] of int: t);
predicate fzn_bin_packingChoco(int: c, array[int] of var int: bin, array[int] of int: w);
predicate fzn_bin_packing_loadChoco(array[int] of var int: load, array[int] of var int: bin, array[int] of int: w);
predicate fzn_bin_packing_capaChoco(array[int] of int: c, array[int] of var int: bin, array[int] of int: w);
predicate fzn_value_precede_chain_intChoco(array[int] of int: c, array[int] of var int: x);