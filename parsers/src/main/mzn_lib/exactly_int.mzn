include "among.mzn";
%-----------------------------------------------------------------------------%
% Requires exactly 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

predicate exactly_int(int: n, array[int] of var int: x, int: v) =
    exactlyChoco(n,x,v);