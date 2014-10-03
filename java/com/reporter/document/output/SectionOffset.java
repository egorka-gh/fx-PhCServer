package com.reporter.document.output;

import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.XlsLayoutDirection;

public class SectionOffset {
    private Layout layuot;
    
    /**
     * starting section Y offset 
     */
    private int startingYOffset;
    
    /**
     * starting section X offset
     */
    private  int startingXOffset;
    /**
     * current section Y offset if section grow direction byRow (Row)
     */
    private int yOffset;
    
    /**
     * current section X offset if section grow direction by Column (Column)
     */
    private  int xOffset;

    /**
     * max section Y offset if section not grow by row (Column, Frame, Sheet)
     */
    private int maxOffsetY;

    /**
     * max section X offset if section not grow by column (Row, Frame, Sheet)
     */
    private  int maxOffsetX;
    
    
    /**
     * @param layuot
     */
    public SectionOffset(Layout layuot) {
        this.layuot = layuot;
        yOffset = 0;
        xOffset = 0;
        maxOffsetY = 0;
        maxOffsetX = 0;
    }
    
    /**
     * end current iteration 
     * grow in main direction
     * adds current section element offsets 
     * 
     * @param elementOffsets
     */
    public void grow(SectionElementOffsets elementOffsets){
        if (layuot instanceof DinamicLayout){
            if (((DinamicLayout)layuot).getDirection() == XlsLayoutDirection.RowDirection){
                //grow by row 
                yOffset += ((DinamicLayout)layuot).getRegionDimentionY(); 
                yOffset += elementOffsets.getResultingOffsetY();
                maxOffsetX = Math.max(maxOffsetX, elementOffsets.getResultingOffsetX());
            } else if (((DinamicLayout)layuot).getDirection() == XlsLayoutDirection.ColumnDirection){
                //grow by column
                xOffset += ((DinamicLayout)layuot).getRegionDimensionX();
                xOffset += elementOffsets.getResultingOffsetX();
                maxOffsetY = Math.max(maxOffsetY, elementOffsets.getResultingOffsetY());
            } else {
                //don't grow just fix max offsets  
                maxOffsetX = Math.max(maxOffsetX, elementOffsets.getResultingOffsetX());
                maxOffsetY = Math.max(maxOffsetY, elementOffsets.getResultingOffsetY());
            }
        }
    }

    /**
     * end all iteration fix offsets
     */
    public void stopGrow(){
        if (layuot instanceof DinamicLayout){
            if ((((DinamicLayout)layuot).getDirection() == XlsLayoutDirection.RowDirection) && yOffset > 0){
                yOffset -= ((DinamicLayout)layuot).getRegionDimentionY(); 
            } else if ((((DinamicLayout)layuot).getDirection() == XlsLayoutDirection.ColumnDirection) && xOffset > 0){
                xOffset -= ((DinamicLayout)layuot).getRegionDimensionX();
            }
            yOffset += maxOffsetY;
            xOffset += maxOffsetX;
        }
    }

    /**
     * @return the xOffset
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * @return the yOffset
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * @return the layuot
     */
    public Layout getLayuot() {
        return layuot;
    }

    /**
     * @return the startingXOffset
     */
    public int getStartingXOffset() {
        return startingXOffset;
    }

    /**
     * @param startingXOffset the startingXOffset to set
     */
    public void setStartingXOffset(int startingXOffset) {
        this.startingXOffset = startingXOffset;
    }

    /**
     * @return the startingYOffset
     */
    public int getStartingYOffset() {
        return startingYOffset;
    }

    /**
     * @param startingYOffset the startingYOffset to set
     */
    public void setStartingYOffset(int startingYOffset) {
        this.startingYOffset = startingYOffset;
    }

}
