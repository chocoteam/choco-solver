%-----------------------------------------------------------------------------%
% Requires that 'card[i]' is the number of occurences of 'value[i]' in 'base'.
%
% XXX: currently the values in 'value' need not be distinct.  Perhaps they
%      should be?
%-----------------------------------------------------------------------------%
predicate distribute(array[int] of var int: card,
                     array[int] of var int: value,
                     array[int] of var int: base) =
    assert(index_set(card) == index_set(value),
        "distribute: card and value arrays must have identical index sets",
        forall (i in index_set(card)) (
            card[i] == sum(j in index_set(base)) (
                            bool2int(value[i] = base[j])
                       )
        )
    );

