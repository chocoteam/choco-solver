%-----------------------------------------------------------------------------%
% Requires that the sum of cs[i1]...cs[iN] equals 's', where i1...iN are the
% elements of the 'i'th set in 'sets'.
%
% Nb: not called 'sum' as in the constraints catalog because 'sum' is a
% MiniZinc built-in function, and we cannot overload a name as both a
% function and a predicate.
%-----------------------------------------------------------------------------%

predicate sum_pred(var int: i, array[int] of set of int: sets,
                   array[int] of int: cs, var int: s) =
    s == sum(j in index_set(cs)) ( bool2int(j in sets[i]) * cs[j] );
