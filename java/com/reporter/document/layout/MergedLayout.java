package com.reporter.document.layout;

public interface MergedLayout extends Layout {
    
    /**
     * @return the copy of merge
     */
    public MergeRegion cloneMerge() throws CloneNotSupportedException ;

    /**
     * @return the merge
     */
    public MergeRegion getMerge();

    /**
     * @param merge the merge to set
     */
    public void setMerge(MergeRegion merge);
    
    /**
     * check if label has merged region
     */
    public boolean hasMerge();
}
