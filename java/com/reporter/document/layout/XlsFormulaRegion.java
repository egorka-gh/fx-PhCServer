/**
 * 
 */
package com.reporter.document.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ovesnov Ilya
 *
 */
public class XlsFormulaRegion {
    /**
     * delimiter
     */
    private static final String CELL_DELIMITER = ",";

    /**
     * region delimiter
     */
    private final static String REGION_DELIMITER = ":";
    
    /**
     * start cell
     */
    private String start;
    
    /**
     * end cell
     */
    private String end;
    
    /**
     * is Region
     */
    private boolean region;
    
    /**
     * result of start position
     */
    private List<XlsReference> startResult;
    
    /**
     * result of end position
     */
    private List<XlsReference> endresult;
    
    /**
     * Constructor
     * 
     * @param region
     */
    public XlsFormulaRegion(String region){
        String []r = region.split(REGION_DELIMITER);
        if (r.length == 1){
            this.region = false;
            this.start = r[0];
        } else if (r.length == 2){
            this.region = true;
            this.start = r[0];
            this.end = r[1];
        }
        this.startResult = new ArrayList<XlsReference>();
        this.endresult = new ArrayList<XlsReference>();
    }
    
    /**
     * check is new reference applied
     * 
     * @return true if ready
     */
    public boolean isReady(){
        if (isRegion() && (getStartResult().size() > 0) && (getEndresult().size() > 0)){
            return true;
        }
        if (!isRegion() && (getStartResult().size() > 0)){
            return true;
        }
        return false;
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }

    /**
     * @return the region
     */
    public boolean isRegion() {
        return region;
    }

    /**
     * @return the start
     */
    public String getStart() {
        return start;
    }
    
    /**
     * get value for region formula
     * 
     * @return value
     */
    private String getRegionValue(){
        if (!isRegion() || (getStartResult().size() != 1) || (getEndresult().size() != 1)){
            return null;
        }
        StringBuffer result = new StringBuffer();
        XlsReference start = getStartResult().get(0);
        XlsReference end = getEndresult().get(0);
        result.append(start.getCellReference()).append(REGION_DELIMITER).append(end.getCellReference());
        return result.toString();
    }
    
    /**
     * get value for simple formula
     * 
     * @return value
     */
    private String getSimpleValue(){
        if (isRegion() || (getStartResult().size() < 1)){
            return null;
        }
        StringBuffer result = new StringBuffer();
        for (XlsReference ref : getStartResult()){
            if (result.length() != 0){
                result.append(CELL_DELIMITER);
            }
            result.append(ref.getCellReference());
        }
        return result.toString();
    }
    
    /**
     * get value
     * 
     * @return value
     */
    public String getValue(){
        if (isRegion()){
            return getRegionValue();
        } else {
            return getSimpleValue();
        }
    }
    
    /**
     * add start region
     * 
     * @param reference
     */
    public void addStartRegion(XlsReference reference){
        if (!isRegion()){
            getStartResult().add(reference);
        } else {
            XlsReference result = reference;
            for (XlsReference ref : getStartResult()){
                if ((ref.getColIndex() <= result.getColIndex()) && 
                        (ref.getRowIndex() <= result.getRowIndex())){
                    result = ref;
                }
            }
            getStartResult().clear();
            getStartResult().add(result);
        }
    }
    
    /**
     * add end region
     * 
     * @param reference
     */
    public void addEndRegion(XlsReference reference){
        if (isRegion()){
            XlsReference result = reference;
            for (XlsReference ref : getEndresult()){
                if ((ref.getColIndex() >= result.getColIndex()) &&
                        (ref.getRowIndex() >= result.getRowIndex())){
                    result = ref;
                }
            }
            getEndresult().clear();
            getEndresult().add(result);
        }
    }
    
    /**
     * reset
     */
    public void reset(){
        startResult = new ArrayList<XlsReference>();
        endresult = new ArrayList<XlsReference>();
    }

    /**
     * @return the endresult
     */
    private List<XlsReference> getEndresult() {
        return endresult;
    }

    /**
     * @return the startResult
     */
    private List<XlsReference> getStartResult() {
        return startResult;
    }
}
