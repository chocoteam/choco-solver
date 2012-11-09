%-----------------------------------------------------------------------------%
% Require adjacent rows and adjacent columns in the array 'x' to be
% lexicographically ordered.  Adjacent rows and adjacent columns may be equal.
%-----------------------------------------------------------------------------%

include "lex_lesseq.mzn";

predicate lex2(array[int, int] of var int: x) =
%    let {
%        int: lbx1 = min(index_set_1of2(x)),
%        int: ubx1 = max(index_set_1of2(x)),
%        int: lbx2 = min(index_set_2of2(x)),
%        int: ubx2 = max(index_set_2of2(x))
%    } in (
%
%        forall(i in lbx1 + 1 .. ubx1) (
%            lex_lesseq([x[i - 1, j] | j in index_set_2of2(x)],
%                       [x[i,     j] | j in index_set_2of2(x)]
%            )
%        )
%
%        /\
%
%        forall(j in lbx2 + 1 .. ubx2) (
%            lex_lesseq([x[i, j - 1] | i in index_set_1of2(x)],
%                       [x[i, j    ] | i in index_set_1of2(x)]
%            )
%        )
%    );
lex2Choco(x);

predicate lex2Choco(array[int, int] of var int: x);