%-----------------------------------------------------------------------------%
% Requires at most 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

predicate at_most_int(int: n, array[int] of var int: x, int: v) =
    %sum(i in index_set(x)) ( bool2int(x[i] == v) ) <= n;
    atmostChoco(n,x,v);

predicate atmostChoco(int: n, array[int] of var int: x, int: v);