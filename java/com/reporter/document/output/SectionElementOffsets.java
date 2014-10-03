package com.reporter.document.output;

import java.util.Map;
import java.util.HashMap;

import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.XLSCommands;


public class SectionElementOffsets {
    //starting & ending row column of layout
    private int startRow = 0;
    private int endRow = 0;
    private int startColumn = 0;
    private int endColumn = 0;
    //inside section element row & max offset at row (RowNumber, maxOffsetAtRow)
    private Map<Integer, Integer> yOffsets;
    
    //inside section element row & map offsets at column (Row, Column, OffsetAtRowAtColumn)
    private Map<Integer, Map<Integer, Integer>> xOffsets;

    private Map<Integer, Integer> rowMaping; //(resultingRow, templateRow )
    
    private int resultingOffsetY = 0;
    private int resultingOffsetX = 0;
    
    /**
     * @param layout
     */
    public SectionElementOffsets(Layout layout) {
        yOffsets = new HashMap<Integer, Integer>();
        xOffsets = new HashMap<Integer, Map<Integer, Integer>>();
        if (layout.getCommand() != XLSCommands.XLSCommandSheet){
            startRow = layout.getReferences().getRowIndex();
            endRow = startRow + layout.getReferences().getRowNumFromRegionReference()-1;
            startColumn = (int)layout.getReferences().getColIndex();
            endColumn = startColumn + layout.getReferences().getColNumFromRegionReference()-1;
        } else {
            //get max Row & Column from childs
            for (Layout lt : ((DinamicLayout)layout).getChildList()){
                int chldRw = lt.getReferences().getRowIndex() + lt.getReferences().getRowNumFromRegionReference()-1;
                endRow = Math.max(endRow,chldRw);
                int chldCl = ((int)lt.getReferences().getColIndex()) + lt.getReferences().getColNumFromRegionReference()-1;
                endColumn = Math.max(endColumn,chldCl);
            }
        }
    }
    
    
    /**
     * add offsets from child dynamic section
     * 
     * @param childSection
     */
    public void addChildSection(SectionOffset childSection){
        if (!(childSection.getLayuot() instanceof DinamicLayout)){
            return;
        }
        int row = childSection.getLayuot().getReferences().getRowIndex();
        int shiftRow = row + childSection.getLayuot().getReferences().getRowNumFromRegionReference();
        //add Y offset
        if (childSection.getYOffset()!= 0){
            //calc offsets inside layout hieght
            int addedOffs = 0; 
            for (int i = row+1; i <= shiftRow ; i++){
                Integer rowOffs = yOffsets.get(new Integer(i));
                addedOffs += (rowOffs == null) ? 0 : rowOffs.intValue(); 
            }
            if (childSection.getYOffset() > addedOffs){
                Integer rowOffs = yOffsets.get(new Integer(shiftRow));
                if (rowOffs == null){
                    //no offest at row
                    yOffsets.put(new Integer(shiftRow), new Integer(childSection.getYOffset()-addedOffs));
                } else {
                    //set max offest at row
                    yOffsets.put(new Integer(shiftRow), new Integer(Math.max(childSection.getYOffset()-addedOffs,rowOffs.intValue())));
                }
            }
            //reduce offsets added after current
            //it must be one case - current lt inside height of previous
            for (int i = shiftRow+1; i <= endRow+1; i++){
                Integer rowOffs = yOffsets.get(new Integer(i));
                if (rowOffs != null){
                    yOffsets.remove(new Integer(i));
                    if (rowOffs.intValue() > (childSection.getYOffset()-addedOffs)){
                        yOffsets.put(new Integer(i), new Integer(rowOffs.intValue() - (childSection.getYOffset()-addedOffs)));
                    }
                }
            }

        }
        //add X offset by row then by column
        int xOffsBefore = getXOffset(childSection.getLayuot());
        int xOffsAfter = childSection.getXOffset();
        if ((xOffsBefore + xOffsAfter)!= 0){
            int childCol = (int)childSection.getLayuot().getReferences().getColIndex();
            int shiftCol = childCol + childSection.getLayuot().getReferences().getColNumFromRegionReference();
            for (int i = row; i < shiftRow ; i++){
                Map<Integer, Integer> offsetsByX = xOffsets.get(new Integer(i));
                if (offsetsByX == null){
                    offsetsByX = new HashMap<Integer, Integer>();
                    if (xOffsBefore > 0){
                        offsetsByX.put(new Integer(childCol), new Integer(xOffsBefore));
                    }
                    if (xOffsAfter > 0){
                        offsetsByX.put(new Integer(shiftCol), new Integer(xOffsAfter));
                    }
                    xOffsets.put(new Integer(i), offsetsByX);
                } else {
                    //check offset before
                    if (xOffsBefore > 0){
                        int currBefore = 0; 
                        for (int j = startColumn; j <= childCol; j++){
                            if (offsetsByX.containsKey(new Integer(j))){
                                currBefore += offsetsByX.get(new Integer(j)).intValue(); 
                            }
                        }
                        if (currBefore < xOffsBefore){
                            /*
                            Integer addBefore = offsetsByX.get(childCol);
                            addBefore = (addBefore == null) ? (xOffsBefore - currBefore) : addBefore + (xOffsBefore - currBefore);
                            */
                            int addBefore = xOffsBefore - currBefore;
                            if (offsetsByX.containsKey(new Integer(childCol))){
                                addBefore =  addBefore + offsetsByX.get(new Integer(childCol)).intValue();
                            }
                            offsetsByX.put(new Integer(childCol), new Integer(addBefore));
                        }
                    }
                    // check offset after
                    if (xOffsAfter > 0){
                        offsetsByX.put(new Integer(shiftCol), new Integer(xOffsAfter));
                    }
                }
            }
        }
    }
    
    /**
     * calc Y offest for child layout
     * 
     * @param layout
     * @return
     */
    public int getYOffset(Layout layout){
        int result = 0;
        int ltRow = layout.getReferences().getRowIndex();
        //sum all Y offsets above layout
        for (int i = startRow; i <=ltRow; i++){
            if (yOffsets.containsKey(new Integer(i))){
                result += yOffsets.get(new Integer(i)).intValue(); 
            }
        }
        return result;
    }
    
    /*
    public int getYOffsetAtRow(int row){
        int result = 0;
        //sum all Y offsets above row
        for (int i = startRow; i <=row; i++){
            if (yOffsets.containsKey(i)){
                result += yOffsets.get(i); 
            }
        }
        return result;
    }
*/
    /**
     * calc X offest for child layout
     * 
     * @param layout
     * @return
     */
    public int getXOffset(Layout layout){
        int result = 0;
        int ltStartRow = layout.getReferences().getRowIndex();
        int ltEndRow = ltStartRow + layout.getReferences().getRowNumFromRegionReference();
        int ltColumn = (int)layout.getReferences().getColIndex();
        for (int i = ltStartRow; i < ltEndRow; i++){
            //look for max offset in layout rows
            int rowXOffs = 0;
            if (xOffsets.containsKey(new Integer(i))){
                //sum all X offsets before layout
                Map<Integer, Integer> offsetsByX = xOffsets.get(new Integer(i));
                for (int j = startColumn; j <= ltColumn; j++){
                    if (offsetsByX.containsKey(new Integer(j))){
                        rowXOffs += offsetsByX.get(new Integer(j)).intValue(); 
                    }
                }
            }
            result = Math.max(result, rowXOffs);
        }
        return result;
    }

    public int getXOffsetAtRow(int row){
        int result = 0;
        if (xOffsets.containsKey(new Integer(row))){
            //sum all X offsets
            Map<Integer, Integer> offsetsByX = xOffsets.get(new Integer(row));
            for (int offs : offsetsByX.values()){
                result += offs; 
            }
        }
        return result;
    }

    /**
     * resulting Y offset of element 
     * @return
     */
    public int getResultingOffsetY(){
        //sum all offsets by row
        return resultingOffsetY;
    }

    /**
     * resulting X offset of element
     * 
     * @return
     */
    public int getResultingOffsetX(){
        //get max X offset in all rows
        return resultingOffsetX;
    }

    public int getHeight(){
        //sum all offsets by row
        return (endRow - startRow + 1 + resultingOffsetY);
    }

    public int getWidth(){
        //get max X offset in all rows
        return (endColumn - startColumn + 1 + resultingOffsetX);
    }

    public boolean isRowInserted(int row){
        Integer tmplRow = rowMaping.get(new Integer(row));
        return (tmplRow == null) ? true : (tmplRow.intValue() < 0);
    }

    public int getNextNotInsertedRow(int row){
        int result = -1;
        Integer tmplRow = rowMaping.get(new Integer(row));
        if (tmplRow != null){
            if (tmplRow.intValue() < 0){
            	if ((row - tmplRow.intValue()) <= (endRow + resultingOffsetY)){
                    result =  row - tmplRow.intValue();
            	}
            }
        }
        /*
        int curRw = row;
        Integer tmplRow = rowMaping.get(curRw);
        while ((tmplRow != null) && (result < 0)){
            if (tmplRow > 0){
                result = curRw;  
            }
            curRw++;
            tmplRow = rowMaping.get(curRw);
        }
        */
        return result;
    }

    public int getPreviousNotInsertedRow(int row){
        int result = row;
        int curRw = row;
        Integer tmplRow = rowMaping.get(new Integer(curRw));
        while ((tmplRow != null) && (tmplRow.intValue() < 0)){
            curRw--;
            tmplRow = rowMaping.get(new Integer(curRw));
            if ((tmplRow != null) && (tmplRow.intValue() > 0)){
                result = curRw;  
            }
        }
        /*
        Integer tmplRow = rowMaping.get(row);
        if (tmplRow != null){
            if (tmplRow < 0){
                result = row + tmplRow; 
            }
        }
        */
        return result;
    }

    public void complite(){
        int currOffset = 0;
        //TODO for inserted row set offset to next not inserted
        //build row maping
        rowMaping = new HashMap<Integer, Integer>();
        for (int i = startRow; i <= endRow + 1; i++){
            Integer iI = new Integer(i);
            if (yOffsets.containsKey(iI)){
                int rowOffs = yOffsets.get(iI).intValue();
                for(int j = rowOffs; j > 0; j--){
                    rowMaping.put(new Integer(i+(rowOffs-j)+currOffset), new Integer(-1 * j));
                }
                currOffset += rowOffs;
                if (i < (endRow + 1)){
                	rowMaping.put(new Integer(i+currOffset), iI);
                }
            } else if (i < (endRow + 1)){
                rowMaping.put(new Integer(i+currOffset), iI);
            }
        }
        //fix offsets
        resultingOffsetY = currOffset;
        resultingOffsetX = 0;
        for (Map<Integer, Integer> rowXOffs : xOffsets.values()){
            int xOffs = 0;
            //sum all X offsets in row
            for (int offs : rowXOffs.values()){
                xOffs += offs; 
            }
            resultingOffsetX = Math.max(resultingOffsetX, xOffs);
        }
    }
    
    public int resultToTemplateY(int row){
    	int result = row;
        //move up to first not inserted row
        Integer moveRw = new Integer(getPreviousNotInsertedRow(row));
        Integer tmplRow = rowMaping.get(moveRw);
        if ((tmplRow != null) && (tmplRow.intValue() >= 0)){
        	result = tmplRow.intValue();
        }
    	return result;
    }
}
