predicate value_precede_int(int: s, int: t, array[int] of var int: x) =
    let {
        int: imin = min(index_set(x)),
        int: imax = max(index_set(x)),
        array[imin..imax+1] of var bool: b
    } in (
        forall (i in imin..imax) (let {
            var bool: xis = (x[i] == s)
        } in
            (xis -> (b[i+1] == true))
        /\  ((not xis) -> (b[i] == b[i+1]))
        /\  ((not b[i]) -> (x[i] != t))
        )
    /\  b[imin] == false
    );
