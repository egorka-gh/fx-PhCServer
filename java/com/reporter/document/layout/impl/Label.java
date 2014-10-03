/**
 * 
 */
package com.reporter.document.layout.impl;

import org.apache.poi.hssf.usermodel.HSSFCell;

import com.reporter.data.XLSCellValue;
import com.reporter.document.layout.MergedLayout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsReference;
import com.reporter.document.layout.MergeRegion;

/**
 * @author Ilya Ovesnov
 *
 */
public class Label extends GenericUnBoundLayout implements MergedLayout{
    
    /**
     * label value
     */
    private XLSCellValue value;

    /* (non-Javadoc)
     * @see com.reporter.document.layout.MergedLayout#cloneMerge()
     */
    public MergeRegion cloneMerge() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        if (hasMerge()){
            return getMerge().clone();
        }
        return null;
    }

    /**
     * merged region
     */
    private MergeRegion merge = null;

    /**
     * constructor
     * 
     * @param references
     */
    public Label(XlsReference references) {
        super();
        this.setReferences(references);
        this.setCommand(XLSCommands.XLSCommandLabel);
    }
    
    /**
     * default constructor
     */
    public Label() {
        super();
        this.setCommand(XLSCommands.XLSCommandLabel);
    }

    /**
     * get label value
     * 
     * @return the value
     */
    public XLSCellValue getValue() {
        return value;
    }

    /**
     * set new label value
     * 
     * @param value the value to set
     */
    public void setValue(XLSCellValue value) {
        this.value = value;
    }

    /**
     * set new label value by xls cell
     * 
     * @param value the value to set
     */
    public void setValue(HSSFCell cell){
        this.value = new XLSCellValue(cell);
    }

    /**
     * @return the merge
     */
    public MergeRegion getMerge() {
        return merge;
    }

    /**
     * @param merge the merge to set
     */
    public void setMerge(MergeRegion merge) {
        this.merge = merge;
    }
    
    /**
     * check if label has merged region
     * 
     * @return hasMerge
     */
    public boolean hasMerge(){
        return (merge != null);
    }
}
