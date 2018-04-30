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
    '/Users/cprudhom/.m2/repository/org/choco-solver/choco-parsers/4.0.2-SNAPSHOT/choco-parsers-4.0.2-SNAPSHOT-with-dependencies.jar'
)

parser.add_argument(
    "-y", "--year",
    help='Challenge year',
    default='2017',
)

parser.add_argument(
    "-d", "--directory",
    help="Flatzinc files directory.",
    default='/Users/cprudhom/Sources/XCSP/instances/'
)
parser.add_argument(
    "-o", "--outputdirectory",
    help="Output files directory.",
    default='/Users/cprudhom/Sources/XCSP/Challenges/logs/'
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
        'DEF:-stat'
        # 'SADAC:-stat'
    ]
)
parser.add_argument(
    "-tl", "--timelimit",
    help='Time limit in seconds for the resolutions.',
    type=int,
    default=1800
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
    print("python3.4 ./benchmark_xcsp.py -cp %s -y %s -fl %s -p %s -c %s -tl %s;" %
          (args.classpath, args.year, args.filelist, args.process, args.configurations, args.timelimit)
          )
    exit(0)

date = time.strftime("%Y%m%d")
fdir = os.path.join(args.directory, args.year)
ldir = os.path.join(args.outputdirectory, args.year, date)
if not os.path.exists(ldir):
    os.makedirs(ldir)
if not os.path.exists(os.path.join(ldir, 'error')):
    os.makedirs(os.path.join(ldir, 'error'))

# generate commands to run
commands = []

for subdir, dirs, files in os.walk(args.directory):
    for file in files:
        #print os.path.join(subdir, file)
        filepath = subdir + os.sep + file

        if filepath.endswith(".lzma"):
            fname = os.path.basename(filepath)
            for conf in args.configurations:
                cc = conf.split(':')
                commands.append(
                    (cmd % (args.jargs, args.classpath, args.timelimit * 1000, cc[1], filepath),
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
