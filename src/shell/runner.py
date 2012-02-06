#!/usr/bin/python
# CONSTANTS
## file name containing list of command to run
import sys, subprocess, shlex, time, threading, os, signal,re, logging
from threading import Thread
from os.path import join
try:
    import MySQLdb
    import database
except ImportError :
    database = None
try :
    import matplotlib
    import pylab
    import plotmysql
except ImportError:
    plotmysql = None

#################################
if database is None:
    print "WARNING : You should install 'MySQLdb' for python to use the database part. This will be skipped."
if plotmysql is None:
    print "WARNING : You should install 'matplotlib' for python to use the plot part. This will be skipped."

#################################

## ENVIRONMENT VARIABLES
name = 'runner'
dir = '.'
pwd = ''
home = '../../'

## java command
JAVA='java -Xmx1024m -Xms1024m '# -XX:+AggressiveOpts -XX:+UseConcMarkSweepG'


## Number of time a problem is run
loop = 1 # can be override
## time limit for a process
timelimit = 180 #in seconds => 10min

## regexp for statisctics
## [STATISTICS S Solutions, Objective: O, Resolution Ts (tms), N Nodes, B Backtracks, F Fails, R Restarts, P + P propagations]
## len is 8 or 9
_SIZE = 15
pattern = re.compile('\d+\,?\d*')
_STAT = '[STATISTICS'
_NAMES = 'SOLUTION','OBJECTIVE','TIME','NODE','BACKTRACK','FAIL','RESTART','PROPAGATIONS','SCRIPT_TIME'


def readParameters(paramlist):
    global loop
    global timelimit
    global name
    global dir
    global pwd
    global home
    if len(paramlist)>0:
        if paramlist[0] == "-l": # option for number of time a problem must be run
            loop = int(paramlist[1])
        elif paramlist[0] == "-t":
            timelimit = int(paramlist[1]) # time limit before killing a process
        elif paramlist[0] == "-n": # subname of output files
            name = paramlist[1]
        elif paramlist[0] == "-d": # directory of output files
            dir = paramlist[1]
        elif paramlist[0] == "-pwd": # user pwd for db connexion
            pwd = paramlist[1]
        elif paramlist[0] == "-h": # user pwd for db connexion
            home = paramlist[1]
        readParameters(paramlist[2:])


readParameters(sys.argv[1:])
print home

# initialize env. variables
CHOCO_SOLVER = join(home, 'solver','target',  'solver-rocs-1.0-SNAPSHOT-with-dep.jar')
CP = '-cp .:'+CHOCO_SOLVER
CMD = JAVA+' '+CP+' '+' samples.FrontEndBenchmarking'
print CHOCO_SOLVER

def buildLog(name, ext, level):
    hdlr = logging.FileHandler(join(dir, name+ext))
    formatter = logging.Formatter('%(message)s')
    hdlr.setFormatter(formatter)
    logger = logging.getLogger(name)
    logger.addHandler(hdlr)
    logger.setLevel(level)
    return logger


out = open(join(dir, name+'.txt'), 'w', 10)
err = buildLog(name, '.log', logging.INFO)

if len(sys.argv) > 1:
    if sys.argv[1] == "-l":
        loop = int(sys.argv[2])
print ">> Run "+ str(loop) +" time(s)"

def kill( process ):
    if process.poll() is None:
        os.kill( process.pid, signal.SIGKILL )

def limit( process, cutoff ):
    t = threading.Timer( cutoff, kill, [process] )
    t.start()
    return t

def compute(line, result, size):
    b = []
    if size == _SIZE:
        b = _NAMES
    else:
        b = [_NAMES[0]]
        b+= _NAMES[2:_SIZE]
    out.write(line+"\n")
    for i in range(size):
        sum = 0.0
        s = len(result[i])
        result[i].sort()
        if s > 2 :
            result[i] = result[i][1:s-1]
        for j in range(len(result[i])):
            sum += result[i][j]
        moy = round(sum/len(result[i]),6)
        stdev = max(result[i]) - min(result[i])
        info = "\t"+b[i] + ": "+ str(moy)
        if stdev > 0:
            info += " ("+str(stdev)+")"
        out.write(info+'\n')

def computeXLS(line, result, size):
    line = line.rstrip("\n")
    out.write(line+";")
    for i in range(size):
        sum = 0.0
        s = len(result[i])
        result[i].sort()
        if not s:
            return
        if s > 2 :
            result[i] = result[i][1:s-1]

        for j in range(len(result[i])):
            sum += result[i][j]
        moy = round(sum/len(result[i]),6)
        stdev = max(result[i]) - min(result[i])
        info = ""+ str(moy)+";"
        info += ""+str(stdev)+";;"
        out.write(info)

def storeInDB(mydatab, line, results):
    parts = line.split(" ", 1) # separate name of problem and parameters
    parts = parts + [" "]
    mydatab.insertValues(parts[0], parts[1], results)

f = open(join(dir, name+'.list'),'r')

class runit(Thread):
    def __init__ (self,args, i, j):
      Thread.__init__(self)
      self.args = args
      self.results = [[]for k in range(_SIZE)]
      self.i = i
      self.j = j
      self.s = _SIZE

    def run(self):
        # print str(self.i)+'_'+str(self.j)
        process = subprocess.Popen(args, bufsize=0, stdout=subprocess.PIPE, stderr=subprocess.PIPE )
        start = time.time()
        clock = limit( process, timelimit * loop )
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
            for char in output:
                if char =='\n':
                    data_line = line.split()
                    s = len(data_line)
                    if s>0 and data_line[0] == '[STATISTICS':
                        m = pattern.findall(line)
                        self.s = len(m)
                        for j in range(self.s):
                            self.results[j].append(float(m[j].replace(',','.')))
                        self.results[len(m)].append(round((end-start)*1000, 2))
                    line =''
                else:
                    line +=char

        process.stdout.close()
        process.stderr.close()


mydatab = None
if database is not None:
    mydatab = database.Database()
    mydatab.createTables()
    mydatab.openSession(con)

for line in f:
    if line[0] != '#' and line != '\n':
        line = line.rstrip("\n")
        print line
        command = CMD+' -loop '+ str(loop) +' -args "'+line + ' -log QUIET"'
        args = shlex.split(command)

        current  = runit(command, 0, 0)
        current.start()
        current.join()

#        compute(line, current.results, current.s)
        computeXLS(line, current.results, current.s)
        if database is not None:
            storeInDB(mydatab, line, current.results)
        out.write('\n')
        out.flush()
out.close()
if database is not None & plotmysql is not None:
    mydatab.plot()

print ">> End of script"




  
