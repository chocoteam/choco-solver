__author__ = 'kyzrsoze'

import subprocess
import yaml
import multiprocessing
import argparse
import os
import time
import tqdm


def expanduser(path):
    """
    Expand user HOME if nedded
    """
    if path.startswith('~'):
        return os.path.expanduser(path)
    return path


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
    cmd, log, err, to, verbose = args
    curproc = multiprocessing.current_process()
    if verbose:
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
        if verbose:
            print(curproc, "Ended Process")
    except subprocess.TimeoutExpired:
        if verbose:
            print(curproc, "Killed Process")
    if os.path.getsize(err) == 0:
        os.remove(err)


def work1(args):
    cmd, log, err, to, verbose = args
    if verbose:
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
        if verbose:
            print("Ended Process")
    except subprocess.TimeoutExpired:
        if verbose:
            print("Killed Process")
    if os.path.getsize(err) == 0:
        os.remove(err)
    pbar.update(1)


EXTENSIONS = ['lzma', 'xml', 'fzn', 'mps']

parser = argparse.ArgumentParser(description='Solve DSL-based files.')
parser.add_argument(
    "-c", "--configuration",
    help='YAML file',

)

parser.add_argument(
    "-v", "--verbose",
    help='Call rsync on exit',
    action="store_true"
)

parser.add_argument(
    "-d","--dryRun",
    help='Print the command to the console and exit',
    action="store_true"
)

# start here
print("# Read arguments")
args = parser.parse_args()

print("# Read configuration file")
with open(args.configuration) as file:
    configurations = yaml.load(file, Loader=yaml.FullLoader)

print("# Create output directory and files")
date = time.strftime("%Y%m%d")
ldir = expanduser(os.path.join(configurations["output"], configurations["name"], date))
if not os.path.exists(ldir):
    os.makedirs(ldir)
if not os.path.exists(os.path.join(ldir, 'error')):
    os.makedirs(os.path.join(ldir, 'error'))

print("# Scan inputs")
instances = {}
for input in configurations["inputs"]:
    input = expanduser(input)
    for dirname, _, files in os.walk(input):
        for file in files:
            if any(file.endswith(ext) for ext in EXTENSIONS):
                instances[file] = os.path.join(dirname, file)
print("# Found ", len(instances), " instances")

print("# Prepare commands to execute")
commands = []

cmd = configurations["runner"] + " " + expanduser(configurations["jar"])

time = configurations["time"]
confs = configurations["configurations"]

print("# Based on ", [*confs.keys()])

for fname in instances.keys():
    for key in confs.keys():
        val = confs[key]
        path = instances[fname]
        outfile = os.path.join(ldir, fname) + '+' + str(key) + '.log'
        errfile = os.path.join(ldir, 'error', fname) + '+' + str(key) + '.err.log'
        commands.append(
            (
                "%s \"%s -limit '(%ds)'\" %s" % (cmd, val, time, path),
                outfile,
                errfile,
                configurations["time"],
                args.verbose
            )
        )
if args.dryRun:
    print(commands[:2])
    exit(0)

print("# Run commands")
process = configurations["process"]

if process == -1:
    process = multiprocessing.cpu_count()
if process == 1:
    pbar = tqdm.tqdm(total=len(commands))
    for cm in commands:
        work1(cm)
        pbar.update(1)
    pbar.close()
else:
    pool = multiprocessing.Pool(process)
    for _ in tqdm.tqdm(pool.imap_unordered(workN, commands), total=len(commands)):
        pass
    pool.close()
    pool.join()