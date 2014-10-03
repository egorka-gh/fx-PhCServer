package com.reporter.utils;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.hssf.util.RangeAddress;

import com.reporter.document.output.OuputSheet;

public class XlsUtils {
    
    /**
     * gets xls cell value converted to String
     * @param xls cell
     * @return cell value converted to String
     */
    public static String getCellValue(HSSFCell cell){
        String result = "";
        if ((cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) || (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR)){
            result = "";
        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
            result = String.valueOf(cell.getNumericCellValue());
        } else if(cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN){
            result = String.valueOf(cell.getBooleanCellValue());
        } else { 
            result =cell.getRichStringCellValue().toString();
        }
        return result;
    }

    /**
     * create empty xls workbook and make copy of all properties from template xls workbook
     * @param template xls workbook
     * @return new xls workbook
     */
    public static HSSFWorkbook cloneWorkbook(HSSFWorkbook template){
        HSSFWorkbook result = new HSSFWorkbook();
        int itemsNum = 0;
        //clone Fonts
        itemsNum = result.getNumberOfFonts();
        for (short i = result.getNumberOfFonts(); i <= template.getNumberOfFonts(); i++){
            result.createFont();
        }
        for (short i = 0; i <= template.getNumberOfFonts(); i++){
            HSSFFont newFont = result.getFontAt(i);
            cloneFont(template.getFontAt(i),newFont);
        }
        //clone Cell Styles
        itemsNum = result.getNumCellStyles();
        for (short i = 0; i < template.getNumCellStyles(); i++){
            HSSFCellStyle newCs;
            if ( i < itemsNum){
                newCs = result.getCellStyleAt(i);
            } else {
                newCs = result.createCellStyle();
            }
            cloneStyle(template.getCellStyleAt(i), newCs, template, result);
        }
        //TODO add clone for Custom Palette (getCustomPalette)
        return result;
    }
    
    private static void cloneFont(HSSFFont template, HSSFFont target){
        target.setFontName(template.getFontName());
        target.setCharSet(template.getCharSet());
        target.setBoldweight(template.getBoldweight());
        target.setColor(template.getColor());
        //target.setFontHeight(template.getFontHeight());
        target.setFontHeightInPoints(template.getFontHeightInPoints());
        target.setItalic(template.getItalic());
        target.setStrikeout(template.getStrikeout());
        target.setTypeOffset(template.getTypeOffset());
        target.setUnderline(template.getUnderline());
    }
    
    private static void cloneStyle(HSSFCellStyle template, HSSFCellStyle target, HSSFWorkbook templateWorkbook, HSSFWorkbook targetWorkbook){
        target.setBorderBottom(template.getBorderBottom());
        target.setBorderLeft(template.getBorderLeft());
        target.setBorderRight(template.getBorderRight());
        target.setBorderTop(template.getBorderTop());
        target.setBottomBorderColor(template.getBottomBorderColor());
        target.setLeftBorderColor(template.getLeftBorderColor());
        target.setRightBorderColor(template.getRightBorderColor());
        target.setTopBorderColor(template.getTopBorderColor());
        target.setAlignment(template.getAlignment());
       
        HSSFDataFormat srcDF = templateWorkbook.createDataFormat();
        HSSFDataFormat dstDF = targetWorkbook.createDataFormat();
        target.setDataFormat(dstDF.getFormat(srcDF.getFormat(template.getDataFormat())));

        target.setFillBackgroundColor(template.getFillBackgroundColor());
        target.setFillForegroundColor(template.getFillForegroundColor());
        target.setFillPattern(template.getFillPattern());
        target.setFont(targetWorkbook.getFontAt(template.getFontIndex()));
        
        target.setHidden(template.getHidden());
        target.setIndention(template.getIndention());
        target.setLocked(template.getLocked());
        target.setRotation(template.getRotation());
        target.setVerticalAlignment(template.getVerticalAlignment());
        target.setWrapText(template.getWrapText());
        //TODO check other style sets
    }
    
    public static HSSFCellStyle cloneCellStyle(HSSFCellStyle templateStyle, HSSFWorkbook workbook){
        HSSFCellStyle result = workbook.createCellStyle();
        cloneStyle(templateStyle, result, workbook, workbook);
        return result;
    }

    public static void cloneCellBorderStyle(HSSFCellStyle template, HSSFCellStyle target){
        target.setBorderBottom(template.getBorderBottom());
        target.setBorderLeft(template.getBorderLeft());
        target.setBorderRight(template.getBorderRight());
        target.setBorderTop(template.getBorderTop());
        target.setBottomBorderColor(template.getBottomBorderColor());
        target.setLeftBorderColor(template.getLeftBorderColor());
        target.setRightBorderColor(template.getRightBorderColor());
        target.setTopBorderColor(template.getTopBorderColor());
        target.setAlignment(template.getAlignment());
        target.setDataFormat(template.getDataFormat());
        target.setFillForegroundColor(template.getFillForegroundColor());
        target.setFillBackgroundColor(template.getFillBackgroundColor());
        target.setFillPattern(template.getFillPattern());
        //target.setFont(targetWorkbook.getFontAt(template.getFontIndex()));
        
        target.setHidden(template.getHidden());
        target.setIndention(template.getIndention());
        target.setLocked(template.getLocked());
        target.setRotation(template.getRotation());
        target.setVerticalAlignment(template.getVerticalAlignment());
        target.setWrapText(template.getWrapText());
        //TODO check other style sets
    }
    
//    public static HSSFSheet cloneSheet(HSSFWorkbook templateWorkbook,  String templateSheet, HSSFWorkbook targetWorkbook, String targetSheet) throws XLSUtilsSheetCloneException, IOException{
    public static HSSFSheet cloneSheet(HSSFWorkbook templateWorkbook,  HSSFWorkbook targetWorkbook, OuputSheet sheet) throws XLSUtilsSheetCloneException, IOException{
        String templateSheet = sheet.getTemplateSheetName();  
        String targetSheet = sheet.getSheetName();  
        HSSFSheet result = null;
        HSSFSheet template = templateWorkbook.getSheet(templateSheet);
        if (template != null){
         result = targetWorkbook.createSheet(targetSheet);
         cloneSheet(template, result);
         //TODO defined on workbook level
         //all after fiiling sheet? (new offsets?)
         //getPrintArea -setPrintArea

         //setRepeatingRowsAndColumns no get metod at all((
         //heh can be solved with namedRange "Print_Titles"
         //restore RepeatingRows or RepeatingColumns 
         //both can't be accessed by HSSFName 
         for (int i = 0; i < templateWorkbook.getNumberOfNames(); i++) {
             HSSFName nm = templateWorkbook.getNameAt(i);
             if ((nm.getNameName().equalsIgnoreCase("print_titles")) && (nm.getSheetName().equals(templateSheet))){
                 RangeAddress ra = new RangeAddress(nm.getReference());
                 if (ra.getWidth() == 256){
                     //Repeating rows
                     //targetWorkbook.setRepeatingRowsAndColumns(targetWorkbook.getSheetIndex(targetSheet), -1, -1, ra.getYPosition(ra.getFromCell())-1, ra.getYPosition(ra.getToCell())-1);
                     sheet.setRepeatingRowsAndColumns(-1, -1, ra.getYPosition(ra.getFromCell())-1, ra.getYPosition(ra.getToCell())-1);
                 } else{
                     //Repeating columns
                     //targetWorkbook.setRepeatingRowsAndColumns(targetWorkbook.getSheetIndex(targetSheet), ra.getXPosition(ra.getFromCell())-1, ra.getXPosition(ra.getToCell())-1, -1, -1);
                     sheet.setRepeatingRowsAndColumns(ra.getXPosition(ra.getFromCell())-1, ra.getXPosition(ra.getToCell())-1, -1, -1);
                 }
                 break;
             }
         }
        } else {
            throw new XLSUtilsSheetCloneException("sheetCloneException", templateSheet);
        }
        return result;
    }
    
    private static void cloneSheet(HSSFSheet template, HSSFSheet target){
        target.setAlternativeExpression(template.getAlternateExpression());
        target.setAlternativeFormula(template.getAlternateFormula());
        target.setAutobreaks(template.getAutobreaks());
        //target.setDefaultColumnStyle(arg0, arg1)
        target.setDefaultColumnWidth(template.getDefaultColumnWidth());
        target.setDefaultRowHeight(template.getDefaultRowHeight());
        //target.setDefaultRowHeightInPoints(arg0)
        target.setDialog(template.getDialog());
        target.setDisplayFormulas(template.isDisplayFormulas());
        target.setDisplayGridlines(template.isDisplayGridlines());
        target.setDisplayGuts(template.getDisplayGuts());
        target.setDisplayRowColHeadings(template.isDisplayRowColHeadings());
        target.setFitToPage(template.getFitToPage());
        target.setGridsPrinted(template.isGridsPrinted());
        target.setHorizontallyCenter(template.getHorizontallyCenter());

        //copy Margins
        target.setMargin(HSSFSheet.BottomMargin,template.getMargin(HSSFSheet.BottomMargin));
        target.setMargin(HSSFSheet.LeftMargin,template.getMargin(HSSFSheet.LeftMargin));
        target.setMargin(HSSFSheet.RightMargin,template.getMargin(HSSFSheet.RightMargin));
        target.setMargin(HSSFSheet.TopMargin,template.getMargin(HSSFSheet.TopMargin));
        
        target.setPrintGridlines(template.isPrintGridlines());
        target.setProtect(template.getProtect());
        target.setRowSumsBelow(template.getRowSumsBelow());
        target.setRowSumsRight(template.getRowSumsRight());
        target.setVerticallyCenter(template.getVerticallyCenter(false));
        //set PrintSetup
        clonePrintSetup(template.getPrintSetup(),target.getPrintSetup());
        //set print Header
        target.getHeader().setCenter(template.getHeader().getCenter());
        target.getHeader().setLeft(template.getHeader().getLeft());
        target.getHeader().setRight(template.getHeader().getRight());
        //set print Footer
        target.getFooter().setCenter(template.getFooter().getCenter());
        target.getFooter().setLeft(template.getFooter().getLeft());
        target.getFooter().setRight(template.getFooter().getRight());
        //restore Freeze Pane by absolute poz
        PaneInformation pi = template.getPaneInformation();
        if ((pi != null) && pi.isFreezePane() && 
                ((pi.getHorizontalSplitPosition() > 0) || (pi.getVerticalSplitPosition() > 0))){
            target.createFreezePane(pi.getVerticalSplitPosition(), pi.getHorizontalSplitPosition());
        }
        //target.setZoom(arg0, arg1) no get metod 
        
        //TODO to set after filling report 
        //outlines
    }
    
    private static void clonePrintSetup(HSSFPrintSetup template, HSSFPrintSetup target){
        target.setCopies(template.getCopies());
        target.setDraft(template.getDraft());
        target.setFitHeight(template.getFitHeight());
        target.setFitWidth(template.getFitWidth());
        target.setFooterMargin(template.getFooterMargin());
        target.setHeaderMargin(template.getHeaderMargin());
        target.setHResolution(template.getHResolution());
        target.setLandscape(template.getLandscape());
        target.setLeftToRight(template.getLeftToRight());
        target.setNoColor(template.getNoColor());
        target.setNoOrientation(template.getNoOrientation());
        target.setNotes(template.getNotes());
        target.setOptions(template.getOptions());
        target.setPageStart(template.getPageStart());
        target.setPaperSize(template.getPaperSize());
        target.setScale(template.getScale());
        target.setUsePage(template.getUsePage());
        target.setValidSettings(template.getValidSettings());//??
        target.setVResolution(template.getVResolution());
    }
    
    /**
     * construct external parametr name
     * 
     * @param sqlName
     * @param paramName
     * @return parametr name
     */
    public static String constructExternalValue(String sqlName, String paramName){
        StringBuffer result = new StringBuffer(sqlName);
        if ((paramName != null) && (paramName.length() > 0)){
            result.append(".").append(paramName);
        }
        return result.toString();
    }

}
