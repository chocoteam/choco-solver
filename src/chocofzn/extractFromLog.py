import os

__author__ = 'cprudhom'

import re

rsol = '%.*Solutions,.*'
ropt = '%.*(Minimize|Maximize).*'
comp = '=====.*'

def read(dir, fname, opt):
    """
    Read the log file, in dir, postfixed by 'opt'
    :return: a list, the solution
    """
    try:
        logfile = open(os.path.join(dir, fname + '+' + opt + '.log'), 'r', encoding='utf8')
    except FileNotFoundError:
        return [0, 900., 999999999, 'UNK', 0, opt]
    sndlast = ""
    last = ""
    for line in logfile:
        if re.search(rsol, line):
            sndlast = last
            last = line
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
        if solution[1] >= 900.:
            solution[1] = float(900.)
    else:
        solution = [0, 900., 999999999, 'UNK', 0, opt]

    print(solution)
    return solution


