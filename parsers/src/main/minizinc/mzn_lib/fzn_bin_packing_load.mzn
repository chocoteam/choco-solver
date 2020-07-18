%-----------------------------------------------------------------------------%
% Requires that each item i with weight w[i], be put into bin[i] such
% that the sum of the weights of the items in each bin b is equal to load[b].
% Assumptions:
% - forall i, w[i] >= 0
%-----------------------------------------------------------------------------%

predicate fzn_bin_packing_load(array[int] of var int: load,
                           array[int] of var int: bin,
                           array[int] of int: w) =
    bin_packing_loadChoco(load,bin,w);

%-----------------------------------------------------------------------------%
