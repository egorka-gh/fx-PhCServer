/**
 * 
 */
package com.reporter.document.output;

import java.util.Comparator;

import com.reporter.data.XLSCellValue;
import com.reporter.document.layout.XlsReference;

/**
 * @author Administrator
 *
 */
public class OutputElement implements Comparator<OutputElement>{
    
    /**
     * template references
     */
    private XlsReference templateReferences;

    /**
     *  xls workbook cell style index
     */
    private short styleIndex;

    /**
     * output references
     */
    private XlsReference outputReferences;
    
    /**
     * cell value
     */
    //private String cellValue;
    private XLSCellValue cellValue;
    
    /**
     * iteration index. {reference}:{iteration index}
     * used for calculation regions in formula used in crosstab
     */
    private String iterationIndex;

    /**
     * flag indicates that this element created by shift main region
     */
    private boolean shiftedElement;
    
    /**
     * number of shifted cells by X
     */
    private int shiftByX;
    
    /**
     * number of shifted cells by Y
     */
    private int shiftByY;
    
    /**
     * flag indicates that element ready for output
     */
    private boolean readyForOutput;
    
    /**
     * flag indicates that cell is formula
     */
    private boolean formula;

    /**
     * is element uder crosstab intersection
     */
    private boolean uderCrosstab;

    /**
     * is element after (righter) crosstab intersection
     */
    private boolean afterCrosstab;
    
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
     * Default constructor
     */
    public OutputElement(){
        super();
        this.formula = false;
    }

    /**
     * get template references
     * 
     * @return the templateReferences
     */
    public XlsReference getTemplateReferences() {
        return templateReferences;
    }

    /**
     * set new template references
     * 
     * @param templateReferences the templateReferences to set
     */
    public void setTemplateReferences(XlsReference templateReferences) {
        this.templateReferences = templateReferences;
    }

    /**
     * get output references
     * 
     * @return the outputReferences
     */
    public XlsReference getOutputReferences() {
        return outputReferences;
    }

    /**
     * set new output references
     * 
     * @param outputReferences the outputReferences to set
     */
    public void setOutputReferences(XlsReference outputReferences) {
        this.outputReferences = outputReferences;
    }

    /**
     * get cell value
     * 
     * @return the cellValue
     */
    public XLSCellValue getCellValue() {
        return cellValue;
    }

    /**
     * set new string cell value 
     * 
     * @param cellValue the string to set
     */
    public void setCellValue(String cellValue) {
        this.cellValue = new XLSCellValue(cellValue);
    }

    /**
     * set new cell value 
     * 
     * @param cellValue the value to set
     */
    public void setCellValue(XLSCellValue cellValue) {
        this.cellValue = cellValue;
    }

    /**
     * get xls workbook cell style index
     * 
     * @return the styleIndex
     */
    public short getStyleIndex() {
        return styleIndex;
    }

    /**
     * set new xls workbook cell style index
     * 
     * @param styleIndex the styleIndex to set
     */
    public void setStyleIndex(short styleIndex) {
        this.styleIndex = styleIndex;
    }

    /**
     * get flag indicates that this element created by shift main region
     * 
     * @return the shiftedElement
     */
    public boolean isShiftedElement() {
        return shiftedElement;
    }

    /**
     * set new flag indicates that this element created by shift main region
     * 
     * @param shiftedElement the shiftedElement to set
     */
    public void setShiftedElement(boolean shiftedElement) {
        this.shiftedElement = shiftedElement;
    }

    /**
     * get number of shifted cells by X
     * 
     * @return the shiftByX
     */
    public int getShiftByX() {
        return shiftByX;
    }

    /**
     * set new number of shifted cells by X
     * 
     * @param shiftByX the shiftByX to set
     */
    public void setShiftByX(int shiftByX) {
        this.shiftByX = shiftByX;
    }

    /**
     * get number of shifted cells by Y
     * 
     * @return the shiftByY
     */
    public int getShiftByY() {
        return shiftByY;
    }

    /**
     * set new number of shifted cells by Y
     * 
     * @param shiftByY the shiftByY to set
     */
    public void setShiftByY(int shiftByY) {
        this.shiftByY = shiftByY;
    }


    /**
     * get flag indicates that element ready for output
     * 
     * @return the readyForOutput
     */
    public boolean isReadyForOutput() {
        return readyForOutput;
    }

    /**
     * set new flag indicates that element ready for output
     * 
     * @param readyForOutput the readyForOutput to set
     */
    public void setReadyForOutput(boolean readyForOutput) {
        this.readyForOutput = readyForOutput;
    }
    
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(OutputElement o1, OutputElement o2) {
        return o1.getOutputReferences().equals(o2.getOutputReferences()) ? 0 : 1;
    }

    /**
     * get flag indicates that cell is formula
     * 
     * @return the formula
     */
    public boolean isFormula() {
        return formula;
    }

    /**
     * set new flag indicates that cell is formula
     * 
     * @param formula the formula to set
     */
    public void setFormula(boolean formula) {
        this.formula = formula;
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
    }

    /**
     * @return POI cell type 
     * CELL_TYPE_BLANK      Blank Cell type (3) 
     * CELL_TYPE_BOOLEAN   Boolean Cell type (4) 
     * CELL_TYPE_ERROR         Error Cell type (5) 
     * CELL_TYPE_FORMULA       Formula Cell type (2) 
     * CELL_TYPE_NUMERIC       Numeric Cell type (0) 
     * CELL_TYPE_STRING        String Cell type (1) 
     */
    public int getXlsCellType() {
        return xlsCellType;
    }

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
    public void setXlsCellType(int xlsCellType) {
        this.xlsCellType = xlsCellType;
    }

}
