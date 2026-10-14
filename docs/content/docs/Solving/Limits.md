---
title: "Limits"
date: 2020-03-06T17:54:10+01:00
weight: 35
description: >
  How to limit exploration?
---


## Built-in search limits

Search can be limited in various ways using the `Solver` (from `model.getSolver()`).


* `limitTime` stops the search when the given time limit has been reached. This is the most common limit, as many applications have a limited available runtime.

* `limitSolution` stops the search when the given solution limit has been reached.


* `limitNode` stops the search when the given search node limit has been reached.


* `limitFail` stops the search when the given fail limit has been reached.


* `limitBacktrack` stops the search when the given backtrack limit has been reached.


{{%alert title="Info"%}}
The potential search interruption occurs at the end of a propagation, i.e. it will not interrupt a propagation algorithm, so the overall runtime of the solver might exceed the time limit.
{{%/alert%}}


For instance, to interrupt search after 10 seconds:

```java
Solver s = model.getSolver();
s.limitTime("10s");
model.getSolver().solve();
```

### Custom search limits

You can design you own search limit by implementing a `Criterion` and using `resolver.limitSearch(Criterion c)`:

```java
Solver s = model.getSolver();
s.limitSearch(new Criterion() {
    @Override
    public boolean isMet() {
        // todo return true if you want to stop search
    }
});
```

In Java 8, this can be shortened using lambda expressions:

```java
Solver s = model.getSolver();
s.limitSearch(() -> { /*todo return true if you want to stop search*/ });
```
