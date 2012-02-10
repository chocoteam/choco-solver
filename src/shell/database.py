#!/usr/bin/python
# -*- coding: utf-8 -*-
# based on http://sourceforge.net/projects/mysql-python/files/
# only work with python 2.6
try:
  import MySQLdb as mdb
except ImportError:
  mdb = None
try :
    import matplotlib
    import pylab
    import plotmysql
except ImportError:
    plotmysql = None
import sys, random

if mdb is None:
    print "ERROR : You should install MySQLdb for python"
    print "http://sourceforge.net/projects/mysql-python/files/"
    print "only work with python 2.6"

if plotmysql is None:
    print "WARNING : You should install 'matplotlib' for python to use the plot part. This will be skipped."

class Database:
    def __init__(self, host, user, pwd, dbname):
        self.con = mdb.connect(host, user, pwd, dbname)
        self.sid = 0

# EXAMPLES OF DATABSE AND USER CREATION ON MYSQL
#mysql> CREATE DATABASE perfdb;
#mysql> CREATE USER 'perfuser'@'localhost' IDENTIFIED BY 'perf123';
#mysql> USE perfdb;
#mysql> GRANT ALL ON perfdb.* TO 'perfuser'@'localhost';


    def dropTables(self):
        with self.con:
            cur = self.con.cursor()
            cur.execute(
                "DROP TABLES IF EXISTS SESSION, PROBLEM, RESULTS, TIME, DATA"
            )


    def createTables(self):
    #with con:
        cur = self.con.cursor()
        cur.execute(
            "CREATE TABLE IF NOT EXISTS SESSION(\
                name VARCHAR(25) NOT NULL, exec_date DATETIME, id BINARY(36),\
                PRIMARY KEY (name, exec_date)\
            )")

        cur.execute(
            "CREATE TABLE IF NOT EXISTS PROBLEM(\
                name VARCHAR(100) NOT NULL, parameters VARCHAR(255), id BINARY(36),\
                PRIMARY KEY (name(75), parameters(100))\
            )")

        cur.execute(
            "CREATE TABLE IF NOT EXISTS TIME( \
                id INT AUTO_INCREMENT, building FLOAT, init FLOAT, init_prop FLOAT, resolution FLOAT,\
                PRIMARY KEY (id)\
            )")

        cur.execute(#variables INT, constraints INT, e_recorders INT,\
            "CREATE TABLE IF NOT EXISTS DATA( \
                id INT AUTO_INCREMENT, solutions INT, objective INT, nodes INT, backtracks INT, fails INT, \
                restarts INT, fines INT, coarses INT, \
                PRIMARY KEY (id)\
            )")

        cur.execute(
            "CREATE TABLE IF NOT EXISTS RESULTS( \
                ses_id BINARY(36), pb_id BINARY(36), time_id INT, data_id INT,\
                PRIMARY KEY (ses_id, pb_id, time_id, data_id)\
            )")

        cur.execute("ALTER TABLE RESULTS ADD FOREIGN KEY (ses_id) REFERENCES SESSION(id)")
        cur.execute("ALTER TABLE RESULTS ADD FOREIGN KEY (pb_id) REFERENCES PROBLEM(id)")
        cur.execute("ALTER TABLE RESULTS ADD FOREIGN KEY (time_id) REFERENCES TIME(id)")
        cur.execute("ALTER TABLE RESULTS ADD FOREIGN KEY (data_id) REFERENCES DATA(id)")


    def openSession(self, name):
        cur = self.con.cursor()
        cur.execute("INSERT INTO SESSION (name, exec_date, id) VALUES (%s, NOW(), UUID())", name)
        cur.execute("SELECT id FROM SESSION ORDER BY exec_date DESC LIMIT 1")
        row = cur.fetchone()
        self.sid = row[0]

    ############################################
    # name: command line
    # results: time and data in an array (in a matrix, of dim nx1
    ############################################
    def insertValues(self, name, parameters, results):
        print results
        if len(results[0])  is 0 :
            results = [[0 for x in range(1)] for x in range(len(results))]
        print results

        #print results
        cur = self.con.cursor()
        # chech wether the problem already exists
        cur.execute("SELECT ID FROM PROBLEM WHERE NAME=%s AND PARAMETERS=%s", (name, parameters))
        row = cur.fetchone()
        if row is None:
            cur.execute("INSERT INTO PROBLEM (name, parameters, id) VALUES (%s, %s, UUID())", (name, parameters))
            cur.execute("SELECT ID FROM PROBLEM WHERE NAME=%s AND PARAMETERS=%s", (name, parameters))
            row = cur.fetchone()

        # get the problem id
        pb_id = row[0]

        # insert into TIME
        cur.execute("INSERT INTO TIME (building, init, init_prop, resolution) VALUES (%s, %s,%s,%s)",
                    (str(results[1][0]), str(results[2][0]), str(results[3][0]), str(results[5][0]))
        )
        cur.execute("SELECT LAST_INSERT_ID()")
        t_id = cur.fetchone()[0]

        # insert into DATA
        cur.execute("INSERT INTO DATA (\
                solutions, objective, nodes, backtracks, fails, restarts, fines, coarses) \
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
                    (str(int(results[0][0])),str(int(results[7][0])),str(int(results[8][0])),str(int(results[9][0])),
                    str(int(results[10][0])),str(int(results[11][0])),str(int(results[12][0])),str(int(results[13][0])),)
        )
        cur.execute("SELECT LAST_INSERT_ID()")
        d_id = cur.fetchone()[0]

        # insert into RESULTS
        cur.execute("INSERT INTO RESULTS (ses_id, pb_id, time_id, data_id) VALUES (%s, %s, %s, %s)",
                    (self.sid, pb_id, t_id, d_id)
        )

    def plot(self):
        plotmysql.plot(self.con)