#!/usr/bin/python
# CONSTANTS
## file name containing list of command to run
import sys, subprocess, shlex, time, threading, os, signal,re, logging
from threading import Thread
from os.path import join

## ENVIRONMENT VARIABLES
## HOME
CHOCO_HOME = '/Users/cprudhom/Documents/Projects/Sources/Galak/trunk/'

## maven local repository
M2_REPO = '/Users/cprudhom/.m2/repository/'

## java class-path for rocs
ARGS = join(M2_REPO, 'args4j', 'args4j', '2.0.12', 'args4j-2.0.12.jar')
LOGBACK_CL = join(M2_REPO, 'ch', 'qos', 'logback', 'logback-classic', '0.9.24', 'logback-classic-0.9.24.jar')
LOGBACK_CO = join(M2_REPO, 'ch', 'qos', 'logback', 'logback-core', '0.9.24', 'logback-core-0.9.24.jar')
SLF4J = join(M2_REPO, 'org', 'slf4j', 'slf4j-api', '1.6.1', 'slf4j-api-1.6.1.jar')
TROVE = join(M2_REPO, 'gnu', 'trove', '2.1.0', 'trove-2.1.0.jar')
JPARSEC = join(M2_REPO, 'jparsec', 'jparsec', '2.0.1', 'jparsec-2.0.1.jar')
CGLIB = join(M2_REPO, 'cglib', 'cglib-nodep', '2.2', 'cglib-nodep-2.2.jar')
DKBRICS = join(M2_REPO, 'dk', 'brics', 'automaton', '1.11-2', 'automaton-1.11-2.jar')
dk.brics.automaton.Automaton

CHOCO_SOLVER = join(CHOCO_HOME, 'solver','target',  'solver-rocs-1.0-SNAPSHOT.jar')

## correct class-path
CP = '-cp .:'+ ARGS+':'+LOGBACK_CL+':'+LOGBACK_CO+':'+SLF4J+':'+TROVE+':'+JPARSEC+':'+CGLIB+':'+DKBRICS':'+CHOCO_SOLVER

## java command
CMD='java -Xmx1024m -Xms1024m -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC'

## Number of time a problem is run
loop = 1 # can be override
## time limit for a process
timelimit = 180 #in seconds
## number threads used
thread = 1
name = 'runner'

## regexp for statisctics
## [STATISTICS S Solutions, Objective: O, Resolution Tms, N Nodes, B Backtracks, F Fails, R Restarts]
## len is 7 or 8
_SIZE = 9
pattern = re.compile('\d+')
_STAT = '[STATISTICS'
_NAMES = 'SOLUTION','OBJECTIVE','TIME','NODE','BACKTRACK','FAIL','RESTART','PROPAGATIONS','SCRIPT_TIME'


def readParameters(paramlist):
    global loop
    global timelimit
    global thread
    global name
    if len(paramlist)>0:
        if paramlist[0] == "-l": # option for number of time a problem must be run
            loop = int(paramlist[1])
        elif paramlist[0] == "-t":
            timelimit = int(paramlist[1]) # time limit before killing a process
        elif paramlist[0] == "-j": # number of thread to use in a loop
            thread = int(paramlist[1])
        elif paramlist[0] == "-n": # subname of output files
            name = paramlist[1]
        readParameters(paramlist[2:])


readParameters(sys.argv[1:])

def buildLog(name, ext, level):
    hdlr = logging.FileHandler('./'+name+ext)
    formatter = logging.Formatter('%(message)s')
    hdlr.setFormatter(formatter)
    logger = logging.getLogger(name)
    logger.addHandler(hdlr)
    logger.setLevel(level)
    return logger


out = open('./'+name+'.txt', 'w', 10)
err = buildLog(name, '.log', logging.INFO)

if len(sys.argv) > 1:
    if sys.argv[1] == "-l":
        loop = int(sys.argv[2])
print ">> Run "+ str(loop) +" time(s)"

def kill( process ):
    if process.poll() == None:
        os.kill( process.pid, signal.SIGKILL )

def limit( process, cutoff ):
    t = threading.Timer( cutoff, kill, [process] )
    t.start()
    return t

def compute(result, size):
    b = []
    if size == _SIZE:
        b = _NAMES
    else:
        b = [_NAMES[0]]
        b+= _NAMES[2:_SIZE]
    for i in range(size):
        sum = 0.0
        s = len(result[i])
        result[i].sort()
        if s > 2 :
            result[i] = result[i][1:s-1]
        for j in range(len(result[i])):
            sum += result[i][j]
        moy = sum/len(result[i])
        stdev = max(result[i]) - min(result[i])
        info = "\t"+b[i] + ": "+ str(moy)
        if stdev > 0:
            info += " ("+str(stdev)+")"
        out.write(info+'\n')

f = open(join('.', name+'.list'),'r')

class runit(Thread):
    def __init__ (self,args, i, j):
      Thread.__init__(self)
      self.args = args
      self.result = []
      self.i = i
      self.j = j

    def run(self):
        # print str(self.i)+'_'+str(self.j)
        process = subprocess.Popen(args, bufsize=0, stdout=subprocess.PIPE, stderr=subprocess.PIPE )
        start = time.time()
        clock = limit( process, timelimit )
        process.wait()
        end = time.time()
        output, error = process.communicate()
        clock.cancel()
        line = ''
        if len(error) > 0:
            out.write("error - see log file\n")
            err.error(args)
            for char in error:
                if char =='\n':
                    err.error(line)
                    line = ''
                else:
                    line+= char
            err.error('\n')
        else:
            statitics = ''
            for char in output:
                if char =='\n':
                    data_line = line.split()
                    s = len(data_line)
                    if s>0 and data_line[0] == '[STATISTICS':
                        statitics = line
                        break
                    line =''
                else:
                    line +=char

            m = pattern.findall(statitics)
            for j in range(len(m)):
                self.result.append(int(m[j]))
            self.result.append(round((end-start)*1000, 2))
        process.stdout.close()
        process.stderr.close()



for line in f:
    if line[0] != '#' and line != '\n':
        out.write(line)
        command = CMD+' '+ CP+' '+line + ' -log QUIET'
        # print command
        args = shlex.split(command)
        results = [[]for k in range(_SIZE)]
        size = _SIZE
        spec = [thread] * (loop/thread)
        spec.append(loop%thread)
        for i in range(len(spec)):
            runlist = []
            for t in range(spec[i]):
                current  = runit(command, i, t)
                runlist.append(current)
                current.start()

            for run in runlist:
                run.join()
                size = len(run.result)
                for k in range(len(run.result)):
                    results[k].append(run.result[k])

        # end loop
        compute(results, size)
        out.write('\n')
        out.flush()
out.close()
print ">> End of script"




  
