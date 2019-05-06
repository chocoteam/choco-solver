import argparse
import multiprocessing
import subprocess

__author__ = 'kyzrsoze'

import os


def convertall(dir, list, CMD):
    """
    Prepare commands to run after
    :param dir: a directory made of one or more mzn files and one or more dzn files
    :param list: list of commands to populate
    :return:
    """
    if os.path.isfile(dir):
        return
    pbname = os.path.basename(dir)
    mznfiles = [file for file in os.listdir(dir) if file.endswith("mzn")]
    dznfiles = [file for file in os.listdir(dir) if file.endswith("dzn")]
    for mzn in mznfiles:
        mznpath = os.path.join(dir, mzn)
        if len(dznfiles) > 0:
            for dzn in dznfiles:
                dznpath = os.path.join(dir, dzn)
                fznname = mzn[:-4] + "+" + pbname + "+" +  dzn[:-4] + ".fzn"
                list.append(CMD % (mznpath + " -d " + dznpath, fznname))
        else:
            fznname = mzn[:-4] + "+" + pbname + ".fzn"
            list.append(CMD % (mznpath, fznname))


def work(args):
    """
    Select a command and run it
    :param args: command and timeout
    """
    cmd = args
    curproc = multiprocessing.current_process()
    print(curproc, "Started Process, args={}".format(args))
    try:
        subprocess.call(
            cmd,
            shell=True,
        )
        print(curproc, "Ended Process")
    except subprocess.TimeoutExpired:
        print(curproc, "Killed Process")

## CMD LINE ARGUMENT
parser = argparse.ArgumentParser(description='Convert MiniZinc files in FlatZinc files.')
parser.add_argument(
    "-G", "--globals",
    help='Directory wherein global constraints supported by Choco are given.',
    default='/Users/cprudhom/Sources/choco/extensions/choco-parsers/src/main/mzn_lib/',
    # default='/Users/cprudhom/Sources/Sandbox/chuffed/trunk/chuffed/flatzinc/mznlib/'
    # default='/Applications/MiniZincIDE.app/Contents/Resources/share/minizinc/std'
)
parser.add_argument(
    "-d", "--directory",
    help="MiniZinc files directory.",
    default="/Users/cprudhom/Sources/MiniZinc/minizinc-challenges/rcpsp/data_psplib"
)
parser.add_argument(
    "-o", "--outputdirectory",
    help="Output files directory.",
    default="/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp/"
)
parser.add_argument(
    "-p", "--process",
    help='Number of processes to run in parallel',
    type=int,
    default=4
)

args = parser.parse_args()
path = args.outputdirectory
os.makedirs(path, exist_ok=True)
CMD = "mzn2fzn -I \"" + args.globals + "\" %s -o " + os.path.join(path, "%s")

commands = []
for bdir in os.listdir(args.directory) :
        convertall(os.path.join(args.directory, bdir), commands, CMD)

pool = multiprocessing.Pool(args.process)
pool.map(
    work,
    commands
)