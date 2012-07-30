%-----------------------------------------------------------------------------%
% Constrains 'c' to be strictly less than the number of occurrences of 'y'
% in 'x'.
%-----------------------------------------------------------------------------%

predicate count_lt(array[int] of var int: x, var int: y, var int: c) =
    c < sum(i in index_set(x)) ( bool2int(x[i] == y) );

%countLqChoco(x, y, c-1);

%predicate countLqChoco(array[int] of var int: x, var int: y, var int: c);

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
