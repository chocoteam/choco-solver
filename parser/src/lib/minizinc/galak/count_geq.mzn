%-----------------------------------------------------------------------------%
% Constrains 'c' to be greater than or equal to the number of occurrences of
% 'y' in 'x'.
%-----------------------------------------------------------------------------%

predicate count_geq(array[int] of var int: x, var int: y, var int: c) =
    c >= sum(i in index_set(x)) ( bool2int(x[i] == y) );
%countGqChoco(x, y, c);

%predicate countGqChoco(array[int] of var int: x, var int: y, var int: c);

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
