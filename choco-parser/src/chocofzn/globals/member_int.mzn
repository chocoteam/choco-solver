%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate member_int(array[int] of var int: x, var int: y) =
    let{
        int: n = length(x),
        var 1..n : w
    }in(
        x[w] = y
    );
