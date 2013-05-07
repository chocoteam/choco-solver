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
    private static final int AL_RES = 10, AL_OBJ = 20, AL_TIME = 30, AL_MOD = 40, AL_MUL = 50;

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

    public void compare(String filename, Solver solver) {
        try {
            File instance = new File(filename);
            String name = instance.getName();
            ResolutionPolicy policy = solver.getSearchLoop().getObjectivemanager().getPolicy();

            statement = connection.prepareStatement("select * from DESCRIPTION where FILENAME = ? ;");
            statement.setString(1, name);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                updateData(name, solver, policy);
                if (resultSet.next()) {
                    addAlarm(name, AL_MUL);
                }
            } else {
                insertData(name, solver, policy);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update DESCRIPTION with data related to filename
     *
     * @param name   name of the instance
     * @param solver the solver
     * @param policy the resolution policy
     */
    private void updateData(String name, Solver solver, ResolutionPolicy policy) {
        try {
            resetAlarm(name); // clean up the ALARM table

            String res = resultSet.getString("RESOLUTION");
            ResolutionPolicy aPolicy = res.equals(SAT) ? ResolutionPolicy.SATISFACTION :
                    res.equals(MIN) ? ResolutionPolicy.MINIMIZE : ResolutionPolicy.MAXIMIZE;

            if (aPolicy.equals(policy)) {
                int bestSol = resultSet.getInt("BEST_SOL");
                int curSol;
                switch (policy) {
                    case MINIMIZE:
                        curSol = solver.getMeasures().getObjectiveValue();
                        if (curSol > bestSol) {
                            addAlarm(name, AL_OBJ);
                        } else if (curSol < bestSol) {
                            addAlarm(name, AL_OBJ);
                            updateField(name, "BEST_SOL", curSol);
                        }
                        break;
                    case MAXIMIZE:
                        curSol = solver.getMeasures().getObjectiveValue();
                        if (curSol < bestSol) {
                            addAlarm(name, AL_OBJ);
                        } else if (curSol > bestSol) {
                            addAlarm(name, AL_OBJ);
                            updateField(name, "BEST_SOL", curSol);
                        }
                        break;
                    default:
                        break;
                }
                long bestTime = resultSet.getLong("TIME");
                long curTime = (long) solver.getMeasures().getTimeCount();
                double ratio = ((curTime - bestTime) / bestTime) * 100;
                if (ratio > 10.0) {
                    addAlarm(name, AL_TIME);
                    if (curTime < bestTime) {
                        updateField(name, "TIME", curTime);
                    }
                }

                int bestVars = resultSet.getInt("NB_VAR");
                int curVars = solver.getNbVars();
                if (curVars != bestVars) {
                    addAlarm(name, AL_MOD);
                }

                int bestCstrs = resultSet.getInt("NB_CSTR");
                int curCstrs = solver.getNbCstrs();
                if (curCstrs != bestCstrs) {
                    addAlarm(name, AL_MOD);
                }

            } else {
                addAlarm(name, AL_RES);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateField(String name, String field, int value) {
        try {
            PreparedStatement pstatement = connection.prepareStatement("update DESCRIPTION set " + field + " = ? WHERE FILENAME = ?;");
            pstatement.setString(2, name);
            pstatement.setInt(1, value);
            pstatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateField(String name, String field, long value) {
        try {
            PreparedStatement pstatement = connection.prepareStatement("update DESCRIPTION set " + field + " = ? WHERE FILENAME = ?;");
            pstatement.setString(2, name);
            pstatement.setLong(1, value);
            pstatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert a new record in DESCRIPTION
     *
     * @param name   name of the instance
     * @param solver the solver
     * @param policy the resolution policy
     */
    private void insertData(String name, Solver solver, ResolutionPolicy policy) {
        try {
            statement = connection.prepareStatement("insert into DESCRIPTION values (?, ?, ?, ?, ?, ?)");
            statement.setString(1, name);
            switch (policy) {
                case SATISFACTION:
                    statement.setString(2, SAT);
                    statement.setInt(3, 0);
                    break;
                case MINIMIZE:
                    statement.setString(2, MIN);
                    statement.setInt(3, solver.getSearchLoop().getObjectivemanager().getBestValue());
                    break;
                case MAXIMIZE:
                    statement.setString(2, MAX);
                    statement.setInt(3, solver.getSearchLoop().getObjectivemanager().getBestValue());
                    break;
            }
            statement.setLong(4, (long) solver.getMeasures().getTimeCount());
            statement.setInt(5, solver.getNbVars());
            statement.setInt(6, solver.getNbCstrs());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetAlarm(String filename) {
        try {
            PreparedStatement pstatement = connection.prepareStatement("delete from ALARM where FILENAME = ? ;");
            pstatement.setString(1, filename);
            pstatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addAlarm(String filename, int alarm) {
        try {
            PreparedStatement pstatement = connection.prepareStatement("insert into ALARM values (?,?)");
            pstatement.setString(1, filename);
            pstatement.setInt(2, alarm);
            pstatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
