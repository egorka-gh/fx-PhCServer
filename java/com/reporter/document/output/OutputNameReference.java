package com.reporter.document.output;

import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.document.layout.XlsReference;

public class OutputNameReference {

    /**
     * Outline from row
     */
    private int fromX;

    /**
     * Outline from column
     */
    private int fromY;

    /**
     * Outline to row
     */
    private int toX;

    /**
     * Outline to column
     */
    private int toY;

    /**
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     */
    public OutputNameReference(int fromX, int fromY, int toX, int toY) {
        super();
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    public OutputNameReference(String reference) {
        super();
        XlsReference xref = new XlsReference(reference); 
        fromX = xref.getColIndex();
        fromY = xref.getRowIndex();
        toX = fromX + xref.getColNumFromRegionReference()-1;
        toY = fromY + xref.getRowNumFromRegionReference()-1;
    }

    /**
     * @return the fromX
     */
    public int getFromX() {
        return fromX;
    }

    /**
     * @param fromX the fromX to set
     */
    public void setFromX(int fromX) {
        this.fromX = fromX;
    }

    /**
     * @return the fromY
     */
    public int getFromY() {
        return fromY;
    }

    /**
     * @param fromY the fromY to set
     */
    public void setFromY(int fromY) {
        this.fromY = fromY;
    }

    /**
     * @return the toX
     */
    public int getToX() {
        return toX;
    }

    /**
     * @param toX the toX to set
     */
    public void setToX(int toX) {
        this.toX = toX;
    }

    /**
     * @return the toY
     */
    public int getToY() {
        return toY;
    }

    /**
     * @param toY the toY to set
     */
    public void setToY(int toY) {
        this.toY = toY;
    }

    public String getReference(String sheetName){
        return XlsReference.composeReferenceString(sheetName, fromY, fromX, toY, toX);
    }
    
}
