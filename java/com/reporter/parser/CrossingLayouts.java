package com.reporter.parser;

import com.reporter.document.layout.DinamicLayout;

public class CrossingLayouts {
    private DinamicLayout column;
    private DinamicLayout row;
    /**
     * @return the column
     */
    public DinamicLayout getColumn() {
        return column;
    }
    /**
     * @return the row
     */
    public DinamicLayout getRow() {
        return row;
    }
    /**
     * @param column
     * @param row
     */
    public CrossingLayouts(DinamicLayout column, DinamicLayout row) {
        super();
        this.column = column;
        this.row = row;
    }
    
}
