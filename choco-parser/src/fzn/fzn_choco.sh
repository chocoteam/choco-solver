#!/bin/sh

STOP_AT_FIRST="yes"
FREE_SEARCH="no"
NB_NODES=1
TIME_LIMIT=900000
ENGINE=-1
CSV=../out.csv
JAVA_ARGS="-Xss64m -Xms64m -Xmx4096m"

usage="\

Usage: fzn_choco.sh [<options>] [<jar>] [<file>]

    Parse and solve <file> using Choco <jar>.

OPTIONS:

    -h, --help
        Display this message.

    -a
        This causes the solver to search for, and output all solutions.
        When this option is not given the solver should search for, and output the first solution.

    -f
        When invoked with this option the solver ignores any specified search strategy.

    -p
        NOT YET AVAILABLE
        When invoked with this option the solver is free to use multiple threads and/or cores during search.
        The argument n specifies the number of cores that are available.  (The default is $NB_NODES.)

    -e, --engine <e>
        Specify the type of propagation engine e to use.

    --csv <filepath>
        Specifiy an CSV file to trace the result

    --time-limit <n>
        Limit the resolution time of each problem instance to n seconds.  (The default is $TIME_LIMIT.)
		
	--jargs <args>
		Override default java argument (\"-Xss64m -Xms64m -Xmx4096m -server\")
		
EXAMPLES:
	
	Basic command to solve a fzn model with choco:
	$> fzn_choco.sh ./choco-parser-13.03-jar-with-dependencies.jar ./alpha.fzn

	Additionnal arguments:
	$> fzn_choco.sh --jargs \"-Xmx128m\" --time-limit 100 \\ 
		./choco-parser-13.03-jar-with-dependencies.jar ./alpha.fzn

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

        --time-limit)
            TIME_LIMIT="$2"
            shift
        ;;

        -e|--engine)
            ENGINE="$2"
            shift
        ;;

        --csv)
            CSV="$2"
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

CHOCO_JAR=$1
FILE=$2


if test $# -eq 0
then
    echo "%% No flatzinc file found"
    exit 1
else
    ARGS="$FILE -tl $TIME_LIMIT -p $NB_NODES -e $ENGINE -csv $CSV"
fi

if test "$STOP_AT_FIRST" = "no"
then
    echo "%% STOP_AT_FIRST"
    ARGS=$ARGS" -a"
fi

if test "$FREE_SEARCH" = "yes"
then
    echo "%% FREE_SEARCH"
    ARGS=$ARGS" -i"
fi

java ${JAVA_ARGS} -cp .:${CHOCO_JAR} parser.flatzinc.ParseAndSolve ${ARGS}
