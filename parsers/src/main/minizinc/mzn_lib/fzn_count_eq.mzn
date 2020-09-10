%-----------------------------------------------------------------------------%
% Constrains 'c' to be the number of occurrences of 'y' in 'x'. 
%-----------------------------------------------------------------------------%

predicate fzn_count_eq(array[int] of var int: x, var int: y, var int: c) =
    count_eqchoco(x,y,c);


