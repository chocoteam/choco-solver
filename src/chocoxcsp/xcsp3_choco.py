import argparse
from subprocess import call

__author__ = 'kyzrsoze'


## CMD LINE ARGUMENT
parser = argparse.ArgumentParser(description='Solve a XCSP3 instance with Choco.')
parser.add_argument(
    "file",
    help='Path of the XCSP3 file to treat.'
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

m2path="~/.m2"
pversion="4.0.2-SNAPSHOT"

parser.add_argument(
    "-cp", "--classpath",
    help='Classpath for Choco (choco-parsers and choco-solver)',
    default='.:%s/repository/org/choco-solver/choco-parsers/%s/choco-parsers-%s-with-dependencies.jar'
            % (m2path,pversion,pversion),
)

args = parser.parse_args()

dargs = ''
if args.a is True:
    dargs += ' -a'
if args.f is True:
    dargs += ' -f'

cmd = 'java %s -cp %s org.chocosolver.parser.xcsp.ChocoXCSP -tl %s -p %s %s %s %s' \
      % (args.jargs, args.classpath, args.timelimit, args.p, dargs, args.cargs, args.file)
print(cmd)
call(cmd, shell=True)
