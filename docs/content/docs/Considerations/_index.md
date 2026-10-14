---
author: "Charles Prud'homme"
title: "Considerations"
weight: 1
description: >
  Considerations when using Choco-solver.
---

First of all we would :heart: to hear about your use of Choco-solver : 
 [so please take 5 minutes to tell us about yourself and your project](mailto:choco-solver@imt-atlantique.fr?subject=Contact), we would really appreciate :metal:! 

## In Research

If you use Choco-solver for your research, you may want to know the right way to cite the library. You can do this by citing [Choco-solver: A Java library for constraint programming](https://joss.theoj.org/papers/10.21105/joss.04708#) from [The Journal of Open Source Software](https://joss.theoj.org/):

```latex
@article{Prud'homme2022, 
  doi = {10.21105/joss.04708}, 
  url = {https://doi.org/10.21105/joss.04708}, 
  year = {2022}, 
  publisher = {The Open Journal}, 
  volume = {7}, 
  number = {78}, 
  pages = {4708}, 
  author = {Charles Prud'homme and Jean-Guillaume Fages}, 
  title = {Choco-solver: A Java library for constraint programming},
  journal = {Journal of Open Source Software} 
}
```
 

And add your article to [:arrow_right: this list](https://scholar.google.com/scholar?oi=bibs&hl=fr&cites=4714165071276482389,1869564298759279370,7119725863983437084,531556205983498199) :white_check_mark:.


##  In Production
Technical considerations aside, if you are going to evaluate Choco-solver for production use, it is important to keep the following in mind.

{{% pageinfo %}}

[IMT Atlantique](https://www.imt-atlantique.fr/), holder of the economic rights of Choco-solver, offers dedicated services, ranging from support to expertise. 

For more information, [please contact us](mailto:choco-solver@imt-atlantique.fr?subject=Contact).

{{% /pageinfo %}}

### Licence

Choco is a Free Open-Source Java library distributed under **BSD 3-Clause license** (as of version 6.0.0; earlier versions used BSD 4-Clause). It is therefore important to know the different aspects that make up this licence. It is of course necessary to contact a legal department, but consulting the [Wikipedia page of the licence](https://en.wikipedia.org/wiki/BSD_licenses) is a good start.

### The project

The first version of Choco-solver in Java was released in 2003. Since then, it has undergone several major redesigns, the last of which was in 2013. Since then, the modelling API has been stabilised (i.e. what concerns the declaration of variables and constraints). Occasionally, the signature of certain methods change but rarely without first deprecating the old usages. However, Choco-solver is an active library.

Most bugs are fixed within a few days and patches included in the next release. On average, there are 2 releases per year and the changes made are reported in a file called `CHANGES.md` (which can be found on GitHub).

There are few regular contributors but there are several thousand downloads per month from Maven Central Repository.


### Test coverage

We pay particular attention to testing. They come in two forms: unit and regression tests and performance tests. The latter evaluate the performance of Choco-solver on MiniZinc, XCSP3, MPS and DIMACS instances and evaluate different statistics (time, failures, ...). The tests are executed at each commit and the results are available from GitHub. Regularly, performance is evaluated on a larger set of instances (MiniZinc and XCSP3 mainly), but because of the time consuming nature of this, they have not been automated. 


### Support

There are different ways to get help. Firstly, you should visit the [Community](/community/) page, which lists the different entry points. Of course, this does not guarantee the response time.

