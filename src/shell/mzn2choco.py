#!/usr/bin/python
# CONSTANTS
import sys, subprocess, shlex, time, threading, os, signal, re, logging, tempfile
from threading import Thread
from os.path import join

## ENVIRONMENT VARIABLES
CHOCO_HOME = '/Users/cprudhom/Documents/Projects/Sources/Galak/trunk/'

# ENVIRONMENT VARIABLES
MINIZINC_DIR = '/Users/cprudhom/Documents/Projects/_Librairies/minizinc-1.2.2'

M2_REPO = '/Users/cprudhom/.m2/repository/'
ARGS = join(M2_REPO, 'args4j', 'args4j', '2.0.12', 'args4j-2.0.12.jar')
LOGBACK_CL = join(M2_REPO, 'ch', 'qos', 'logback', 'logback-classic', '0.9.24', 'logback-classic-0.9.24.jar')
LOGBACK_CO = join(M2_REPO, 'ch', 'qos', 'logback', 'logback-core', '0.9.24', 'logback-core-0.9.24.jar')
SLF4J = join(M2_REPO, 'org', 'slf4j', 'slf4j-api', '1.6.1', 'slf4j-api-1.6.1.jar')
TROVE = join(M2_REPO, 'gnu', 'trove', '2.1.0', 'trove-2.1.0.jar')
JPARSEC = join(M2_REPO, 'jparsec', 'jparsec', '2.0.1', 'jparsec-2.0.1.jar')
CGLIB = join(M2_REPO, 'cglib', 'cglib-nodep', '2.2', 'cglib-nodep-2.2.jar')
CP = '.:' + ARGS + ':' + LOGBACK_CL + ':' + LOGBACK_CO + ':' + SLF4J + ':' + TROVE + ':'+JPARSEC +':'+CGLIB

CHOCO_SOLVER = join(CHOCO_HOME, 'solver','target',  'solver-rocs-1.0-SNAPSHOT.jar')
CHOCO_PARSERS = join(CHOCO_HOME, 'parser','target',  'parser-rocs-1.0-SNAPSHOT.jar')
CHOCO_LIB = join(CHOCO_HOME, 'parser','src', 'lib','minizinc')

JAVA_OPTS = CMD = '-Xmx256m -Xms256m -XX:+AggressiveOpts'

MZN_FILE = ''
DZN_FILE = ''

## time limit for a process
TIMELIMIT = 180 #in seconds
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

    COMMAND = join(MINIZINC_DIR, 'bin', 'mzn2fzn')
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
    print ("..."+str(current.time) + 's')
    return OUTPUT.name

def solve():
    COMMAND = 'java -cp ' + CP + ':' + CHOCO_SOLVER + ':' + CHOCO_PARSERS + ' parser.flatzinc.ParseAndSolve'
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
    COMMAND = join(MINIZINC_DIR, 'bin', 'solns2dzn')
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
        COMMAND = join(MINIZINC_DIR, 'bin', 'mzn')
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




