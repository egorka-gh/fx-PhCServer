/**
 * 
 */
package com.reporter.document.layout.impl;

import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.MergeRegion;
import com.reporter.document.layout.MergedLayout;
import com.reporter.document.layout.StaticLayout;

/**
 * @author Ilya Ovesnov
 *
 */
public class GenericStaticLayout extends GenericBoundLayout implements StaticLayout, MergedLayout {
    /**
     * merged region
     */
    private MergeRegion merge = null;

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
     */
    public boolean hasMerge(){
        return (merge != null);
    }

    /* (non-Javadoc)
     * @see com.reporter.document.layout.MergedLayout#cloneMerge()
     */
    public MergeRegion cloneMerge() throws CloneNotSupportedException {
        if (hasMerge()){
            return getMerge().clone();
        }
        return null;
    }

    /**
     * @param sqlName
     * @return sql Availability in parents
     */
    public boolean isSqlAvailable(String sqlName) {
        for (DinamicLayout layout : getParents()) {
            if (((GenericDinamicLayout)layout).isSqlAvailable(sqlName)){
                return true;
            }
        }
        return false;
    }

}
