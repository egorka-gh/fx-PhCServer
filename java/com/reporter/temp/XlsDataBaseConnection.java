/**
 * 
 */
package com.reporter.temp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Administrator
 *
 */
public class XlsDataBaseConnection {
    
    /**
     * connection
     */
    private static Connection cn;
    
    /**
     * get Connection
     */
    public void createConnection(){
        String url = "jdbc:odbc:repa";
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            cn = DriverManager.getConnection(url);
//            st = cn.prepareStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @return the cn
     */
    public Connection getCn() {
        return cn;
    }
    
    /**
     * load data from sql
     * 
     * @param sql
     * @return resultSet
     * @throws SQLException
     */
//    public ResultSet loadDataFromSql(String sql) throws SQLException{
//        return st.executeQuery(sql);
//    }
    
    /**
     * close statement
     */
    public void closeStatement(){
        try {
            if ((cn != null) && (!cn.isClosed())){
                cn.close();
                
            }
//            if (st != null){
//                st.close();
//            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}