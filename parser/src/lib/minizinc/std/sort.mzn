include "alldifferent.mzn";
include "increasing.mzn";
%-----------------------------------------------------------------------------%
% Requires that the multiset of values in 'x' are the same as the
% multiset of values in 'y' but 'y' is in sorted order.
%-----------------------------------------------------------------------------%
predicate sort(array[int] of var int: x, array[int] of var int: y) =
    assert(card(index_set(x)) == card(index_set(y)),
        "sort: x and y must be same sized arrays",
        let { int: lx = min(index_set(x)),
              int: ux = max(index_set(x)),
              int: ly = min(index_set(y)),
              int: uy = max(index_set(y)),
              array[lx..ux] of var ly..uy: p } in
        forall(i in index_set(x)) ( y[p[i]] == x[i] ) /\
        alldifferent(p) /\
        increasing(y)
    );

