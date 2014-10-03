/**
 * 
 */
package com.reporter.document.layout;

import java.util.List;

import com.reporter.xml.data.SqlParametr;

/**
 * @author Administrator
 *
 */
public interface BoundLayout extends Layout{
    /**
     * get sql name
     * 
     * @return the sqlName
     */
    public String getSqlName();

    /**
     * set new sql name
     * 
     * @param sqlName the sqlName to set
     */
    public void setSqlName(String sqlName);

    /**
     * get column name
     * 
     * @return the columnName
     */
    public String getColumnName();

    /**
     * set new column name
     * 
     * @param columnName the columnName to set
     */
    public void setColumnName(String columnName);
    
    /**
     * get element name
     * 
     * @return element name
     */
    public String getElementName();
    
    /**
     * get layout direction
     * 
     * @return the direction
     */
    public XlsLayoutDirection getDirection();

    /**
     * get list with parametrs
     * 
     * @return the parametrs
     */
    public List<SqlParametr> getParametrs();

    /**
     * set new list with parametrs
     * 
     * @param parametrs the parametrs to set
     */
    public void setParametrs(List<SqlParametr> parametrs);

    /**
     * @return the outlineH
     */
    public boolean isOutlineH();

    /**
     * @param outlineH the outlineH to set
     */
    public void setOutlineH(boolean outlineH);

    /**
     * @return the outlineV
     */
    public boolean isOutlineV();

    /**
     * @param outlineV the outlineV to set
     */
    public void setOutlineV(boolean outlineV);

    /**
     * @return the outline
     */
    public boolean isOutline();

    /**
     * @param outlineV the outlineV to set
     */
    public void setOutline(boolean outlineV);
}
