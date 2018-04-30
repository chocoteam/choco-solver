__author__ = 'kyzrsoze'

import multiprocessing
import argparse
import os
import time
import utils.worker as worker


parser = argparse.ArgumentParser(description='Solve flatzinc files.')
parser.add_argument(
    "-cp", "--classpath",
    help='Classpath for Choco (choco-parsers and choco-solver)',
    default=  # '.:'
    '/Users/cprudhom/Sources/MiniZinc/Challenges/jars/sum/choco-parsers.jar',
)

parser.add_argument(
    "-n", "--name",
    help='Benchmark name',
    default='sum',
)

parser.add_argument(
    "-fl", "--filelists",
    help='Files containing name of flatzinc files to solve.',
    nargs='+',
    default=[
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/list2012.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/listALL.txt',
        # '/Users/cprudhom/Sources/MiniZinc/sandbox/listRCPSP.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_at.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_bl.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_ksd15_d.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_la_x.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_pack_d.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_pack.txt',
            # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_psplib_j30.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_psplib_j60.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_psplib_j90.txt',
        # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_psplib_j120.txt',
    ]
)


parser.add_argument(
    "-d", "--directory",
    help="Flatzinc files directory.",
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/'
)
parser.add_argument(
    "-o", "--outputdirectory",
    help="Output files directory.",
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/logs/'
)
parser.add_argument(
    "-p", "--process",
    help='Number of processes to run in parallel',
    type=int,
    default=6
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name:options\'',
    nargs='+',
    default=[
        # 'DFT+MZN:-stat -cum MZN',
        # 'EX+D+MZN:-stat -x 3 -cum MZN',
        # 'EX+S+MZN:-stat -x 3 -cum MZN -sumdft',
        # 'EX+G+MZN:-stat -x 3 -cum MZN -sumdft -sumglb',
        # 'DFT+MIC:-stat -cum MIC',
        # 'EX+D+MIC:-stat -x 3 -cum MIC',
        'EX+S+MZN:-stat -x 3 -cum MZN -oes',
        'EX+G+MZN:-stat -x 3 -cum MZN -oes -sumglb',
    ]
)
parser.add_argument(
    "-tl", "--timelimit",
    help='Time limit in seconds for the resolutions.',
    type=int,
    default=600
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

cmd = 'java %s -cp %s org.chocosolver.parser.flatzinc.ChocoFZN -tl %s %s %s'

# start here
args = parser.parse_args()

if args.print:
    print(
        "python3.4 ./benchmark_fzn.py -cp %s org.chocosolver.parser.flatzinc.ChocoFZN -n %s -fl %s -p %s -c %s -tl %s;" %
        (args.classpath, args.name, args.filelist, args.process, args.configurations, args.timelimit)
    )
    exit(0)

date = time.strftime("%Y%m%d")
fdir = os.path.join(args.directory, args.name)
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
