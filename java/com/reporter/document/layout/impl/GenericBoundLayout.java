/**
 * 
 */
package com.reporter.document.layout.impl;

import java.util.ArrayList;
import java.util.List;

import com.reporter.document.layout.BoundLayout;
import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.xml.data.SqlParametr;


/**
 * @author Ilya Ovesnov
 *
 */
public class GenericBoundLayout extends GenericLayout implements BoundLayout{
    
    /**
     * layout direction
     */
    private XlsLayoutDirection direction;

    /**
     * sql name
     */
    private String sqlName;
    
    /**
     * column name
     */
    private String columnName;
    
    /**
     * list with parametrs
     */
    private List<SqlParametr> parametrs;

    /**
     * Outline section by rows 
     */
    private boolean outlineH = false;
    /**
     * Outline section by columns 
     */
    private boolean outlineV = false;
    
    /**
     * Outline section  
     */
    private boolean outline = false;

    /**
     * default constructor
     *
     */
    public GenericBoundLayout(){
        this.direction = XlsLayoutDirection.NoneDirection;
        this.parametrs = new ArrayList<SqlParametr>();
    }
    
    /**
     * sql name
     * 
     * @return the sqlName
     */
    public String getSqlName() {
        return sqlName;
    }

    /**
     * set new sql name
     * 
     * @param sqlName the sqlName to set
     */
    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    /**
     * get column name
     * 
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * set new column name
     * 
     * @param columnName the columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * get element name
     * 
     * @return element name
     */
    public String getElementName(){
        return getReferences().getReference();
    }

    /**
     * get layout direction
     * 
     * @return the direction
     */
    public XlsLayoutDirection getDirection() {
        return direction;
    }

    /**
     * set new layout direction
     * 
     * @param direction the direction to set
     */
    protected void setDirection(XlsLayoutDirection direction) {
        this.direction = direction;
    }

    /**
     * get list with parametrs
     * 
     * @return the parametrs
     */
    public List<SqlParametr> getParametrs() {
        return parametrs;
    }

    /**
     * set new list with parametrs
     * 
     * @param parametrs the parametrs to set
     */
    public void setParametrs(List<SqlParametr> parametrs) {
        this.parametrs = parametrs;
    }

    /**
     * @return the outlineH
     */
    public boolean isOutlineH() {
        return outlineH;
    }

    /**
     * @param outlineH the outlineH to set
     */
    public void setOutlineH(boolean outlineH) {
        this.outlineH = outlineH;
    }

    /**
     * @return the outlineV
     */
    public boolean isOutlineV() {
        return outlineV;
    }

    /**
     * @param outlineV the outlineV to set
     */
    public void setOutlineV(boolean outlineV) {
        this.outlineV = outlineV;
    }

	/* (non-Javadoc)
	 * @see com.reporter.document.layout.BoundLayout#isOutline()
	 */
	public boolean isOutline() {
		return outline;
	}

	/* (non-Javadoc)
	 * @see com.reporter.document.layout.BoundLayout#setOutline(boolean)
	 */
	public void setOutline(boolean outline) {
		this.outline=outline;
	}

}
