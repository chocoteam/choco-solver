__author__ = 'kyzrsoze'

from extractFromLog import read
import argparse
from pylatex import Document, Section, Subsection, Table, TikZ, Axis, \
    Plot, Package, Subsubsection


parser = argparse.ArgumentParser(description='Pretty flatzinc log files.')
parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of flatzinc files to pretty.',
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/list2012.txt'
)
parser.add_argument(
    "-d", "--directory",
    help="Log files directory.",
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/logs/2012'
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name:options\'',
    nargs='+',
    default=[
        'fixed',
        ]
)


def addPlots(doc, options, coords):
    with doc.create(Subsection('Plot')):
        with doc.create(TikZ()):
            with doc.create(Axis(
                    options='height=5cm, width=10cm, legend style ={legend pos=outer north east}')) as plot:
                for opt in options:
                    plot.append(Plot(name=opt, coordinates=list(coords[opt])))


args = parser.parse_args()

optPerSol = {}
fnames = []
options = args.configurations
best = {}  # store the best know values
# analyse all solutions from all configurations
with open(args.filelist, 'r') as f:
    for fname in f:
        fname = fname.replace('\n', '')
        fnames.append(fname)
        optPerSol[fname] = []
        best[fname] = 999999999
        for opt in options:
            solution = read(args.directory, fname, opt)
            optPerSol[fname].append(solution)
            print(solution)
            if solution[3] != 'SAT':
                if best[fname] == 999999999:
                    best[fname] = solution[4]
                else:
                    if solution[3] == 'MIN' and best[fname] > solution[4]:
                        best[fname] = solution[4]
                    if solution[3] == 'MAX' and best[fname] < solution[4]:
                        best[fname] = solution[4]

timPerOpt = {}
for opt in options:  # init
    timPerOpt[opt] = []
for fname in fnames:
    for solution in optPerSol[fname]:
        tim = solution[1]
        if solution[3] != 'SAT':
            if best[fname] == 999999999:
                tim =900.
            elif solution[3] == 'MIN' and best[fname] < solution[4]:
                tim =900.
            elif solution[3] == 'MAX' and best[fname] > solution[4]:
                tim =900.
        timPerOpt[solution[6]].append(tim)


# Then start the document
doc = Document(title='Benchmark analysis of %d problems and %d configurations.' % (len(fnames), len(options)),
               author='Charles Prud\'homme', maketitle=True)
doc.packages.append(Package('geometry'))
presec = ""
prevsubsec = ""
section = None
subsection = None
# First global view
section = Section('Global')
doc.append(section)

with doc.create(TikZ()):
    with doc.create(Axis(options='height=20cm, width=15cm, legend style ={legend pos=outer north east}')) as plot:
        for opt in options:
            times = timPerOpt[opt]
            times.sort()
            coords = []
            for i in range(0, len(times)):
                coords.append((i, times[i]))
            plot.append(Plot(name=opt, coordinates=coords))

coords = {}
for o in options:
    coords[o] = []
k = 0
# Second problem per problem
for fname in fnames:
    parts = fname.split("+")
    solutions = optPerSol[fname]
    if parts[0] != presec:
        presec = parts[0]
        if k > 0:
            addPlots(doc, options, coords)
            for o in options:
                coords[o].clear()
            k = 0
        section = Section('%s' % (presec.replace("_", "\_")))
        doc.append(section)
        print("create section: " + presec)

    if parts[1] != prevsubsec:
        prevsubsec = parts[1]
        subsection = Subsection('%s' % (prevsubsec.replace("_", "\_")))
        section.append(subsection)
        print("create subsection: " + prevsubsec)

    if len(parts) > 2:
        subsubsection = Subsubsection('%s' % (parts[2].replace("_", "\_")))
        subsection.append(subsubsection)
        print("create subsubsection: " + parts[2])
    else:
        subsubsection = Subsubsection('%s' % (parts[1].replace("_", "\_")))
        subsection.append(subsubsection)
        print("create subsubsection: " + parts[1])

    if solutions[0][3] == 'SAT':
        solutions.sort(key=lambda x: (x[3], x[1]))
        table = Table('l|r|l|r|r')
        subsubsection.append(table)
        table.add_hline()
        table.add_row(("Param.", 'Status', "\#Sol", 'Time(sec)', 'Nodes'))
        table.add_hline()
        for i in range(0, len(solutions)):
            table.add_row((solutions[i][6], solutions[i][5], solutions[i][0], solutions[i][1], solutions[i][2]))
            coords[solutions[i][6]].append((k, solutions[i][1]))
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

        table = Table('l|r|l|r|r|r')
        subsubsection.append(table)

        table.add_hline()
        table.add_row(("Param.", type, 'Status', "\#Sol", 'Time(sec)', 'Nodes'))
        table.add_hline()
        for i in range(0, len(solutions)):
            table.add_row(
                (solutions[i][6], solutions[i][4], solutions[i][5], solutions[i][0], solutions[i][1], solutions[i][2]))
            if solutions[i][4] == best:
                coords[solutions[i][6]].append((k, solutions[i][1]))
            else:
                coords[solutions[i][6]].append((k, 900.0))
        table.add_hline()
    k += 1
if k > 0:
    addPlots(doc, options, coords)
    for o in options:
        coords[o].clear()
    k = 0

doc.generate_pdf(
    # filename='results',
    clean=True)