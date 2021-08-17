%-------------------------------------------------------------------------------------------%
% Requires that a set of tasks given by start times s, durations d, and resource requirements r,
% never require more than a global resource bound b at any one time.
% Aborts if s, d, and r do not have identical index sets.
% Aborts if a duration or resource requirement is negative.
%-------------------------------------------------------------------------------------------%

predicate fzn_cumulative(array[int] of var int: s, array[int] of var int: d,
                     array[int] of var int: r, var int: b);
