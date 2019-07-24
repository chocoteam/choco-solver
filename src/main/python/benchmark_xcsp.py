__author__ = 'kyzrsoze'

import subprocess
import multiprocessing
import argparse
import os
import time
import utils.worker as worker


parser = argparse.ArgumentParser(description='Solve flatzinc files.')
parser.add_argument(
    "-cp", "--classpath",
    help='Classpath for Choco (choco-parsers and choco-solver)',
    default='.:'
    '/Users/cprudhom/.m2/repository/org/choco-solver/choco-parsers/4.0.5-SNAPSHOT/choco-parsers-4.0.5-SNAPSHOT-with-dependencies.jar'
)

parser.add_argument(
    "-n", "--name",
    help='Benchmark name',
    default='xcps18',
)

parser.add_argument(
    "-fl", "--filelists",
    help='Files containing name of flatzinc files to solve.',
    nargs='+',
    default=[
        '/Users/cprudhom/Nextcloud/50-Choco/XCSP/Challenges/inst/2018/xcsp3-opt.txt',
    ]
)


parser.add_argument(
    "-o", "--outputdirectory",
    help="Output files directory.",
    default='/Users/cprudhom/Nextcloud/50-Choco/XCSP/Challenges/logs/'
)
parser.add_argument(
    "-p", "--process",
    help='Number of processes to run in parallel',
    type=int,
    default=4
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name:options\'',
    nargs='+',
    default=[
        # 'IDL+IBS:-stat',
        # 'CPL+IBS:-stat -bb 5',
        # 'IBS+:-stat -bb 6',
        #'MIXED:-stat -bb 8',
        'PROB:-stat'
    ]
)
parser.add_argument(
    "-tl", "--timelimit",
    help='Time limit in seconds for the resolutions.',
    type=int,
    default=2400
)
parser.add_argument(
    "-jargs",
    help='Java Virtual Machine arguments (eg: -Xss64m -Xms64m -Xmx4096m -server)',
    default='-Xss64m -Xms64m -Xmx4096m -server'
)
parser.add_argument(
    "-print",
    help='Print the command to the console and exit',
    default=False
)

cmd = 'java %s -cp %s org.chocosolver.parser.xcsp.ChocoXCSP -tl %s %s %s'

# start here
args = parser.parse_args()

if args.print:
    print("python3.4 ./benchmark_xcsp.py -cp %s -n %s -fl %s -p %s -c %s -tl %s;" %
        (args.classpath, args.name, args.filelists, args.process, args.configurations, args.timelimit)
          )
    exit(0)

date = time.strftime("%Y%m%d")
ldir = os.path.join(args.outputdirectory, args.name, date)
if not os.path.exists(ldir):
    os.makedirs(ldir)
if not os.path.exists(os.path.join(ldir, 'error')):
    os.makedirs(os.path.join(ldir, 'error'))

# generate commands to run
commands = []
for filelist in args.filelists:
    with open(filelist, 'r') as f:
        for abspath in f:
            abspath = abspath.replace('\n', '')
            fname = os.path.basename(abspath)
            for conf in args.configurations:
                cc = conf.split(':')
                commands.append(
                     (cmd % (args.jargs, args.classpath, args.timelimit * 1000, cc[1], abspath),
                     os.path.join(ldir, fname) + '+' + cc[0] + '.log',
                     os.path.join(ldir, 'error', fname) + '+' + cc[0] + '.err.log',
                     args.timelimit
                     )
                )
# run commands
for cm in commands:
    print(cm)
if args.process == 1:
    for cm in commands:
        worker.work1(cm)
else:
    pool = multiprocessing.Pool(args.process)
    pool.map(
        worker.workN,
        commands
    )
