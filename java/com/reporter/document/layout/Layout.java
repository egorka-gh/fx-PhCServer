/**
 * 
 */
package com.reporter.document.layout;

import java.util.List;

/**
 * @author Administrator
 *
 */
public interface Layout {

    /**
     * get references
     * 
     * @return the references
     */
    public XlsReference getReferences();

    /**
     * set new references
     * 
     * @param references the references to set
     */
    public void setReferences(XlsReference references);

    /**
     * get original reference
     * 
     * @return the originalReference
     */
    public XlsReference getOriginalReference();

    /**
     * set new original reference
     * 
     * @param originalReference the originalReference to set
     */
    public void setOriginalReference(XlsReference originalReference);

    /**
     * get iteration index
     * 
     * @return the iterationIndex
     */
    public String getIterationIndex();

    /**
     * set new iteration index
     * 
     * @param iterationIndex the iterationIndex to set
     */
    public void setIterationIndex(String iterationIndex);

    /**
     * get parents
     * 
     * @return the parents
     */
    public List<DinamicLayout> getParents();

    /**
     * get flag indicates is ready element for output
     * 
     * @return the readyForOutput
     */
    public boolean isReadyForOutput();

    /**
     * set new flag indicates is ready element for output
     * 
     * @param readyForOutput the readyForOutput to set
     */
    public void setReadyForOutput(boolean readyForOutput);

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Layout clone() throws CloneNotSupportedException;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString();
    
    /**
     * @return the styleIndex
     */
    public short getStyleIndex();

    /**
     * @param styleIndex the styleIndex to set
     */
    public void setStyleIndex(short styleIndex);

    /**
     * get layout command
     * @return command (enum XLSCommands) 
     */
    public XLSCommands getCommand();
    
    /**
     * set layout command
     * @param command (enum XLSCommands)
     */
    public void setCommand(XLSCommands command);

    /**
     * @return the columnWidth
     */
    public short getColumnWidth();

    /**
     * @param columnWidth the columnWidth to set
     */
    public void setColumnWidth(short columnWidth);

    /**
     * @return the rowHeight
     */
    public short getRowHeight();

    /**
     * @param rowHeight the rowHeight to set
     */
    public void setRowHeight(short rowHeight);
    
    /**
     * is element after (righter) crosstab intersection
     */
    public boolean isAfterCrosstab();

    /**
     * @param afterCrosstab the afterCrosstab to set
     */
    public void setAfterCrosstab(boolean afterCrosstab);
    
    /**
     * is element uder crosstab intersection
     */
    public boolean isUderCrosstab();

    /**
     * @param uderCrosstab the uderCrosstab to set
     */
    public void setUderCrosstab(boolean uderCrosstab);
    
    /**
     * generate iteration index
     * 
     * @param ind
     * @return iteration index
     */
    public String generateIterationInd(int ind);

    /**
     * @return POI cell type 
     * CELL_TYPE_BLANK      Blank Cell type (3) 
     * CELL_TYPE_BOOLEAN   Boolean Cell type (4) 
     * CELL_TYPE_ERROR         Error Cell type (5) 
     * CELL_TYPE_FORMULA       Formula Cell type (2) 
     * CELL_TYPE_NUMERIC       Numeric Cell type (0) 
     * CELL_TYPE_STRING        String Cell type (1) 
     */
    public int getXlsCellType();
    
    /**
     * set POI cell type 
     * CELL_TYPE_BLANK      Blank Cell type (3) 
     * CELL_TYPE_BOOLEAN   Boolean Cell type (4) 
     * CELL_TYPE_ERROR         Error Cell type (5) 
     * CELL_TYPE_FORMULA       Formula Cell type (2) 
     * CELL_TYPE_NUMERIC       Numeric Cell type (0) 
     * CELL_TYPE_STRING        String Cell type (1)
     *  
     * @param xlsCellType the xlsCellType to set
     */
    public void setXlsCellType(int xlsCellType);

    /**
     * @return the pageBreak
     */
    public XlsLayoutDirection getPageBreak();

    /**
     * @param pageBreak the pageBreak to set
     */
    public void setPageBreak(XlsLayoutDirection pageBreak);
}
