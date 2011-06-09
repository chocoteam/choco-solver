%-----------------------------------------------------------------------------%
% Requires that each item i with weight w[i], be put into bin[i] such that the
% sum of the weights of the items in each bin does not exceed the capacity c.
% Assumptions:
% - forall i, w[i] >=0
% - c >=0
%-----------------------------------------------------------------------------%

predicate bin_packing(int: c,
                      array[int] of var int: bin,
                      array[int] of int: w) =
    assert(index_set(bin) == index_set(w),
        "bin_packing: the bin and weight arrays must have identical index sets",
    assert(lb_array(w) >= 0,
        "bin_packing: the weights must be non-negative",
    assert(c >= 0, "bin_packing: capacity must be non-negative",
        forall( b in lb_array(bin)..ub_array(bin) ) (
            c >= sum ( i in index_set(bin) ) (
                w[i] * bool2int( bin[i] == b )
            )
        )
    )));

%-----------------------------------------------------------------------------%
