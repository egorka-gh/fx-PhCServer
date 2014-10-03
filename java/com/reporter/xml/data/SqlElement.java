/**
 * 
 */
package com.reporter.xml.data;

import java.util.List;

/**
 * @author Administrator
 *
 */
public class SqlElement {
    private String sqlName;
    
    private String sql;
    
    private List<SqlParametr> parametrs;

    /**
     * @return the parametrs
     */
    public List<SqlParametr> getParametrs() {
        return parametrs;
    }

    /**
     * @param parametrs the parametrs to set
     */
    public void setParametrs(List<SqlParametr> parametrs) {
        this.parametrs = parametrs;
    }

    /**
     * @return the sql
     */
    public String getSql() {
        return sql;
    }

    /**
     * @param sql the sql to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * @return the sqlName
     */
    public String getSqlName() {
        return sqlName;
    }

    /**
     * @param sqlName the sqlName to set
     */
    public void setSqlName(String sqlName) {
        this.sqlName = sqlName.toLowerCase();
    }

}
