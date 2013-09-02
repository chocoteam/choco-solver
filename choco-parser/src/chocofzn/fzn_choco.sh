#!/bin/sh

STOP_AT_FIRST="yes"
FREE_SEARCH="no"
NB_NODES=1
TIME_LIMIT=900000
JAVA_ARGS="-server"
CHOCO_JAR=/Users/jfages07/Documents/code/Choco3/choco-parser/target/choco-parser-13.04-SNAPSHOT-jar-with-dependencies.jar
usage="\

Usage: fzn_choco.sh [<options>] [<file>]

    Parse and solve <file> using Choco.

OPTIONS:

    -h, --help
        Display this message.

    -a
        This causes the solver to search for, and output all solutions.
        When this option is not given the solver should search for, and output the first solution.

    -f
        When invoked with this option the solver ignores any specified search strategy.

    -p
        When invoked with this option the solver is free to use multiple threads and/or cores during search.
        The argument n specifies the number of cores that are available.  (The default is $NB_NODES.)

    -tl <n>
        Limit the resolution time of each problem instance to n ms.  (The default is $TIME_LIMIT.)

    -jar <j>
        Override the jar file.  (The default is JAR.)


    --jargs <args>
		Override default java argument (\"-Xss64m -Xms64m -Xmx4096m -server\")
		
EXAMPLES:
	
	Basic command to solve a fzn model with choco:
	$> fzn_choco.sh ./choco.jar ./alpha.fzn

	Additionnal arguments:
	$> fzn_choco.sh --jargs \"-Xmx128m\" -tl 100 ./choco.jar ./alpha.fzn

"

while test $# -gt 0
do

    case "$1" in

        -h|--help)
            echo "$usage"
            exit 0
        ;;

        -a)
            STOP_AT_FIRST="no"
        ;;

        -f)
            FREE_SEARCH="yes"
        ;;

        -p)
            NB_NODES="$2"
            shift
        ;;

        -tl)
            TIME_LIMIT="$2"
            shift
        ;;

        -jar)
            CHOCO_JAR="$2"
            shift
        ;;

    	--jargs)
            JAVA_ARGS="$2"
            shift
        ;;

        -*)
            echo "$0: unknown option \`$1'" 1>&2
            echo "$usage" 1>&2
            exit 2
        ;;

        *)
            break
        ;;

    esac
    shift
done

#CHOCO_JAR=$1
FILE=$1


if test $# -eq 0
then
    echo "%% No flatzinc file found"
    exit 1
else
    ARGS="$FILE -tl $TIME_LIMIT -p $NB_NODES"
fi

if test "$STOP_AT_FIRST" = "no"
then
    ARGS=$ARGS" -a"
fi

if test "$FREE_SEARCH" = "yes"
then
    echo "%% FREE_SEARCH"
    ARGS=$ARGS" -i"
fi

java ${JAVA_ARGS} -cp .:${CHOCO_JAR} parser.flatzinc.ChocoFZN ${ARGS}
