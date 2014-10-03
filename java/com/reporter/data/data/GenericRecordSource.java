package com.reporter.data.data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.reporter.data.XLSCellValue;
import com.reporter.data.XLSConnectionBrokenException;
import com.reporter.data.XLSDataLockedException;
import com.reporter.data.XLSDataType;
import com.reporter.document.exception.XlsOutputValueNotFoundException;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.impl.Sheet;
import com.reporter.xml.data.SqlParametr;

/**
 * @author Ovesnov Ilya
 *
 */
public class GenericRecordSource implements XLSRecordSource{
    
    /**
     * layout that handle resultSet  
     */
    private Layout owner;

    /**
     * layout name  
     */
    private String ownerName;

    /**
     * current record fields values
     */
    private Map<String, XLSCellValue> record;
    
    /**
     * default value (first field or simple value)
     */
    private XLSCellValue defaultValue; 

    /**
     * name (SQL/ValueDistributor/UnboundSheet name)
     */
    private String name; 
    
    /**
     * group field name if source grouped
     */
    private String groupField = "";
    

    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#getField(java.lang.String)
     */
    public XLSCellValue getField(String fieldName) throws SQLException, IOException, XlsOutputValueNotFoundException {
        //TODO add check is data loaded
        XLSCellValue result = defaultValue; 
        if ((fieldName != null) && (fieldName.length()>0)){
            if (record != null){
                if (!record.containsKey(fieldName.toLowerCase())){
                    throw new XlsOutputValueNotFoundException("fieldNameNotFound", fieldName, name);
                } else {
                    result = record.get(fieldName.trim().toLowerCase());
                }
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#next()
     */
    public boolean next() throws SQLException, XlsOutputValueNotFoundException, IOException {
        return false;
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#release(java.lang.String)
     */
    public void release(Layout owner) throws XLSDataLockedException, IOException{
        if (owner == null){
            return;
        } 
        if ((this.owner != null) && (this.owner != owner)) {
            // exception - Attempt to release locked resultSet
            throw new XLSDataLockedException("dataLockedRelease", name, ownerName, getLayoutName(owner));
        } else {
            this.owner = null;
        }
    }
    
    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#nextInSubSet()
     */
    public boolean nextInSubSet() throws SQLException, XlsOutputValueNotFoundException, IOException{
        return false;
    }


    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#setOwner(java.lang.String)
     */
    public void setOwner(Layout owner) throws XLSDataLockedException, IOException {
        if (owner == null){
            return;
        } 
        if (this.owner == null){
            this.owner = owner;
            ownerName = getLayoutName(owner);
        } else if ((owner != null) && (!ownerName.equals(getLayoutName(owner)))) {
            // exception - Attempt to get locked resultSet
            throw new XLSDataLockedException("dataLockedSetOwner", name, ownerName, getLayoutName(owner), this.owner.getReferences().getReference());
        }
    }
    
    /**
     * get layout name
     * 
     * @param layout
     * @return layout name
     */
    private String getLayoutName(Layout layout){
        if (layout == null){
            return "";
        } else if (layout instanceof DinamicLayout){
            if (((DinamicLayout)layout).getCommand() == XLSCommands.XLSCommandSheet){
                return ((Sheet)layout).getSheetName().toLowerCase();
            } else {
                return ((DinamicLayout) layout).getElementName().toLowerCase();
            }
        } else {
            return layout.getReferences().getReference().toLowerCase();
        }
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#getOwner()
     */
    public Layout getOwner() {
        return owner;
    }

    /**
     * get owner command
     * 
     * @return owner command
     */
    public XLSCommands getOwnerCommand() {
        return owner.getCommand();
    }

    /**
     * @param name
     */
    public GenericRecordSource(String name) {
        super();
        this.name = name;
        record = new HashMap<String, XLSCellValue>();
    }
    
    /**
     * add or set field value in current record
     * 
     * @param fieldName
     * @param value
     */
    protected void setFieldValue(String fieldName, String value){
        XLSCellValue val = new XLSCellValue(value,XLSDataType.XLSDataText);
        if (record.size() == 0){
            defaultValue = val;
        }
        if ((fieldName == null) || (fieldName.trim().length() == 0)){
            record.put("~~DefaultValue~~", val);
        } else {
            record.put(fieldName.trim().toLowerCase(), val);
        }
    }

    /**
     * add or set field value in current record
     * 
     * @param fieldName
     * @param value
     */
    protected void setFieldValue(String fieldName, XLSCellValue value){
        if (record.size() == 0){
            defaultValue = value;
        }
        if ((fieldName == null) || (fieldName.trim().length() == 0)){
            record.put("~~DefaultValue~~", value);
        } else {
            record.put(fieldName.trim().toLowerCase(), value);
        }
    }

    /**
     * add or set field value in current record
     * 
     * @param fieldName
     * @param value
     */
    protected void clearRecord(){
        this.record = new HashMap<String, XLSCellValue>();
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#runSql(java.util.List)
     */
    public void runSql(List<SqlParametr> parametrs) throws XLSConnectionBrokenException, IOException, SQLException {
        //TODO check owner?
    }

    /* (non-Javadoc)
     * @see com.reporter.data.data.XLSRecordSource#setGroupField(java.lang.String)
     */
    public void setGroupField(String groupField) {
        this.groupField = groupField == null ? "" : groupField.toLowerCase().trim();
    }

    /**
     * @return the groupField
     */
    protected String getGroupField() {
        return groupField;
    }
    
    public boolean isRecordEmpty(){
        if ((record == null) ||(record.isEmpty())){
            return true;
        }
        return false;
    }

}
