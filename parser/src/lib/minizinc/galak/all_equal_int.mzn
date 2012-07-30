predicate all_equal_int(array[int] of var int: x) =
	forall(i, j in index_set(x) where i < j) ( x[i] = x[j] );
