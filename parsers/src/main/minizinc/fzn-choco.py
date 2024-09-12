# Compilation mode, support OS-specific options
# nuitka-project: --onefile
# nuitka-project: --remove-output

import argparse
import os
import sys

# THIS IS WHERE YOU NEED TO CHANGE THE PATH TO THE JAR FILE
JAR_FILE='~/.m2/repository/org/choco-solver/choco-parsers/4.10.18-SNAPSHOT/choco-parsers-4.10.18-SNAPSHOT-light.jar'

JVM_ARGS = '-server -Xss64M -Xms2G -Xmx8G -XX:NewSize=512M'
LOG_LEVEL = 'COMPET'
TIME_LIMIT = -1
NB_SOLUTIONS = -1


def parse_id_port(value):
    try:
        id_str, port_str = value[1:-1].split(',')
        id = int(id_str)
        port = int(port_str)
        return id, port
    except ValueError:
        raise argparse.ArgumentTypeError("The cp-profiler argument must be in the form ID,PORT")


# parse arguments and options
parser = argparse.ArgumentParser(
    prog='fzn_choco',
    description='Parse and solve a FlatZinc model using Choco solver')

parser.add_argument('fzn_file', help='flatZinc file to solve')

parser.add_argument('-a', '--all-solutions', action='store_true',
                    help='this causes the solver to search for, and output all solutions. \
                        When this option is not given the solver should search for, \
                        and output the first solution or the best known one.')

parser.add_argument('-f', '--free-search', action='store_true',
                    help='when invoked with this option the solver ignores any specified search strategy.')

parser.add_argument('-p', '--parallel', type=int, default=1,
                    help='when invoked with this option the solver is free to use multiple threads \
                        and/or cores during search. \
                        The argument PARALLEL specifies the number of cores that are available.')

parser.add_argument('-t', '--time-limit', type=int, default=TIME_LIMIT,
                    help='limit the resolution time of each problem instance to TIME_LIMIT ms.')

parser.add_argument('--verbose', action='store_true',
                    help='when invoked with this option verbose solving is activated.')

parser.add_argument('-s', '--stasol', action='store_true',
                    help='when invoked with this option statistics on solutions are activated.')

parser.add_argument('-n', '--number-solutions', type=int, default=NB_SOLUTIONS,
                    help='limit the number of solutions to NUMBER_SOLUTIONS.')

parser.add_argument('-r', '--random-seed', type=int,
                    help='Use RANDOM_SEED as the random seed for the solver.')

parser.add_argument('--cp-profiler', type=parse_id_port,
                    help='enable the cp-profiler with the given id and port.')

parser.add_argument('--jvm-args', type=str,
                    default=JVM_ARGS,
                    help='JVM arguments to pass to the Java Virtual Machine.')

parser.add_argument('--jar-file', type=str,
                    default=JAR_FILE,
                    help='Absolute path to the jar file.')


# Since the id can be negative, we need to surround the argument with parentheses
# Doing so, the argument is not interpreted as an option but as a value
for i in sys.argv[1:]:
    if i == '--cp-profiler':
        sys.argv[sys.argv.index(i) + 1] = "(" + sys.argv[sys.argv.index(i) + 1] + ")"

args = parser.parse_args()

if args.verbose:
    LOG_LEVEL = 'INFO'
if args.time_limit:
    TIME_LIMIT = args.time_limit
if args.number_solutions:
    NB_SOLUTIONS = args.number_solutions

arguments = f'-limit=[{TIME_LIMIT},{NB_SOLUTIONS}sols] -lvl {LOG_LEVEL}'

if args.all_solutions:
    arguments += ' -a'
if args.free_search:
    arguments += ' -f'
if args.parallel:
    arguments += ' -p ' + str(args.parallel)
if args.stasol:
    arguments += ' -stasol'
if args.random_seed:
    arguments += ' -r ' + str(args.random_seed)
if args.cp_profiler:
    arguments += ' --cp-profiler ' + str(args.cp_profiler[0]) + ',' + str(args.cp_profiler[1])

cmd = f'java {args.jvm_args} -cp {args.jar_file} org.chocosolver.parser.flatzinc.ChocoFZN {args.fzn_file} {arguments}'

# if __lvl__ == 'INFO':
#cprint(f'Running command: {cmd}')
os.system(cmd)
