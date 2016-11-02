__author__ = 'cprudhom'

import argparse
from pylatex import Document, Section, Subsection, Tabular, TikZ, Axis, \
    Plot, Package, Subsubsection

parser = argparse.ArgumentParser(description='Read arrays from Element constraints.')
parser.add_argument(
    "-d", "--data",
    help='File containing the arrays.',
    default='/Users/cprudhom/Sources/MiniZinc/Challenges/fzn/arrays.txt'
)

args = parser.parse_args()


def alldifferent(array):
    for i in range(len(array) - 1):
        for j in range(i + 1, len(array)):
            if array[i] == array[j]:
                return False
    return True


def monotoneup(array):
    for i in range(len(array) - 1):
        if array[i] > array[i + 1]:
            return False
    return True


def monotonedown(array):
    for i in range(len(array) - 1):
        if array[i] < array[i + 1]:
            return False
    return True


def booleans(array):
    for i in range(len(array)):
        if array[i] != 0 and array[i] != 1:
            return False
    return True


def pseudoBooleans(array):
    for i in range(len(array)):
        if array[i] != 0 and array[i] != -1:
            return False
    return True


def _sawtooth(array):
    k = 0
    while k < len(array) - 1 and array[k] == array[k + 1]:
        k += 1
    if k >= len(array) - 1:
        return False
    up = array[k] <= array[k + 1]
    c = 1
    for i in range(len(array) - 1):
        if up:
            if array[i] > array[i + 1]:
                c += 1
                up = False
        if not up:
            if array[i] < array[i + 1]:
                c += 1
                up = True
    return c


def sawtooth(array, min, max):
    return min <= _sawtooth(array) <= max


def sawtoothRatio(array, r):
    return _sawtooth(array) + 1 <= r * len(array)


def nary(array, d):
    count = {}
    for i in range(len(array)):
        if array[i] in count:
            count[array[i]] += 1
        else:
            count[array[i]] = 1
    return len(count.values()) == d


def joker(array, d):
    count = {}
    for i in range(len(array)):
        if array[i] in count:
            count[array[i]] += 1
        else:
            count[array[i]] = 1
    values = count.values()
    return sorted(values)[-1] >= d * len(array)


def addPlots(doc, coords, name):
    with doc.create(Section(name)):
        for c in coords:
            # with doc.create(Subsection('Plots')):
            doc.append('')
            with doc.create(TikZ()):
                with doc.create(
                        Axis(options='height=5cm, width=12cm, legend style ={legend pos=outer north east}')) as plot:
                    plot.append(Plot(coordinates=list(c)))


# Then start the document
doc = Document(title='Benchmark analysis of Element arrays.',
               author='Charles Prud\'homme', maketitle=True)
doc.packages.append(Package('geometry'))
doc.packages.append(Package('hyperref'))
doc.append("\\hypersetup{colorlinks,citecolor=black,filecolor=black,linkcolor=black,urlcolor=black}")

presec = ""
prevsubsec = ""
section = None
subsection = None

coords = {}
# print("%s" % _sawtooth([1, 6, 20, 4, 15, 13, 9, 3, 19, 12, 17, 7, 17, 5]))

doc.append("\\tableofcontents")
tot = 0
with open(args.data, 'r') as file:
    for line in file:
        line = line.replace('[', '')
        line = line.replace(']', '')
        array = line.split(',')
        array = list(map(int, array))
        name = ""
        tot += 1
        r = (_sawtooth(array) + 1) / len(array)
        name += "sawtooth[%.1f]" % r
        # if booleans(array) or pseudoBooleans(array):
        # name += "boolean, "
        # if sawtoothRatio(array, .10):
        # name += "sawtooth[.33], "
        # elif monotoneup(array) or monotonedown(array):
        #         name += "monotone, "
        #     elif sawtooth(array, 1, 1):
        #         name += "pick or valley, "
        #     elif sawtooth(array, 2, 6):
        #         name += "sawtooth[2,6], "
        # else:
        #     if nary(array, 2):
        #         name += "binary, "
        #     elif nary(array, 3):
        #         name += "ternary, "
        #     if sawtoothRatio(array, .33):
        #         name += "sawtooth[.33], "
        #     elif monotoneup(array) or monotonedown(array):
        #         name += "monotone, "
        #     elif sawtooth(array, 1, 1):
        #         name += "pick or valley, "
        #     elif sawtooth(array, 2, 6):
        #         name += "sawtooth[2,6], "
        # elif alldifferent(array):
        # name += "alldiff, "
        # if joker(array, .66):
        # name += "freq(.66), "
        # elif len(array) > 2 and joker(array, .49):
        # name += "freq(.49), "
        # elif len(array) > 3 and joker(array, .33):
        # name += "freq(.33), "
        values = []
        if name is "":
            name += "unclassified"
        for i in range(len(array)):
            values.append((i, array[i]))
        if name not in coords:
            coords[name] = []
        # if len(values) < 1001 and r < .4:
        coords[name].append(values)

order = {}
sum = 0.0
for k in coords.keys():
    r = round((len(coords[k]) / tot) * 100, 4)
    order[r] = k
    print('%s %s' % (k, r))
    sum += r
with doc.create(Section('Summary')):
    with doc.create(Tabular('l|l')) as tab:
        tab.add_hline()
        tab.add_row(("Param.", 'Perc.'))
        tab.add_hline()
        for r in sorted(order.keys(), reverse=True):
            tab.add_row((order[r], r))

# for name in coords.keys():
#     addPlots(doc, coords.get(name), name)
# addPlots(doc, coords.get('sawtooth[0.4]'), 'sawtooth[.4]')

# doc.generate_tex(
# filename='results'
# )

doc.generate_pdf(
    filepath='element',
    clean=True
)
# doc.generate_pdf(
# filepath='element',
# clean=True
# )