%-----------------------------------------------------------------------------%
% Requires that 'y' is the ith element of the array 'x'.
%-----------------------------------------------------------------------------%

include "element_bool.mzn";
include "element_float.mzn";
include "element_int.mzn";
include "element_set.mzn";

predicate element(var int: i, array[int] of var bool: x, var bool: y) =
    element_bool(i, x, y);

predicate element(var int: i, array[int] of var float: x, var float: y) =
    element_float(i, x, y);

predicate element(var int: i, array[int] of var int: x, var int: y) =
    element_int(i, x, y);

predicate element(var int: i, array[int] of var set of int: x,
        var set of int: y) =
    element_set(i, x, y);
