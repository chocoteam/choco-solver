%-----------------------------------------------------------------------------%
% Constrains 'c' to not be the number of occurrences of 'y' in 'x'.
%-----------------------------------------------------------------------------%

predicate count_neq(array[int] of var int: x, var int: y, var int: c) =
    c != sum(i in index_set(x)) ( bool2int(x[i] == y) );

%countNqChoco(x, y, c);

%predicate countNqChoco(array[int] of var int: x, var int: y, var int: c);

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%

