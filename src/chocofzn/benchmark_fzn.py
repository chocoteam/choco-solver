__author__ = 'kyzrsoze'

import subprocess
import multiprocessing
import argparse
import os
import time


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
            '/Users/cprudhom/.m2/repository/org/choco-solver/choco-solver/3.3.2-SNAPSHOT/choco-solver-3.3.2-SNAPSHOT-jar-with-dependencies.jar:'
            '/Users/cprudhom/.m2/repository/org/choco-solver/choco-parsers/3.3.2-SNAPSHOT/choco-parsers-3.3.2-SNAPSHOT-with-dependencies.jar',
)
parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of flatzinc files to solve.',
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/list2012.txt'
)
parser.add_argument(
    "-d", "--directory",
    help="Flatzinc files directory.",
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/2012/'
)
parser.add_argument(
    "-o", "--outputdirectory",
    help="Output files directory.",
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/logs/2012/'
)
parser.add_argument(
    "-p", "--process",
    help='Number of processes to run in parallel',
    type=int,
    default=1
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name:options\'',
    nargs='+',
    default=[
       'fixed:-stat'
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
    help='Java Virtual Machine arguments (eg: -Xss64m -Xms64m -Xmx4096m -server)',
    default=''
)

cmd = 'java %s -cp %s org.chocosolver.parser.flatzinc.ChocoFZN -tl %s %s %s'
args = parser.parse_args()

date = time.strftime("%Y%m%d")
if not os.path.exists(os.path.join(args.outputdirectory, date)):
    os.makedirs(os.path.join(args.outputdirectory, date))
if not os.path.exists(os.path.join(args.outputdirectory, date, 'error')):
    os.makedirs(os.path.join(args.outputdirectory, date, 'error'))

# generate commands to run
commands = []
with open(args.filelist, 'r') as f:
    for fznfile in f:
        fznfile = fznfile.replace('\n', '')
        for conf in args.configurations:
            cc = conf.split(':')
            commands.append(
                (cmd % (args.jargs, args.classpath, args.timelimit * 1000, cc[1], args.directory + fznfile),
                 os.path.join(args.outputdirectory, date, fznfile) + '+' + cc[0] + '.log',
                 os.path.join(args.outputdirectory, date, 'error', fznfile) + '+' + cc[0] + '.err.log',
                 args.timelimit
                 )
            )
# run commands
# for cm in commands:
# print(cm)
if args.process == 1:
    for cm in commands:
        work1(cm)

else:
    pool = multiprocessing.Pool(args.process)
    pool.map(
        workN,
        commands
    )
