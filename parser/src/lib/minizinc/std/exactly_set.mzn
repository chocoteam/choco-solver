%-----------------------------------------------------------------------------%
% Requires exactly 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

predicate exactly_set(int: n, array[int] of var set of int: x, set of int: v) =
    n == sum(i in index_set(x)) ( bool2int(x[i] == v) );

