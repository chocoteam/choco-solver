#!/usr/bin/python
# -*- coding: utf-8 -*-
# based on http://sourceforge.net/projects/mysql-python/files/
# only work with python 2.6

import MySQLdb as mdb
import sys, random


# EXAMPLES OF DATABSE AND USER CREATION ON MYSQL
#mysql> CREATE DATABASE perfdb;
#mysql> CREATE USER 'perfuser'@'localhost' IDENTIFIED BY 'perf123';
#mysql> USE perfdb;
#mysql> GRANT ALL ON perfdb.* TO 'perfuser'@'localhost';


def dropTables(con):
    with con:
        cur = con.cursor()
        cur.execute(
            "DROP TABLES IF EXISTS SESSION, PROBLEM_PER_SESSION, PROBLEM, RESULTS, TIME, DATA"
        )


def createTables(con):
#with con:
    cur = con.cursor()
    cur.execute(
        "CREATE TABLE IF NOT EXISTS SESSION(\
            name VARCHAR(25) NOT NULL, exec_date DATETIME, id BINARY(36),\
            PRIMARY KEY (name, exec_date)\
        )")

    cur.execute(
        "CREATE TABLE IF NOT EXISTS PROBLEM_PER_SESSION (\
            ses_id BINARY(36), pb_id BINARY(36),\
            PRIMARY KEY (ses_id, pb_id)\
        )")

    cur.execute(
        "CREATE TABLE IF NOT EXISTS PROBLEM(\
            name VARCHAR(100) NOT NULL, parameters VARCHAR(255), id BINARY(36),\
            PRIMARY KEY (name(75), parameters(100))\
        )")

    cur.execute(
        "CREATE TABLE IF NOT EXISTS RESULTS( \
            pb_id BINARY(36), exec_date DATETIME, time_id INT, data_id INT,\
            PRIMARY KEY (pb_id, exec_date)\
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

    cur.execute("ALTER TABLE PROBLEM_PER_SESSION ADD FOREIGN KEY (pb_id) REFERENCES PROBLEM(pb_id)")
    cur.execute("ALTER TABLE RESULTS ADD FOREIGN KEY (pb_id) REFERENCES PROBLEM(pb_id)")
    cur.execute("ALTER TABLE RESULTS ADD FOREIGN KEY (time_id) REFERENCES TIME(id)")
    cur.execute("ALTER TABLE RESULTS ADD FOREIGN KEY (data_id) REFERENCES DATA(id)")


def openSession(con):
    cur = con.cursor()
    name = "test"
    cur.execute("INSERT INTO SESSION (name, exec_date, id) VALUES (%s, NOW(), UUID())", name)
    cur.execute("SELECT id FROM SESSION ORDER BY exec_date DESC LIMIT 1")
    row = cur.fetchone()
    return row[0]

############################################
# con: the connection
# ses_id: session id
# name: command line
# results: time and data in an array (in a matrix, of dim nx1
############################################
def insertValues(con, ses_id, name, parameters, results):
    print results
    if len(results[0])  is 0 :
        results = [[0 for x in range(1)] for x in range(len(results))]
    print results

    #print results
    cur = con.cursor()
    # chech wether the problem already exists
    cur.execute("SELECT ID FROM PROBLEM WHERE NAME=%s AND PARAMETERS=%s", (name, parameters))
    row = cur.fetchone()
    if row is None:
        cur.execute("INSERT INTO PROBLEM (name, parameters, id) VALUES (%s, %s, UUID())", (name, parameters))
        cur.execute("SELECT ID FROM PROBLEM WHERE NAME=%s AND PARAMETERS=%s", (name, parameters))
        row = cur.fetchone()

    # get the problem id
    pb_id = row[0]

    # insert into set
    cur.execute("INSERT INTO PROBLEM_PER_SESSION (ses_id, pb_id) VALUES (%s, %s)",(ses_id, pb_id))

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
    cur.execute("INSERT INTO RESULTS (pb_id, exec_date, time_id, data_id) VALUES (%s, NOW(), %s, %s)",
                (pb_id, t_id, d_id)
    )



def select(con):
    cur = con.cursor()
    # get the problem id
    name = "golomb"
    cur.execute("SELECT id FROM PROBLEM WHERE name=%s", name)
    row = cur.fetchone()
    pb_id = row[0]

    # get time_id and data_id
    cur.execute("SELECT time_id, data_id FROM RESULTS WHERE pb_id = %s ORDER BY exec_date", pb_id)
    rows = cur.fetchall()

    for row in rows:
        print "%d  - %d" % (row[0], row[1])
        cur.execute("SELECT resolution FROM TIME WHERE id = %s", row[0])
        res = cur.fetchone()[0]
        cur.execute("SELECT solutions FROM DATA WHERE id = %s", row[1])
        sol = cur.fetchone()[0]
        print "%s, %s, %s" % (name, res, sol)



#con = mdb.connect('localhost', 'testuser', 'test623', 'testdb')
#print "DROP"
#dropTables(con)
#print "CREATE"
#createTables(con)
#
#print "OPEN SESSION"
#ses_id = openSession(con)
#
#print "ADD RESULT"
#insertValues(con, ses_id, "golomb", "")
#insertValues(con, ses_id, "msquare", "-n 10")
#
#print "SELECT"
#select(con)
#
#if con:
#    con.close()