__author__ = 'kyzrsoze'

import argparse
import os

from utils import LogExtractor, ChuLogExtractor2, PDFCreator

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
        #'EX+S+MZN',
        #'EX+G+MZN',
        'DFT+MIC',
        'EX+D+MIC',
        'EX+S+MIC',
        #'EX+G+MIC',
        #'CHU',
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

lex = LogExtractor.LogExtractor("fzn", maxobj=maxobj, maxtime=maxtime)

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
                solution = lex.read(args.directory, fname, options[o])
            optPerSol[fname].append(solution)
            # print(solution)

timPerOpt = {}
for opt in options:  # init
    timPerOpt[opt] = []
timPerOpt['GOD'] = []
for fname in fnames:
    solutions = optPerSol[fname]
    solutions.sort(key=lambda x: (x[3], x[4], x[1]))
    best = solutions[0][4]
    times = []
    for solution in solutions:
        tim = solution[1]
        if solution[3] == 'MIN':
            if best < solution[4] or (fname in bestever and len(bestever[fname]) > 1 and bestever[fname][1] < solution[4]):
                tim = maxtime
        if solution[3] == 'MAX':
            if best < solution[4] or (fname in bestever and len(bestever[fname])>1 and bestever[fname][1] > solution[4]):
                tim = maxtime
        timPerOpt[solution[6]].append(tim)
        times.append(tim)
    timPerOpt['GOD'].append(min(times))

pdf = PDFCreator.PDFCreator()
pdf.publish(filelist=args.filelist, options=options, timPerOpt=timPerOpt, optPerSol=optPerSol, fnames=fnames,
            maxtime=maxtime, bestever=bestever)
