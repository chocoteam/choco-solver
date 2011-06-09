%-----------------------------------------------------------------------------%
% Constrains two arrays of set of int variables, 'f' and 'invf', so that
% a 'j in f[i]' iff 'i in invf[j]'.  All the values in each array's sets
% must be within the index set of the other array.
%-----------------------------------------------------------------------------%
predicate inverse_set(array[int] of var set of int: f,
                      array[int] of var set of int: invf) =
    forall(i in index_set(f)) (
           f[i] subset index_set(invf)
    ) /\
    forall(j in index_set(invf)) (
        invf[j] subset index_set(f)
    ) /\
    forall(i in index_set(f), j in index_set(invf)) (
        (j in f[i] <-> i in invf[j])
    );

