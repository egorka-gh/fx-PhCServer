/**
 * 
 */
package com.reporter.document.layout.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.poifs.property.RootProperty;

import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsReference;
import com.reporter.document.layout.comparators.TemplateCoordinatesComparator;


/**
 * @author Ilya Ovesnov
 *
 */
public class GenericDinamicLayout extends GenericBoundLayout implements DinamicLayout {

    /**
     * region name
     */
    private String regionName;
    
    /**
     * childs
     */
    private Set<Layout> childs;

    /**
     * added childs (crossTab)
     */
    private List<Layout> addedChilds;

    /**
     * removed childs (crossTab)
     */
    private List<Layout> removedChilds;

    /**
     * childs under crossTab inresection
     */
    private List<Layout> undeCrossChilds;

    /**
     * childs after crossTab inresection (on the right hand) 
     */
    private List<Layout> afterCrossChilds;

    /**
     * region dimension by X
     */
    private int regionDimensionX;
    
    /**
     * region dimension Y
     */
    private int regionDimentionY;

    /**
     * temprorary resize by Y
     */
    private int addedYSize;

    /**
     * mark if layout has no RegionName 
     */
    private boolean hasDummyRegionName;
    
    /**
     * default constructor
     *
     */
    public GenericDinamicLayout(){
        childs = new TreeSet<Layout>(new TemplateCoordinatesComparator<Layout>());
        removedChilds = new ArrayList<Layout>(); 
        addedChilds = new ArrayList<Layout>(); 
        undeCrossChilds = new ArrayList<Layout>();
        afterCrossChilds = new ArrayList<Layout>();
        hasDummyRegionName = false; 
    }

    /* (non-Javadoc)
     * @see com.reporter.document.layout.impl.GenericLayout#setReferences(java.lang.String)
     */
    public void setReferences(XlsReference references) {
        super.setReferences(references);
        this.setRegionDimensionX(getReferences().getColNumFromRegionReference());
        this.setRegionDimentionY(getReferences().getRowNumFromRegionReference());
    }

    /**
     * get element name
     * 
     * @return element name
     */
    public String getElementName() {
        return getRegionName();
    }


    /**
     * get region name
     * 
     * @return the regionName
     */
    public String getRegionName() {
        return regionName;
    }

    /**
     * set new region name
     * 
     * @param regionName the regionName to set
     */
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    /**
     * get childs
     * 
     * @return the childs
     */
    public Set<Layout> getChilds() {
        return childs;
    }
    
    /* (non-Javadoc)
     * @see com.reporter.document.layout.DinamicLayout#getChildList()
     */
    public List<Layout> getChildList(){
        return new ArrayList<Layout>(childs);
    }
    
    /* (non-Javadoc)
     * @see com.reporter.document.layout.DinamicLayout#setChilds(java.util.List)
     */
    public void setChilds(Set<Layout> childs){
        this.childs = childs;
    }
    
    /**
     * check is child list not empty
     * 
     * @return true if not empty
     */
    public boolean hasChilds() {
        return (getChilds() != null) && (getChilds().size() > 0);
    }

    /**
     * chech is region name specified
     * 
     * @return true if specified
     */
    public boolean isRegionSpecified() {
        return (getRegionName() != null) && (getRegionName().length() > 0); 
    }

    /**
     * get region dimension by X
     * 
     * @return the regionDimensionX
     */
    public int getRegionDimensionX() {
        return regionDimensionX;
    }

    /**
     * set new region dimension by X
     * 
     * @param regionDimensionX the regionDimensionX to set
     */
    public void setRegionDimensionX(int regionDimensionX) {
        this.regionDimensionX = regionDimensionX;
    }

    /**
     * get region dimension Y
     * 
     * @return the regionDimentionY
     */
    public int getRegionDimentionY() {
        return regionDimentionY;
    }

    /**
     * set new region dimension Y
     * 
     * @param regionDimentionY the regionDimentionY to set
     */
    public void setRegionDimentionY(int regionDimentionY) {
        this.regionDimentionY = regionDimentionY;
    }

    /**
     * @param sqlName
     * @return sql Availability in parents
     */
    public boolean isSqlAvailable(String sqlName) {
    	if ((getSqlName() != null) && (getSqlName().equals(sqlName))){
    	    return true;
    	}else{
    	    for (DinamicLayout layout : getParents()) {
    	        if (((GenericDinamicLayout)layout).isSqlAvailable(sqlName)){
    	            return true;
    	        }
    	    }
    	}
		return false;
	}

    /**
     * temprorary add child layout (crosstab)
     *  
     * @param layout
     */
    public void addChild(Layout layout){
        for (Layout lt : getChildList()){
            //if (layout.getReferences().equals(lt.getReferences())){
            if ((layout.getReferences().getColIndex() == lt.getReferences().getColIndex()) && (layout.getReferences().getRowIndex() == lt.getReferences().getRowIndex())){
                removeChild(lt);
                break;
            }
        }
        childs.add(layout);
        addedChilds.add(layout);
    }
    
    public boolean isChildAdded(Layout child){
        return addedChilds.contains(child); 
    }

    /**
     * temprorary remove child layout (crosstab)
     *  
     * @param layout
     */
    public void removeChild(Layout layout){
        if (childs.remove(layout)){
            removedChilds.add(layout);
        }
    }

    /**
     * 4 crosstab
     * remove temprorary added childs & restore temprorary removed childs
     *  
     * @param layout
     */
    public void resetChilds(){
        if (addedChilds.size()>0){
            childs.removeAll(addedChilds);
            addedChilds.clear();
        }
        if (removedChilds.size()>0){
            childs.addAll(removedChilds);
            removedChilds.clear();
        }
        for (Layout lt : getChildList()){
            if (lt instanceof DinamicLayout){
                ((DinamicLayout) lt).resetChilds();
            }
        }
        //restore Y size
        regionDimentionY -= addedYSize;
        getReferences().grow(-addedYSize, 0);
        addedYSize = 0;
    }
    
    /**
     * Completely remove child layout
     *  
     * @param layout
     */
    public void deleteChild(Layout layout){
        //childs.remove(layout);
        //TreeSet<Layout> newChilds = new TreeSet<Layout>(new TemplateCoordinatesComparator<Layout>());
        List<Layout> llt = getChildList();
        llt.remove(layout);
        //newChilds.addAll(llt);
        /*
        for (Layout lt : getChildList()){
            if (lt != layout){
                newChilds.add(arg0)
            }
        }
        */
        childs.clear();
        childs.addAll(llt);
    }

    /* (non-Javadoc)
     * @see com.reporter.document.layout.DinamicLayout#setIntersection(int, int, int, int)
     */
    public void setIntersection(int row, int column, int height, int width){
        //save childs after or under intersection
        int endRow = row + height -1;
        int endColumn = column + width -1;
        //List<Layout> checkLt = 
        for (Layout lt : getChildList()){
            if ((lt.getReferences().getRowIndex() > endRow) &&
                    (lt.getReferences().getColIndex() >= column) &&
                    (lt.getReferences().getColIndex() <= endColumn)){
                lt.setUderCrosstab(true);
                undeCrossChilds.add(lt);
                childs.remove(lt);
            } else if ((lt.getReferences().getColIndex() > endColumn) && 
                    (lt.getReferences().getRowIndex() >= row) &&
                    (lt.getReferences().getRowIndex() <= endRow)){
                lt.setAfterCrosstab(true);
                afterCrossChilds.add(lt);
                //childs.remove(lt);
            } else if (lt instanceof DinamicLayout){
                ((DinamicLayout)lt).setIntersection(row, column, height, width);
            }
        }
    }

    /**
     * 4 crosstab 
     * shift childs under crosstab intersection
     *  
     * @param rowOffset
     * @throws CloneNotSupportedException
     */
    public void shiftChildsUnderCross(int rowOffset) throws CloneNotSupportedException{
        if (getCommand() != XLSCommands.XLSCommandColumn){
            return;
        }
        addedYSize = rowOffset;
        regionDimentionY += rowOffset;
        getReferences().grow(rowOffset, 0);
        //do same for child columns
        for (Layout lt : getChildList()){
            if (lt.getCommand() == XLSCommands.XLSCommandColumn){
                ((DinamicLayout)lt).shiftChildsUnderCross(rowOffset);
            }
        }
        //shift childs under crosstab intersection
        for (Layout lt : undeCrossChilds){
            Layout addLt = lt.clone();
            addLt.setReferences(lt.getReferences().getCloneWithOffset(0, rowOffset));
            addChild(addLt); 
        }
    }

    /**
     * @return the hasDummyRegionName
     */
    public boolean hasDummyRegionName() {
        return hasDummyRegionName;
    }

    /**
     * @param hasDummyRegionName the hasDummyRegionName to set
     */
    public void hasDummyRegionName(boolean hasDummyRegionName) {
        this.hasDummyRegionName = hasDummyRegionName;
    }
}
