#!/usr/bin/python
# CONSTANTS
import sys, subprocess, shlex, time, threading, os, signal, re, logging, tempfile
from threading import Thread
from os.path import join

## ENVIRONMENT VARIABLES
home = '../../'

# ENVIRONMENT VARIABLES
#CHOCO_SOLVER = join(home, 'solver', 'target', 'solver-rocs-1.0-SNAPSHOT-with-dep.jar')
CHOCO_PARSERS = join(home, 'parser','target',  'parser-rocs-1.0-SNAPSHOT-with-dep.jar')
CHOCO_LIB = join(home, 'parser','src', 'lib','minizinc')
CONFIG=join(home, 'src', 'shell', 'config.xml')

JAVA_OPTS = CMD = '-Xmx256m -Xms256m -XX:+AggressiveOpts'

MZN_FILE = ''
DZN_FILE = ''

## time limit for a process
TIMELIMIT = 300 #in seconds
## number threads used
THREAD = 1
# AVOID CHECKING
CHECK = True


#####################################################################
################### FUNCTIONS #######################################
#####################################################################
def readParameters(paramlist):
    global MZN_FILE
    global DZN_FILE
    global TIMELIMIT
    global CHECK
    offset = 2
    if len(paramlist) > 0:
        if paramlist[0] == "-f": # file name
            MZN_FILE = paramlist[1]
        elif paramlist[0] == "-d":
            DZN_FILE = paramlist[1] # data file
        elif paramlist[0] == "-tl":
            TIMELIMIT = int(paramlist[1]) # process time limit
        elif paramlist[0] == "-nocheck":
            CHECK = False
            offset = 1
        readParameters(paramlist[offset:])

def buildLog(name, ext, level):
    hdlr = logging.FileHandler('./' + name + ext)
    formatter = logging.Formatter('%(message)s')
    hdlr.setFormatter(formatter)
    logger = logging.getLogger(name)
    logger.addHandler(hdlr)
    logger.setLevel(level)
    return logger

def kill( process ):
    if process.poll() is None:
        os.kill(process.pid, signal.SIGKILL)

def limit( process, cutoff ):
    t = threading.Timer(cutoff, kill, [process])
    t.start()
    return t


class runit(Thread):
    def __init__ (self, args):
        Thread.__init__(self)
        self.args = args
        self.time = 0
        self.out = ''

    def run(self):
        process = subprocess.Popen(self.args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        start = time.time()
        clock = limit(process, TIMELIMIT)
        process.wait()
        end = time.time()
        self.out , error = process.communicate()
        clock.cancel()
        if len(error) > 0:
            print("error - see log file")
            err.error(self.args)
            line = ''
            for char in error:
                if char == '\n':
                    err.error(line)
                    line = ''
                else:
                    line += char
            err.error('\n')
            sys.exit(-1)

        process.stdout.close()
        process.stderr.close()
        self.time = end - start

## PARSE THE MINIZINC FILE IN FLATZINC FILE USING CHOCO LIBRARIES
def parse():
    OUTPUT = tempfile.NamedTemporaryFile(suffix='.fzn', prefix='tmp', delete=False)

    #COMMAND = join(MINIZINC_DIR, 'bin', 'mzn2fzn')
    COMMAND = 'mzn2fzn'
    COMMAND+=' --stdlib-dir '+ CHOCO_LIB
    COMMAND+=' -G std'
    COMMAND += ' ' + MZN_FILE
    COMMAND += ' -o ' + OUTPUT.name
    if DZN_FILE != '':
        COMMAND += ' -d ' + DZN_FILE

    ARGS = shlex.split(COMMAND)
    current = runit(ARGS)
    current.start()
    current.join()
    print ("..."+str(current.time) + 's (file : '+OUTPUT.name+')')
    return OUTPUT.name

def solve():
    COMMAND = 'java -cp .:' + CHOCO_PARSERS + '\
     -Dlogback.configurationFile='+ CONFIG +' parser.flatzinc.ParseAndSolve'
    COMMAND += ' -f ' + FZN_FILE
    #    COMMAND += ' -o ' + OUTPUT.name
    ARGS = shlex.split(COMMAND)
    current = runit(ARGS)
    current.start()
    current.join()
    print ("..."+str(current.time) + 's')
    SOLUTION = tempfile.NamedTemporaryFile(suffix='.sol', prefix='tmp', delete=False)
    line = ''
    for char in current.out:
        SOLUTION.write(char)
        if char == '\n':
            print (line)
            line=''
        else:
            line+= char
    print (line)
    SOLUTION.close()
    return SOLUTION

def check(solution):
    ## solution to mzn file
    #COMMAND = join(MINIZINC_DIR, 'bin', 'solns2dzn')
    COMMAND = 'solns2dzn'
    COMMAND += ' -s ' + solution.name
    ARGS = shlex.split(COMMAND)
    current = runit(ARGS)
    current.start()
    current.join()

    ## check solution
    ## ugly....
    i =1
    name = solution.name+'.'+str(i)
    while os.path.exists(name):
        DATA = ''
        SOL = open(name)
        while 1:
            char = SOL.read(1)
            if not char: break
            if char != '\n': DATA+=char
        SOL.close()

        DATA='\"' + DATA +'\"'
        #COMMAND = join(MINIZINC_DIR, 'bin', 'mzn')
        COMMAND = 'mzn'
        COMMAND += ' --quiet '
    #    COMMAND+=' --stdlib-dir '+ CHOCO_LIB
    #    COMMAND+=' -G choco_std'
        COMMAND += ' -D ' + DATA
        COMMAND += ' ' + MZN_FILE
        COMMAND += ' ' + DZN_FILE
        ARGS = shlex.split(COMMAND)
        current = runit(ARGS)
        current.start()
        current.join()
        i+=1
        name = solution.name+'.'+str(i)



#####################################################################
################### FUNCTIONS #######################################
#####################################################################
err = buildLog('fzn2choco', '.log', logging.ERROR)

## FIRST, READ PARAMETERS
readParameters(sys.argv[1:])
if not MZN_FILE:
    print ("ERR>> \"-f mzn_file\" is required")
    err.error("ERR>> \"-f mzn_file\" is required")
    sys.exit(0)

EXTENSION = MZN_FILE[len(MZN_FILE) - 3:].lower()
FZN_FILE = None
#OUTPUT=None

if EXTENSION == 'mzn':
    print ('PARSE MZN FILE'),
    FZN_FILE = parse()

elif EXTENSION == 'fzn':
    FZN_FILE = MZN_FILE

else:
    print ("ERR>> Unexpected extension")
    err.error("ERR>> Unexpected extension")
    sys.exit(0)

print ('SOLVE PROBLEM USING CHOCO'),
solution = solve()

if os.path.getsize(solution.name) == 0:
    print ('TIME LIMIT REACHED')

if CHECK:
    print ('CHECK SOLUTION'),
    check(solution)


#if OUTPUT is not None:
#    os.unlink(OUTPUT.name)




