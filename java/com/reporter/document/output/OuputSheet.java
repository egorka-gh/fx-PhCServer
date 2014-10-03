/**
 * 
 */
package com.reporter.document.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.reporter.document.layout.MergeRegion;
import com.reporter.document.layout.XlsLayoutDirection;

/**
 * @author Administrator
 *
 */
public class OuputSheet {
    
    /**
     * template Sheet
     */
    private HSSFSheet templateSheet;

    /**
     * default column width
     */
    private short defaultColumnWidth;

    /**
     * template sheet name
     */
    private String templateSheetName;

    /**
     * output sheet name
     */
    private String sheetName;
    
    /**
     * elements with data for output
     */
    private List<OutputElement> elements;
    
    /**
     * outlines on sheet
     */
    private List<OutputOutline> outlines;

    /**
     * merges on sheet
     */
    private List<MergeRegion> merges;

    /**
     * row heights
     * non default row heights 
     */
    private Map<Integer, Short> rowHeights;

    /**
     * column width
     * non default column width 
     */
    private Map<Short, Short> columnWidth;
    
    /**
     * row page breaks
     */
    private Set<Integer> rowPageBreaks = new TreeSet<Integer>();

    /**
     * column page breaks
     */
    private Set<Short> columnPageBreaks = new TreeSet<Short>();
    
    /**
     * named regions
     */
    private Map<String, OutputName> namesMap;
    
    /**
     * RepeatingRowsAndColumns
     */
    private int repeatingColumnsFrom = -1;
    private int repeatingColumnsTo = -1;
    private int repeatingRowsFrom = -1;
    private int repeatingRowsTo = -1;
    /**
     * Default constructor
     */
    public OuputSheet(String templateSheetName, String outputSheetName, HSSFSheet templateSheet){
        super();
        this.elements = new ArrayList<OutputElement>();
        this.outlines = new ArrayList<OutputOutline>();
        this.merges = new ArrayList<MergeRegion>();
        this.columnWidth = new HashMap<Short, Short>();
        this.rowHeights = new HashMap<Integer, Short>();
        this.namesMap = new HashMap<String, OutputName>();
        this.templateSheet = templateSheet;
        this.defaultColumnWidth = templateSheet.getDefaultColumnWidth(); 
        setTemplateSheetName(templateSheetName);
        setSheetName(outputSheetName);
    }

    
    
    /**
     * get sheet name
     * 
     * @return the sheetName
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * set new sheet name
     * 
     * @param sheetName the sheetName to set
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
        if ((this.templateSheetName == null) || (this.templateSheetName.length() == 0)){
            this.templateSheetName = sheetName;
        }
    }

    /**
     * get elements with data for output
     * 
     * @return the elements
     */
    public List<OutputElement> getElements() {
        return elements;
    }

    /**
     * set new elements with data for output
     * 
     * @param elements the elements to set
     */
    public void setElements(List<OutputElement> elements) {
        this.elements = elements;
    }

    /**
     * @return the outlines
     */
    public List<OutputOutline> getOutlines() {
        return outlines;
    }

    /**
     * @return the merges
     */
    public List<MergeRegion> getMerges() {
        return merges;
    }

    /**
     * @return the templateSheetName
     */
    public String getTemplateSheetName() {
        return templateSheetName;
    }

    /**
     * @param templateSheetName the templateSheetName to set
     */
    public void setTemplateSheetName(String templateSheetName) {
        this.templateSheetName = templateSheetName;
        if ((this.sheetName == null) || (this.sheetName.length() == 0)){
            this.sheetName = templateSheetName;
        }
    }

    /**
     * @param column
     * @param width
     */
    public void setColumnWidth(short column, short width) {
        if ((width != defaultColumnWidth) && (width != -1)){
            //store not default width
            this.columnWidth.put(column, width);
        }
    }

    /**
     * @param row
     * @param height
     */
    public void setRowHeight(int row, short height) {
        Short currHeight = rowHeights.get(new Integer(row));
        if ((currHeight != null) && (currHeight == -1)){
            return;
        }
        if (height != -1){
            //store not default height
            rowHeights.put(new Integer(row), height);
        }
    }

    /**
     * set & lock row height to -1 (default) for rows ws wrapText
     * 
     * @param row
     */
    public void setRowHeightDefault(int row) {
    	/*
    	int i=0;
    	if(row>19 && row<25){
    		i++;
    	}
    	*/
        rowHeights.remove(new Integer(row));
        rowHeights.put(new Integer(row), (short)-1);
    }

    /**
     * add named region reference 
     */
    public void addNameReference(String refName, OutputNameReference reference, XlsLayoutDirection direction) {
       if (namesMap.containsKey(refName)){
           //name can has set of ref's so add ref to exthisting name
           namesMap.get(refName).addReference(reference);
       } else {
           //create new
           namesMap.put(refName, new OutputName(direction, sheetName, refName, reference));
       }
    }

    /**
     * @return the columnWidth
     */
    public Map<Short, Short> getColumnWidth() {
        return columnWidth;
    }
    /**
     * @return the rowHeights
     */
    public Map<Integer, Short> getRowHeights() {
        return rowHeights;
    }



	public HSSFSheet getTemplateSheet() {
		return templateSheet;
	}



    /**
     * @return the columnPageBreaks
     */
    public Set<Short> getColumnPageBreaks() {
        return columnPageBreaks;
    }



    /**
     * @return the rowPageBreaks
     */
    public Set<Integer> getRowPageBreaks() {
        return rowPageBreaks;
    }

    /**
     * @return the named regions (namesMap)
     */
    public Map<String, OutputName> getNames() {
        return namesMap;
    }
    /**
     * set sheet repeating Rows and Columns
     * @param colFrom
     * @param colTo
     * @param rowFrom
     * @param rowTo
     */
    public void setRepeatingRowsAndColumns(int colFrom, int colTo, int rowFrom, int rowTo){
        repeatingColumnsFrom = colFrom;
        repeatingColumnsTo = colTo;
        repeatingRowsFrom = rowFrom;
        repeatingRowsTo = rowTo;
    }

    /**
     * @return the repeatingColumnsFrom
     */
    public int getRepeatingColumnsFrom() {
        return repeatingColumnsFrom;
    }

    /**
     * @return the repeatingColumnsTo
     */
    public int getRepeatingColumnsTo() {
        return repeatingColumnsTo;
    }

    /**
     * @return the repeatingRowsFrom
     */
    public int getRepeatingRowsFrom() {
        return repeatingRowsFrom;
    }

    /**
     * @return the repeatingRowsTo
     */
    public int getRepeatingRowsTo() {
        return repeatingRowsTo;
    }

}
