/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/10/2019
 */
module org.chocosolver.parsers {
    exports org.chocosolver.parser to args4j;
    requires org.chocosolver.solver;
    requires org.chocosolver.pf4cs;
    requires choco.geost;
    requires xcsp3.tools;
    requires args4j;
    requires gson;
    requires java.sql;
    requires antlr4.runtime;
    requires trove4j;
}