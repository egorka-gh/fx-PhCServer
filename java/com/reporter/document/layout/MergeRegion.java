package com.reporter.document.layout;

import org.apache.poi.hssf.util.Region;

/**
 * @author egorka
 * cass to hold merge info
 */
public class MergeRegion implements Cloneable{
	/**
	 * Merge Type 
	 */
	private XLSMergeType mergeType = XLSMergeType.XLSMergeNone;
	
	/**
	 * merged region direction (if merge by element or section)
	 */
	private XlsLayoutDirection direction = XlsLayoutDirection.NoneDirection;
	
	/**
	 * merged region width
	 */
	private short width = 1;
	
	/**
	 * merged region height
	 */
	private int height = 1;
	
	/**
	 * section name to detect merged region size   
	 */
	private String sectionName;
	
    /**
     * merged region start row   
     */
    private int rowFrom;

    /**
     * merged region start column   
     */
    private short colFrom;
	

    /**
     * set merge starting row and column 
     * @param rowFrom the rowFrom to set
     * @param colFrom the colFrom to set
     */
    public void setFrom(int rowFrom, short colFrom) {
        this.rowFrom = rowFrom;
        this.colFrom = colFrom;
    }

    /**
     * set merge ending row  
     * @param rowFrom the rowTo to set
     */
    public void setRowTo(int rowTo) {
        height =  rowTo - rowFrom + 1;
        if (height < 1){
            height = 1;
        }
    }

    /**
     * set merge ending column  
     * @param colTo the column to set
     */
    public void setColumnTo(short colTo) {
        Integer wd = new Integer(colTo - colFrom + 1);
        width = wd.shortValue();
        if (width < 1){
            width = (short) 1;
        }
    }

    /**
	 * constructor
	 *  set merge region by current element sise
	 */
	public MergeRegion() {
		super();
		this.mergeType = XLSMergeType.XLSMergeCurrent;
	}

	/**
	 * constructor
	 *  set merge region by section
	 * @param sectionName 
	 */
	public MergeRegion(String sectionName) {
		super();
        if ((sectionName != null) && (sectionName.trim().length()>0)){
            this.mergeType = XLSMergeType.XLSMergeSection;
            this.sectionName = sectionName.trim().toLowerCase();
        } else {
            this.mergeType = XLSMergeType.XLSMergeCurrent;
        }
	}
	/**
	 * constructor
	 *  set static merge region
	 * @param width
	 * @param height
	 */
	public MergeRegion(int height, short width) {
		super();
		this.mergeType = XLSMergeType.XLSMergeStatic;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return the mergeType
	 */
	public XLSMergeType getMergeType() {
		return mergeType;
	}

	/**
	 * @return the sectionName
	 */
	public String getSectionName() {
		return sectionName;
	}

	/**
	 * @return the width
	 */
	public short getWidth() {
		return width;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(short width) {
		this.width = width;
	}

	/**
	 * @return the region to set merge
	 */
	public Region getMergeRegion() {
		return new Region(rowFrom, colFrom, 
				height < 2 ? rowFrom : rowFrom + height-1,
				width < 2 ? colFrom : (short)(colFrom + width-1));
	}

	/**
	 * @return the direction
	 */
	public XlsLayoutDirection getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(XlsLayoutDirection direction) {
		this.direction = direction;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public MergeRegion clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return (MergeRegion) super.clone();
    }

    /**
     * @return the colFrom
     */
    public short getColFrom() {
        return colFrom;
    }

    /**
     * @return the rowFrom
     */
    public int getRowFrom() {
        return rowFrom;
    }

}
