include "among.mzn";
%-----------------------------------------------------------------------------%
% Requires exactly 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

predicate exactly_int(int: n, array[int] of var int: x, int: v) =
    %n == sum(i in index_set(x)) ( bool2int(x[i] == v) );
    exactlyChoco(n,x,v);

predicate exactlyChoco(int: n, array[int] of var int: x, int: v);