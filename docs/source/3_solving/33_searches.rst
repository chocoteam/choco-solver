Search Strategies
=================

If no search strategy is specified in the model, Choco |version| will generate a default one. In many cases, this strategy will not be sufficient to produce satisfying performances and it will be necessary to specify a dedicated strategy, using ``solver.set(…)``.

``IntStrategyFactory`` offers several built-in search strategies and a simple framework to build custom searches. 