## How-to: run a sample from the samples java archive ##

In the following, replace `/path/to` with the correct path, and `.Y.Z` with the version of Choco ;)

1. Create a directory named choco-samples/

    $ mkdir choco-samples

2. Move choco-samples-X.Y.Z-sources.jar in it and rename it choco-samples-X.Y.Z-sources.zip

    $ mv /path/to/choco-samples-X.Y.Z-sources.jar /path/to/choco-samples/choco-samples-X.Y.Z-sources.zip

3. Unzip choco-samples-X.Y.Z-sources.zip into src

    $ cd /path/to/choco-samples/

    $ unzip choco-samples-X.Y.Z-sources.zip -d src

4. Create two additionnal directories: lib/ and classes/

    $ mkdir lib classes

5. Move choco-solver-X.Y.Z-jar-with-dependencies.jar to lib/

    $ mv /path/to/choco-solver-X.Y.Z-jar-with-dependencies.jar /path/to/choco-samples/lib/

6. Compile the class, for instance Alpha.java

    $ javac -d classes -sourcepath src -cp lib/choco-solver-X.Y.Z-jar-with-dependencies.jar src/samples/integer/Photo.java

7. Run the execution

    $ java -cp classes:lib/choco-solver-X.Y.Z-jar-with-dependencies.jar samples.integer.Photo


Note that some samples may need parameters, simple add them at the end of the instruction 7.

===================
The Choco3 dev team.