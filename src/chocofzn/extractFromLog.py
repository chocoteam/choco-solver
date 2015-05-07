__author__ = 'cprudhom'

import re
from pylatex import Document, Section, Subsection, Table, Math, TikZ, Axis, \
    Plot, Figure, Package
import argparse

rsol = '%.*Solutions,.*'
ropt = '%.*(Minimize|Maximize).*'


def read(dir, fname, opt):
    """
    Read the log file, in dir, postfixed by 'opt'
    :return: a list, the solution
    """
    f = open(dir + fname + '.' + opt + '.log', 'r')
    last = ""
    for line in f:
        if re.search(rsol, line):
            last = line
    # get the last solution, there should be at least one
    # line = line.replace(',', '')
    line = line.replace('s', '')
    parts = line.split()
    solution = []
    solution.append(parts[1])  # nb sol
    if (re.search(ropt, last)):
        # extract values
        solution.append(float(parts[8][:-1].replace(',', '.')))  # time
        solution.append(parts[9])  # nodes
        if parts[3] == 'Minimize':
            solution.append('MIN')
        else:
            solution.append('MAX')
        solution.append(int(parts[6].replace(',', '')))  # obj
    else:
        # extract values
        solution.append(float(parts[4][:-1].replace(',', '.')))  # time
        solution.append(parts[5])  # nodes
        solution.append('SAT')
        solution.append(int(0))  # obj
    solution.append(opt)
    print(solution)
    if solution[1] >= 900.:
        solution[1] = float(900.)

    return solution


parser = argparse.ArgumentParser(description='Pretty flatzinc log files.')
parser.add_argument(
    "-fl", "--filelist",
    help='File containing name of flatzinc files to pretty.',
    default='/Users/kyzrsoze/Sandbox/fzn/fzn2014.txt'
)
parser.add_argument(
    "-d", "--directory",
    help="Log files directory.",
    default='/Users/kyzrsoze/Sandbox/fzn/logs/'
)
parser.add_argument(
    "-c", "--configurations",
    help='Configurations to evaluate, \'name:options\'',
    nargs='+',
    default=['wigc', 'wigc+lc']
)

args = parser.parse_args()

doc = Document(title='Benchmark analysis', author='Charles Prud\'homme')
doc.packages.append(Package('geometry'))

with open(args.filelist, 'r') as f:
    for fname in f:
        fname = fname.replace('\n', '')
        print(fname)
        solutions = []
        for opt in args.configurations:
            solutions.append(read(args.directory, fname, opt))
        with doc.create(Section('%s' % (fname.replace("_", "\_")))):
            if solutions[0][3] == 'SAT':
                solutions.sort(key=lambda x: x[1])
                with doc.create(Table('l|r|r|r')) as table:
                    table.add_hline()
                    table.add_row(("Param.", "\#Sol", 'Time(sec)', 'Nodes'))
                    table.add_hline()
                    for i in range(0, len(solutions)):
                        table.add_row((solutions[i][5], solutions[i][0], solutions[i][1], solutions[i][2]))
                    table.add_hline()
            else:
                if solutions[0][3] == 'MIN':
                    solutions.sort(key=lambda x: (x[4], x[1]))
                else:
                    solutions.sort(key=lambda x: (-x[4], x[1]))
                with doc.create(Table('l|r|r|r|r')) as table:
                    table.add_hline()
                    table.add_row(("Param.", solutions[0][3], "\#Sol", 'Time(sec)', 'Nodes'))
                    table.add_hline()
                    for i in range(0, len(solutions)):
                        table.add_row(
                            (solutions[i][5], solutions[i][4], solutions[i][0], solutions[i][1], solutions[i][2]))
                    table.add_hline()

doc.generate_pdf(True)