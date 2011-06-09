%-----------------------------------------------------------------------------%
% A table constraint: table(x, t) represents the constraint x in t where we
% consider each row in t to be a tuple and t as a set of tuples.
%-----------------------------------------------------------------------------%

predicate table_int(array[int] of var int: x, array[int, int] of int: t) =
    assert (index_set_2of2(t) == index_set(x),
        "The second dimension of the table must equal the number of variables "
            ++ "in the first argument",
        let { int: l = min(index_set(x)),
              int: u = max(index_set(x)),
              int: lt = min(index_set_1of2(t)),
              int: ut = max(index_set_1of2(t)),
              var lt..ut: i,
              array[l..u, lt..ut] of int: t_transposed =
                  array2d(l..u, lt..ut, [ t[i,j] | j in l..u, i in lt..ut ]) }
        in
            forall(j in l..u) (
                % Having the variable index component at the left position
                % means that the nD-to-1D array translation during Mzn-to-Fzn
                % will generate at most an offset constraint, instead of a
                % scaling + offset constraint.
                %
                t_transposed[j,i] = x[j]
                %
                % t[i,j] = x[j]
            )
    );
