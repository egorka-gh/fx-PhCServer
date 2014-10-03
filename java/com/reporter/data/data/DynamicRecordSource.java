package com.reporter.data.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.sql.Date;
import java.text.DateFormat;

import com.reporter.data.XLSCellValue;
import com.reporter.data.XLSConnectionBrokenException;
import com.reporter.data.XLSDataLockedException;
import com.reporter.data.XLSDataType;
import com.reporter.document.exception.XlsOutputValueNotFoundException;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.xml.data.SqlElement;
import com.reporter.xml.data.SqlParametr;

public class DynamicRecordSource extends GenericRecordSource {
    
    /**
     * is last move next failed
     */
    private boolean isAfterLast = false;

    /**
     * is any records fetched
     */
    private boolean isDataFetched = false;

    /**
     * sql info holder 
     */
    private SqlElement sqlElement;
    
    /**
     * database connection
     */
    private Connection connection;

    /**
     * result set
     */
    private ResultSet resultSet;

    /**
     * current value of group field
     * null if no records fetched
     */
    private Object currentGroupValue = null;
    

    /**
     * Constructor
     * 
     * @param layout
     * @param sqlElement
     * @param connection
     * @throws XLSDataLockedException
     * @throws IOException
     */
    public DynamicRecordSource(DinamicLayout layout, SqlElement sqlElement, Connection connection) throws XLSDataLockedException, IOException {
        super(sqlElement.getSqlName());
        setOwner(layout);
        this.sqlElement = sqlElement;
        this.connection = connection;
    }


    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#next()
     */
    public boolean next() throws SQLException, XlsOutputValueNotFoundException, IOException {
        //TODO fix group record if record source grouped ?
        if (!canIterate() || isAfterLast){
            return false; 
        } 
        boolean result = false;
        if (!isDataFetched || (getGroupField().length() == 0)){
            //simple move next
            result = resultSet.next();
            if (result){
                fillRecord();
                if (getGroupField().length() != 0){
                    currentGroupValue = getField(getGroupField()).getValue();
                }
            } else {
                isAfterLast = true;
                if (!isDataFetched){
                    //generate null record to fill dynamic borders & formats
                    result = true;
                    createNullRecord();
                }
            }
        } else {
            //move to next group value
            boolean fetched = true;
            while (fetched && getField(getGroupField()).equals(currentGroupValue)){
                fetched = resultSet.next();
                if (fetched){
                    fillRecord();
                }  else {
                    isAfterLast = true;
                }
            }
            if (fetched){
                currentGroupValue = getField(getGroupField()).getValue();
            }
            result = fetched; 
        }
        isDataFetched = true;
        return result;
    }
    
    
    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#nextInSubSet()
     */
    public boolean nextInSubSet() throws SQLException, XlsOutputValueNotFoundException, IOException {
        boolean result = false;
        if ((getGroupField().length() == 0) || !canIterate() || isAfterLast){
            result = false; 
        } else {
            //try to move next
            if (resultSet.next()){
                fillRecord();
                //check if still in same group
                result = getField(getGroupField()).equals(currentGroupValue);
            } else {
                isAfterLast = true;
            }
        }
        return result;
    }

    /**
     * convert sql data type to cell value data type
     * 
     * @param sqlType
     * @return
     */
    private XLSDataType toReportType(int sqlType){
        XLSDataType result = XLSDataType.XLSDataText;
        if ((sqlType == Types.DATE) 
                || (sqlType == Types.TIMESTAMP)
                || (sqlType == Types.TIME)){
            result = XLSDataType.XLSDataDate;
        } else if ((sqlType == Types.BIGINT) 
                || (sqlType == Types.BIT)
                || (sqlType == Types.DECIMAL)
                || (sqlType == Types.DOUBLE)
                || (sqlType == Types.FLOAT)
                || (sqlType == Types.INTEGER)
                || (sqlType == Types.NUMERIC)
                || (sqlType == Types.REAL)
                || (sqlType == Types.SMALLINT)
                || (sqlType == Types.TINYINT)){
            result = XLSDataType.XLSDataNumeric;
        }
        return result;
    }

    /**
     * save resultSet fields value
     * @throws SQLException
     */
    private void fillRecord() throws SQLException{
        clearRecord();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int colNum = metaData.getColumnCount();
        for (int i = 1; i <= colNum; i++){
            //String colName = metaData.getColumnName(i);
        	String colName = metaData.getColumnLabel(i);
            XLSCellValue colVal = new XLSCellValue(resultSet.getObject(i), toReportType(metaData.getColumnType(i)));
            setFieldValue(colName.toLowerCase(), colVal);
        }
    }

    /**
     * fills current record ws empty values
     * 
     * @throws SQLException
     */
    private void createNullRecord(){
        clearRecord();
        ResultSetMetaData metaData;
        try {
            metaData = resultSet.getMetaData();
            int colNum = metaData.getColumnCount();
            for (int i = 1; i <= colNum; i++){
                String colName = metaData.getColumnName(i);
                //setFieldValue(colName.toLowerCase(), "");
                XLSCellValue colVal = new XLSCellValue(null, toReportType(metaData.getColumnType(i)));
                setFieldValue(colName.toLowerCase(), colVal);
            }
        } catch (SQLException e) {
            // 4 db2 if ResultSet has no records it return closed ResultSet
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#runSql(java.util.List)
     */
    public void runSql(List<SqlParametr> parametrs) throws XLSConnectionBrokenException, IOException, SQLException{ // 
        // TODO check owner?
        isDataFetched = false;
        isAfterLast = false;
        //open result set
        if ((connection != null) && (!connection.isClosed())){
            PreparedStatement st= connection.prepareStatement(sqlElement.getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (parametrs != null){
                int i =1;
                for (SqlParametr param : parametrs){
                    if (!param.isExternalValue()){
                        st.setObject(i, param.getParamValue());
                    } else {
                        st.setString(i, (String) param.getParamValue());   
                    }
                    i++;
                }
            }
           // try {
                resultSet  = st.executeQuery();
           /*
           } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            */
        } else {
            throw new XLSConnectionBrokenException("connectionBroken",sqlElement.getSqlName());
        }
    }
    
    /**
     * check if can move next record
     * @return true if can iterate
     */
    private boolean canIterate(){
        return (getOwnerCommand() != XLSCommands.XLSCommandCell) || !isDataFetched;
    }


    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#getField(java.lang.String)
     */
    public XLSCellValue getField(String fieldName) throws SQLException, IOException, XlsOutputValueNotFoundException {
        if (super.isRecordEmpty() || this.isAfterLast){
            return new XLSCellValue("");
        }
        return super.getField(fieldName);
    }


    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#release(com.reporter.document.layout.Layout)
     */
    public void release(Layout owner) throws XLSDataLockedException, IOException {
        super.release(owner);
        try {
            this.resultSet.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
           // e.printStackTrace();
        }
    }

}
