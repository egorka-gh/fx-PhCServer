/**
 * 
 */
package com.reporter.document.layout;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.RangeAddress;

/**
 * @author Administrator
 *
 */
public class XlsReference implements Cloneable{
    /**
     * list delimiter
     */
    private static String LIST_DELIMITER = "!"; 
    
    /**
     * cel delimiter
     */
    private static String CELL_DELIMITER = ":"; 

    /**
     * ranges join delimiter
     */
    private static String JOIN_DELIMITER = ","; 

    
    /**
     * flag indicates that reference to region. otherwise cell
     */
    private boolean region;
    
    /**
     * range address
     */
    private RangeAddress rangeAddress;
    
    /**
     * row
     */
    private int row;

    /**
     * column
     */
    private short column;
    
    /**
     * width
     */
    private int width;
    
    /**
     * height
     */
    private int height;
    
    /**
     * Constructor
     * 
     * @param reference
     */
    public XlsReference(String reference){
        setReference(reference);
    }

    /**
     * Constructor
     * 
     * @param sheetName
     * @param fromRow
     * @param fromCol
     * @param height
     * @param width
     */
    public XlsReference(String sheetName, int fromRow, int fromCol, int height, int width){
        CellReference fromCell = new CellReference(fromRow, fromCol);
        CellReference toCell = new CellReference(fromRow+height-1, fromCol+width-1);
        setRangeAddress(new RangeAddress(sheetName + LIST_DELIMITER + fromCell + CELL_DELIMITER + toCell));
    }

    /**
     * get flag indicates that reference to region. otherwise cell
     * 
     * @return the region
     */
    public boolean isRegion() {
        return region;
    }
    
    /**
     * set references
     * 
     * @param references
     */
    private void setReference(String references){
        RangeAddress ra = new RangeAddress(references);
        setRangeAddress(ra);
        setRegion(!ra.getFromCell().equals(ra.getToCell()));
    }

    /**
     * get reference
     * 
     * @return reference
     */
    public String getReference(){
        return getRangeAddress().getAddress();
    }
    
    /**
     * get from reference as string
     * 
     * @return from reference
     */
    public String getFromReferenceString(){
        return getRangeAddress().getFromCell();
    }
    
    /**
     * get to reference as string
     * 
     * @return to reference
     */
    public String getToReferenceString(){
        return getRangeAddress().getToCell();
    }
    
    /**
     * set new flag indicates that reference to region. otherwise cell
     * 
     * @param region the region to set
     */
    private void setRegion(boolean region) {
        this.region = region;
    }

    /**
     * set new range address
     * 
     * @param rangeAddress the rangeAddress to set
     */
    private void setRangeAddress(RangeAddress rangeAddress) {
        this.rangeAddress = rangeAddress;
        row = getCellReference().getRow();
        height = getRangeAddress().getHeight();
        CellReference cell = new CellReference(getRangeAddress().getFromCell());
        column = cell.getCol();
        width = getRangeAddress().getWidth();
    }
    
    /**
     * generate sheet information
     * 
     * @return sheet information
     */
    public String getSheetName(){
        if (getRangeAddress().getSheetName() != null){
            return getRangeAddress().getSheetName();
        }
        return "";
    }

    /**
     * generate sheet information
     * 
     * @return sheet information
     */
    private String getSheetInformation(){
        if (getRangeAddress().getSheetName() != null){
            return getRangeAddress().getSheetName() + LIST_DELIMITER;
        }
        return "";
    }

    /**
     * get cell reference
     * 
     * @return the cellReference
     */
    public CellReference getCellReference(){
        return new CellReference(getSheetInformation() + getRangeAddress().getFromCell());
    }
    
    /**
     * get cell reference to
     * 
     * @return the cellReference
     */
    public CellReference getCellReferenceTo(){
        return new CellReference(getSheetInformation() + getRangeAddress().getToCell());
    }

    /**
     * get new range address
     * 
     * @return the rangeAddress
     */
    public RangeAddress getRangeAddress(){
        return rangeAddress;
    }

    /**
     * equals reference
     * 
     * @param obj
     * @return true if equal
     */
    public boolean equals(Object obj){
        if (!(obj instanceof XlsReference)){
            return false;
        }
        XlsReference o = (XlsReference)obj;
        if (this.isRegion() != o.isRegion()){
            return false;
        }
        
        return (this.getFromReferenceString().equals(o.getFromReferenceString())) && (getToReferenceString().equals(getToReferenceString()));
    }
    
    /**
     * apply offsets for cell
     * 
     * @param rowOffset
     * @param colOffset
     * @return reference
     */
    protected XlsReference applyOffsetsForCell(int rowOffset, int colOffset){
        CellReference cell1 = getCellReference();
        CellReference cell2 = getCellReferenceTo();
        CellReference cell1Offset = new CellReference(cell1.getRow() + rowOffset, cell1.getCol() + colOffset);
        CellReference cell2Offset = new CellReference(cell2.getRow() + rowOffset, cell2.getCol() + colOffset);
        setRangeAddress(new RangeAddress(getSheetInformation() + cell1Offset + CELL_DELIMITER + cell2Offset));
        return this;
    }
    
    /**
     * get clone with applied offsets
     * 
     * @param rowOffset
     * @param colOffset
     * @return reference
     */
    public XlsReference getCloneWithOffset(int colOffset, int rowOffset){
        XlsReference result = this.clone();
        return result.applyOffsetsForCell(rowOffset, colOffset);
    }
    
    /**
     * get cloned reference
     * 
     * @return new reference
     */
    public XlsReference getClonedReference(){
        return this.clone();
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected XlsReference clone() {
        return new XlsReference(this.getReference());
    }

    /**
     * get number row in region
     * 
     * @return row number in region
     */
    public int getRowNumFromRegionReference(){
        return height; 
    }

    /**
     * get number col in region
     * 
     * @return col number in region
     */
    public int getColNumFromRegionReference(){
        return width; 
    }
    
    /**
     * get Row index
     * 
     * @return row index
     */
    public int getRowIndex(){
        return row;
    }

    /**
     * get col index
     * 
     * @return col index
     */
    public int getColIndex(){
        return column;
    }
    
    public void grow(int growByY, int growByX){
        rangeAddress.setSize(rangeAddress.getWidth() + growByX, rangeAddress.getHeight() + growByY);
        width = rangeAddress.getWidth();
        height = rangeAddress.getHeight();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getRangeAddress().getAddress();
    }

    public static String composeReferenceString(String sheetName, int fromRow, int fromCol, int toRow, int toCol){
        String result;
        CellReference fromCell = new CellReference(fromRow, fromCol, true, true);
        CellReference toCell = new CellReference(toRow, toCol, true, true);
        /*
        RangeAddress ar = new RangeAddress(sheetName + LIST_DELIMITER + fromCell + CELL_DELIMITER + toCell);
        result = ar.getAddress();
        */
        result = sheetName + LIST_DELIMITER + fromCell + CELL_DELIMITER + toCell;
        return result;
    }

    public static String joinReferences(String reference1, String reference2){
        return reference1 + JOIN_DELIMITER + reference2;
    }


    
}
