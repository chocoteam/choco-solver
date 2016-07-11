__author__ = 'kyzrsoze'

import subprocess
import multiprocessing
import argparse
import os
import time
import ctypes 


def call(*popenargs, timeout=None, **kwargs):
    """Run command with arguments.  Wait for command to complete or
    timeout, then return the returncode attribute.

    The arguments are the same as for the Popen constructor.  Example:

    retcode = call(["ls", "-l"])
    """
    with subprocess.Popen(*popenargs, **kwargs) as p:
        try:
            return p.wait(timeout=timeout)
        except subprocess.TimeoutExpired:
            pass
        except:
            p.terminate()  # THIS THE MAIN REASON I CREATE THIS METHOD
            try:
                p.wait(.1)
            except:
                p.kill()
                p.wait()
            raise


def workN(args):
    """ Prepare the execution of the command
    :param args: the set of arguments
    :return:
    """
    cmd, log, err, to = args
    curproc = multiprocessing.current_process()
    print(curproc, "Started Process, args={}".format(args))
    lfile = open(log, 'w')
    efile = open(err, 'w')
    try:
        call(
            cmd,
            shell=True,
            stdout=lfile,
            stderr=efile,
            timeout=to,
        )
        print(curproc, "Ended Process")
    except subprocess.TimeoutExpired:
        print(curproc, "Killed Process")
    if os.path.getsize(err) == 0:
        os.remove(err)


def work1(args):
    cmd, log, err, to = args
    print("Started Process, args={}".format(args))
    lfile = open(log, 'w')
    efile = open(err, 'w')
    try:
        call(
            cmd,
            shell=True,
            stdout=lfile,
            stderr=efile,
            timeout=to,
        )
        print("Ended Process")
    except subprocess.TimeoutExpired:
        print("Killed Process")
    if os.path.getsize(err) == 0:
        os.remove(err)


        ## CMD LINE ARGUMENT


parser = argparse.ArgumentParser(description='Solve flatzinc files.')
parser.add_argument(
    "-cp", "--classpath",
    help='Classpath for Choco (choco-parsers and choco-solver)',
    default='.:'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/jars/20150730/choco-parsers.jar',
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/jars/20160624/choco-parsers.jar',
    '/Users/cprudhom/Sources/MiniZinc/Challenges/jars/20160627/choco-parsers.jar',
)

parser.add_argument(
    "-y", "--year",
    help='Challenge year',
    default='2012',
)

parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of flatzinc files to solve.',
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/list2012.txt'
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
    default=8
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name:options\'',
    nargs='+',
    default=[
        'C4DFS:-stat',
        'C4LDS:-stat -tree 1',
        'C4DDS:-stat -tree 2',
        'C4CBJ:-stat -tree 3',
        'C4DFS+LC:-stat -f',
        'C4LDS+LC:-stat -f -tree 1',
        'C4DDS+LC:-stat -f -tree 2',
        'C4CBJ+LC:-stat -f -tree 3',
        'C4DFS+COS:-stat -f -cos',
        'C4LDS+COS:-stat -f -cos -tree 1',
        'C4DDS+COS:-stat -f -cos -tree 2',
        'C4CBJ+COS:-stat -f -cos -tree 3',
        # 'SADAC:-stat'
    ]
)
parser.add_argument(
    "-tl", "--timelimit",
    help='Time limit in seconds for the resolutions.',
    type=int,
    default=300
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
    print("python3.4 ./benchmark_fzn.py -cp %s -y %s -fl %s -p %s -c %s -tl %s;" %
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
with open(args.filelist, 'r') as f:
    for fznfile in f:
        fznfile = fznfile.replace('\n', '')
        for conf in args.configurations:
            cc = conf.split(':')
            commands.append(
                (cmd % (args.jargs, args.classpath, args.timelimit * 1000, cc[1], os.path.join(fdir, fznfile)),
                 os.path.join(ldir, fznfile) + '+' + cc[0] + '.log',
                 os.path.join(ldir, 'error', fznfile) + '+' + cc[0] + '.err.log',
                 args.timelimit
                 )
            )
# run commands
for cm in commands:
    print(cm)
if args.process == 1:
    for cm in commands:
        work1(cm)

else:
    pool = multiprocessing.Pool(args.process)
    pool.map(
        workN,
        commands
    )
