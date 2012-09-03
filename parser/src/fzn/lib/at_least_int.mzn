%-----------------------------------------------------------------------------%
% Requires at least 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

predicate at_least_int(int: n, array[int] of var int: x, int: v) =
    %sum(i in index_set(x)) ( bool2int(x[i] == v) ) >= n;
    atleastChoco(n,x,v);


predicate atleastChoco(int: n, array[int] of var int: x, int: v);