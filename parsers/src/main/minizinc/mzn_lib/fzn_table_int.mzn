%-----------------------------------------------------------------------------%
% A table constraint: table(x, t) represents the constraint x in t where we
% consider each row in t to be a tuple and t as a set of tuples.
%-----------------------------------------------------------------------------%

predicate fzn_table_int(array[int] of var int: x, array[int, int] of int: t) =
    tableChoco(x,t);
