package com.reporter.parser;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsReference;
import com.reporter.document.layout.impl.Column;
import com.reporter.document.layout.impl.Frame;
import com.reporter.document.layout.impl.Row;

public class CrossTab extends Frame{
    /*
    //crosstab root rows
    private Map<String, DinamicLayout> topRows = new HashMap<String, DinamicLayout>();
    //crosstab root columns
    private Map<String, DinamicLayout> topColumns = new HashMap<String, DinamicLayout>();
    */ 
    //crosstab root row
    private DinamicLayout rootRow;
    //crosstab root column
    private DinamicLayout rootColumn;
  
    /**
     * @param allRows list ws rows ordered from bottom (most inner row) to top
     * @param allColumns list ws columns ordered from bottom (most inner column) to top
     */
    public CrossTab(DinamicLayout rootRow, DinamicLayout rootColumn){
        super();
        setCommand(XLSCommands.XLSCommandCrossTab);
        this.rootRow = rootRow;
        this.rootColumn = rootColumn;
        this.setRegionName("~cross~" + rootRow.getReferences().getColIndex() + "~" + rootColumn.getReferences().getRowIndex());
        this.hasDummyRegionName(true);
        //XlsReference(String sheetName, int fromRow, int fromCol, int toRow, int toCol)
        XlsReference ref = new XlsReference(rootRow.getReferences().getSheetName(), 
                rootColumn.getReferences().getRowIndex(), rootRow.getReferences().getColIndex(),
                rootColumn.getReferences().getRowNumFromRegionReference(), rootRow.getReferences().getColNumFromRegionReference());
        this.setReferences(ref);
    }

    /**
     * @return the rootColumn
     */
    public DinamicLayout getRootColumn() {
        return rootColumn;
    }

    /**
     * @return the rootRow
     */
    public DinamicLayout getRootRow() {
        return rootRow;
    }

}
