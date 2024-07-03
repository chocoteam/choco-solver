# How to create Docker image from Dockerfile


The Dockerfile named `Dockerfile.dms` is used to create a Docker image from local files.
Run the following command:

```bash
docker build -f ./Dockerfile.dms -t chocoteam/choco-solver-mzn:<tag> <path-to-choco-solver>
```

where 
- `<tag>` is the tag of the Docker image (e.g. `4.10.0`)
- `<path-to-choco-solver>` is the path to the local repository of choco-solver (relative or absolute).

Then you can run the Docker image with the following command:

```bash
docker run --rm chocoteam/choco-solver-mzn:<tag> minizinc fd.mpc /minizinc/test.mzn /minizinc/2.dzn
```

