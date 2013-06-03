/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.ResolutionPolicy;
import solver.Solver;
import util.ESat;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

/**
 * A class designed to query a data base.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/05/13
 */
public class MySQLAccess {

    private static final Logger logger = LoggerFactory.getLogger(MySQLAccess.class);

    private static final String SAT = "SAT", MIN = "MIN", MAX = "MAX";

    private Properties properties = new Properties();

    private final String url, dbname, user, pwd;

    private Connection connection;

    private PreparedStatement statement;

    private ResultSet resultSet;

    public MySQLAccess(File mysqlProperties) {
        try {
            properties.load(new FileInputStream(mysqlProperties));
        } catch (Exception e) {
            logger.error("Unable to load " + mysqlProperties + " file from classpath.", e);
            System.exit(1);
        }

        url = properties.getProperty("mysql.url");
        dbname = properties.getProperty("mysql.dbname");
        user = properties.getProperty("mysql.user");
        pwd = properties.getProperty("mysql.pwd");
    }


    public void connect() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + url + "/" + dbname, user, pwd);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void insert(String filename, String benchname, Solver solver) {
        try {
            File instance = new File(filename);
            String name = instance.getName();

            boolean optpb = solver.getSearchLoop().getObjectivemanager().isOptimization();
            // 1. request BENCHMARK
            int bid = getBenchID(benchname);
            int pid = getPbID(name,
                    solver.getSearchLoop().getObjectivemanager().getPolicy(),
                    optpb ? solver.getMeasures().getObjectiveValue() : solver.getMeasures().getSolutionCount(),
                    optpb ? solver.getMeasures().isObjectiveOptimal() : !solver.isFeasible().equals(ESat.UNDEFINED));

            insertData(solver, bid, pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert a new record in DESCRIPTION
     *
     * @param solver the solver
     */
    private void insertData(Solver solver, int bid, int pid) {
        try {
            statement = connection.prepareStatement("insert into RESOLUTIONS values (?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setInt(1, bid);
            statement.setInt(2, pid);
            statement.setLong(3, (long) solver.getMeasures().getReadingTimeCount());
            statement.setLong(4, (long) solver.getMeasures().getTimeCount());
            statement.setLong(5, solver.getMeasures().getSolutionCount());
            statement.setLong(6, solver.getMeasures().getObjectiveValue());
            statement.setLong(7, solver.getMeasures().getNodeCount());
            statement.setLong(8, solver.getMeasures().getFailCount());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public int getBenchID(String benchname) throws SQLException {
        statement = connection.prepareStatement("select BID from BENCHMARKS where NAME = ?;");
        statement.setString(1, benchname);
        resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1);
        } else {
            statement = connection.prepareStatement("insert into BENCHMARKS (NAME, DATE, SID) values (?,?,?);");
            statement.setString(1, benchname);
            java.util.Date today = new java.util.Date();
            java.sql.Date sqlToday = new java.sql.Date(today.getTime());
            statement.setDate(2, sqlToday);
            statement.setInt(3, 1);
            statement.executeUpdate();
            return getBenchID(benchname);
        }
    }

    private int getPbID(String filename, ResolutionPolicy policy, long solution, boolean isopt) throws SQLException {
        statement = connection.prepareStatement("select PID from PROBLEMS where NAME = ?;");
        statement.setString(1, filename);
        resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1);
        } else {
            statement = connection.prepareStatement("insert into PROBLEMS (NAME, RESOLUTION, SOLUTION, OPTIMAL) values (?,?,?,?);");
            statement.setString(1, filename);
            switch (policy) {
                case SATISFACTION:
                    statement.setString(2, SAT);
                    break;
                case MINIMIZE:
                    statement.setString(2, MIN);
                    break;
                case MAXIMIZE:
                    statement.setString(2, MAX);
                    break;
            }
            statement.setLong(3, solution);
            statement.setBoolean(4, isopt);
            statement.executeUpdate();
            return getPbID(filename, policy, solution, isopt);
        }
    }
}
