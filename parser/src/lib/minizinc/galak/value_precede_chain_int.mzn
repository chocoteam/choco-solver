include "value_precede.mzn";

predicate value_precede_chain_int(array[int] of int: c, array[int] of var int: x) =
    forall (i in min(index_set(c)) + 1 .. max(index_set(c))) (
        value_precede(c[i - 1], c[i], x)
    );
