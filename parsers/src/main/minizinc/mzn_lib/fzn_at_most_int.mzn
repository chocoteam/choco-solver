%-----------------------------------------------------------------------------%
% Requires at most 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

predicate fzn_at_most_int(int: n, array[int] of var int: x, int: v) =
    atmostChoco(n,x,v);