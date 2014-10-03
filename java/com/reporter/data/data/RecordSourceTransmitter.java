package com.reporter.data.data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.reporter.data.XLSCellValue;
import com.reporter.data.XLSDataLockedException;
import com.reporter.document.exception.XlsOutputValueNotFoundException;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.xml.data.SqlParametr;

/**
 * @author Igor Zhadenov
 *
 */
public class RecordSourceTransmitter extends GenericRecordSource {
    /**
     * parent record source
     */
    private XLSRecordSource parentSource;
    
    /**
     * is any records fetched
     */
    private boolean isDataFetched = false;

    /**
     * is last move next failed
     */
    private boolean isAfterLast = false;

    /**
     * current value of group field
     * null if no records fetched
     */
    private Object currentGroupValue = null;


    /**
     * @param parentSource
     * @param layout
     * @throws XLSDataLockedException
     * @throws IOException
     */
    public RecordSourceTransmitter(XLSRecordSource parentSource, DinamicLayout layout) throws XLSDataLockedException, IOException {
        super(parentSource.getName());
        this.parentSource = parentSource;
        setOwner(layout);
    }
    
    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#next()
     */
    public boolean next() throws SQLException, XlsOutputValueNotFoundException, IOException {
        //TODO fix group record if record source grouped ?
        if (!canIterate() || isAfterLast){
            return false; 
        }
        if (!isDataFetched ){
            // no records fetched, parent already hold current record
            if (getGroupField().length() > 0){
                currentGroupValue = getField(getGroupField()).getValue();
            }
            isDataFetched = true;
            return true;
        }
        boolean result = false;
        if (getGroupField().length() == 0){
            //simple move next
            result = parentSource.nextInSubSet();
            isAfterLast = !result;
        } else {
            //move to next group value
            boolean fetched = true;
            while (fetched && getField(getGroupField()).equals(currentGroupValue)){
                fetched = parentSource.nextInSubSet();
            }
            if (fetched){
                currentGroupValue = getField(getGroupField()).getValue();
            } else {
                isAfterLast = true;
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
        if ((getGroupField().length() == 0) || !canIterate()){
            result = false; 
        } else {
            //try to move next
            if (parentSource.nextInSubSet()){
                //check if still in same group
                result = getField(getGroupField()).equals(currentGroupValue);
            } else {
                isAfterLast = true;
            }
        }
        return result;
    }

    
    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#getField(java.lang.String)
     */
    public XLSCellValue getField(String fieldName) throws SQLException, IOException, XlsOutputValueNotFoundException {
        return parentSource.getField(fieldName);
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#runSql(java.util.List)
     */
    public void runSql(List<SqlParametr> parametrs){
        // TODO check owner?
        isDataFetched = false;
        isAfterLast = false;
    }

    /**
     * check if can move next record
     * @return true if can iterate
     */
    private boolean canIterate(){
        return (getOwnerCommand() != XLSCommands.XLSCommandCell) || !isDataFetched;
    }

}
