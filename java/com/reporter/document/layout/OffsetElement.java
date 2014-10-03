/**
 * 
 */
package com.reporter.document.layout;

/**
 * @author Administrator
 *
 */
public class OffsetElement {
    /**
     * row minimum index
     */
    private int rowMin;
    
    /**
     * row maximum index
     */
    private int rowMax;
    
    /**
     * column index
     */
    private int colMin;
    
    /**
     * column maximum index
     */
    private int colMax;
    
    /**
     * offset X
     */
    private int offsetX;
    
    /**
     * offset Y
     */
    private int offsetY;
    
    /**
     * default constructor
     */
    public OffsetElement(){
        super();
    }
    
    /**
     * Constructor
     * 
     * @param rowMin
     * @param rowMax
     * @param column
     * @param colMax 
     * @param offsetX 
     * @param offsetY 
     * @param offset
     */
    public OffsetElement(int rowMin, int rowMax, int column, int colMax, int offsetX, int offsetY){
        this.rowMin = rowMin;
        this.rowMax = rowMax;
        this.colMin = column;
        this.colMax = colMax;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * get column index
     * 
     * @return the column
     */
    public int getColMin() {
        return colMin;
    }

   
    /**
     * @return X offset
     */
    public int getOffsetX() {
		return offsetX;
	}

	/**
	 * @return Y offset
	 */
	public int getOffsetY() {
		return offsetY;
	}

	/**
     * get row maximum index
     * 
     * @return the rowMax
     */
    public int getRowMax() {
        return rowMax;
    }

    /**
     * get row minimum index
     * 
     * @return the row
     */
    public int getRowMin() {
        return rowMin;
    }

	/**
     * get column maximum
     * 
	 * @return colMax
	 */
	public int getColMax() {
		return colMax;
	}

    /**
     * set new column maximum
     * 
     * @param colMax the colMax to set
     */
    public void setColMax(int colMax) {
        this.colMax = colMax;
    }

    /**
     * set new column minimum
     * 
     * @param colMin the colMin to set
     */
    public void setColMin(int colMin) {
        this.colMin = colMin;
    }

    /**
     * set new offset X
     * 
     * @param offsetX the offsetX to set
     */
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * set new offset Y
     * 
     * @param offsetY the offsetY to set
     */
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * set new row maximum
     * 
     * @param rowMax the rowMax to set
     */
    public void setRowMax(int rowMax) {
        this.rowMax = rowMax;
    }

    /**
     * set new row minimum
     * 
     * @param rowMin the rowMin to set
     */
    public void setRowMin(int rowMin) {
        this.rowMin = rowMin;
    }

}
