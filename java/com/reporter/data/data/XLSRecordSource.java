package com.reporter.data.data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.reporter.data.XLSCellValue;
import com.reporter.data.XLSConnectionBrokenException;
import com.reporter.data.XLSDataLockedException;
import com.reporter.document.exception.XlsOutputValueNotFoundException;
import com.reporter.document.layout.Layout;
import com.reporter.xml.data.SqlParametr;

/**
 * 
 * @author Igor Zhadenov
 *
 */
public interface XLSRecordSource{
    
    /**
     * @return the owner
     */
    public Layout getOwner();

    /**
     * @param owner the owner to set
     * @throws XLSDataLockedException 
     * @throws IOException 
     */
    public void setOwner(Layout owner) throws XLSDataLockedException, IOException ;
    
    /**
     * get field value
     * 
     * @param fieldName
     * @return field value
     * @throws SQLException
     * @throws IOException 
     * @throws XlsOutputValueNotFoundException 
     */
    public XLSCellValue getField(String fieldName) throws SQLException, IOException, XlsOutputValueNotFoundException;
    
    /**
     * next
     * 
     * @return true if has next
     * @throws SQLException
     * @throws IOException 
     * @throws XlsOutputValueNotFoundException 
     */
    public boolean next() throws SQLException, XlsOutputValueNotFoundException, IOException;
    
    /**
     * next record in parent records sub set (for group & record source transmitter)
     * 
     * @return true if have next in subset
     * @throws SQLException
     * @throws XlsOutputValueNotFoundException
     * @throws IOException
     */
    public boolean nextInSubSet() throws SQLException, XlsOutputValueNotFoundException, IOException;

    /**
     * get name (sql name)
     * 
     * @return the Name
     */
    public String getName();

    /**
     * release RecordSource (make it available for other layouts)
     * @param owner the owner of RecordSet
     * @throws XLSDataLockedException 
     * @throws IOException 
     * @throws SQLException 
     */
    public void release(Layout owner) throws XLSDataLockedException, IOException, SQLException;

    /**
     * open resultset with parametrs for bound source
     * 
     * @param parameters
     * @throws IOException 
     * @throws XLSConnectionBrokenException 
     * @throws SQLException 
     */
    public void runSql(List<SqlParametr> parameters) throws XLSConnectionBrokenException, IOException, SQLException;
    
    /**
     * @param groupField the groupField to set
     */
    public void setGroupField(String groupField);
    
}
