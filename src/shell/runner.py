#!/usr/bin/python
# CONSTANTS
## file name containing list of command to run
import sys, subprocess, shlex, time, threading, os, signal, re, logging
from threading import Thread
from os.path import join

try:
    import MySQLdb
    import database
except ImportError:
    database = None
try:
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
host = ''
user = ''
pwd = ''
dbname = ''
home = '../../'

## java command
JAVA = 'java -Xmx1024m -Xms1024m '# -XX:+AggressiveOpts -XX:+UseConcMarkSweepG'


## Number of time a problem is run
loop = 1 # can be override
## time limit for a process
timelimit = 180 #in seconds => 10min
## number threads used
thread = 1
## insert in db
db = False
## plot results
plot = False
## front end running
frontend = True

## regexp for statisctics
## [STATISTICS S Solutions, Objective: O, Resolution Ts (tms), N Nodes, B Backtracks, F Fails, R Restarts, P + P propagations]
## len is 8 or 9
_SIZE = 15
pattern = re.compile('\d+\,?\d*')
_STAT = '[STATISTICS'
_NAMES = 'SOLUTION', 'OBJECTIVE', 'TIME', 'NODE', 'BACKTRACK', 'FAIL', 'RESTART', 'PROPAGATIONS', 'SCRIPT_TIME'


def readParameters(paramlist):
    global loop
    global timelimit
    global thread
    global name
    global dir
    global host
    global user
    global pwd
    global dbname
    global home
    global db
    global plot
    global frontend
    offset = 2
    if len(paramlist) > 0:
        if paramlist[0] == "-l": # option for number of time a problem must be run
            loop = int(paramlist[1])
        elif paramlist[0] == "-t": # time limit before killing a process
            timelimit = int(paramlist[1])
        elif paramlist[0] == "-j": # number of thread to use in a loop
            thread = int(paramlist[1])
        elif paramlist[0] == "-n": # subname of output files
            name = paramlist[1]
        elif paramlist[0] == "-d": # directory of output files
            dir = paramlist[1]
        elif paramlist[0] == "-host": # user pwd for db connexion
            host = paramlist[1]
        elif paramlist[0] == "-user": # user for db connexion
            user = paramlist[1]
        elif paramlist[0] == "-pwd": # user pwd for db connexion
            pwd = paramlist[1]
        elif paramlist[0] == "-dbname": # dbname pwd for db connexion
            dbname = paramlist[1]
        elif paramlist[0] == "-h": # user pwd for db connexion
            home = paramlist[1]
        elif paramlist[0] == "-db": # database
            db = True and (database is not None)
            offset = 1
        elif paramlist[0] == "-plot": # no plot
            plot = True and (database is not None) and (plotmysql is not None)
            offset = 1
        elif paramlist[0] == "-nofe": # no front end
            frontend = False
            offset = 1
        readParameters(paramlist[offset:])


def checkParam():
    if not frontend and loop > 1:
        print 'Loop parameter is not considered as front end option is off'
    if db | plot:
        print host
        if host == '': raise Exception('database cnx : host must be defined')
        if user == '': raise Exception('database cnx : user must be defined')
        if dbname == '': raise Exception('database cnx : dbname must be defined')


def buildCmd():
    CMD = JAVA + ' ' + CP + ' '
    if frontend:
        CMD += ' samples.FrontEndBenchmarking -loop ' + str(loop) + ' -args "' + line + ' -log QUIET"'
    else:
        CMD += line + ' -log QUIET'
    print CMD
    return CMD


def buildLog(name, ext, level):
    hdlr = logging.FileHandler(join(dir, name + ext))
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


def computeXLS(line, result):
    line = line.rstrip("\n")
    csv.write(line + ";")
    for i in range(len(result)):
        sum = 0.0
        s = len(result[i])
        result[i].sort()
        if not s:
            return
        if s > 2:
            result[i] = result[i][1:s - 1]

        for j in range(len(result[i])):
            sum += result[i][j]
        moy = round(sum / len(result[i]), 6)
        stdev = max(result[i]) - min(result[i])
        info = "" + str(moy) + ";"
        info += "" + str(stdev) + ";"
        csv.write(info)
    csv.write('\n')
    csv.flush()


def storeInDB(mydatab, line, results):
    parts = line.split(" ", 1) # separate name of problem and parameters
    parts = parts + [" "]
    mydatab.insertValues(parts[0], parts[1], results)


class runit(Thread):
    def __init__ (self, args, i, j):
        Thread.__init__(self)
        self.args = args
        self.results = [[]for k in range(_SIZE)]
        self.i = i
        self.j = j
        self.s = _SIZE

    def run(self):
    #        print str(self.i)+'_'+str(self.j)
        process = subprocess.Popen(args, bufsize=0, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        start = time.time()
        clock = limit(process, timelimit * loop)
        process.wait()
        end = time.time()
        output, error = process.communicate()
        clock.cancel()
        line = ''
        if len(error) > 0:
            csv.write("error - see log file\n")
            err.error(args)
            for char in error:
                if char == '\n':
                    err.error(line)
                    line = ''
                else:
                    line += char
            err.error('\n')
        else:
            for char in output:
                if char == '\n':
                    data_line = line.split()
                    s = len(data_line)
                    if s > 0 and data_line[0] == '[STATISTICS':
                        m = pattern.findall(line)
                        #                        print line
                        txt.write(line)
                        txt.write('\n')
                        txt.flush()
                        self.s = len(m)
                        for j in range(self.s):
                            self.results[j].append(float(m[j].replace(',', '.')))
                        self.results[len(m)].append(round((end - start) * 1000, 2))
                    line = ''
                else:
                    line += char

        process.stdout.close()
        process.stderr.close()


readParameters(sys.argv[1:])
checkParam()

# initialize env. variables
CHOCO_SOLVER = join(home, 'solver', 'target', 'solver-rocs-1.0-SNAPSHOT-with-dep.jar')
CP = '-cp .:' + CHOCO_SOLVER
#print CHOCO_SOLVER

csv = open(join(dir, name + '.csv'), 'w', 10)
# HEADERS
csv.write("PROB_NAME;NB_SOL;stdev;BUIL_T;stdev;INIT_T;stdev;\
INIT_PROP_T;stdev;RES_T_SEC;stdev;RES_T_MS;stdev;TIME;stdev;\
OBJ;stdev;NODES;stdev;BKS;stdev;FAILS;stdev;RESTARTS;stdev;\
FINE_ER;stdev;COARSE_ER;stdev;SCRIPT_T;stdev;\n")

txt = open(join(dir, name + '.txt'), 'w', 10)

err = buildLog(name, '.log', logging.INFO)

print ">> Run " + str(loop) + " time(s)"

f = open(join(dir, name + '.list'), 'r')

mydatab = None
if (database is not None) and (db | plot):
    mydatab = database.Database(host, user, pwd, dbname)

if (database is not None) and db:
    mydatab.createTables()
    mydatab.openSession(name)

## get commands per thread
runlist = [[]]
i = 0
j = 0
runlist.append([])
for line in f:
    if line[0] != '#' and line != '\n':
        if i == len(runlist):
            runlist.append([])
        line = line.rstrip("\n")
        runlist[i].append(line)
        j += 1
        if j == thread:
            j = 0
            i += 1

print 'Start resolving'
for i in range(len(runlist)):
    runner = []
    for t in range(len(runlist[i])):
        line = runlist[i][t]
        txt.write(line+": \n")
        command = buildCmd()
        args = shlex.split(command)
        current = runit(command, i, t)
        runner.append(current)
        current.start()

    for run in runner:
        run.join()
        computeXLS(line, run.results)
        if (database is not None) and db:
            storeInDB(mydatab, line, run.results)
    print str((i + 1) * 100 / len(runlist)) + '% done'

csv.close()
txt.close()

if (database is not None) and (plotmysql is not None) and plot:
    mydatab.plot()

print ">> End of script"




  
