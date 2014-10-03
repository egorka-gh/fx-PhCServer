/**
 * 
 */
package com.reporter.document.layout;

import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 *
 */
public interface DinamicLayout extends BoundLayout{
    /**
     * get region name
     * 
     * @return the regionName
     */
    public String getRegionName();

    /**
     * set new region name
     * 
     * @param regionName the regionName to set
     */
    public void setRegionName(String regionName);

    /**
     * get childs
     * 
     * @return the childs
     */
    public Set<Layout> getChilds();
    
    public List<Layout> getChildList();
    
    /**
     * set childs
     * 
     * @param childs
     */
    public void setChilds(Set<Layout> childs);
    
    /**
     * check is child list not empty
     * 
     * @return true if not empty
     */
    public boolean hasChilds();

    /**
     * chech is region name specified
     * 
     * @return true if specified
     */
    public boolean isRegionSpecified();

    /**
     * get region dimension by X
     * 
     * @return the regionDimensionX
     */
    public int getRegionDimensionX();
    
    /**
     * set new region dimension by X
     * 
     * @param regionDimensionX the regionDimensionX to set
     */
    public void setRegionDimensionX(int regionDimensionX);

    /**
     * get region dimension Y
     * 
     * @return the regionDimentionY
     */
    public int getRegionDimentionY();

    /**
     * set new region dimension Y
     * 
     * @param regionDimentionY the regionDimentionY to set
     */
    public void setRegionDimentionY(int regionDimentionY);
    
    /**
     * @param sqlName
     * @return sqlAvailability
     */
    public boolean isSqlAvailable(String sqlName);
    
    /**
     * temprorary add child layout (crosstab)
     *  
     * @param layout
     */
    public void addChild(Layout layout);

    /**
     * temprorary remove child layout (crosstab)
     *  
     * @param layout
     */
    public void removeChild(Layout layout);
    
    /**
     * 4 crosstab
     * remove temprorary added childs & restore temprorary removed childs
     *  
     * @param layout
     */
    public void resetChilds();
    
    /**
     * Completely remove child layout
     *  
     * @param layout
     */
    public void deleteChild(Layout layout);
    
    /**
     * is child temprorary added 
     *  
     * @param layout
     */
    public boolean isChildAdded(Layout child);
    
    /**
     * 4 crosstab 
     * set intersection region
     *  
     * @param row
     * @param column
     * @param height
     * @param width
     */
    public void setIntersection(int row, int column, int height, int width);

    /**
     * 4 crosstab 
     * shift childs under crosstab intersection
     *  
     * @param rowOffset
     * @throws CloneNotSupportedException
     */
    public void shiftChildsUnderCross(int rowOffset) throws CloneNotSupportedException;

    /**
     * @return the hasDummyRegionName
     */
    public boolean hasDummyRegionName();

    /**
     * @param hasDummyRegionName the hasDummyRegionName to set
     */
    public void hasDummyRegionName(boolean hasDummyRegionName);
}
