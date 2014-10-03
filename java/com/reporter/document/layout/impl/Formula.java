/**
 * 
 */
package com.reporter.document.layout.impl;

import java.util.ArrayList;
import java.util.List;

import com.reporter.data.XLSCellValue;
import com.reporter.data.XLSDataType;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsFormulaRegions;
import com.reporter.document.layout.XlsReference;
import com.reporter.document.output.OutputElement;


/**
 * @author Ilya Ovesnov
 *
 */
public class Formula extends Label {
    
    /**
     * values
     */
    private List<String> valueBlocks;
    
    /**
     * formula regions
     */
    private List<XlsFormulaRegions> formulaRegions;
    
    /**
     * output refernce. needs becouse formula prepare after prepare all other elements.
     *  used for store all information for output
     */
    private OutputElement outputElement;

    /**
     * constructor
     * 
     * @param references
     */
    public Formula(XlsReference references) {
        super();
        this.setReferences(references);
        this.setCommand(XLSCommands.XLSCommandFormula);
        this.valueBlocks = new ArrayList<String>();
        this.formulaRegions = new ArrayList<XlsFormulaRegions>();
    }

    /**
     * default constructor
     */
    public Formula() {
        super();
        this.setCommand(XLSCommands.XLSCommandFormula);
    }

    /**
     * @return the valueBlocks
     */
    public List<String> getValueBlocks() {
        return valueBlocks;
    }

    /**
     * @return the formulaRegions
     */
    public List<XlsFormulaRegions> getFormulaRegions() {
        return formulaRegions;
    }

    /**
     * get flag indicates that formula regions ready
     * 
     * @return the formulaReady
     */
    public boolean isFormulaReady() {
        for (XlsFormulaRegions reg : getFormulaRegions()){
            if (!reg.isReady()){
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.reporter.document.layout.impl.Label#getValue()
     */
    public XLSCellValue getValue() {
        // TODO : check is formula ready
        String result = "";
        if (getValueBlocks().size() != getFormulaRegions().size() + 1){
//            System.out.println("*********** ERROR IN FORMULA **************");
        }
        int size = getValueBlocks().size();
        for (int i = 0; i < size; i++){
            result += getValueBlocks().get(i);
            if (i < size - 1){
                result += ((XlsFormulaRegions)getFormulaRegions().get(i)).getValue();
            }
        }
        
        return new XLSCellValue(result, XLSDataType.XLSDataFormula);
    }

    /**
     * @return the outputElement
     */
    public OutputElement getOutputElement() {
        return outputElement;
    }

    /**
     * @param outputElement the outputElement to set
     */
    public void setOutputElement(OutputElement outputElement) {
        this.outputElement = outputElement;
    }
    
    /**
     * reset formula
     */
    public void reset(){
        for (XlsFormulaRegions regions : getFormulaRegions()){
            regions.reset();
        }
    }

}
