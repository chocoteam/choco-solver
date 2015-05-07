__author__ = 'kyzrsoze'

import subprocess
import multiprocessing
import argparse
import os


def work(args):
    cmd,log,to = args
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
            '/Users/kyzrsoze/.m2/repository/choco/choco-solver/3.2.1-SNAPSHOT/choco-solver-3.2.1-SNAPSHOT-jar-with-dependencies.jar:'
            '/Users/kyzrsoze/.m2/repository/choco/choco-parsers/3.2.1-SNAPSHOT/choco-parsers-3.2.1-SNAPSHOT.jar',
)
parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of flatzinc files to solve.',
    default='/Users/kyzrsoze/Sandbox/fzn/fzn2014.txt'
)
parser.add_argument(
    "-d", "--directory",
    help="Flatzinc files directory.",
    default='/Users/kyzrsoze/Sandbox/fzn/choco/'
)
parser.add_argument(
    "-o", "--outputdirectory",
    help="Output files directory.",
    default='/Users/kyzrsoze/Sandbox/fzn/logs/'
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
    default=['fixed:']
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
cmd = 'java %s -cp %s parser.flatzinc.ChocoFZN -tl %s %s %s'
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
                 args.outputdirectory + fznfile + '+' + cc[0] + '.log',
                 args.timelimit
                )
            )
# run commands
pool = multiprocessing.Pool(args.process)
pool.map(
    work,
    commands
)