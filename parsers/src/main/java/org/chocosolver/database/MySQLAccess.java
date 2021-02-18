/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.database;

import org.chocosolver.solver.ResolutionPolicy;

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


    private static final String SAT = "SAT", MIN = "MIN", MAX = "MAX";

    private Properties properties = new Properties();

    private final String url, dbname, user, pwd;

    private Connection connection;

    private PreparedStatement statement;

    private ResultSet resultSet;

    public MySQLAccess(File mysqlProperties) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(mysqlProperties);
            properties.load(fileInputStream);
            fileInputStream.close();
        } catch (Exception e) {
            System.err.println("Unable to load " + mysqlProperties + " file from classpath.\n" + e);
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

    public void insert(String filename, String benchname, Number[] measures, ResolutionPolicy policy, boolean isFeasible, boolean isopt) {
        try {
            File instance = new File(filename);
            String name = instance.getName();

            boolean optpb = policy != ResolutionPolicy.SATISFACTION;
            // 1. request BENCHMARK
            int bid = getBenchID(benchname);
            int pid = getPbID(name,
                    policy,
                    optpb ? measures[5].longValue() : measures[0].longValue(),
                    optpb ? isopt : isFeasible);

            insertData(optpb, measures, bid, pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert a new record in DESCRIPTION
     */
    private void insertData(boolean optpb, Number[] measures, int bid, int pid) {
        try {
            statement = connection.prepareStatement("insert into RESOLUTIONS values (?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setInt(1, bid);
            statement.setInt(2, pid);
            statement.setLong(3, measures[1].longValue()); // building time
            statement.setLong(4, measures[4].longValue()); // solving time
            long obj = -100;
            if (optpb) {
                if (measures[0].longValue() > 0) {
                    obj = measures[5].intValue(); // obj value, if any
                }
            } else {
                obj = measures[0].longValue();
            }
            statement.setLong(5, obj);
            statement.setLong(6, measures[0].longValue()); // solution count
            statement.setLong(7, measures[7].longValue()); // node count
            statement.setLong(8, measures[9].longValue()); // fail count
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
            statement = connection.prepareStatement("insert into PROBLEMS (NAME, RESOLUTION, OBJECTIVE, OPTIMAL) values (?,?,?,?);");
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
            statement.setLong(3, solution == Integer.MAX_VALUE ? -100 : solution);
            statement.setBoolean(4, isopt);
            statement.executeUpdate();
            return getPbID(filename, policy, solution, isopt);
        }
    }
}
