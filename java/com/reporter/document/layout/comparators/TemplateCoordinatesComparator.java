/**
 * 
 */
package com.reporter.document.layout.comparators;

import java.util.Comparator;

import org.apache.poi.hssf.util.CellReference;

import com.reporter.document.layout.Layout;

/**
 * @author Ilya Ovesnov
 *
 * @param <T>
 */
public class TemplateCoordinatesComparator<T extends Layout> implements Comparator<Layout> {
    
    /**
     * inverse value
     */
    private static final int INVERSE = -1;

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Layout o1, Layout o2) {
        
        CellReference cell1From = o1.getReferences().getCellReference();
        CellReference cell2From = o2.getReferences().getCellReference();
        CellReference cell2To = o2.getReferences().getCellReferenceTo();
        
        if (cell1From.getCol() > cell2From.getCol()){
            return INVERSE * compare(o2, o1);
        } else if (cell1From.getCol() == cell2From.getCol()){
            if (cell1From.getRow() > cell2From.getRow()){
                return 1;
            } else if (cell1From.getRow() < cell2From.getRow()){
                return -1;
            } else {
                return 0;
            }
        } else {
            if (cell1From.getRow() > cell2To.getRow()){
                return 1;
            } else {
                return -1;
            }
        }
        
    }

}
