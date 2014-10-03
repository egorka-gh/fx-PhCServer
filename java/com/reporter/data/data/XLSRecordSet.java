/**
 * 
 */
package com.reporter.data.data;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.reporter.data.XLSDataLockedException;
import com.reporter.document.exception.XlsOutputValueNotFoundException;
import com.reporter.xml.data.SqlParametr;

/**
 * @author Administrator
 *
 */
public class XLSRecordSet {
    
    /**
     * sql name
     */
    private String sqlName;
    
    /**
     * result set
     */
    private ResultSet resultSet;
    
    /**
     * statement with parametrs 
     */
    private PreparedStatement preparedSQL;
    
    /**
     * values
     */
    private Map<String, String> values;

    /**
     * layout that handle resultSet  
     */
    private String owner;
    
    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     * @throws XLSDataLockedException 
     * @throws IOException 
     */
    public void setOwner(String owner) throws XLSDataLockedException, IOException {
        if (owner == null){
            return;
        } 
        if (this.owner == null){
            this.owner = owner;
        } else if ((owner != null) && (this.owner != owner)) {
            // exception - Attempt to get locked resultSet
            throw new XLSDataLockedException("dataLockedSetOwner", this.sqlName, this.owner, owner);
        }
    }
    
    /**
     * release RecordSet (make it available for other layouts)
     * @param owner the owner of RecordSet
     * @throws XLSDataLockedException 
     * @throws IOException 
     */
    public void releaseRecordSet(String owner) throws XLSDataLockedException, IOException{
        if (owner == null){
            return;
        } 
        if ((this.owner != null) && (this.owner != owner)) {
            // exception - Attempt to release locked resultSet
            throw new XLSDataLockedException("dataLockedRelease", this.sqlName, this.owner, owner);
        } else {
            this.owner = null;
        }
    }

    /**
     * constructor
     * 
     * @param sqlName
     * @param resultSet
     * @param preparedStatement
     */
    public XLSRecordSet(String sqlName, ResultSet resultSet, PreparedStatement preparedStatement){
        this.sqlName = sqlName;
        this.resultSet = resultSet;
        this.preparedSQL = preparedStatement;
        this.values = new HashMap<String, String>();
    }
    
    /**
     * constructor
     * 
     * @param sqlName
     * @param resultSet
     */
    public XLSRecordSet(String sqlName, ResultSet resultSet){
        this.sqlName = sqlName;
        this.resultSet = resultSet;
        this.values = new HashMap<String, String>();
    }
    
    /**
     * get column value
     * 
     * @param columnName
     * @return column value
     * @throws SQLException
     * @throws IOException 
     * @throws XlsOutputValueNotFoundException 
     */
    public String getColumnValue(String columnName) throws SQLException, IOException, XlsOutputValueNotFoundException{
        if ((columnName == null) || (columnName.length()==0)){
            columnName = getResultSet().getMetaData().getColumnName(1);
        }
        if (!values.containsKey(columnName.toLowerCase())){
            throw new XlsOutputValueNotFoundException("fieldNameNotFound", columnName, sqlName);
        }
        String result = values.get(columnName.toLowerCase());
        return result == null ? "" : result;
    }
    
    /**
     * next
     * 
     * @return true if has next
     * @throws SQLException
     */
    public boolean next() throws SQLException{
        this.values = new HashMap<String, String>();
        boolean result = getResultSet().next();
        if (result ){
            ResultSetMetaData metaData = getResultSet().getMetaData();
            int colNum = metaData.getColumnCount();
            for (int i = 1; i <= colNum; i++){
                String colName = metaData.getColumnName(i);
                String colVal = getResultSet().getString(colName);
                values.put(colName.toLowerCase(), colVal);
            }
        }
        return result;
    }
    
    /**
     * get sql name
     * 
     * @return the sqlName
     */
    public String getSqlName() {
        return sqlName;
    }

    /**
     * get result set
     * 
     * @return the resultSet
     */
    public ResultSet getResultSet() {
        return resultSet;
    }

    /**
     * reopen resultset with new parametrs
     * 
     * @param owner
     * @param parameters
     * @throws XLSDataLockedException 
     * @throws SQLException 
     * @throws IOException 
     */
    public void reOpen(String owner, List<SqlParametr> parameters) throws XLSDataLockedException, SQLException, IOException{
        setOwner(owner);
        if ((resultSet!=null)){
            if (resultSet.isAfterLast()){
                resultSet.close();
            } else {
                return;
            }
        }
        if (parameters != null){
            int i =1;
            for (SqlParametr param : parameters){
                preparedSQL.setObject(i, param.getParamValue());
                i++;
            }
        }
        ResultSet rst = preparedSQL.executeQuery();
        resultSet = rst;
    }

}
