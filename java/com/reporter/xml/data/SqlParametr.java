/**
 * 
 */
package com.reporter.xml.data;

/**
 * @author Administrator
 *
 */
public class SqlParametr implements Cloneable{
    private String originalName;
    private String sqlName;
    private String columnName = "";
    private Object paramValue;
    private boolean dataLoaded;
    //is value come form ValueDistributor
    private boolean externalValue = false;

    /**
     * @return the dataLoaded
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }
    /**
     * @param dataLoaded the dataLoaded to set
     */
    public void setDataLoaded(boolean dataLoaded) {
        this.dataLoaded = dataLoaded;
    }
    /**
     * @return the paramValue
     */
    public Object getParamValue() {
        return paramValue;
    }
    /**
     * @param paramValue the paramValue to set
     */
    public void setParamValue(Object paramValue) {
        this.paramValue = paramValue;
    }
    public String getColumnName() {
        return columnName;
    }
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    public String getOriginalName() {
        return originalName;
    }
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    public String getSqlName() {
        return sqlName;
    }
    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public SqlParametr clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return (SqlParametr)super.clone();
    }
    /**
     * @return the externalValue
     */
    public boolean isExternalValue() {
        return externalValue;
    }
    /**
     * @param externalValue the externalValue to set
     */
    public void setExternalValue(boolean externalValue) {
        this.externalValue = externalValue;
    }

}
