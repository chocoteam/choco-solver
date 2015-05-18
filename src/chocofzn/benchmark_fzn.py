__author__ = 'kyzrsoze'

import subprocess
import multiprocessing
import argparse
import os


def work(args):
    cmd, log, to = args
    curproc = multiprocessing.current_process()
    print(curproc, "Started Process, args={}".format(args))
    lfile = open(log, 'w')
    try:
        subprocess.call(
            cmd,
            shell=True,
            stdout=lfile,
            timeout=to,
        )
        print(curproc, "Ended Process")
    except subprocess.TimeoutExpired:
        print(curproc, "Killed Process")

## CMD LINE ARGUMENT
parser = argparse.ArgumentParser(description='Solve flatzinc files.')
parser.add_argument(
    "-cp", "--classpath",
    help='Classpath for Choco (choco-parsers and choco-solver)',
    default='.:'
            '/Users/cprudhom/.m2/repository/org/choco-solver/choco-solver/3.3.1/choco-solver-3.3.1-jar-with-dependencies.jar:'
            '/Users/cprudhom/.m2/repository/org/choco-solver/choco-parsers/3.3.1-SNAPSHOT/choco-parsers-3.3.1-SNAPSHOT-with-dependencies.jar',
)
parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of flatzinc files to solve.',
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/2014/list2014.txt'
)
parser.add_argument(
    "-d", "--directory",
    help="Flatzinc files directory.",
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/2014/'
)
parser.add_argument(
    "-o", "--outputdirectory",
    help="Output files directory.",
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/logs/2014/'
)
parser.add_argument(
    "-p", "--process",
    help='Number of processes to run in parallel',
    type=int,
    default=2
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name:options\'',
    nargs='+',
    default=['fixed:', 'free:-f ', 'wdeg:-bb 1 ', 'ibs:-bb 2 ', 'abs:-bb 3 ',
             'wdeg+lc:-f -bb 1 ', 'ibs+lc:-f -bb 2 ', 'abs+lc:-f -bb 3 ',
             'cbj:-ee 1 ', 'dbt:-ee 3', 'cbj+lc:-f -ee 1 ', 'dbt+lc:-f -ee 3',
	     'cbj+wdeg:-ee 1 -bb 1', 'cbj+wdeg+lc:-ee 1 -bb 1 -f',
             ]
)
parser.add_argument(
    "-tl", "--timelimit",
    help='Time limit in seconds for the resolutions.',
    type=int,
    default=900
)
parser.add_argument(
    "-jargs",
    help='Java Virtual Machine arguments',
    default='-Xss64m -Xms64m -Xmx4096m -server'
)
cmd = 'java %s -cp %s org.chocosolver.parser.flatzinc.ChocoFZN -tl %s %s %s'
args = parser.parse_args()

if not os.path.exists(args.outputdirectory):
    os.makedirs(args.outputdirectory)

# generate commands to run
commands = []
with open(args.filelist, 'r') as f:
    for fznfile in f:
        fznfile = fznfile.replace('\n', '')
        for conf in args.configurations:
            cc = conf.split(':')
            commands.append(
                (cmd % (args.jargs, args.classpath, args.timelimit * 1000, cc[1], args.directory + fznfile),
                 os.path.join(args.outputdirectory, fznfile) + '+' + cc[0] + '.log',
                 args.timelimit
                 )
            )
# run commands
# for cm in commands:
#     print(cm)
pool = multiprocessing.Pool(args.process)
pool.map(
    work,
    commands
)
