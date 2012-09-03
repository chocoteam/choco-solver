%-----------------------------------------------------------------------------%
% Constrains the elements of 'x' to define a subcircuit where 'x[i] = j'
% means that 'j' is the successor of 'i' and 'x[i] = i' means that 'i'
% is not in the circuit.
%-----------------------------------------------------------------------------%

include "alldifferent.mzn";

predicate subcircuit(array[int] of var int: x) = 
%    let { set of int: S = index_set(x),
%          int: l = min(S),
%          int: u = max(S),
%          int: n = card(S),
%          array[S] of var 1..n: order,
%          array[S] of var bool: ins = array1d(S,[ x[i] != i | i in S]),
%          var l..u+1: firstin = min([ u+1 + bool2int(ins[i])*(i-u-1) | i in S]),
%          var S: lastin,
%          var bool: empty = (firstin > u),
%    } in
%    alldifferent(x) /\
%    alldifferent(order) /\
%
%    % If the subcircuit is empty then each node points at itself.
%    %
%    (empty -> forall(i in S)(not ins[i])) /\
%
%    % If the subcircuit is non-empty then order numbers the subcircuit.
%    %
%    ((not empty) ->
%
%       % The firstin node is numbered 1.
%       order[firstin] = 1 /\
%
%       % The lastin node is greater than firstin.
%       lastin > firstin /\
%
%       % The lastin node points at firstin.
%       x[lastin] = firstin /\
%
%       % And both are in
%       ins[lastin] /\ ins[firstin] /\
%
%       % The successor of each node except where it is firstin is
%       % numbered one more than the predecessor.
%       forall(i in S) (
%           (ins[i] /\ x[i] != firstin) -> order[x[i]] = order[i] + 1
%       ) /\
%
%       % Each node that is not in is numbered after the lastin node.
%       forall(i in S) (
%           ins[i] \/ order[lastin] < order[i]
%       )
%    );
subcircuitChoco(x);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.

predicate subcircuitChoco(array[int] of var int: x);

%-----------------------------------------------------------------------------%


predicate subcircuit_reif(array[int] of var int: x, var bool: b) = 
    abort("Reified subcircuit/1 is not supported.");

%-----------------------------------------------------------------------------%
