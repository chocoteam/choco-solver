%-----------------------------------------------------------------------------%
% Requires that the number of distinct values in 'x' is 'n'.
%-----------------------------------------------------------------------------%
predicate fzn_nvalue(var int: n, array[int] of var int: x) =
    nvalueChoco(n,x);


