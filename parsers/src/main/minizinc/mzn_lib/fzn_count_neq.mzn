%-----------------------------------------------------------------------------%
% Constrains 'c' to not be the number of occurrences of 'y' in 'x'.
%-----------------------------------------------------------------------------%

predicate count_neq(array[int] of var int: x, var int: y, var int: c) =
    let {
        int: n = length(x),
        var 0..n : w
    }
    in (count_eq(x,y,w) /\ w != c);

