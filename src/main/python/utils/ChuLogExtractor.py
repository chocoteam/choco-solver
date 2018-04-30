import os

__author__ = 'cprudhom'

import re

# % Current Best Bound: 55
# % Current Solution Time: 0.20689201355

ropt = '% Current Best Bound:.*'
rtim = '% Current Solution Time:.*'
status = '=====.*'
comp = '==========\n'
unsa = '=====UNSATISFIABLE=====\n'
unkn = '=====UNKNOWN=====\n'
unbo = '=====UNBOUNDED=====\n'

maxobj=999999999

def read(dir, fname, maxtime):
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
    solution = [0, maxtime, 0, 'UNK', 0, 'unknown', 'CHU', 0]
    try:
        logfile = open(os.path.join(dir, fname[:-4] + '+CHU.log'), 'r', encoding='utf8')
    except FileNotFoundError:
        return solution
    tim = ""
    opt = ""
    rstat = ""
    nbsol=0
    for line in logfile:
        if re.search(ropt, line):
            opt = line
            nbsol += 1
        if re.search(rtim, line):
            tim = line
        if re.search(status, line):
            rstat = line
    # get the last solution, there should be at least one
    # line = line.replace(',', '')
    solution[0] = nbsol
    solution[3] = 'MIN'
    if tim != "":
        solution[1] = float(tim.split()[4])
        solution[4] = int(opt.split()[4])
    if rstat == "":
        solution[5] = '  '
    elif rstat == comp or rstat == unsa:
        solution[5] = 'proof'
    elif rstat == unbo:
        solution[5] = 'unbounded'

    return solution
