/**
 * 
 */
package com.reporter.document.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.reporter.data.XLSDataCache;
import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.utils.XlsUtils;

/**
 * @author Administrator
 *
 */
public class OutputWorkbook {
    
    /**
     * sql data cache
     */
    private XLSDataCache dataCache;
    
    /**
     * border style index cashe
     */
    private Map<String, Short> borderMap;

    /**
     * output xls workbook
     */
    private HSSFWorkbook workbook;

    /**
     * template xls workbook
     */
    private HSSFWorkbook templateWorkbook;
    
    /**
     * sheets with data for output
     */
    private List<OuputSheet> ouputSheets;

    /**
     * current ouput Sheet (for recursion in XlsPrepareElement)
     */
    private OuputSheet ouputSheet;
    
    /**
     * named regions
     */
    private Map<String, OutputName> namesMap;
    
    /**
     * default constructor
     */
    public OutputWorkbook(HSSFWorkbook workbook, HSSFWorkbook templateWorkbook, XLSDataCache dataCache){
        super();
        this.ouputSheets = new ArrayList<OuputSheet>();
        this.borderMap = new HashMap<String, Short>();
        this.namesMap = new HashMap<String, OutputName>();
        this.dataCache = dataCache;
        this.workbook = workbook;
        this.templateWorkbook =templateWorkbook; 
    }

    /**
     * get sheets with data for output
     * 
     * @return the elements
     */
    public List<OuputSheet> getOuputSheets() {
        return ouputSheets;
    }

    /**
     * set new sheets with data for output
     * 
     * @param elements the elements to set
     */
    public void setOuputSheets(List<OuputSheet> ouputSheets) {
        this.ouputSheets = ouputSheets;
    }

    /**
     * @return the borderMap
     */
    public Map<String, Short> getBorderMap() {
        return borderMap;
    }

    /**
     * @return the dataCache
     */
    public XLSDataCache getDataCache() {
        return dataCache;
    }

    /**
     * @return current ouput sheet
     */
    public OuputSheet getOuputSheet() {
        return ouputSheet;
    }

    /**
     * @return current ouput sheet
     */
    public OuputSheet createOuputSheet(String templateSheetName, String outputSheetName) {
        OuputSheet result;
        result = new OuputSheet(templateSheetName, outputSheetName, templateWorkbook.getSheet(templateSheetName));
        /*
        if (this.ouputSheets.size() > 255 ){
            //too many sheets exception
        }
        */
        this.ouputSheets.add(result);
        this.ouputSheet = result;
        return result;
    }

    /**
     * create user defined style by template
     * 
     * @param templateStyleIndex 
     * @param useLeft 
     * @param useRight 
     * @param useTop 
     * @param useBottom 
     * @return style 
     */
    public short getNewStyle(short templateStyleIndex, boolean useLeft, boolean useRight, boolean useTop, boolean useBottom){
        short result = -1;
        HSSFCellStyle oldStl = workbook.getCellStyleAt(templateStyleIndex);
        if ((oldStl.getBorderLeft()!=0 && useLeft) ||
        		(oldStl.getBorderRight()!=0 && useRight) ||
        		(oldStl.getBorderTop()!=0 && useTop) ||
        		(oldStl.getBorderBottom()!=0 && useBottom)){
            String key = "u";
            //key = key + oldStl.getFillBackgroundColor();
            key = key + oldStl.getFillForegroundColor();
            key = key + "~" + ((oldStl.getBorderLeft()!=0 && useLeft) ? oldStl.getBorderLeft()+"~"+oldStl.getLeftBorderColor() : "0~0"); 
            key = key + "~" + ((oldStl.getBorderRight()!=0 && useRight) ? oldStl.getBorderRight()+"~"+oldStl.getRightBorderColor() : "0~0");
            key = key + "~" + ((oldStl.getBorderTop()!=0 && useTop) ? oldStl.getBorderTop()+"~"+oldStl.getTopBorderColor() : "0~0");
            key = key + "~" + ((oldStl.getBorderBottom()!=0 && useBottom) ? oldStl.getBorderBottom()+"~"+oldStl.getBottomBorderColor() : "0~0");
            if (oldStl.getFillBackgroundColor() != 64){
                int r =0;
                r++;
            }
            Short borderIdx = borderMap.get(key);
            if (borderIdx == null){
                HSSFCellStyle newStl = workbook.createCellStyle();
                XlsUtils.cloneCellBorderStyle(oldStl, newStl);
                if (!useLeft){
                    newStl.setBorderLeft((short)0);
                }
                if (!useRight){
                    newStl.setBorderRight((short)0);
                }
                if (!useTop){
                    newStl.setBorderTop((short)0);
                }
                if (!useBottom){
                    newStl.setBorderBottom((short)0);
                }
                borderIdx = newStl.getIndex();
                borderMap.put(key, borderIdx);
            }
            result = borderIdx;
        }
        return result;
    }

    /**
     * combine vertical borders
     * 
     * @param styleIndexUp 
     * @param styleIndexNext 
     * @return style
     */
    public short getNewStyleVCombine(short styleIndexUp, short styleIndexNext){
        short result = -1;
        HSSFCellStyle stlUp = workbook.getCellStyleAt(styleIndexUp);
        HSSFCellStyle stlNext = workbook.getCellStyleAt(styleIndexNext);
        if ((stlUp.getBorderLeft()!= 0) && (stlNext.getBorderLeft()==stlUp.getBorderLeft()) || ((stlUp.getBorderRight()!=0) && (stlNext.getBorderRight()== stlUp.getBorderRight()))){
            result = styleIndexUp;
            if ((stlUp.getBorderTop() != 0) || (stlUp.getBorderBottom() != 0)){
                /*
                String key = "rv";
                key = key + "~" + (((stlUp.getBorderLeft()!= 0) && (stlNext.getBorderLeft()==stlUp.getBorderLeft())) ? stlUp.getBorderLeft()+"~" +stlUp.getLeftBorderColor() : "0~0");
                key = key + "~" + (((stlUp.getBorderRight()!=0) && (stlNext.getBorderRight()== stlUp.getBorderRight())) ? stlUp.getBorderRight()+"~" +stlUp.getRightBorderColor() : "0~0");
                */
                String key = "u";
                //key = key + stlUp.getFillBackgroundColor();
                key = key + stlUp.getFillForegroundColor();
                key = key + "~" + (((stlUp.getBorderLeft()!= 0) && (stlNext.getBorderLeft()==stlUp.getBorderLeft())) ? stlUp.getBorderLeft()+"~" +stlUp.getLeftBorderColor() : "0~0");
                key = key + "~" + (((stlUp.getBorderRight()!=0) && (stlNext.getBorderRight()== stlUp.getBorderRight())) ? stlUp.getBorderRight()+"~" +stlUp.getRightBorderColor() : "0~0");
                key = key + "~0~0";
                key = key + "~0~0";
                Short borderIdx = borderMap.get(key);
                if (borderIdx == null){
                    HSSFCellStyle newStl = workbook.createCellStyle();
                    XlsUtils.cloneCellBorderStyle(stlUp, newStl);
                    newStl.setBorderTop((short)0);
                    newStl.setBorderBottom((short)0);
                    if (stlUp.getBorderLeft() != stlNext.getBorderLeft()){
                    	newStl.setBorderLeft((short)0);
                    }
                    if (stlUp.getBorderRight() != stlNext.getBorderRight()){
                    	newStl.setBorderRight((short)0);
                    }
                    borderIdx = newStl.getIndex();
                    borderMap.put(key, borderIdx);
                }
                result = borderIdx;
            }
        }
        return result;
    }

    /**
     * create new style index
     * 
     * @param oldStyleIndex
     * @param saveBorder 
     * @return style index
     */
    public short getNewStyleIndex(short oldStyleIndex, XlsLayoutDirection saveBorder/*, XlsLayoutDirection saveBorderRow*/){
        //TODO make slyle lookup ?? 
        short result = oldStyleIndex;
        HSSFCellStyle oldStl = workbook.getCellStyleAt(oldStyleIndex);
        HSSFCellStyle newStl = null;
        if (saveBorder == XlsLayoutDirection.RowDirection){
            if ((oldStl.getBorderTop() != 0) || (oldStl.getBorderBottom() != 0)){
                /*
                String key = "r"+
                    oldStl.getBorderLeft()+"~" +oldStl.getLeftBorderColor()+
                "~"+oldStl.getBorderRight()+"~"+oldStl.getRightBorderColor();
                */
                String key = "u";
                //key = key + oldStl.getFillBackgroundColor();
                key = key + oldStl.getFillForegroundColor();
                key = key + "~" + oldStl.getBorderLeft()+"~" +oldStl.getLeftBorderColor();
                key = key + "~" + oldStl.getBorderRight()+"~"+oldStl.getRightBorderColor();
                key = key + "~0~0";
                key = key + "~0~0";
                
                Short borderIdx = borderMap.get(key);
                if (borderIdx == null){
                    if (newStl == null){
                        newStl = workbook.createCellStyle();
                    }
                    XlsUtils.cloneCellBorderStyle(oldStl, newStl);
                    newStl.setBorderTop((short)0);
                    newStl.setBorderBottom((short)0);
                    borderIdx = newStl.getIndex();
                    borderMap.put(key, borderIdx);
                }
                result = borderIdx;
            }
        } else if (saveBorder == XlsLayoutDirection.ColumnDirection){
            if ((oldStl.getBorderLeft() != 0) || (oldStl.getBorderRight() != 0)){
                //String key = "c";
                String key = "u";
                //key = key + oldStl.getFillBackgroundColor();
                key = key + oldStl.getFillForegroundColor();
                key = key + "~0~0"; 
                key = key + "~0~0";
                key = key + "~" + oldStl.getBorderTop()+"~"+oldStl.getTopBorderColor();
                key = key + "~" + oldStl.getBorderBottom()+"~"+oldStl.getBottomBorderColor();
                Short borderIdx = borderMap.get(key);
                if (borderIdx == null){
                    if (newStl == null){
                        newStl = workbook.createCellStyle();
                    }
                    XlsUtils.cloneCellBorderStyle(oldStl, newStl);
                    newStl.setBorderLeft((short)0);
                    newStl.setBorderRight((short)0);
                    borderIdx = newStl.getIndex();
                    borderMap.put(key, borderIdx);
                }
                result = borderIdx;
            }
        }
        return result;
    }
    
    public void checkTemplateStyle(OutputElement checkElement, XlsLayoutDirection checkBorder){
        //TODO make slyle lookup ?? 
    	HSSFCellStyle chkStl = workbook.getCellStyleAt(checkElement.getStyleIndex());
    	if ((checkBorder == XlsLayoutDirection.RowDirection) && (chkStl.getBorderBottom() != 0)){
            /*
    		String key ="r" + checkElement.getOutputReferences().getSheetName() + "!" +
    		checkElement.getOutputReferences().getRowIndex() + "!" +
    		checkElement.getOutputReferences().getColIndex();
            */
            String key = "u";
            //key = key + chkStl.getFillBackgroundColor();
            key = key + chkStl.getFillForegroundColor();
            key = key + "~" + chkStl.getBorderLeft()  + "~" + chkStl.getLeftBorderColor(); 
            key = key + "~" + chkStl.getBorderRight() + "~" + chkStl.getRightBorderColor();
            key = key + "~" + chkStl.getBorderTop()   + "~" + chkStl.getTopBorderColor();
            key = key + "~" + "0~0";
    		Short borderIdx = borderMap.get(key);
    		if (borderIdx == null){
    			HSSFCellStyle newStl = XlsUtils.cloneCellStyle(chkStl, workbook);
    			newStl.setBorderBottom((short)0);
    			borderIdx = newStl.getIndex();
    			borderMap.put(key, borderIdx);
    		}
    		checkElement.setStyleIndex(borderIdx);
        } else if ((checkBorder == XlsLayoutDirection.ColumnDirection) && (chkStl.getBorderRight() != 0)
                      && !(chkStl.getBorderLeft()!=0 && chkStl.getBorderBottom()!=0 && chkStl.getBorderTop()!=0)){
            /*
            String key = "c";
            key = key + "~" + chkStl.getBorderLeft()+"~"+chkStl.getLeftBorderColor(); 
            key = key + "~" + chkStl.getBorderRight()+"~"+chkStl.getRightBorderColor();
            key = key + "~" + chkStl.getBorderTop()+"~"+chkStl.getTopBorderColor();
            key = key + "~" + chkStl.getBorderBottom()+"~"+chkStl.getBottomBorderColor();
            */
            String key = "u";
            //key = key + chkStl.getFillBackgroundColor();
            key = key + chkStl.getFillForegroundColor();
            key = key + "~" + chkStl.getBorderLeft()+"~"+chkStl.getLeftBorderColor(); 
            key = key + "~" + "0~0";
            key = key + "~" + chkStl.getBorderTop()+"~"+chkStl.getTopBorderColor();
            key = key + "~" + chkStl.getBorderBottom()+"~"+chkStl.getBottomBorderColor();

            Short borderIdx = borderMap.get(key);
            if (borderIdx == null){
                HSSFCellStyle newStl = XlsUtils.cloneCellStyle(chkStl, workbook);
                newStl.setBorderRight((short)0);
                borderIdx = newStl.getIndex();
                borderMap.put(key, borderIdx);
            }
            checkElement.setStyleIndex(borderIdx);
        }
    }

    /**
     * @return the workbook
     */
    public HSSFWorkbook getWorkbook() {
        return workbook;
    }
    /**
     * @return named regions (the namesMap)
     */
    public Map<String, OutputName> getNamesMap() {
        return namesMap;
    }
}
