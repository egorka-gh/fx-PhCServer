package com.reporter.document.output;

import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.document.layout.XlsReference;
import java.util.ArrayList;
import java.util.List;

public class OutputName {
    /**
     * Outline direction
     */
    private XlsLayoutDirection direction;

    /*
     * all references on sheet 
     */
    private List <OutputNameReference> references;
    
    /*
     * name
     */
    private String name;

    /*
     * sheet name
     */
    private String sheetName;

    /*
     * indicates that name is 4 sheet (lockal), not 4 workbook
     */
    private boolean isSheetName;

    /**
     * @param direction
     * @param sheetName
     */
    public OutputName(XlsLayoutDirection direction, String sheetName, String name, OutputNameReference reference) {
        super();
        this.direction = direction;
        this.sheetName = sheetName;
        this.name = name;
        references = new ArrayList <OutputNameReference>();
        if (reference != null){
            references.add(reference);
        }
    }

    /**
     * @return the direction
     */
    public XlsLayoutDirection getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(XlsLayoutDirection direction) {
        this.direction = direction;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the sheetName
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * @param sheetName the sheetName to set
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
    
    /*
     * add reference 
     */
    public void addReference(OutputNameReference reference){
        references.add(reference);
    }

    /**
     * @return the isSheetName
     */
    public boolean isSheetName() {
        return isSheetName;
    }

    /**
     * @param isSheetName the isSheetName to set
     */
    public void setSheetName(boolean isSheetName) {
        this.isSheetName = isSheetName;
    }
    
    /*
     * concat & return all references strings
     */
    public String getReference(){
        String result;
        result = "";
        int xFrom = -1;
        int xTo = -1;
        int yFrom = -1;
        int yTo = -1;
        for (OutputNameReference ref : references){
            /*TODO POI 3.0.1 can't create multy reference named range
             * it is done in 3.5 betta but it's not stable
            //TODO create compact references method
            if (result.length()>0){
                result = XlsReference.joinReferences(result, ref.getReference(sheetName));
            } else {
                result = ref.getReference(sheetName);
            }
            */
            //so create name ws simple reference by min & max cell
            if (xFrom == -1){
                //first ref
                xFrom = ref.getFromX();
                xTo = ref.getToX();
                yFrom = ref.getFromY();
                yTo = ref.getToY();
            } else {
                if (xFrom > ref.getFromX()){
                    xFrom = ref.getFromX();
                }
                if (xTo < ref.getToX()){
                    xTo = ref.getToX();
                }
                if (yFrom > ref.getFromY()){
                    yFrom = ref.getFromY();
                }
                if (yTo < ref.getToY()){
                    yTo = ref.getToY();
                }
            }
        }
        result = XlsReference.composeReferenceString(sheetName, yFrom, xFrom, yTo, xTo);
        return result;    
    }
    
}
