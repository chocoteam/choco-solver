import os

__author__ = 'cprudhom'

import re

rsol = '%.*Solutions,.*'
ropt = '%.*(Minimize|Maximize).*'
status = '=====.*'
comp = '==========\n'
unsa = '=====UNSATISFIABLE=====\n'
unkn = '=====UNKNOWN=====\n'
unbo = '=====UNBOUNDED=====\n'

maxtime=300.

def read(dir, fname, opt, old):
    """
    Read the log file, in dir, postfixed by 'opt'
    :return: a list:
    (0) nb sol,
    (1) time,
    (2) nodes,
    (3) resolution policy,
    (4) obj value,
    (6) resolution status,
    (7) resolution options
    """
    try:
        logfile = open(os.path.join(dir, fname + '+' + opt + '.log'), 'r', encoding='utf8')
    except FileNotFoundError:
        return [0, maxtime, 999999999, 'UNK', 0, 'unknown', opt]
    sndlast = ""
    last = ""
    rstat = ""
    for line in logfile:
        if re.search(rsol, line):
            sndlast = last
            last = line
        if re.search(status, line):
            rstat = line
    # get the last solution, there should be at least one
    # line = line.replace(',', '')
    last = last.replace('s', '')
    parts = last.split()
    sndlast = sndlast.replace('s', '')
    sndparts = sndlast.split()
    if len(parts) > 0:
        if len(sndparts) > 0 and parts[1] == sndparts[1]:
            parts = sndparts
        solution = [parts[1]]
        if re.search(ropt, last):
            # extract values
            if old is True:
                solution.append(float(parts[8][:-1].replace(',', '.')))  # time
                solution.append(parts[9])  # nodes
            else:
                solution.append(float(parts[9][:-1].replace(',', '.')))  # time
                solution.append(parts[10])  # nodes
            if parts[3] == 'Minimize':
                solution.append('MIN')
            else:
                solution.append('MAX')
            solution.append(int(parts[6].replace(',', '')))  # obj
        else:
            # extract values
            if old is True:
                solution.append(float(parts[4][:-1].replace(',', '.')))  # time
                solution.append(parts[5])  # nodes
            else:
                solution.append(float(parts[5][:-1].replace(',', '.')))  # time
                solution.append(parts[6])  # nodes
            solution.append('SAT')
            solution.append(int(0))  # obj
        solution.append('unknown')
        if rstat == "":
            solution[5] = '  '
        elif rstat == comp:
            solution[5] = 'proof'
        elif rstat == unsa:
            solution[5] = 'proof'
        elif rstat == unbo:
            solution[5] = 'unbounded'

        solution.append(opt)

        if solution[1] >= maxtime:
            solution[1] = float(maxtime)
    else:
        solution = [0, maxtime, 999999999, 'UNK', 0, 'unknown', opt]

    print(solution)
    return solution
