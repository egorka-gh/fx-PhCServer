package com.reporter.data.data;

import java.io.IOException;
import java.sql.SQLException;

import com.reporter.data.XLSDataLockedException;
import com.reporter.document.layout.Layout;

/**
 *simple record source with one record and one default field
 *use for unbound sheet & ValueDistributor or any static values for report 
 */
public class StaticRecordSource extends GenericRecordSource {
    
    /**
     * is data fetched
     */
    private boolean isDataFetched = false;

    /**
     * @param name
     * @param value
     */
    public StaticRecordSource(String name, String value){
        super(name);
        setFieldValue(null, value);
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#next()
     */
    public boolean next() throws SQLException {
        if (!isDataFetched){
            isDataFetched = true; 
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.GenericRecordSource#release(java.lang.String)
     */
    public void release(Layout owner) throws XLSDataLockedException, IOException {
        super.release(owner);
        isDataFetched = false;
    }

}
