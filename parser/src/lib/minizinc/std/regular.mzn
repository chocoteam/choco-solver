%-----------------------------------------------------------------------------%
% The sequence of values in array 'x' (which must all be in the range 1..S)
% is accepted by the DFA of 'Q' states with input 1..S and transition
% function 'd' (which maps (1..Q, 1..S) -> 0..Q)) and initial state 'q0'
% (which must be in 1..Q) and accepting states 'F' (which all must be in
% 1..Q).  We reserve state 0 to be an always failing state.
%-----------------------------------------------------------------------------%
predicate regular(array[int] of var int: x, int: Q, int: S,
                  array[int,int] of int: d, int: q0, set of int: F) =
    assert(Q > 0,
        "regular: 'Q' must be greater than zero",

    assert(S > 0,
        "regular: 'S' must be greater than zero",

    assert(index_set_1of2(d) = 1..Q /\ index_set_2of2(d) == 1..S,
        "regular: the transition function 'd' must be [1..Q,1..S]",

    assert(forall([d[i, j] in 0..Q | i in 1..Q, j in 1..S]),
        "regular: transition function 'd' points to states outside 0..Q",

        % Nb: we need the parentheses around the expression otherwise the
        % parser thinks it's a generator call!
    assert((q0 in 1..Q),
        "regular: start state 'q0' not in 1..Q",

    assert(F subset 1..Q,
        "regular: final states in 'F' contain states outside 1..Q",

        let {
            % If x has index set m..n-1, then a[m] holds the initial state
            % (q0), and a[i+1] holds the state we're in after  processing
            % x[i].  If a[n] is in F, then we succeed (ie. accept the string).
              int: m = min(index_set(x)),
              int: n = max(index_set(x)) + 1,
              array[m..n] of var 1..Q: a
            } in
        a[m] = q0 /\                    % Set a[0].
        forall(i in index_set(x)) (
            x[i] in 1..S /\             % Do this in case it's a var.
            a[i+1] = d[a[i], x[i]]      % Determine a[i+1].
        ) /\
        a[n] in F                       % Check the final state is in F.

     ))))));
