__author__ = 'kyzrsoze'

import argparse
import os
from pylatex import Document, Section, Subsection, Tabular, TikZ, Axis, \
    Plot, Package, Subsubsection, MultiColumn, Command, NoEscape

from utils import LogExtractor as le, PDFCreator

parser = argparse.ArgumentParser(description='Pretty XCSP3 log files.')
parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of XCSP3 files to pretty.',
    default=
    '/cprudhom/Nextcloud/50-Choco/XCSP/Challenges/inst/2018/xcsp18.txt'
                    #'/Users/cprudhom/Nextcloud/50-Choco/XCSP/Challenges/inst/2018/xcsp3-sm.txt'
    # '/Users/cprudhom/Nextcloud/50-Choco/XCSP/Challenges/inst/2018/xcsp3-sat.txt'
)
parser.add_argument(
    "-d", "--directory",
    help="Log files directory.",
    default=[
        '/Users/cprudhom/Nextcloud/50-Choco/XCSP/Challenges/logs/xcps18/20190513',
        '/Users/cprudhom/Nextcloud/50-Choco/XCSP/Challenges/logs/xcps18/20190612'
    ]
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name\'',
    nargs='+',
    default=[
        'IBS+',
        'IDL+IBS',
        'CPL+IBS',
        'MIXED',
        'PROB'
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


maxtime=2399.#599.
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


args = parser.parse_args()

lex = le.LogExtractor("xcsp",maxobj=maxobj, maxtime=maxtime)

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
        m = 0.
        for o in range(len(options)):
            d = 0
            while(d < len(args.directory)-1 and
                  os.path.isfile(os.path.join(args.directory[d], fname + '+' + options[o] + '.log')) is False):
                d = d+1
            solution = lex.read(args.directory[d], fname, options[o])
            optPerSol[fname].append(solution)
            m = max(m, float(solution[1]))
        if m < 2:
            print("clear " + fname)
            optPerSol[fname].clear()


timPerOpt = {}
for opt in options:  # init
    timPerOpt[opt] = []
timPerOpt['GOD'] = []
for fname in fnames:
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
pdf.publish(filelist=args.filelist, options=options, timPerOpt=timPerOpt, optPerSol=optPerSol, fnames=fnames,
            maxtime=maxtime, bestever={},details="xcsp")
