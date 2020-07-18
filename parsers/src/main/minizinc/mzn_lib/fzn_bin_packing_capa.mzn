%-----------------------------------------------------------------------------%
% Requires that each item i weight w[i], be put into bin[i] such
% that the sume of the weights in each bin b does not exceed capacity c[b].
% Assumptions:
% - forall i, w[i] >= 0
% - forall b, c[b] >= 0
%-----------------------------------------------------------------------------%

predicate fzn_bin_packing_capa(array[int] of int: c,
                           array[int] of var int: bin,
                           array[int] of int: w) =
    bin_packing_capaChoco(c,bin,w);

%-----------------------------------------------------------------------------%
