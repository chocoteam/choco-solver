import os

from pylatex import Document, Section, Subsection, Tabular, TikZ, Axis, \
    Plot, Package, Subsubsection, MultiColumn, Command, NoEscape
import numpy as np

__author__ = 'cprudhom'


class PDFCreator:

    def __init__(self) -> None:
        super().__init__()

    def publish(self, filelist, options, timPerOpt, optPerSol, fnames, maxtime,
                bestever={}, plot=True, details=True, problems="fzn"):
        # Then start the document
        doc = Document(document_options="table")
        doc.packages.append(Package("xcolor", options="dvipsnames"))  # to deal with colors
        doc.color = True  # to deal with colors
        # np.random.seed(0)
        for opt in options:
            col = list(np.random.choice(range(128), size=3))
            doc.add_color(opt, "RGB", "%d,%d,%d" % (col[0] + 128, col[1] + 128, col[2] + 128))

        doc.preamble.append(
            Command('title', 'Benchmark analysis of %d problems and %d configurations.' % (len(fnames), len(options))))
        doc.preamble.append(Command('author', "Charles Prud'homme"))
        doc.append(NoEscape(r'\maketitle'))

        doc.packages.append(Package('geometry'))

        if plot:
            self.__globals(doc, options, timPerOpt, maxtime)

        self.__sybil(doc, options, optPerSol, fnames, maxtime, bestever)
        if details:
            if problems is "fzn":
                self.__detailsFZN(doc, options, optPerSol, fnames, maxtime, bestever)
            elif problems is "xcsp":
                self.__detailsXCSP(doc, options, optPerSol, fnames, maxtime, bestever)

        name = os.path.basename(filelist)
        print(name)
        # doc.generate_tex(name[:-4])
        doc.generate_pdf(
            filepath=name[:-4],
            clean=True, silent=False)

    def __addTimePlots(self, doc, options, coords):
        with doc.create(Subsection('CPU Time')):
            with doc.create(TikZ()):
                with doc.create(Axis(
                        options='xlabel=Instances, ylabel=CPU time (s), height=5cm, width=10cm,legend pos=outer north east')) as plot:
                    for opt in options:
                        plot.append(Plot(name=opt, coordinates=list(coords[opt])))

    def __addObjPlots(self, doc, options, objs, pol):
        with doc.create(Subsection('Objective')):
            with doc.create(TikZ()):
                with doc.create(Axis(
                        options='log basis y=10,xlabel=Instances, ylabel=Objective (%s), height=5cm, width=10cm,legend pos=outer north east' % pol)) as plot:
                    for opt in options:
                        plot.append(Plot(name=opt, coordinates=list(objs[opt])))
                    plot.append(Plot(name='syb', coordinates=list(objs['syb'])))

    def __globals(self, doc, options, timPerOpt, maxtime):
        # First global view
        section = Section('Global')
        doc.append(section)
        options.append('GOD')
        with doc.create(TikZ()):
            with doc.create(Axis(options='xlabel=Instances (ordered wrt to increasing resolution time),'
                                         'ylabel=CPU time (s),height=20cm, width=15cm,legend pos=outer north east')) as plot:
                for opt in options:
                    times = [x for x in timPerOpt[opt] if x <= maxtime]
                    times.sort()
                    coords = []
                    print("%s"%(opt), end=";")
                    for i in range(0, len(times)):
                        coords.append((i, times[i]))
                        print("%d"%(times[i]), end=";")
                    print("\n")
                    plot.append(Plot(name=opt, coordinates=coords))

        # Second zoom
        section = Section('Global + zoom')
        doc.append(section)

        with doc.create(TikZ()):
            with doc.create(Axis(options='xlabel=Instances (ordered wrt to increasing resolution time),'
                                         'ylabel=CPU time (s),height=20cm, width=15cm,legend pos=outer north east')) as plot:
                for opt in options:
                    times = timPerOpt[opt].copy()
                    times.sort()
                    if maxtime in times:
                        id = times.index(maxtime)
                    else:
                        id = len(times)
                    coords = []
                    for i in range(max(0,id-30), min(id+1, len(times))):
                        coords.append((i, times[i]))
                    plot.append(Plot(name=opt, coordinates=coords))
        options.remove('GOD')

        # First global view
        for o1 in range(0, len(options) - 1):
            for o2 in range(o1 + 1, len(options)):
                opt1 = options[o1]
                opt2 = options[o2]

                section = Section('%s vs. %s' % (opt1, opt2))
                doc.append(section)
                subsection = Subsection("1st view")
                doc.append(subsection)

                with doc.create(TikZ()):
                    with doc.create(Axis(options=('xlabel=time(%s) - time(%s),'
                                                  'ylabel=CPU time diff. (s),height=15cm, width=15cm,legend pos=outer north east') % (
                                                         opt1, opt2))) as plot:
                        times1 = timPerOpt[opt1]
                        times2 = timPerOpt[opt2]
                        times = [times1[i] - times2[i] for i in range(0, len(times1))]
                        times.sort()
                        coords1 = []
                        coords2 = []
                        for i in range(0, len(times)):
                            if times[i] < 0:
                                coords1.append((i, -times[i]))
                                coords2.append((i, 0))
                            elif times[i] > 0:
                                coords1.append((i, 0))
                                coords2.append((i, times[i]))
                        plot.append(Plot(name=opt2, coordinates=coords1))
                        plot.append(Plot(name=opt1, coordinates=coords2))

                subsection = Subsection("2nd view")
                doc.append(subsection)
                with doc.create(TikZ()):
                    with doc.create(Axis(options=('xmode=log, ymode=log, xlabel=%s,'
                                                  'ylabel=%s,height=15cm, width=15cm,legend pos=outer north east') % (
                                                         opt1, opt2))) as plot:
                        times1 = timPerOpt[opt1]
                        times2 = timPerOpt[opt2]
                        coords = []
                        for i in range(0, len(times1)):
                            coords.append((times1[i], times2[i]))
                        plot.append(Plot(name="Instance", coordinates=coords, options='only marks, mark=+'))
                        plot.append(Plot(func="x", options=("domain=0.001:%s") % (maxtime)))

    def __sybil(self, doc, options, optPerSol, fnames, maxtime, bestever):
        # Second summary
        section = Section('Summary : %d problems, %d configurations.' % (len(fnames), len(options)))
        doc.append(section)
        table = Tabular('|l||c|c||c|c||c|')
        table.color = True  # to deal with colors in table

        table.add_hline()
        table.add_row(
            ("", MultiColumn(2, align='c||', data="CSP"), MultiColumn(2, align='c||', data='COP'), "Times best"))
        table.add_row(("Config.", 'sat', "unsat", "best", "proof", "< %.1f" % maxtime))
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
                if len(solutions) == 0:
                    continue
                gbest = solutions[0][4]
                mybest = gbest
                gtime = solutions[0][1]
                mytime = gtime
                b = 0
                for i in range(0, len(solutions)):
                    if solutions[i][6] == opt:
                        if solutions[i][5] == 'proof':
                            proof += 1
                            b += 1
                            if solutions[i][3] is 'SAT':
                                if int(solutions[i][0]) == 0:
                                    unsat += 1
                                elif int(solutions[i][0]) == 1:
                                    sat += 1
                        elif solutions[i][5] != 'unknown':
                            b += 1
                        mybest = solutions[i][4]
                        mytime = solutions[i][1]
                    gtime = min(gtime, solutions[i][1])
                    if solutions[0][3] is 'MIN':
                        gbest = min(gbest, solutions[i][4])
                    elif solutions[0][3] is 'MAX':
                        gbest = max(gbest, solutions[i][4])
                if gbest == mybest and b > 0:
                    fbest += 1
                if gtime == mytime and mytime < maxtime:
                    tbest += 1
            table.add_row((opt, sat, unsat, fbest, proof, tbest), color=opt)
        # now VBS
        sat = 0
        unsat = 0
        proof = 0
        fbest = 0
        tbest = 0
        for fname in fnames:
            solutions = optPerSol[fname]
            if len(solutions) == 0:
                continue
            gbest = solutions[0][4]
            gtime = solutions[0][1]
            p = 0
            b = 0
            for i in range(0, len(solutions)):
                if solutions[i][5] == 'proof':
                    p += 1
                    b += 1
                    if solutions[i][3] is 'SAT':
                        if int(solutions[i][0]) == 0:
                            unsat += 1
                        elif int(solutions[i][0]) == 1:
                            sat += 1
                elif solutions[i][5] != 'unknown':
                    b += 1
                gtime = min(gtime, solutions[i][1])
                if solutions[0][3] is 'MIN':
                    gbest = min(gbest, solutions[i][4])
                elif solutions[0][3] is 'MAX':
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
        for fname in fnames:
            if fname in bestever:
                if len(bestever[fname]) > 1:
                    fbest += 1
                if 'C' in bestever[fname][0]:
                    proof += 1
        table.add_hline()
        table.add_hline()
        table.add_row(('syb', sat, unsat, fbest, proof, "--"))

        table.add_hline()
        section.append(table)

    def __detailsFZN(self, doc, options, optPerSol, fnames, maxtime, bestever):
        coords = {}
        objs = {}
        for o in options:
            coords[o] = []
            objs[o] = []
        objs['syb'] = []
        pol = 'SAT'
        presec = ""
        prevsubsec = ""
        section = None
        subsection = None
        # Third problem per problem
        k = 0
        for fname in fnames:
            parts = fname.split("+")
            solutions = optPerSol[fname]
            if parts[0] != presec:
                presec = parts[0]
                if k > 0:
                    self.__addTimePlots(doc, options, coords)
                    for o in options:
                        coords[o].clear()
                    k = 0
                    if len(objs) > 0:
                        self.__addObjPlots(doc, options, objs, pol)
                        for o in objs.keys():
                            objs[o].clear()

                section = Section('%s' % (presec))  # .replace("_", "\_")))
                doc.append(section)
                print("create section: " + presec)

            if parts[1] != prevsubsec:
                prevsubsec = parts[1]
                subsection = Subsection('%s' % (prevsubsec))  # .replace("_", "\_")))
                section.append(subsection)
                print("create subsection: " + prevsubsec)

            if len(parts) > 2:
                subsubsection = Subsubsection('%s' % (parts[2]))  # .replace("_", "\_")))
                subsection.append(subsubsection)
                print("create subsubsection: " + parts[2])
            else:
                subsubsection = Subsubsection('%s' % (parts[1]))  # .replace("_", "\_")))
                subsection.append(subsubsection)
                print("create subsubsection: " + parts[1])

            pol = solutions[0][3]
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
                if fname in bestever:
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
                if fname in bestever:
                    if len(bestever[fname]) > 1:
                        table.add_row("syb", bestever[fname][1], bestever[fname][0], "--", "--", "--", "--")
                        objs['syb'].append((k, bestever[fname][1]))
                    else:
                        table.add_row("syb", "--", bestever[fname][0], "--", "--", "--", "--")
                table.add_hline()

            k += 1
        if k > 0:
            self.__addTimePlots(doc, options, coords)
            for o in options:
                coords[o].clear()
            k = 0
            if len(objs) > 0:
                self.__addObjPlots(doc, options, objs, pol)
                for o in objs.keys():
                    objs[o].clear()

    def __detailsXCSP(self, doc, options, optPerSol, fnames, maxtime, bestever):
        section = Section('Details')
        doc.append(section)
        print("create section: \"Details\"")

        coords = {}
        objs = {}
        for o in options:
            coords[o] = []
            objs[o] = []
        objs['syb'] = []
        # Third problem per problem
        k = 0
        for fname in fnames:
            solutions = optPerSol[fname]
            if len(solutions) == 0:
                continue
            subsection = Subsection('%s' % (fname))  # .replace("_", "\_")))
            section.append(subsection)
            print("create subsection: " + fname)
            if solutions[0][3] == 'SAT':
                solutions.sort(key=lambda x: (x[3], x[1]))
                table = Tabular('l|r|l|r|r|r')
                table.color = True  # to deal with colors in table
                subsection.append(table)
                table.add_hline()
                table.add_row(("Config.", 'Status', "#Sol", 'Time(sec)', 'Build(sec)', 'Nodes'))
                table.add_hline()
                for i in range(0, len(solutions)):
                    table.add_row((solutions[i][6], solutions[i][5], solutions[i][0], solutions[i][1], solutions[i][7],
                                   solutions[i][2]), color=solutions[i][6])
                    coords[solutions[i][6]].append((k, solutions[i][1]))
                table.add_hline()
                table.add_hline()
                # add syb
                if fname in bestever:
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
                table.color = True  # to deal with colors in table
                subsection.append(table)

                table.add_hline()
                table.add_row(("Config.", type, 'Status', "#Sol", 'Time(sec)', 'Build(sec)', 'Nodes'))
                table.add_hline()
                for i in range(0, len(solutions)):
                    table.add_row((solutions[i][6], solutions[i][4], solutions[i][5], solutions[i][0], solutions[i][1],
                                   solutions[i][7], solutions[i][2]), color=solutions[i][6])
                    if solutions[i][4] == best:
                        coords[solutions[i][6]].append((k, solutions[i][1]))
                    else:
                        coords[solutions[i][6]].append((k, maxtime))
                    if int(solutions[i][0]) > 0:
                        objs[solutions[i][6]].append((k, solutions[i][4]))
                table.add_hline()
                table.add_hline()
                # add syb
                if fname in bestever:
                    if len(bestever[fname]) > 1:
                        table.add_row("syb", bestever[fname][1], bestever[fname][0], "--", "--", "--", "--")
                        objs['syb'].append((k, bestever[fname][1]))
                    else:
                        table.add_row("syb", "--", bestever[fname][0], "--", "--", "--", "--")
                table.add_hline()

            # self.__addTimePlots(doc, options, coords)
            for o in options:
                coords[o].clear()
