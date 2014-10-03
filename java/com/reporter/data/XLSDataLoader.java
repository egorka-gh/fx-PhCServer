/**
 * 
 */
package com.reporter.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Ilya Ovesnov
 *
 */
public class XLSDataLoader {
    
    /**
     * default constructor
     *
     */
    public XLSDataLoader(){
        super();
    }

    /**
     * execute query
     * 
     * @param st
     * @param sql
     * @return result set
     * @throws SQLException
     */
    public static ResultSet executeQuery(Statement st, String sql) throws SQLException{
        return st.executeQuery(sql);
    }
}
