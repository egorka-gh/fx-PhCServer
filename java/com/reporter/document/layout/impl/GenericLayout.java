/**
 * 
 */
package com.reporter.document.layout.impl;

import java.util.ArrayList;
import java.util.List;

//import org.apache.poi.hssf.usermodel.HSSFCell;

import com.reporter.data.XLSCellValue;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.BoundLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.MergedLayout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.document.layout.XlsReference;
import com.reporter.xml.data.SqlParametr;

/**
 * @author Ilya Ovesnov
 *
 */
/**
 * @author igor zadenov
 *
 */
public class GenericLayout implements Layout, Cloneable{

    /**
     * this simbol use between reference and index in iteration index
     */
    private static final String SEPARATOR = "#";
    
    /**
     * row Height
     */
    private short rowHeight = -1;

    /**
     * column width
     */
    private short columnWidth = -1;

    /**
     * layout command
     */
    private XLSCommands command;

    /**
     * references
     */
    private XlsReference references;
    
    /**
     * original reference. (used for correct 
     *      calculating regions in formula used in crosstab)
     */
    private XlsReference originalReference;
    
    /**
     * iteration index. {reference}:{iteration index}
     * used for calculation regions in formula used in crosstab
     */
    private String iterationIndex;

    /**
     *  xls workbook cell style index
     */
    private short styleIndex;

    /**
     * parents
     */
    private List<DinamicLayout> parents;
    
    /**
     * is element uder crosstab intersection
     */
    private boolean uderCrosstab;

    /**
     * is element after (righter) crosstab intersection
     */
    private boolean afterCrosstab;

    /**
     * flag indicates is ready element for output
     */
    private boolean readyForOutput;
    
    
    /**
     * POI cell type 
     * CELL_TYPE_BLANK      Blank Cell type (3) 
     * CELL_TYPE_BOOLEAN   Boolean Cell type (4) 
     * CELL_TYPE_ERROR         Error Cell type (5) 
     * CELL_TYPE_FORMULA       Formula Cell type (2) 
     * CELL_TYPE_NUMERIC       Numeric Cell type (0) 
     * CELL_TYPE_STRING        String Cell type (1) 
     */
    private int xlsCellType;
    
    /**
     * page break holder
     */
    private XlsLayoutDirection pageBreak = XlsLayoutDirection.NoneDirection;
    
    /**
     * default constructor
     */
    public GenericLayout(){
        this.parents = new ArrayList<DinamicLayout>();
    }
    
    /**
     * get references
     * 
     * @return the references
     */
    public XlsReference getReferences() {
        return references;
    }

    /**
     * set new references
     * 
     * @param references the references to set
     */
    public void setReferences(XlsReference references) {
        this.references = references;
    }

    /**
     * get parents
     * 
     * @return the parents
     */
    public List<DinamicLayout> getParents() {
        return parents;
    }

    /**
     * get flag indicates is ready element for output
     * 
     * @return the readyForOutput
     */
    public boolean isReadyForOutput() {
        return readyForOutput;
    }

    /**
     * set new flag indicates is ready element for output
     * 
     * @param readyForOutput the readyForOutput to set
     */
    public void setReadyForOutput(boolean readyForOutput) {
        this.readyForOutput = readyForOutput;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Layout clone() throws CloneNotSupportedException {
        Layout result = (Layout)super.clone();
        if (result instanceof DinamicLayout){
            List<SqlParametr> newParam = new ArrayList<SqlParametr>();
            for (SqlParametr param : ((DinamicLayout)result).getParametrs()){
                newParam.add(param.clone());
            }
            ((DinamicLayout)result).setParametrs(newParam);
        }
        /*
         * TODO clone value ?
        if (result instanceof Label){
            
        } 
        */ 
        
        //clone merge
        if (result instanceof MergedLayout){
            ((MergedLayout)result).setMerge(((MergedLayout)result).cloneMerge());
        }
        if (result instanceof BoundLayout){
            if (((BoundLayout)result).getDirection() == XlsLayoutDirection.ColumnDirection){
                ((BoundLayout)this).setOutlineH(false);
            }
            if (((BoundLayout)result).getDirection() == XlsLayoutDirection.RowDirection){
                ((BoundLayout)this).setOutlineV(false);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String result = "";
        if (this instanceof GenericBoundLayout){
            result += ((GenericBoundLayout)this).getCommand().getCommand() + " - ";
        }
        return result + getReferences();
    }

    /**
     * @return the styleIndex
     */
    public short getStyleIndex() {
        return styleIndex;
    }

    /**
     * @param styleIndex the styleIndex to set
     */
    public void setStyleIndex(short styleIndex) {
        this.styleIndex = styleIndex;
    }
    
    /* (non-Javadoc)
     * @see com.reporter.document.layout.Layout#setCommand(com.reporter.document.layout.XLSCommands)
     */
    public void setCommand(XLSCommands command){
        this.command = command;
    }
    /* (non-Javadoc)
     * @see com.reporter.document.layout.Layout#getCommand()
     */
    public XLSCommands getCommand(){
        return this.command;
    }

    /**
     * @return the columnWidth
     */
    public short getColumnWidth() {
        return columnWidth;
    }

    /**
     * @param columnWidth the columnWidth to set
     */
    public void setColumnWidth(short columnWidth) {
        this.columnWidth = columnWidth;
    }

    /**
     * @return the rowHeight
     */
    public short getRowHeight() {
        return rowHeight;
    }

    /**
     * @param rowHeight the rowHeight to set
     */
    public void setRowHeight(short rowHeight) {
        this.rowHeight = rowHeight;
    }

    /**
     * @return the afterCrosstab
     */
    public boolean isAfterCrosstab() {
        return afterCrosstab;
    }

    /**
     * @param afterCrosstab the afterCrosstab to set
     */
    public void setAfterCrosstab(boolean afterCrosstab) {
        this.afterCrosstab = afterCrosstab;
    }

    /**
     * @return the uderCrosstab
     */
    public boolean isUderCrosstab() {
        return uderCrosstab;
    }

    /**
     * @param uderCrosstab the uderCrosstab to set
     */
    public void setUderCrosstab(boolean uderCrosstab) {
        this.uderCrosstab = uderCrosstab;
    }

    /**
     * get original reference
     * 
     * @return the originalReference
     */
    public XlsReference getOriginalReference() {
        if (originalReference != null){
            return originalReference;
        } else {
            return getReferences();
        }
    }

    /**
     * set new original reference
     * 
     * @param originalReference the originalReference to set
     */
    public void setOriginalReference(XlsReference originalReference) {
        this.originalReference = originalReference;
    }

    /**
     * get iteration index
     * 
     * @return the iterationIndex
     */
    public String getIterationIndex() {
        return iterationIndex;
    }

    /**
     * set new iteration index
     * 
     * @param iterationIndex the iterationIndex to set
     */
    public void setIterationIndex(String iterationIndex) {
        this.iterationIndex = iterationIndex;
        if (this instanceof DinamicLayout){
            for (Layout lt : ((DinamicLayout) this).getChildList()){
                lt.setIterationIndex(iterationIndex);
            }
        }
    }

    /**
     * generate iteration index
     * 
     * @param ind
     * @return iteration index
     */
    public String generateIterationInd(int ind){
        return getIterationIndex() + getReferences().toString() + SEPARATOR + ind + SEPARATOR;
    }

    /* (non-Javadoc)
     * @see com.reporter.document.layout.Layout#getXlsCellType()
     */
    public int getXlsCellType() {
        return xlsCellType;
    }

    /* (non-Javadoc)
     * @see com.reporter.document.layout.Layout#setXlsCellType(int)
     */
    public void setXlsCellType(int xlsCellType) {
        this.xlsCellType = xlsCellType;
    }

    /**
     * @return the pageBreak
     */
    public XlsLayoutDirection getPageBreak() {
        return pageBreak;
    }

    /**
     * @param pageBreak the pageBreak to set
     */
    public void setPageBreak(XlsLayoutDirection pageBreak) {
        this.pageBreak = pageBreak;
    }

}
