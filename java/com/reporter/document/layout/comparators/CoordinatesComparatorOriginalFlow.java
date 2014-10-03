package com.reporter.document.layout.comparators;

import java.util.Comparator;

import com.reporter.document.layout.Layout;
import com.reporter.document.layout.XlsReference;

public class CoordinatesComparatorOriginalFlow<T extends Layout> implements Comparator<Layout> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Layout o1, Layout o2) {
        XlsReference ref1 = o1.getReferences();
        XlsReference ref2 = o2.getReferences();
        if ((ref1.getColIndex() == ref2.getColIndex()) && (ref1.getRowIndex() == ref2.getRowIndex())){
            //same cell
            return 0;
        } else if (ref1.getRowIndex() < ref2.getRowIndex()){
            //over
            return -1;
        } else if (ref1.getRowIndex() > ref2.getRowIndex()){
            //udner
            return 1;
        } else {
            //same row
            if (ref1.getColIndex() < ref2.getColIndex()){
                //is before
                return -1;
            } else {
                //is after
                return 1;
            }
        }
    }
}
