import os

__author__ = 'cprudhom'

import re

class LogExtractor:

    def __init__(self, format="mzn", maxobj=999999999, maxtime=599.):
        comment = "%" # mzn
        self.format = format
        self.status = '=====.*'
        self.comp = '==========\n'
        self.unsa = '=====UNSATISFIABLE=====\n'
        self.unkn = '=====UNKNOWN=====\n'
        self.unbo = '=====UNBOUNDED=====\n'
        if format is "xcsp":
            comment = "c"
            self.status = '^s .*'
            self.comp = 's OPTIMUM FOUND\n'
            self.unsa = 's UNSATISFIABLE\n'
            self.unkn = 's UNKNOWN\n'
            self.unbo = 's SATISFIABLE\n'
        self.rsol = comment+'.*Solutions,.*'
        self.rmod = comment+'.building.*'
        self.ropt = comment+'.*(MINIMIZE|MAXIMIZE).*'

        self.maxobj = maxobj
        self.maxtime = maxtime


    def read(self, dir, fname, opt):
        """
        Read the log file, in dir, postfixed by 'opt'
        :return: a list:
        (0) nb sol,
        (1) time,
        (2) nodes,
        (3) resolution policy,
        (4) obj value,
        (5) resolution status,
        (6) resolution options
        (7) building time
        """
        try:
            logfile = open(os.path.join(dir, fname + '+' + opt + '.log'), 'r', encoding='utf8')
        except FileNotFoundError:
            return [-1, self.maxtime, self.maxobj, 'UNK', 0, 'unknown', opt, self.maxtime]
        sndlast = ""
        last = ""
        rstat = ""
        mstat = ""
        for line in logfile:
            if re.search(self.rsol, line):
                sndlast = last
                last = line
            if re.search(self.status, line):
                rstat = line
            if mstat == ""  and re.search(self.rmod, line):
                mstat = line
        # get the last solution, there should be at least one
        # line = line.replace(',', '')
        last = last.replace('s', '')
        parts = last.split()
        sndlast = sndlast.replace('s', '')
        sndparts = sndlast.split()
        if len(parts) > 0:
            if len(sndparts) > 0 and parts[1] == sndparts[1]:
                parts = sndparts
            solution = [parts[2]]
            if re.search(self.ropt, last):
                # extract values
                solution.append(float(parts[10][:-1].replace(',', '.')))  # time
                solution.append(parts[16])  # nodes
                if parts[4] == 'MINIMIZE':
                    solution.append('MIN')
                else:
                    solution.append('MAX')
                solution.append(int(parts[7].replace(',', '')))  # obj
            else:
                # extract values
                solution.append(float(parts[6][:-1].replace(',', '.')))  # time
                solution.append(parts[7])  # nodes
                solution.append('SAT')
                solution.append(int(0))  # obj
            solution.append('unknown')
            if rstat == "":
                solution[5] = '  '
            elif rstat == self.comp:
                solution[5] = 'proof'
            elif rstat == self.unsa:
                solution[5] = 'proof'
            elif rstat == self.unbo:
                solution[5] = 'unbounded'

            solution.append(opt)

            if solution[1] >= self.maxtime:
                solution[1] = float(self.maxtime)
        else:
            solution = [-1, self.maxtime, self.maxobj, 'UNK', 0, 'unknown', opt, self.maxtime]
        btime = mstat.replace('s', '').split()
        if len(btime) > 0:
            solution.append(float(btime[8][:-1].replace(',', '.')))
        else:
            solution.append(0)
        # print(solution)
        return solution
