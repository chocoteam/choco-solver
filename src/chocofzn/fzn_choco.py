import argparse
from subprocess import call

__author__ = 'kyzrsoze'


## CMD LINE ARGUMENT
parser = argparse.ArgumentParser(description='Solve a flatzinc instance with Choco.')
parser.add_argument(
    "file",
    help='Path of the Flatzinc file to treat.'
)
parser.add_argument(
    "-a",
    help='This causes the solver to search for and output all solutions.\n'
         'When this option is not given the solver should search for, and output the first solution '
         'or the best known one.',
    action='store_true',
    default=False
)
parser.add_argument(
    "-f",
    help="When invoked with this option the solver ignores any specified search strategy.",
    action='store_true',
    default=False
)
parser.add_argument(
    "-p",
    help="When invoked with this option the solver is free to use multiple threads and/or cores during search.\n"
         "The argument n specifies the number of cores that are available.",
    type=int,
    default=1,
)
parser.add_argument(
    "-tl", "--timelimit",
    metavar='T',
    help='Limit the resolution time of each problem instance to <n> seconds.',
    type=int,
    default=900
)
parser.add_argument(
    "-jargs",
    help='Java Virtual Machine optional arguments',
    default='-Xss64m -Xms64m -Xmx4096m -server'
)

parser.add_argument(
    "-cargs",
    help='Choco optional arguments',
    default=''
)
parser.add_argument(
    "-cp", "--classpath",
    help='Classpath for Choco (choco-parsers and choco-solver)',
    default='.:'
            '/Users/kyzrsoze/.m2/repository/choco/choco-solver/3.2.1-SNAPSHOT/choco-solver-3.2.1-SNAPSHOT-jar-with-dependencies.jar:'
            '/Users/kyzrsoze/.m2/repository/choco/choco-parsers/3.2.1-SNAPSHOT/choco-parsers-3.2.1-SNAPSHOT.jar',
)

args = parser.parse_args()

dargs = ''
if args.a is True:
    dargs += ' -a'
if args.f is True:
    dargs += ' -f'

cmd = 'java %s -cp %s parser.flatzinc.ChocoFZN -tl %s -p %s %s %s %s' \
      % (args.jargs, args.classpath, args.timelimit, args.p, dargs, args.cargs, args.file)
print(cmd)
call(cmd, shell=True)
