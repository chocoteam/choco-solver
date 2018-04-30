%-----------------------------------------------------------------------------%
% Constrains 'c' to be greater than or equal to the number of occurrences of
% 'y' in 'x'.
%-----------------------------------------------------------------------------%

predicate count_geq(array[int] of var int: x, var int: y, var int: c) =
    let {
        int: n = length(x),
        var 0..n : w
    }
    in (count_eq(x,y,w) /\ w >= c);
