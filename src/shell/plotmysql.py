import sys
try:
  import MySQLdb
except ImportError:
  print "ERROR : You should install MySQLdb for python"
try :
    import matplotlib.pyplot as plt
    import matplotlib.ticker as ticker
except ImportError:
    print "ERROR : You should install matplotlib for python"
try :
    import pylab as plab
except ImportError:
    print "ERROR : You should install matplotlib for python"

host= ''
user = ''
pwd = ''
dbname = ''

def make_patch_spines_invisible(ax):
    ax.set_frame_on(True)
    ax.patch.set_visible(False)
    for sp in ax.spines.itervalues():
        sp.set_visible(False)

def plot(con):
    cur = con.cursor()

    cur.execute("SELECT id FROM PROBLEM")
    ids = cur.fetchall()
    k = 1
    for id in ids:
        cur.execute("SELECT name, parameters FROM PROBLEM WHERE id=%s", id)
        res1  = cur.fetchone()
        name = res1[0]
        param = res1[1]

        # get time_id
        cur.execute("SELECT time_id, exec_date FROM RESULTS WHERE pb_id = %s ORDER BY exec_date", id)
        rows = cur.fetchall()

        d = []
        r = []
        b = []
        i = []
        p = []
        n = []
        j = 1
        for row in rows:
            cur.execute("SELECT building, init, init_prop, resolution FROM TIME WHERE id = %s", row[0])
            res = cur.fetchone()
            b.append(res[0])
            i.append(res[1])
            p.append(res[2])
            r.append(res[3])
            d.append(row[1])
            n.append(j)
            j +=1

        fig = plt.figure(k)
        fig.subplots_adjust(right=0.75, bottom=0.25)
        host = fig.add_subplot(111)
        par1 = host.twinx()
        par2 = host.twinx()
        par3 = host.twinx()

        par2.spines["right"].set_position(("axes", 1.1))
        make_patch_spines_invisible(par2)
        par2.spines["right"].set_visible(True)

        par3.spines["right"].set_position(("axes", 1.2))
        make_patch_spines_invisible(par3)
        par3.spines["right"].set_visible(True)

        p1, = host.plot(n, r, linewidth=1.0,  color='r')
        p2,= par1.plot(n, b, linewidth=1.0, color='b')
        p3,= par2.plot(n, i, linewidth=1.0, color='purple')
        p4,= par3.plot(n, p, linewidth=1.0, color='g')

        host.set_xlim(min(n), max(n)+1)
        host.set_ylim(min(r), max(r)*1.1)
        par1.set_ylim(min(b), max(b)*1.1)
        par2.set_ylim(min(i), max(i)*1.1)
        par3.set_ylim(min(p), max(p)*1.1)

        host.set_xlabel("Build #")
    #    host.set_ylabel("Resolution")
    #    par1.set_ylabel("Building")
    #    par2.set_ylabel("Initialisation")
    #    par3.set_ylabel("Init. Propagation")

        host.yaxis.label.set_color(p1.get_color())
        par1.yaxis.label.set_color(p2.get_color())
        par2.yaxis.label.set_color(p3.get_color())
        par3.yaxis.label.set_color(p4.get_color())

        tkw = dict(size=4, width=1.5)
        host.tick_params(axis='y', colors=p1.get_color(), **tkw)
        par1.tick_params(axis='y', colors=p2.get_color(), **tkw)
        par2.tick_params(axis='y', colors=p3.get_color(), **tkw)
        par3.tick_params(axis='y', colors=p4.get_color(), **tkw)
        host.tick_params(axis='x', **tkw)

        host.set_title(name + ' '+param)
        host.set_xticklabels(d, rotation=-90)
    #    plt.autofmt_xdate()
        lines = [p1, p2, p3,p4]
        labels = ['Resolution','Building','Initialisation','Init. Propagation']
        host.legend(lines, labels,bbox_to_anchor = (1.42, 0))
        plt.savefig( str(k) +'.png')
        k +=1
    #    plt.show()

    filout = open('res.html', 'w')
    filout.write("<html>\n<body><h1>Resultats:</h1>\n")
    for i in range(1,k):
        filout.write("<IMG src="+str(i)+".png >\n")
    filout.write("</body>\n</html>")
    filout.close()
    filout.close()

def readParameters(paramlist):
    global host
    global user
    global pwd
    global dbname
    offset = 2
    if len(paramlist) > 0:
        if paramlist[0] == "-host": # user pwd for db connexion
            host = paramlist[1]
        elif paramlist[0] == "-user": # user for db connexion
            user = paramlist[1]
        elif paramlist[0] == "-pwd": # user pwd for db connexion
            pwd = paramlist[1]
        elif paramlist[0] == "-dbname": # dbname pwd for db connexion
            dbname = paramlist[1]
        readParameters(paramlist[offset:])

readParameters(sys.argv[1:])
con = MySQLdb.connect(host, user, pwd, dbname)
plot(con)