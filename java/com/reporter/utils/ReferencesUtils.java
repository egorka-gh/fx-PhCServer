/**
 * 
 */
package com.reporter.utils;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.RangeAddress;

import com.reporter.document.layout.XlsReference;

/**
 * @author Administrator
 *
 */
public class ReferencesUtils {
//    private final static String DEFAULT_LIST_NAME = "Лист1";
    private final static String DEFAULT_LIST_DELIMITER = "!";
//    private final static String DEFAULT_CELL_DELIMITER = ":";
    private final static String DEFAULT_CELL_PREFICS = "$";
    
    /**
     * create cell reference
     * 
     * @param sheetName
     * @param rowNum
     * @param cellNum
     * @return references
     */
    public String getCellReferences(String sheetName, int rowNum, short cellNum){
        String postfics = DEFAULT_CELL_PREFICS + rowNum + DEFAULT_CELL_PREFICS + cellNum;
        return sheetName == null ? postfics : sheetName + DEFAULT_LIST_DELIMITER + postfics;
    }
    
    /**
     * compare cell reference
     * 
     * @param first
     * @param second
     * @return true if equal
     */
    public boolean compareCellReference(CellReference first, CellReference second){
        boolean result = first.getSheetName().equals(second.getSheetName()) && (first.getRow() == second.getRow()) &&
                            (first.getCol() == second.getCol());
        return result;
    }
    
    /**
     * compare cell references by coordinates without sheet name
     * 
     * @param first
     * @param second
     * @return true if equals
     */
    public boolean compareCellByCoords(String first, String second){
        if ((first == null) || (second == null)){
            return false;
        }
        ReferencesUtils referencesUtils = new ReferencesUtils();
        CellReference firstCell = new CellReference(referencesUtils.getReference(first));
        CellReference secondCell = new CellReference(referencesUtils.getReference(second));
        return (firstCell.getCol() == secondCell.getCol()) && (firstCell.getRow() == secondCell.getRow());
    }

    /**
     * check if cell is left top cell of region
     *
     * @param cellReference
     * @param rangeReference
     * @return true if cell in region
     */
    public boolean isCellTopOfRange(String cellReference, String rangeReference){
        String sTmp = cellReference.replace("'", "");
        if (rangeReference.contains(":")){
            sTmp = sTmp + ":";
        }
        return rangeReference.replace("'", "").startsWith(sTmp);
    }
    
    
    /**
     * check is cell in region
     * 
     * @param rangeReference
     * @param cellReference
     * @return true if cell in region
     */
    public boolean isCellInRange(String rangeReference, String cellReference){
        RangeAddress rng = new RangeAddress(rangeReference);
        CellReference cell = new CellReference(cellReference);
        return isCellInRange(rng, cell);
    }

    /**
     * check if rande & cell has sheet name
     * 
     * @param address
     * @param reference
     * @return true if in range
     */
    public boolean isCellInRange(RangeAddress address, CellReference reference){
        // check column
        int col = reference.getCol()+1;
        if ((col < address.getXPosition(address.getFromCell())) ||
            (col > address.getXPosition(address.getToCell()))   ){
            return false;
        }
        // check row
        int row = reference.getRow()+1;
        if ((row < address.getYPosition(address.getFromCell())) ||
            (row > address.getYPosition(address.getToCell()))   ){
            return false;
        }
        
        return true;
        
    }

    /**
     * check is subRange in region
     * @param subRange
     * @param range
     * @return true if subRange is inside range
     */
    public boolean isRangeInRange(RangeAddress subRange, RangeAddress range){
        return isCellInRange(range,new CellReference(subRange.getFromCell())) &&
            isCellInRange(range,new CellReference(subRange.getToCell()));
    }
    
    /**
     * check is range in range
     * 
     * @param subRange
     * @param range
     * @return true if in range
     */
    public boolean isRangeInRange(String subRange, String range){
        RangeAddress subRef = new RangeAddress(subRange);
        RangeAddress ref = new RangeAddress(range);
        return isRangeInRange(subRef, ref);
    }
    
    /**
     * check is column fully cross row
     * 
     * @param columnReference
     * @param rowReference
     * @return true if column fully cross row
     */
    public boolean isColumnCrossRow(String columnReference, String rowReference){
        RangeAddress column = new RangeAddress(columnReference);
        RangeAddress row = new RangeAddress(rowReference);
        if (column.getYPosition(column.getFromCell()) >= row.getYPosition(row.getFromCell())){
            //column top under row top
            return false;
        }
        if (column.getYPosition(column.getToCell()) < row.getYPosition(row.getToCell())){
            //column bottom over row bottom
            return false;
        }
        if (row.getXPosition(row.getFromCell()) >= column.getXPosition(column.getFromCell())){
            //row left after column left
            return false;
        }
        if (row.getXPosition(row.getToCell()) < column.getXPosition(column.getToCell())){
            //row right before column right
            return false;
        }
        return true;
    }
    
    /**
     * get reference
     * 
     * @param ref
     * @return reference
     */
    public String getReference(String ref){
        RangeAddress ra = new RangeAddress(ref);
        return ra.getFromCell();
    }

    /**
     * get references with offset
     * 
     * @param references
     * @param offsetX
     * @param offsetY
     * @return new references
     */
    public String getReferencesWithOffset(String references, int offsetX, int offsetY){
        RangeAddress rangeAddress = new RangeAddress(references);
        String cellFrom = rangeAddress.getFromCell();
        int xPosition = rangeAddress.getXPosition(cellFrom);
        int yPosition = rangeAddress.getYPosition(cellFrom);
        
        CellReference fromCell = new CellReference(yPosition + offsetY - 1, xPosition + offsetX - 1);
        return fromCell.toString();
    }
    
    
    /**
     * get row number from cell reference
     * 
     * @param cellReference
     * @return row num
     */
    public int getRowNumFromRegionReference(String cellReference){
        RangeAddress ra = new RangeAddress(cellReference);
        CellReference cell = new CellReference(ra.getFromCell());
        return cell.getRow();
    }
    
    /**
     * get column number from cell reference
     * 
     * @param cellReference
     * @return column num
     */
    public int getColumnNumFromRegionReference(String cellReference){
        RangeAddress ra = new RangeAddress(cellReference);
        CellReference cell = new CellReference(ra.getFromCell());
        return cell.getCol();
    }
    
    /**
     * get first cell position references
     * 
     * @param ref
     * @return cell references
     */
    public String getFirstCellReferences(String ref){
        RangeAddress ra = new RangeAddress(ref);
        String sheetName = ra.getSheetName() == null ? "" : ra.getSheetName() + DEFAULT_LIST_DELIMITER;
        return sheetName + ra.getFromCell();
    }
    
    /**
     * get row index from cell references
     * 
     * @param cellRef
     * @return row index
     */
    public int getRowIndexFromCellReference(String cellRef){
        CellReference cell = new CellReference(cellRef);
        return cell.getRow();
    }
    
    /**
     * get sheet name from cell references
     * 
     * @param cellRef
     * @return sheet name
     */
    public String getSheetNameFromCellReference(String cellRef){
        CellReference cell = new CellReference(cellRef);
        return cell.getSheetName();
    }
    
    /**
     * get column index from cell references
     * 
     * @param cellRef
     * @return column index
     */
    public short getColumnIndexFromCellReferences(String cellRef){
        CellReference cell = new CellReference(cellRef);
        return cell.getCol();
    }
    
    /**
     * get Cell dimention by row
     * 
     * @param reference
     * @return dimention
     */
    public int getCellDimentionRow(XlsReference reference){
        CellReference cFirst = reference.getCellReferenceTo();
        CellReference cSecond = reference.getCellReference();
        return cFirst.getRow() - cSecond.getRow() + 1;
    }
    
    /**
     * get cell dimention by col
     * 
     * @param reference
     * @return dimention
     */
    public int getCellDimentionCol(XlsReference reference){
        CellReference cFirst = reference.getCellReferenceTo();
        CellReference cSecond = reference.getCellReference();
        return cFirst.getCol() - cSecond.getCol() + 1;
    }
}
