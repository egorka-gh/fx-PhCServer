/**
 * 
 */
package com.reporter.document;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import org.apache.poi.hssf.record.formula.ArrayPtg;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.util.RangeAddress;

import com.reporter.constants.Constants;
import com.reporter.data.XLSCellValue;
import com.reporter.data.XLSConnectionBrokenException;
import com.reporter.data.XLSDataCache;
import com.reporter.data.XLSDataLockedException;
import com.reporter.data.XLSDataType;
import com.reporter.data.XLSUnknownSourceException;
import com.reporter.document.exception.OutputValueNotReadyException;
import com.reporter.document.exception.XlsOutputValueNotFoundException;
import com.reporter.document.layout.MergeRegion;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.document.layout.impl.Sheet;
import com.reporter.document.output.OuputSheet;
import com.reporter.document.output.OutputElement;
import com.reporter.document.output.OutputOutline;
import com.reporter.document.output.OutputWorkbook;
import com.reporter.document.output.OutputName;
import com.reporter.document.output.OutputNameReference;
import com.reporter.parser.exceptions.CommandStructureException;
import com.reporter.utils.XLSUtilsSheetCloneException;
import com.reporter.utils.XlsFontUtils;
import com.reporter.utils.XlsUtils;

/**
 * @author Administrator
 *
 */
public class XLSDocumentPrepareOutput {
    

    /**
     * prepare document to output
     * 
     * @param templates - template sheet layouts 
     * @param dataCache
     * @param workbook
     * @param templateWorkbook
     * @return result workbook
     * @throws SQLException
     * @throws XlsOutputValueNotFoundException
     * @throws CloneNotSupportedException
     * @throws OutputValueNotReadyException
     * @throws XLSDataLockedException
     * @throws XLSConnectionBrokenException
     * @throws XLSUtilsSheetCloneException
     * @throws IOException
     * @throws CommandStructureException
     * @throws XLSUnknownSourceException 
     */
    public HSSFWorkbook prepareDocument(List<Sheet> templates, XLSDataCache dataCache, HSSFWorkbook workbook, HSSFWorkbook templateWorkbook, Map<String, Integer> keepSheetList) throws SQLException, XlsOutputValueNotFoundException, CloneNotSupportedException, OutputValueNotReadyException, XLSDataLockedException, XLSConnectionBrokenException, XLSUtilsSheetCloneException, IOException, CommandStructureException, XLSUnknownSourceException{
        
        OutputWorkbook outputWorkbook = new OutputWorkbook(workbook, templateWorkbook, dataCache);
        for (Sheet templateSheet : templates){
            XlsPrepareElement prepareElement = new XlsPrepareElement( outputWorkbook, null, XLSCommands.XLSCommandSheet, templateSheet);
            prepareElement.prepareElement(0, 0, null);
        }
        return fillDocument(outputWorkbook, workbook, templateWorkbook, keepSheetList);
    }
    
    /**
     * fill document
     * 
     * @param outputLayout
     * @param workbook
     * @param templateWorkbook
     * @return new document
     * @throws XLSUtilsSheetCloneException 
     * @throws IOException 
     */
    private HSSFWorkbook fillDocument(OutputWorkbook outputLayout, HSSFWorkbook workbook, HSSFWorkbook templateWorkbook, Map<String, Integer> keepSheetList) throws XLSUtilsSheetCloneException, IOException{
    	for (OuputSheet sheet : outputLayout.getOuputSheets()){
            HSSFSheet currentSheet = workbook.getSheet(sheet.getSheetName());
            if (currentSheet == null){
                //clone sheet & save Print_Titles 
                currentSheet = XlsUtils.cloneSheet(templateWorkbook, workbook, sheet);
            }
            
            //set column width
            for (short column : sheet.getColumnWidth().keySet()){
                currentSheet.setColumnWidth(column, sheet.getColumnWidth().get(column));
            }
            //max sheet column
            short maxColumn = 0;
            //all wrapped cells
            Map<String,OutputElement> wrappedCells = new HashMap<String,OutputElement>();
            
            //fill cells
            //XlsFontUtils  fontUtl =  new XlsFontUtils(workbook); 
            for (OutputElement cell : sheet.getElements()){
                int rowInd = cell.getOutputReferences().getRowIndex();
                short colInd = (short)cell.getOutputReferences().getColIndex();
                //save sheet max column
                maxColumn = (short) Math.max((int)maxColumn, (int)colInd);
                HSSFRow currentRow  = currentSheet.getRow(rowInd);
                if (currentRow == null){
                    //create row
                    currentRow = currentSheet.createRow(rowInd);
                    //set row height
                    Short rwHt = sheet.getRowHeights().get(new Integer(rowInd));
                    if ((rwHt != null) && (rwHt != -1)){ 
                        currentRow.setHeight(rwHt);
                    }
                }
                
                HSSFCell currentCell = currentRow.getCell(colInd);
                if (currentCell == null){
                    currentCell = currentRow.createCell(colInd);
                } else if (currentCell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
                    //CELL_TYPE_BLANK can be only from BorderBuilder so ignore overwrite
                    System.out.println("Cell " + cell.getOutputReferences() +" is not empty. Cell overwrited.");
                }
                //currentCell.setCellStyle(workbook.getCellStyleAt(cell.getStyleIndex()));
                
                //save all wrapped cells (only ws text value)
                if (workbook.getCellStyleAt(cell.getStyleIndex()).getWrapText()){
                        if((cell.getCellValue() != null)
                                && !cell.getCellValue().isEpmpty() 
                                && ((cell.getCellValue().getType() == XLSDataType.XLSDataText) 
                                        || ((cell.getCellValue().getType() == XLSDataType.XLSDataAutoDetect) && (cell.getXlsCellType() == HSSFCell.CELL_TYPE_STRING)))
                                && !cell.isFormula()){
                            wrappedCells.put(rowInd + "~" + colInd, cell);
                        }
                }
                
                
                if (!Constants.OUTPUT_SHIFT || ((cell.getShiftByX() == 0) && (cell.getShiftByY() == 0))){
                    XLSCellValue val = cell.getCellValue();
                    if (cell.isFormula()){
                        //process formula cell
                        String cellValue = cell.getCellValue().toString();
                        int ind = cellValue.toUpperCase().indexOf("ATTR(SEMIVOLATILE)");
                        if (ind != -1){
                            String resultValue = cellValue.substring(0, ind); 
                            resultValue += cellValue.substring(ind + 18);
                            cellValue = resultValue;
                        }
                        currentCell.setCellFormula(cellValue);
                    } else if ((val != null) && !val.isEpmpty()){
                        String stringVal = val.toString();
                        HSSFRichTextString defaultValue = new HSSFRichTextString(stringVal);
                        //set cell value
                        XLSDataType type = val.getType(); 
                        if (type == XLSDataType.XLSDataText){
                            currentCell.setCellValue(defaultValue);
                        } else if (type == XLSDataType.XLSDataDate){
                            //set date value
                            currentCell.setCellValue((Date)val.getValue());
                        } else if (type == XLSDataType.XLSDataNumeric){
                            try {
                                Double dValue = Double.valueOf(stringVal);
                                currentCell.setCellValue(dValue);
                            } catch (NumberFormatException e) {
                                currentCell.setCellValue(defaultValue);
                            }
                        } else if (type == XLSDataType.XLSDataAutoDetect){
                            if (cell.getXlsCellType() == HSSFCell.CELL_TYPE_STRING ){
                                //text type
                                currentCell.setCellValue(defaultValue);
                                /* fit row height 4 wrapped text
                                if (workbook.getCellStyleAt(cell.getStyleIndex()).getWrapText()){
                                    short rwHt = fontUtl.getWrappedHeight(currentValue.getString(), workbook.getCellStyleAt(cell.getStyleIndex()).getFontIndex(), currentSheet.getColumnWidth(colInd));
                                    currentRow.setHeight(rwHt);
                                }
                                */
                            } else{
                                try {
                                    Double dValue = Double.valueOf(stringVal);
                                    currentCell.setCellValue(dValue);
                                } catch (NumberFormatException e) {
                                    currentCell.setCellValue(defaultValue);
                                }
                            }
                            
                        }
                    }
                    
                } else {
                    String val = "X:" + cell.getShiftByX() + "; Y:" + cell.getShiftByY();
                    HSSFRichTextString newVal = new HSSFRichTextString(String.valueOf(val));
                    currentCell.setCellValue(newVal);
                }
                currentCell.setCellStyle(workbook.getCellStyleAt(cell.getStyleIndex()));
            }
            //create hidden columns to auto fit wrapped row height
            Map<Short, Short> hdColumns = new HashMap<Short, Short>();
            //create merges
            for (MergeRegion mr : sheet.getMerges()){
                currentSheet.addMergedRegion(mr.getMergeRegion());
                //is one row merge?
                if (mr.getHeight() == 1){
                    //has wrapped cell?
                    String key = mr.getRowFrom() + "~" + mr.getColFrom();
                    OutputElement oe = wrappedCells.get(key);
                    if (oe != null){
                        //calc merged region width
                        short colWidth = 0;
                        for (short i = mr.getColFrom(); i < mr.getColFrom() + mr.getWidth(); i++){
                            short wd = currentSheet.getColumnWidth(i);
                            if (wd == 8) {
                                //default width
                                wd = 2304;
                            }
                            colWidth += wd;
                        }
                        //get/create hidden column to auto fit wrapped row height
                        Short hC = hdColumns.get(colWidth);
                        if (hC == null){
                            if ((maxColumn + 1 + hdColumns.size()) < 255){
                                hC = new Integer(maxColumn + 1 + hdColumns.size()).shortValue();
                                currentSheet.setColumnWidth(hC, colWidth);
                                currentSheet.setColumnHidden(hC, true);
                                hdColumns.put(colWidth, hC);
                            }
                        }
                        if (hC != null){
                            HSSFCell wrCell = currentSheet.getRow(mr.getRowFrom()).getCell(hC);
                            HSSFCell mrCell = currentSheet.getRow(mr.getRowFrom()).getCell(mr.getColFrom());
                            if (wrCell == null){
                                wrCell = currentSheet.getRow(mr.getRowFrom()).createCell(hC);
                            }
                            wrCell.setCellStyle(mrCell.getCellStyle());
                            wrCell.setCellValue(mrCell.getRichStringCellValue());
                            
                            //wrCell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
                            //wrCell.setCellFormula( oe.getOutputReferences().getFromReferenceString());
                        }
                    }
                }
            }
            //create outlines on sheet
            for (OutputOutline outline : sheet.getOutlines()){
                if (outline.getDirection() == XlsLayoutDirection.RowDirection){
                    currentSheet.groupRow(outline.getFromY(), outline.getToY());
                } else if (outline.getDirection() == XlsLayoutDirection.ColumnDirection){
                    currentSheet.groupColumn((short)outline.getFromX(),(short)outline.getToX());
                } else
                {
                	currentSheet.groupColumn((short)outline.getFromX(), (short)outline.getToX());
                    currentSheet.groupRow(outline.getFromY(), outline.getToY());
                } 
            }
            //create page breaks
            for (Integer row : sheet.getRowPageBreaks()){
                currentSheet.setRowBreak(row.intValue());
            }
            for (Short column : sheet.getColumnPageBreaks()){
                currentSheet.setColumnBreak(column.shortValue());
            }
            /*
            //autofit
            System.out.println(currentSheet.getColumnWidth((short)6));
            currentSheet.autoSizeColumn((short)6);
            System.out.println(currentSheet.getDefaultColumnWidth() + "/" + currentSheet.getColumnWidth((short)6));
            currentSheet.autoSizeColumn((short)8);
            currentSheet.autoSizeColumn((short)9);
            System.out.println(currentSheet.getDefaultRowHeightInPoints() + "/" + currentSheet.getDefaultRowHeight());
            System.out.println(currentSheet.getRow(5).getHeightInPoints() +"/" +currentSheet.getRow(5).getHeight());
            */
        }
        //restore sheet order (4 keeped sheet)
        for (String shName : keepSheetList.keySet()){
            workbook.setSheetOrder(shName, keepSheetList.get(shName));
        }
        //create named regions (make it only after sheet reordering, names not moved vs sheet
        Map<String, OutputName> wbNames;
        wbNames = outputLayout.getNamesMap();
        //save existing names to awoid duplicates
        for (int i = 0; i < workbook.getNumberOfNames(); i++) {
            HSSFName regionName = workbook.getNameAt(i);
            wbNames.put(regionName.getNameName(), null);
        }
        //scan all sheets (check duplicated names)  
        for (OuputSheet sheet : outputLayout.getOuputSheets()){
            for (OutputName shName : sheet.getNames().values()){
                //process name
                int i = 0;
                String shNm = shName.getName();
                while (wbNames.containsKey(shNm)){
                    i = i+1;
                    shNm = shName.getName()+ "_" + i;
                }
                if (!shName.getName().equals(shNm)){
                    shName.setName(shNm);
                }
                wbNames.put(shName.getName(), shName);
            }
        }
        //create names
        for (OutputName wbName : wbNames.values()){
            if (wbName != null){
                //null - name already exists
                HSSFName newName = workbook.createName();
                newName.setNameName(wbName.getName());
                newName.setReference(wbName.getReference());
            }
        }
        //set sheet repeating rows and columns (print titles)
        for (OuputSheet sheet : outputLayout.getOuputSheets()){
            workbook.setRepeatingRowsAndColumns(workbook.getSheetIndex(sheet.getSheetName()), sheet.getRepeatingColumnsFrom(), sheet.getRepeatingColumnsTo(), sheet.getRepeatingRowsFrom(), sheet.getRepeatingRowsTo());
        }
        
        return workbook;
    }
    
}
