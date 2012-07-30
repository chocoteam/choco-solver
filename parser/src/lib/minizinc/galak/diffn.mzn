%-----------------------------------------------------------------------------%
% diffn: constrains rectangles, given by their origins and sizes, to be
% non-overlapping
%-----------------------------------------------------------------------------%

predicate diffn(array[int] of var int: x,
                array[int] of var int: y,
                array[int] of var int: dx,
                array[int] of var int: dy) =
    assert(
        index_set(x) = index_set(y)  /\
        index_set(x) = index_set(dx) /\
        index_set(x) = index_set(dy),
        "diffn: index set mismatch",
    forall(i,j in index_set(x) where i < j)(
        x[i] + dx[i] <= x[j] \/ y[i] + dy[i] <= y[j] \/
        x[j] + dx[j] <= x[i] \/ y[j] + dy[j] <= y[i]
    )
    );

%-----------------------------------------------------------------------------%
