%-----------------------------------------------------------------------------%
% Requires exactly 'n' variables in 'x' to take one of the values in 'v'.
%-----------------------------------------------------------------------------%
predicate fzn_among(var int: n, array[int] of var int: x, set of int: v) =
    amongChoco(n,x,v);

%-----------------------------------------------------------------------------%
