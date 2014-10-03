/**
 * 
 */
package com.reporter.document.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class XlsFormulaRegions {
    /**
     * delimiter
     */
    private static final String CELL_DELIMITER = ",";
    /**
     * formula regions
     */
    private List<XlsFormulaRegion> regions;
    
    /**
     * Constructor
     * 
     * @param regions
     */
    public XlsFormulaRegions(String regions){
        this.regions = new ArrayList<XlsFormulaRegion>();
        String []reg = regions.split(CELL_DELIMITER);
        for (String region : reg){
            getRegions().add(new XlsFormulaRegion(region));
        }
    }

    /**
     * @return the regions
     */
    public List<XlsFormulaRegion> getRegions() {
        return regions;
    }
    
    /**
     * check is new reference pllied for all regions
     * 
     * @return true if ready
     */
    public boolean isReady(){
        for (XlsFormulaRegion reg : getRegions()){
            if (!reg.isReady()){
                return false;
            }
        }
        return true;
    }
    
    /**
     * get Value
     * 
     * @return value
     */
    public String getValue(){
        String result = "";
        for (XlsFormulaRegion reg : getRegions()){
            result += CELL_DELIMITER + reg.getValue();
        }
        return result.substring(1);
    }
    
    /**
     * reset
     */
    public void reset(){
        for (XlsFormulaRegion region : getRegions()){
            region.reset();
        }
    }
}
