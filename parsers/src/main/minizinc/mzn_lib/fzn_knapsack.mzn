/**  @group globals.packing
Requires that items are packed in a knapsack with certain weight and profit restrictions.

Assumptions:
  - Weights \a w and profits \a p must be non-negative
  - \a w, \a p and \a x must have the same index sets

@param w: weight of each type of item
@param p: profit of each type of item
@param x: number of items of each type that are packed
@param W: sum of sizes of all items in the knapsack
@param P: sum of profits of all items in the knapsack
*/

predicate fzn_knapsack(array[int] of int: w, array[int] of int: p, array[int] of var int: x, var int: W,var int: P) =
	knapsackChoco(w, p, x, W, P);