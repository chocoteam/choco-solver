%-----------------------------------------------------------------------------%
% Requires that each item i with weight w[i], be put into bin[i] such that the
% sum of the weights of the items in each bin does not exceed the capacity c.
% Assumptions:
% - forall i, w[i] >=0
% - c >=0
%-----------------------------------------------------------------------------%

predicate fzn_bin_packing(int: c, array[int] of var int: bin, array[int] of int: w) =
    bin_packingChoco(c,bin,w);

%-----------------------------------------------------------------------------%
