import os

__author__ = 'cprudhom'

import re

# % Current Best Bound: 55
# % Current Solution Time: 0.20689201355

ropt = '%opt.*'
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
        logfile = open(os.path.join(dir, fname + '+CHU.log'), 'r', encoding='utf8')
    except FileNotFoundError:
        return solution
    sndlast = ""
    last = ""
    rstat = ""
    nbsol=0
    for line in logfile:
        if re.search(ropt, line):
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
        solution[0] = int(parts[2]) # nb solutions
        solution[1] = float(parts[5][:-1]) # time
        solution[2] = int(parts[6])  # nodes
        solution[3] = 'MIN' # res. policy
        solution[4] = int(parts[1][:-1]) # obj. value
    if rstat == "":
        solution[5] = '  ' # res. status
    elif rstat == comp or rstat == unsa:
        solution[5] = 'proof' # res. status
    elif rstat == unbo:
        solution[5] = 'unbounded' # res. status

    return solution
