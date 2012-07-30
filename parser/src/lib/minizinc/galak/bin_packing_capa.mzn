%-----------------------------------------------------------------------------%
% Requires that each item i weight w[i], be put into bin[i] such
% that the sume of the weights in each bin b does not exceed capacity c[b].
% Assumptions:
% - forall i, w[i] >= 0
% - forall b, c[b] >= 0
%-----------------------------------------------------------------------------%

predicate bin_packing_capa(array[int] of int: c,
                           array[int] of var int: bin,
                           array[int] of int: w) =
    assert(index_set(bin) = index_set(w),
        "bin_packing_capa: the bin and weight arrays must have identical index sets",
    assert(lb_array(w) >= 0,
        "bin_packing_capa: the weights must be non-negative",
    assert(lb_array(c) >= 0,
        "bin_packing_capa: the capacities must be non-negative",
    
      forall( i in index_set(bin) ) (
            min(index_set(c)) <= bin[i] /\ bin[i] <= max(index_set(c))
      )
    /\
      forall( b in index_set(c) ) (
            c[b] >= sum ( i in index_set(bin) ) (
                w[i] * bool2int( bin[i] = b )
            )
      )
    )));

%-----------------------------------------------------------------------------%
