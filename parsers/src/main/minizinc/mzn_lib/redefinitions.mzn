% This file is intentionally empty since there are no standard redefinitions
% of FlatZinc built-ins.  The file has to exist because globals.mzn includes
% it.

predicate array_bool_and_imp(array [int] of var bool: as, var bool: r);
predicate array_bool_or_imp(array [int] of var bool: as, var bool: r);
predicate array_bool_xor_imp(array [int] of var bool: as, var bool: r);

predicate bool_and_imp(var bool: a, var bool: b, var bool: r);
predicate bool_clause_imp(array [int] of var bool: as,
                          array [int] of var bool: bs, var bool: r);
predicate bool_ge_imp(var bool: a, var bool: b, var bool: r);
predicate bool_gt_imp(var bool: a, var bool: b, var bool: r);
predicate bool_le_imp(var bool: a, var bool: b, var bool: r);
predicate bool_lt_imp(var bool: a, var bool: b, var bool: r);
predicate bool_eq_imp(var bool: a, var bool: b, var bool: r);
predicate bool_ne_imp(var bool: a, var bool: b, var bool: r);
predicate bool_or_imp(var bool: a, var bool: b, var bool: r);
predicate bool_xor_imp(var bool: a, var bool: b, var bool: r);

predicate bool_lin_eq_imp(array [int] of int: as, array [int] of var bool: bs,
                          var int: c, var bool: r);
predicate bool_lin_ge_imp(array [int] of int: as, array [int] of var bool: bs,
                          var int: c, var bool: r);
predicate bool_lin_gt_imp(array [int] of int: as, array [int] of var bool: bs,
                          var int: c, var bool: r);
predicate bool_lin_le_imp(array [int] of int: as, array [int] of var bool: bs,
                          var int: c, var bool: r);
predicate bool_lin_lt_imp(array [int] of int: as, array [int] of var bool: bs,
                          var int: c, var bool: r);
predicate bool_lin_ne_imp(array [int] of int: as, array [int] of var bool: bs,
                          var int: c, var bool: r);

predicate int_eq_imp(var int: a, var int: b, var bool: r);
predicate int_ge_imp(var int: a, var int: b, var bool: r);
predicate int_gt_imp(var int: a, var int: b, var bool: r);
predicate int_le_imp(var int: a, var int: b, var bool: r);
predicate int_lt_imp(var int: a, var int: b, var bool: r);
predicate int_ne_imp(var int: a, var int: b, var bool: r);

predicate int_lin_eq_imp(array [int] of int: as, array [int] of var int: bs,
                         int: c, var bool: r);
predicate int_lin_ge_imp(array [int] of int: as, array [int] of var int: bs,
                         int: c, var bool: r);
predicate int_lin_gt_imp(array [int] of int: as, array [int] of var int: bs,
                         int: c, var bool: r);
predicate int_lin_le_imp(array [int] of int: as, array [int] of var int: bs,
                         int: c, var bool: r);
predicate int_lin_lt_imp(array [int] of int: as, array [int] of var int: bs,
                         int: c, var bool: r);
predicate int_lin_ne_imp(array [int] of int: as, array [int] of var int: bs,
                         int: c, var bool: r);

predicate set_in_imp(var int: x, set of int: S, var bool: r);