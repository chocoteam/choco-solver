__author__ = 'kyzrsoze'

import argparse
import os
from pylatex import Document, Section, Subsection, Tabular, TikZ, Axis, \
    Plot, Package, Subsubsection, MultiColumn, Command, NoEscape

from utils import logExtractor, ChuLogExtractor2

parser = argparse.ArgumentParser(description='Pretty flatzinc log files.')
parser.add_argument(
    "-b", "--benchmark",
    help='A CSV file containing best known solutions.',
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/benchmarks.csv'
)
parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of flatzinc files to pretty.',
    # default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/listALLOPT.txt'
    # default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/sums.txt'
    default=
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_at-s27.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_at.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_bl.txt',
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_ksd15_d.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_la_x.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_pack_d.txt'
    '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_pack.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_psplib_j30.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_psplib_j60.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_psplib_j90.txt'
    # '/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/rcpsp_psplib_j120.txt'
)
parser.add_argument(
    "-d", "--directory",
    help="Log files directory.",
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/logs/sum/rcpsp'
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name:options\'',
    nargs='+',
    default=[
        'EX+S+MZN',
        'EX+G+MZN',
        'DFT+MIC',
        'EX+D+MIC',
        'EX+S+MIC',
        'EX+G+MIC',
        'CHU',
        ]
)

parser.add_argument(
    "-pl", "--plot",
    help="Add global plot",
    default=True,
)
parser.add_argument(
    "-det", "--details",
    help="Add all instances' detail",
    default=True,
)


maxtime=599.
maxobj=999999999

def addTimePlots(doc, options, coords):
    with doc.create(Subsection('CPU Time')):
        with doc.create(TikZ()):
            with doc.create(Axis(
                    options='xlabel=Instances, ylabel=CPU time (s), height=5cm, width=10cm,legend pos=outer north east')) as plot:
                for opt in options:
                    plot.append(Plot(name=opt, coordinates=list(coords[opt])))


def addObjPlots(doc, options, objs, pol):
    with doc.create(Subsection('Objective')):
        with doc.create(TikZ()):
            with doc.create(Axis(
                    options='log basis y=10,xlabel=Instances, ylabel=Objective (%s), height=5cm, width=10cm,legend pos=outer north east' % pol)) as plot:
                for opt in options:
                    plot.append(Plot(name=opt, coordinates=list(objs[opt])))
                plot.append(Plot(name='syb', coordinates=list(objs['syb'])))

## LOAD BEST KNOW RESULTS (extracted from MZN website)
def loadBestEver(benchs):
    map = {}
    with open(benchs, 'r') as f:
        f.readline() # to skip the first line
        for line in f:
            parts = line.split(';')
            # fname=(parts[1]+"/"+parts[2])
            fname=(parts[2])
            res = [parts[4]]
            obj = parts[5].replace('\n','')
            if obj != '':
                res.append(int(obj))
            map[fname] = res
    return map


args = parser.parse_args()

bestever = loadBestEver(args.benchmark)
# print(bestever)


optPerSol = {}
fnames = []
options = args.configurations
# analyse all solutions from all configurations
with open(args.filelist, 'r') as f:
   for fname in f:
        fname = fname.replace('\n', '')
        print(fname)
        fname = os.path.basename(fname)
        fnames.append(fname)
        optPerSol[fname] = []
        for o in range(len(options)):
            if options[o] == 'CHU':
                solution = ChuLogExtractor2.read(args.directory, fname, maxtime)
            else:
                solution = logExtractor.read(args.directory, fname, options[o], False, maxtime)
            optPerSol[fname].append(solution)
            # print(solution)

timPerOpt = {}
for opt in options:  # init
    timPerOpt[opt] = []
for fname in fnames:
    solutions = optPerSol[fname]
    solutions.sort(key=lambda x: (x[3], x[4], x[1]))
    best = solutions[0][4]

    for solution in solutions:
        tim = solution[1]
        if solution[3] == 'MIN':
            if best < solution[4] or (fname in bestever and len(bestever[fname]) > 1 and bestever[fname][1] < solution[4]):
                tim = maxtime
        if solution[3] == 'MAX':
            if best < solution[4] or (fname in bestever and len(bestever[fname])>1 and bestever[fname][1] > solution[4]):
                tim = maxtime
        timPerOpt[solution[6]].append(tim)

# Then start the document
doc = Document()
doc.preamble.append(Command('title', 'Benchmark analysis of %d problems and %d configurations.' % (len(fnames), len(options))))
doc.preamble.append(Command('author', "Charles Prud'homme"))
doc.append(NoEscape(r'\maketitle'))

doc.packages.append(Package('geometry'))
presec = ""
prevsubsec = ""
section = None
subsection = None


if args.plot:
    # First global view
    section = Section('Global')
    doc.append(section)

    with doc.create(TikZ()):
        with doc.create(Axis(options='xlabel=Instances (ordered wrt to increasing resolution time),'
                                     'ylabel=CPU time (s),height=20cm, width=15cm,legend pos=outer north east')) as plot:
            for opt in options:
                times = timPerOpt[opt]
                times.sort()
                coords = []
                for i in range(0, len(times)):
                    coords.append((i, times[i]))
                plot.append(Plot(name=opt, coordinates=coords))

if args.plot:
    # First global view
    section = Section('Global + log')
    doc.append(section)

    with doc.create(TikZ()):
        with doc.create(Axis(options='xlabel=Instances (ordered wrt to increasing resolution time), ymode=log,'
                                     'ylabel=CPU time (s),height=20cm, width=15cm,legend pos=outer north east')) as plot:
            for opt in options:
                times = timPerOpt[opt]
                times.sort()
                coords = []
                for i in range(0, len(times)):
                    coords.append((i, times[i]))
                plot.append(Plot(name=opt, coordinates=coords))

coords = {}
objs = {}
for o in options:
    coords[o] = []
    objs[o] = []
objs['syb'] = []
pol='SAT'
# Second summary
section = Section('Summary : %d problems, %d configurations.' % (len(fnames), len(options)))
doc.append(section)
table = Tabular('|l||c|c||c|c||c|')
table.add_hline()
table.add_row(("", MultiColumn(2, align='c||', data="CSP"), MultiColumn(2, align='c||',data='COP'), "Times best"))
table.add_row(("Config.", 'sat', "unsat", "best", "proof","< %.1f" % maxtime))
table.add_hline()
for opt in options:
    sat = 0
    unsat = 0
    proof = 0
    fbest = 0
    tbest = 0
    for fname in fnames:
        print(opt + '->' + fname)
        solutions = optPerSol[fname]
        gbest = solutions[0][4]
        mybest = gbest
        gtime = solutions[0][1]
        mytime = gtime
        b = 0
        for i in range(0, len(solutions)):
            if solutions[i][6] == opt:
                if solutions[i][5] == 'proof':
                    proof += 1
                    b +=1
                elif solutions[i][5] != 'unknown':
                    b += 1
                mybest = solutions[i][4]
                mytime = solutions[i][1]
            gtime = min(gtime, solutions[i][1])
            if solutions[0][3] == 'MIN':
                gbest = min(gbest, solutions[i][4])
            else:
                gbest = max(gbest, solutions[i][4])

        if gbest == mybest and b > 0:
            fbest += 1
        if gtime == mytime and mytime < maxtime:
            tbest += 1
    table.add_row((opt, sat, unsat, fbest, proof, tbest))
# now VBS
sat = 0
unsat = 0
proof = 0
fbest = 0
tbest = 0
for fname in fnames:
    solutions = optPerSol[fname]
    gbest = solutions[0][4]
    gtime = solutions[0][1]
    p = 0
    b = 0
    for i in range(0, len(solutions)):
        if solutions[i][5] == 'proof':
            p += 1
            b += 1
        elif solutions[i][5] != 'unknown':
            b += 1
        gtime = min(gtime, solutions[i][1])
        if solutions[0][3] == 'MIN':
            gbest = min(gbest, solutions[i][4])
        else:
            gbest = max(gbest, solutions[i][4])

    if p > 0:
        proof += 1
    if b > 0:
        fbest += 1
    if gtime < maxtime:
        tbest += 1
table.add_hline()
table.add_hline()
table.add_row(('VBS', sat, unsat, fbest, proof, tbest))
# now Sybille
sat = 0
unsat = 0
proof = 0
fbest = 0
tbest = 0
for fname in fnames:
    solutions = optPerSol[fname]
    gbest = maxobj
    if fname in bestever :
        if len(bestever[fname]) > 1:
            fbest += 1
        if 'C' in bestever[fname][0]:
            proof += 1
table.add_hline()
table.add_hline()
table.add_row(('syb', sat, unsat, fbest, proof, "--"))

table.add_hline()
section.append(table)

if args.details:
    # Third problem per problem
    k = 0
    for fname in fnames:
        parts = fname.split("+")
        solutions = optPerSol[fname]
        if parts[0] != presec:
            presec = parts[0]
            if k > 0:
                addTimePlots(doc, options, coords)
                for o in options:
                    coords[o].clear()
                k = 0
                if len(objs)>0:
                    addObjPlots(doc, options, objs, pol)
                    for o in objs.keys():
                        objs[o].clear()

            section = Section('%s' % (presec))#.replace("_", "\_")))
            doc.append(section)
            print("create section: " + presec)

        if parts[1] != prevsubsec:
            prevsubsec = parts[1]
            subsection = Subsection('%s' % (prevsubsec))#.replace("_", "\_")))
            section.append(subsection)
            print("create subsection: " + prevsubsec)

        if len(parts) > 2:
            subsubsection = Subsubsection('%s' % (parts[2]))#.replace("_", "\_")))
            subsection.append(subsubsection)
            print("create subsubsection: " + parts[2])
        else:
            subsubsection = Subsubsection('%s' % (parts[1]))#.replace("_", "\_")))
            subsection.append(subsubsection)
            print("create subsubsection: " + parts[1])


        pol=solutions[0][3]
        if solutions[0][3] == 'SAT':
            solutions.sort(key=lambda x: (x[3], x[1]))
            table = Tabular('l|r|l|r|r|r')
            subsubsection.append(table)
            table.add_hline()
            table.add_row(("Config.", 'Status', "#Sol", 'Time(sec)', 'Build(sec)', 'Nodes'))
            table.add_hline()
            for i in range(0, len(solutions)):
                table.add_row((solutions[i][6], solutions[i][5], solutions[i][0], solutions[i][1], solutions[i][7],
                               solutions[i][2]))
                coords[solutions[i][6]].append((k, solutions[i][1]))
            table.add_hline()
            table.add_hline()
            # add syb
            if fname in bestever :
                table.add_row("syb", bestever[fname][0], "--", "--", "--", "--")
            table.add_hline()
        else:
            # sort for MIN
            type = 'MIN'
            solutions.sort(key=lambda x: (x[3], x[4], x[1]))
            best = solutions[0][4]
            # check first row and last row
            if solutions[0][3] == 'MAX' or solutions[len(solutions) - 1][3] == 'MAX':
                solutions.sort(key=lambda x: (x[3], -x[4], x[1]))
                best = solutions[0][4]
                type = 'MAX'

            table = Tabular('l|r|l|r|r|r|r')
            subsubsection.append(table)

            table.add_hline()
            table.add_row(("Config.", type, 'Status', "#Sol", 'Time(sec)', 'Build(sec)', 'Nodes'))
            table.add_hline()
            for i in range(0, len(solutions)):
                table.add_row((solutions[i][6], solutions[i][4], solutions[i][5], solutions[i][0], solutions[i][1],
                               solutions[i][7], solutions[i][2]))
                if solutions[i][4] == best:
                    coords[solutions[i][6]].append((k, solutions[i][1]))
                else:
                    coords[solutions[i][6]].append((k, maxtime))
                if int(solutions[i][0]) > 0:
                    objs[solutions[i][6]].append((k, solutions[i][4]))
            table.add_hline()
            table.add_hline()
            # add syb
            if fname in bestever :
                if len(bestever[fname])>1:
                    table.add_row("syb", bestever[fname][1],bestever[fname][0], "--", "--", "--", "--")
                    objs['syb'].append((k, bestever[fname][1]))
                else:
                    table.add_row("syb", "--",bestever[fname][0], "--", "--", "--", "--")
            table.add_hline()

        k += 1
    if k > 0:
        addTimePlots(doc, options, coords)
        for o in options:
            coords[o].clear()
        k = 0
        if len(objs) > 0:
            addObjPlots(doc, options, objs, pol)
            for o in objs.keys():
                objs[o].clear()


name = os.path.basename(args.filelist)
print(name)
doc.generate_pdf(
   filepath=name[:-4],
   clean=True,silent=False)
