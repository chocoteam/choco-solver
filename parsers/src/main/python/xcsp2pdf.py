__author__ = 'kyzrsoze'

import argparse
import os
from pylatex import Document, Section, Subsection, Tabular, TikZ, Axis, \
    Plot, Package, Subsubsection, MultiColumn, Command, NoEscape

from utils import LogExtractor as le, PDFCreator
from pathlib import Path

home = str(Path.home())

parser = argparse.ArgumentParser(description='Pretty XCSP3 log files.')
parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of XCSP3 files to pretty.',
    default=
    # home + '/cloudUniv/50-Choco/XCSP/Challenges/inst/2018/xcsp3-5s.txt'
    "/Users/kyzrsoze/cloudUniv/50-Choco/XCSP/Challenges/inst/2018/all.txt"
)
parser.add_argument(
    "-d", "--directory",
    help="Log files directory.",
    default=[
        # home + '/cloudUniv/50-Choco/XCSP/Challenges/logs/xcps18/20200617',
        # '/Users/cprudhom/Nextcloud/50-Choco/XCSP/Challenges/logs/xcps18/20200629'
        "/Users/kyzrsoze/Sources/XCSP3/logs/20200626",
        "/Users/kyzrsoze/Sources/XCSP3/logs/20200629",
        "/Users/kyzrsoze/Sources/XCSP3/logs/20200630",
        "/Users/kyzrsoze/Sources/XCSP3/logs/20200715"
    ]
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name\'',
    nargs='+',
    default=[
        #'CUR',
        #'WDEG',
        'WDEGMIN',
        #'CACD',
        'CACDMIN',
        #'CHS',
        'CHSMIN',
        # 'BB1',
        # 'BB2',
        # 'ABS'
        #'PRLL', 'PRLL2'
    ]
)

parser.add_argument(
    "-int", "--intersection",
    help="Consider intersection of problems",
    default=True,
)

parser.add_argument(
    "-pl", "--plot",
    help="Add global plot",
    default=True,
)
parser.add_argument(
    "-det", "--details",
    help="Add all instances' detail",
    default=False,
)

maxtime = 899.  # 599.
maxobj = 999999999


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


args = parser.parse_args()

lex = le.LogExtractor("xcsp", maxobj=maxobj, maxtime=maxtime)

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
        unk = 0
        for o in range(len(options)):
            d = 0
            while (d < len(args.directory) - 1 and
                   os.path.isfile(os.path.join(args.directory[d], fname + '+' + options[o] + '.log')) is False):
                d = d + 1
            solution = lex.read(args.directory[d], fname, options[o])
            if solution[0] == -1:
                solution[0] = 0
                unk += 1
            optPerSol[fname].append(solution)

        if (args.intersection and unk > 0) \
                or (not args.intersection and unk == len(options)):
            print("clear " + fname)
            optPerSol[fname].clear()
            optPerSol.pop(fname)

timPerOpt = {}
for opt in options:  # init
    timPerOpt[opt] = []
timPerOpt['GOD'] = []
for fname in optPerSol:
    solutions = optPerSol[fname]
    if len(solutions) == 0:
        continue
    solutions.sort(key=lambda x: (x[3], x[4], x[1]))
    best = solutions[0][4]
    times = []
    for solution in solutions:
        tim = solution[1]
        if solution[3] == 'MIN':
            if best < solution[4]:
                tim = maxtime
        if solution[3] == 'MAX':
            if best < solution[4]:
                tim = maxtime
        timPerOpt[solution[6]].append(tim)
        times.append(tim)
    timPerOpt['GOD'].append(min(times))

pdf = PDFCreator.PDFCreator()
pdf.publish(filelist=args.filelist, options=options, timPerOpt=timPerOpt, optPerSol=optPerSol, fnames=optPerSol.keys(),
            maxtime=maxtime, bestever={}, plot=args.plot, details=args.details, problems="xcsp")
