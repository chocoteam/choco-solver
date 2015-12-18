__author__ = 'cprudhom'

import configparser
import pymysql.cursors
from collections import OrderedDict
from pylatex import Document, Section, Subsection, Table, Math, TikZ, Axis, \
    Plot, Figure, Package


class result:
    name = ""  # pb name
    res = ""  # SAT, MIN or MAX
    bobj = -1  # best know objective
    opt = 0  # optimality proven
    stim = 900  # solving time
    obj = 0  # best value
    nsol = 0  # nb solutions
    nnod = 0  # nb nodes
    nfail = 0  # nb fails
    bug = False  # knwon to be buggy

    def __init__(self, row):
        self.name = row.get('NAME')
        self.res = row.get('RESOLUTION')
        self.bobj = row.get('BOBJ')
        self.opt = row.get('OPTIMAL')
        self.stim = row.get('SOLVING_TIME')
        self.obj = row.get('OBJ')
        self.nsol = row.get('NB_SOL')
        self.nnod = row.get('NB_NODES')
        self.nfail = row.get('NB_FAILS')
        self.bug = row.get('BUG')


rqtB = "SELECT * FROM BENCHMARKS ORDER BY BID"
rqtR = "SELECT P.NAME, P.PID, P.RESOLUTION, P.OBJECTIVE as BOBJ, P.OPTIMAL, R.SOLVING_TIME, R.OBJECTIVE as OBJ, R.NB_SOL, R.NB_NODES, R.NB_FAILS, R.BUG" \
       " FROM RESOLUTIONS as R, PROBLEMS as P WHERE R.BID = '%d' and P.PID=R.PID ORDER BY PID"

## Global variable

# Name of bids
biddics = {}


def connect(config):
    # Connect to the database
    connection = pymysql.connect(host=config.get('database','host'),
                                 user=config.get('database','user'),
                                 passwd=config.get('database','passwd'),
                                 db=config.get('database','db'),
                                 cursorclass=pymysql.cursors.DictCursor)
    return connection


def getBenchmarks(connection):
    """
    Get the list of benchmarks from the database
    :param connection: the connection
    :return: the two l
    """
    biddics = {}
    cursor = connection.cursor()
    cursor.execute(rqtB)
    results = cursor.fetchall()
    for row in results:
        bid = row.get('BID')
        name = row.get('NAME')
        dtime = row.get('DATE')
        print("\t[%d] %s (%s)" % (bid, name, dtime))
        biddics[bid] = name
    return biddics


def getResults(bid):
    """
    Extract the result for a given BID
    :param bid: a benchmark ID
    :return: dictionnary of result {pid, result}
    """
    pids = {}
    cursor = connection.cursor()
    cursor.execute(rqtR % (bid))
    results = cursor.fetchall()
    for row in results:
        pid = row.get('PID')
        mres = result(row)
        pids[pid] = mres
    print(len(pids))
    return pids


def getEqualities(rs1, rs2):
    """
    Return the dictionnaty (pid, stim) of problems solved equally with the two approaches
    :param rs1: result set of the 1st approach
    :param rs2: result set of the 2nd approach
    :return: a dictionnary {pid, stim}
    """
    eqs = set()
    for pid in rs1.keys():
        r1 = rs1[pid]
        both = True
        if r1 is None:
            print("Unknow problem ID '%d' for BID '%d'" % (pid, bid1))
            both = False
        r2 = rs2[pid]
        if r2 is None:
            print("Unknow problem ID '%d' for BID '%d'" % (pid, bid2))
            both = False
        if both:
            if (r1.stim < 900 and r2.stim < 900):
                d = r1.stim - r2.stim
                if d == 0:
                    eqs.add(pid)
    return eqs


def getBothTimedOut(rs1, rs2):
    """
    Return the set of problem IDs which both timed out
    :param rs1: result set of the 1st approach
    :param rs2: result set of the 2nd approach
    :return: a set {pid, stim}
    """
    bto = set()
    for pid in rs1.keys():
        r1 = rs1[pid]
        both = True
        if r1 is None:
            print("Unknow problem ID '%d' for BID '%d'" % (pid, bid1))
            both = False
        r2 = rs2[pid]
        if r2 is None:
            print("Unknow problem ID '%d' for BID '%d'" % (pid, bid2))
            both = False
        if both:
            if (r1.stim >= 900 and r2.stim >= 900):
                bto.add(pid)
    return bto


def getIncomparable(rs1, rs2):
    """
    Return the set of problem IDs which are only solved by rs1
    :param rs1: result set of the 1st approach
    :param rs2: result set of the 2nd approach
    :return: a set {pid, stim}
    """
    inc = set()
    for pid in rs1.keys():
        r2 = rs2[pid]
        if r2 is None:
            inc.add(pid)
    return inc


def getBest(set1, set2):
    """
    Return the set of problem IDs better solved with rs1 in comparison with rs2
    :param set1: a results set
    :param set2: another results set
    :return: a set {pid, stim}
    """
    bst = set()
    for pid in set1.keys():
        r1 = set1[pid]
        both = True
        if r1 is None:
            print("Unknow problem ID '%d' for BID '%d'" % (pid, bid1))
            both = False
        r2 = set2[pid]
        if r2 is None:
            print("Unknow problem ID '%d' for BID '%d'" % (pid, bid2))
            both = False
        if both:
            if (r1.stim < r2.stim and r1.stim < 900):
                bst.add(pid)
    return bst


def retrieveName(list, result):
    """
    Retrieve the name of the problems, and sort the dictionary wrt to their name
    :param list: list of specific pids
    :param result: a list of result -- to get the name
    :return:
    """
    dict = OrderedDict()
    for pid in list:
        name = result.get(pid).name
        name = name.replace("_", "\_")
        dict[name] = pid
    return OrderedDict(sorted(dict.items(), key=lambda t: t[0]))


config = configparser.ConfigParser()
config.read('database.ini')

connection = connect(config)

# List benchmarks
print("Available benchmarks:")
biddics = getBenchmarks(connection)

bid1 = sorted(biddics.keys())[-1]
bid2 = sorted(biddics.keys())[-2]

# BID selection
bid1 = int(input("\nChoose one BID : (default %d)" % bid1) or int(bid1))
print("First benchmark selected is : %d" % bid1)
bid2 = int(input("\nChoose another BID : (default %d)" % bid2) or int(bid2))
print("Second benchmark selected is : %d" % bid2)

results1 = getResults(bid1)
results2 = getResults(bid2)

eqs = getEqualities(results1, results2)
sorted(eqs)
bto = getBothTimedOut(results1, results2)
bst1 = getBest(results1, results2)
bst2 = getBest(results2, results1)
inc1 = getIncomparable(results1, results2)
inc2 = getIncomparable(results2, results1)
print(bst2)
connection.close()


# Now start document
doc = Document(title='Comparing %s and %s' % (biddics[bid1], biddics[bid2]), author='Charles Prud\'homme')
doc.packages.append(Package('geometry'))

with doc.create(Section('Overview')):
    with doc.create(Table('l|c')) as table:
        table.add_hline()
        table.add_row(("Comment", "Number"))
        table.add_hline()
        table.add_row(("Equality", len(eqs)))
        table.add_row((biddics[bid1], len(bst1)))
        table.add_row((biddics[bid2], len(bst2)))
        table.add_row(("Both TO", len(bto)))
        table.add_row(("%s only" % biddics[bid1], len(inc1)))
        table.add_row(("%s only" % biddics[bid2], len(inc2)))
        table.add_hline()

    with doc.create(Subsection('Equality')):
        with doc.create(Table('r|l|r')) as table:
            table.add_hline()
            table.add_row(("pid", "Name", "Time (sec)"))
            table.add_hline()
            dict = retrieveName(eqs, results1)
            for name in dict.keys():
                pid = dict.get(name)
                table.add_row((pid, name, results1.get(pid).stim))
            table.add_hline()

    with doc.create(Subsection('%s' % (biddics[bid1]))):
        with doc.create(Table('r|l|r|r')) as table:
            table.add_hline()
            table.add_row(("pid", "Name", "Time (sec)", "Gain"))
            table.add_hline()
            dict = retrieveName(bst1, results1)
            for name in dict.keys():
                pid = dict.get(name)
                st1 = results1.get(pid).stim
                st2 = results2.get(pid).stim
                table.add_row((pid, name, st1, st2 - st1))
            table.add_hline()

    with doc.create(Subsection('%s' % (biddics[bid2]))):
        with doc.create(Table('r|l|r|r')) as table:
            table.add_hline()
            table.add_row(("pid", "Name", "Time (sec)", "Gain"))
            table.add_hline()
            dict = retrieveName(bst2, results1)
            for name in dict.keys():
                pid = dict.get(name)
                st1 = results1.get(pid).stim
                st2 = results2.get(pid).stim
                table.add_row((pid, name, st2, st1 - st2))
            table.add_hline()

    with doc.create(Subsection('Both timed out')):
        with doc.create(Table('r|l')) as table:
            table.add_hline()
            table.add_row(("pid", "Name"))
            table.add_hline()
            dict = retrieveName(bst1, results1)
            for name in dict.keys():
                pid = dict.get(name)
                table.add_row((pid, name))
            table.add_hline()

    with doc.create(Subsection('Only solved by %s' % biddics[bid1])):
        with doc.create(Table('r|l')) as table:
            table.add_hline()
            table.add_row(("pid", "Name"))
            table.add_hline()
            dict = retrieveName(inc1, results1)
            for name in dict.keys():
                pid = dict.get(name)
                table.add_row((pid, name))
            table.add_hline()

    with doc.create(Subsection('Only solved by %s' % biddics[bid2])):
        with doc.create(Table('r|l')) as table:
            table.add_hline()
            table.add_row(("pid", "Name"))
            table.add_hline()
            dict = retrieveName(inc2, results2)
            for name in dict.keys():
                pid = dict.get(name)
                table.add_row((pid, name))
            table.add_hline()

doc.generate_pdf(True)
# doc.generate_tex()