/**
 * 
 */
package com.reporter.document;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.util.CellReference;

import com.reporter.data.XLSCellValue;
import com.reporter.data.XLSConnectionBrokenException;
import com.reporter.data.XLSDataLockedException;
import com.reporter.data.XLSDataType;
import com.reporter.data.XLSUnknownSourceException;
import com.reporter.data.data.RecordSourceTransmitter;
import com.reporter.data.data.XLSRecordSource;
import com.reporter.document.exception.OutputValueNotReadyException;
import com.reporter.document.exception.XlsOutputValueNotFoundException;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.BoundLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.MergeRegion;
import com.reporter.document.layout.MergedLayout;
import com.reporter.document.layout.OffsetElement;
import com.reporter.document.layout.StaticLayout;
import com.reporter.document.layout.UnBoundLayout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XLSMergeType;
import com.reporter.document.layout.XlsFormulaRegion;
import com.reporter.document.layout.XlsFormulaRegions;
import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.document.layout.XlsReference;
import com.reporter.document.layout.comparators.TemplateCoordinatesComparator;
import com.reporter.document.layout.impl.Formula;
import com.reporter.document.layout.impl.GenericDinamicLayout;
import com.reporter.document.layout.impl.GenericStaticLayout;
import com.reporter.document.layout.impl.Label;
import com.reporter.document.layout.impl.Sheet;
import com.reporter.document.output.OutputElement;
import com.reporter.document.output.OutputNameReference;
import com.reporter.parser.CrossTab;
import com.reporter.parser.exceptions.CommandStructureException;
import com.reporter.utils.XlsUtils;
import com.reporter.xml.data.SqlElement;
import com.reporter.xml.data.SqlParametr;
import com.reporter.document.output.BorderBuilder;
import com.reporter.document.output.OutputOutline;
import com.reporter.document.output.OutputWorkbook;
import com.reporter.document.output.SectionElementOffsets;
import com.reporter.document.output.SectionOffset;


/**
 * @author Administrator
 *
 */
public class XlsPrepareElement {

    /**
     * prepared elements list (ex result in prepareElement)
     */
    private List<OutputElement> preparedElements;
    
    /**
     * list with not ready for output formulas
     */
    private List<Formula> notReadyFormula;
    
    /**
     * list with not ready for output formulas in cross tab
     */
    private List<Formula> notReadyFormulaInCross;
    
    /**
     * sql rows fetched
     */
    private int sqlRows;
    
    /**
     * result set
     * private XLSRecordSet resultSet;
     */
    private XLSRecordSource recordSource;
    
    /**
     * parent instance
     */
    private XlsPrepareElement parent;
    
    /**
     * current offset by X
     */
    private int currentOffsetX;
    
    /**
     * current offset by Y
     */
    private int currentOffsetY;

    /**
     * parent element type
     */
    private XLSCommands elementType;
    
    /**
     * offset list
     */
    private List<OffsetElement> offsets;
    
    /**
     * output workbook (replaces OuputSheet)
     */
    private OutputWorkbook outputWorkbook;
    
    /**
     * merges by current element size
     */
    private List<MergeRegion> elementMerges;

    /**
     * merges by current section size
     */
    private List<MergeRegion> sectionMerges;
    
    /**
     * section name
     */
    private String sectionName;
    
    /**
     * section offset
     */
    private SectionOffset sectionOffset;
    
    /**
     * layout
     */
    private Layout layout;
    
    /**
     * constructor
     * 
     * @param outputWorkbook
     * @param parent
     * @param elementType
     * @param layout
     */
    public XlsPrepareElement(OutputWorkbook outputWorkbook, XlsPrepareElement parent, XLSCommands elementType, Layout layout){
        super();
        this.outputWorkbook = outputWorkbook;
        this.currentOffsetX = 0;
        this.currentOffsetY = 0;
        //save parent element type
        this.elementType = elementType;
        this.offsets = new ArrayList<OffsetElement>();
        this.preparedElements = new ArrayList<OutputElement>();
        this.sectionName = "";
        this.layout = layout;
        this.sectionOffset = new SectionOffset(layout);
        this.notReadyFormula = new ArrayList<Formula>();
        this.notReadyFormulaInCross = new ArrayList<Formula>();
        setParent(parent);
    }
    
    /**
     * prepare fields for output
     * 
     * @param field
     * @param offsetX
     * @param offsetY
     * @param ref
     * @return OutputElement prepared elements
     * @throws SQLException
     * @throws XlsOutputValueNotFoundException
     * @throws IOException 
     */
    private OutputElement prepareField(StaticLayout field, int offsetX, int offsetY, XlsReference ref) 
            throws SQLException, XlsOutputValueNotFoundException, IOException{
        if (field.getReferences() == null){
            field.setReferences(ref);
        }
        OutputElement element = new OutputElement();
        try {
            element.setTemplateReferences(field.getOriginalReference());
            element.setIterationIndex(field.getIterationIndex());
            element.setOutputReferences(field.getReferences().getCloneWithOffset(offsetX, offsetY));
            element.setCellValue(getValueFromContext(field.getSqlName(), field.getColumnName(),!isSqlInContext(field.getSqlName())));
            element.setStyleIndex(field.getStyleIndex());
            element.setXlsCellType(field.getXlsCellType());
            element.setAfterCrosstab(field.isAfterCrosstab());
            element.setUderCrosstab(field.isUderCrosstab());
        } catch (XLSUnknownSourceException e) {
            //TODO what about excepttion ???
            return null;
        }
        
        return element;
    }
    
    /**
     * apply parametrs
     * 
     * @param layout
     * @throws SQLException
     * @throws XlsOutputValueNotFoundException 
     * @throws CloneNotSupportedException 
     * @throws XLSConnectionBrokenException 
     * @throws XLSDataLockedException 
     * @throws IOException 
     * @throws XLSUnknownSourceException 
     */
    private void applyParametersForBoundElement(BoundLayout layout) throws SQLException, XlsOutputValueNotFoundException, CloneNotSupportedException, XLSDataLockedException, XLSConnectionBrokenException, IOException, XLSUnknownSourceException{
        String sql = layout.getSqlName();
        SqlElement sqlElement = null;
        if (sql != null) {
            sqlElement = getSql().get(sql);
        }
        boolean isAllParamLoaded = true;
        if (sqlElement != null) {
            boolean isParametrsCopy = true;

            if ((layout.getParametrs().size() != sqlElement.getParametrs().size())){
                isParametrsCopy = false;
            }

            int paramIndex = 0;
            for (SqlParametr param : sqlElement.getParametrs()){

                SqlParametr parametr = null;
                if (!isParametrsCopy){
                    parametr = param.clone();
                    layout.getParametrs().add(parametr);
                } else {
                    parametr = layout.getParametrs().get(paramIndex);
                }


                if ((parametr != null) && !parametr.isDataLoaded()){
                    //String val = null;
                    try {
                        /*
                        val = getValueFromContext(parametr.getSqlName(), parametr.getColumnName());
                        parametr.setParamValue(val);
                        parametr.setDataLoaded(true);
                        */
                        setParameterFromContext(parametr, !isSqlInContext(parametr.getSqlName()));
                    } catch (XLSUnknownSourceException e) {
                        isAllParamLoaded = false;
                        if (layout.getParents().size() < 2){
                            //exception
                            throw new XLSUnknownSourceException("unknownSQLParameter", parametr.getOriginalName(), sql, layout.getReferences().getReference());
                        }
                    }
                }
                paramIndex++;
            }
        }
        if (isAllParamLoaded){
            recordSource = outputWorkbook.getDataCache().getRecordSet((DinamicLayout)layout, layout.getParametrs());
        } else {
            if (layout.getParents().size() < 2){
                //exception
                //throw new XLSUnknownSourceException("unknownSource", sqlName, columnName);
                //throw new OutputValueNotReadyException("cantBePrepared", layout.getReferences().getReference());
            } else {
                recordSource = null;
            }
        }
    }
    
    /**
     * load result set
     * 
     * @param layout
     * @throws SQLException
     * @throws OutputValueNotReadyException 
     * @throws XlsOutputValueNotFoundException 
     * @throws CloneNotSupportedException 
     * @throws XLSConnectionBrokenException 
     * @throws XLSDataLockedException 
     * @throws IOException 
     * @throws XLSUnknownSourceException 
     */
    private void loadResultSet(BoundLayout layout) throws SQLException, XlsOutputValueNotFoundException, OutputValueNotReadyException, CloneNotSupportedException, XLSDataLockedException, XLSConnectionBrokenException, IOException, XLSUnknownSourceException{
        XLSRecordSource parentSource = getSourceFromContext(layout);
        if (parentSource != null){
            RecordSourceTransmitter rst = new RecordSourceTransmitter(parentSource, (DinamicLayout) layout);
            XLSCommands cmd = layout.getCommand();
            //set group field
            if ((cmd != XLSCommands.XLSCommandCell) && (cmd != XLSCommands.XLSCommandFrame)){
                rst.setGroupField(layout.getColumnName());
            }
            recordSource = rst;
            //runsql ? 
        } else {
            applyParametersForBoundElement(layout);
        }
        
    }
    
    /**
     * look up already opened record source in parents 
     * @param layout
     * @return record source
     */
    protected XLSRecordSource getSourceFromContext(BoundLayout layout){
        //TODO what about cell in crosstab?
        XLSRecordSource result = null;
        if (getParent() != null){
            XLSRecordSource parentSrc = getParent().getRecordSource();
            if ((parentSrc != null) && (parentSrc.getName().equals(layout.getSqlName()))){
                result = parentSrc;
            } else {
                result = getParent().getSourceFromContext(layout);
            }
        }
        return result;
    }
    
    /**
     * @return the recordSource
     */
    protected XLSRecordSource getRecordSource() {
        return recordSource;
    }
    
    /**
     * gets layout level from top of the cross, 0 - layout not in crosstab
     *   
     * @param layout
     * @return
     */
    private int getCrossTabLevel(Layout layout){
        int result = 0;
        if (layout.getParents() != null){
            for (Layout lt : layout.getParents()){
                if (lt instanceof CrossTab){
                    result = 1;
                    break;
                }
                int i = getCrossTabLevel(lt);
                if (i>0){
                    result = i+1;
                    break;
                }
            }
        }
        return result;
    }
    /**
     * prepare BoundLayout element to output
     * 
     * @param layout
     * @param offsetX
     * @param offsetY
     * @return output elements
     * @throws SQLException
     * @throws XlsOutputValueNotFoundException
     * @throws CloneNotSupportedException
     * @throws OutputValueNotReadyException 
     * @throws XLSConnectionBrokenException 
     * @throws XLSDataLockedException 
     * @throws IOException 
     * @throws CommandStructureException 
     * @throws XLSUnknownSourceException 
     */
    private List<OutputElement> prepareBoundElement(BoundLayout layout, int offsetX, int offsetY) 
                    throws SQLException, XlsOutputValueNotFoundException, CloneNotSupportedException, OutputValueNotReadyException, XLSDataLockedException, XLSConnectionBrokenException, IOException, CommandStructureException, XLSUnknownSourceException{
        List<OutputElement> result = new ArrayList<OutputElement>();

        loadResultSet(layout);
        if ((recordSource != null)){
            if (layout instanceof DinamicLayout){  
                sqlRows = 0;
                while (recordSource.next()){
                    //getOffsets().clear();
                    if (layout.getCommand() == XLSCommands.XLSCommandSheet){
                        String templateSheetName = ((Sheet)layout).getSheetName(); 
                        String outputSheetName = templateSheetName;
                        outputSheetName = recordSource.getField(((Sheet)layout).getCaptionField()).toString();
                        if (outputSheetName.trim().length() == 0){
                            outputSheetName = templateSheetName;
                        }
                        result = new ArrayList<OutputElement>();
                        preparedElements = new ArrayList<OutputElement>();
                        outputWorkbook.createOuputSheet(templateSheetName, outputSheetName);
                    }
                    //object to hold offsets inside section element
                    SectionElementOffsets elementOffsets = new SectionElementOffsets(layout);
                    //section element output childs
                    List<OutputElement> allChilds = new ArrayList<OutputElement>();
                    sqlRows ++;

                    if (((DinamicLayout)layout).hasChilds()){
                        //build child layouts
                        List<Layout> childs = new ArrayList<Layout>(((DinamicLayout)layout).getChilds());
                        List<OutputElement> crossRootRowOutput = new ArrayList<OutputElement>();
                        for (Layout childLayout : childs){
                            //mark row output by iteration number for crosstab
                            if ((layout.getCommand() == XLSCommands.XLSCommandRow) && 
                                    (layout.getParents() != null) && (layout.getParents().size() == 1)){
                                if (getCrossTabLevel(layout)>0){
                                    //row inside crosstab
                                    childLayout.setIterationIndex(layout.generateIterationInd(sqlRows));
                                }
                            }
                            XlsPrepareElement prepareElement = new XlsPrepareElement(outputWorkbook, this, layout.getCommand(), childLayout.clone());
                            // get offsets for child element
                            int oX = offsetX + sectionOffset.getXOffset() + elementOffsets.getXOffset(childLayout);
                            int oY = offsetY + sectionOffset.getYOffset() + elementOffsets.getYOffset(childLayout);
                            prepareElement.prepareElement(oX, oY, layout.getReferences());
                            List<OutputElement> childResult = prepareElement.getPreparedElements();
                            //cross tab
                            if (layout.getCommand() == XLSCommands.XLSCommandCrossTab){
                                //cross tab
                                if (childLayout == ((CrossTab)layout).getRootRow()){
                                    //rootRow done
                                    //save rootRow output
                                    crossRootRowOutput = new ArrayList<OutputElement>(childResult);
                                    childResult.clear();
                                    //by rootRow result height resize columns & shift layouts under rootRow
                                    SectionOffset rootRowSection = prepareElement.getSectionOffset();
                                    ((CrossTab)layout).getRootColumn().shiftChildsUnderCross(rootRowSection.getYOffset());
                                } else if (childLayout == ((CrossTab)layout).getRootColumn()){
                                    //rootColumn done
                                    //shift rootRow output by rootColumn result width
                                    SectionOffset rootColSection = prepareElement.getSectionOffset();
                                    int xOffs = rootColSection.getXOffset();
                                    for (OutputElement oe : crossRootRowOutput){
                                        if (oe.isAfterCrosstab()){
                                            oe.setOutputReferences(oe.getOutputReferences().getCloneWithOffset(xOffs, 0));
                                        }
                                    }
                                    
                                    // FIXME It is looking as hack 
                                    for (Formula formula : getNotReadyFormulaInCross()){
                                        OutputElement oe = formula.getOutputElement();
                                        if (formula.isAfterCrosstab()){
                                            oe.setOutputReferences(oe.getOutputReferences().getCloneWithOffset(xOffs, 0));
                                        }
                                    }
                                    getNotReadyFormulaInCross().clear();
                                    
                                    //add rootRow output to result
                                    result.addAll(crossRootRowOutput);
                                }
                                getNotReadyFormulaInCross().addAll(prepareElement.getNotReadyFormula());
                            }
                            getNotReadyFormula().addAll(prepareElement.getNotReadyFormula());
                            //add offset from dynamic child to current section element
                            elementOffsets.addChildSection(prepareElement.getSectionOffset());
                            
                            if ((childResult != null) && (childResult.size() > 0)){
                                allChilds.addAll(childResult);
                            }

                        }

                    }
                    //TODO is it working ws crosstab
                    if (getNotReadyFormula().size() > 0){
                        prepareFormula(allChilds);
                    }
                    
                    //finalize element offsets 
                    elementOffsets.complite();

                    //bild border
                    //TODO sheet borders?
                    if (layout.getCommand() != XLSCommands.XLSCommandSheet){
                        BorderBuilder bb = new BorderBuilder(allChilds, elementOffsets, sectionOffset, outputWorkbook);
                        bb.build();
                    }
                    
                    int regionDimensionX = ((DinamicLayout)layout).getRegionDimensionX();
                    int regionDimensionY = ((DinamicLayout)layout).getRegionDimentionY();
                    //process element merges
                    if ((elementMerges != null) && (elementMerges).size()>0){
                        int rowTo = layout.getReferences().getRowIndex()-1+ offsetY + regionDimensionY + elementOffsets.getResultingOffsetY() + sectionOffset.getYOffset();
                        Integer colTo = new Integer(layout.getReferences().getColIndex()-1+ offsetX + regionDimensionX + elementOffsets.getResultingOffsetX() + sectionOffset.getXOffset());
                        for (MergeRegion mr : elementMerges){
                            if (mr.getDirection() == XlsLayoutDirection.RowDirection){
                                mr.setRowTo(rowTo);
                            } else if (mr.getDirection() == XlsLayoutDirection.ColumnDirection){
                                mr.setColumnTo(colTo.shortValue());
                            }
                            outputWorkbook.getOuputSheet().getMerges().add(mr);
                        }
                        elementMerges.clear();
                    }
                    
                    sectionOffset.grow(elementOffsets);
                    
                    result.addAll(allChilds);
                    if (layout.getCommand() == XLSCommands.XLSCommandSheet){
                        outputWorkbook.getOuputSheet().getElements().addAll(result);
                    } 
                }
                //section complite

                recordSource.release(layout);
                sectionOffset.stopGrow();

                if (layout.getCommand() != XLSCommands.XLSCommandSheet){
                    //calc section dimentions
                    int clFrom = layout.getReferences().getColIndex() + offsetX;
                    int rwFrom = layout.getReferences().getRowIndex() + offsetY;
                    int clTo = clFrom -1+ sectionOffset.getXOffset() + ((DinamicLayout)layout).getRegionDimensionX();
                    int rwTo = rwFrom -1+ sectionOffset.getYOffset() + ((DinamicLayout)layout).getRegionDimentionY();
                    //add named range and/or named range reference for section
                    if (!((DinamicLayout)layout).hasDummyRegionName()){
                        /*
                        if  (layout.getElementName().equalsIgnoreCase("rpsrow")){
                            String gg = ""; 
                        }
                        */
                        OutputNameReference outNameRef = new OutputNameReference(clFrom, rwFrom, clTo, rwTo);
                        outputWorkbook.getOuputSheet().addNameReference( ((DinamicLayout)layout).getRegionName(), outNameRef, layout.getDirection());
                    }   
                    //check outlines
                    if (layout.isOutlineH()){
                        OutputOutline newOutline = new OutputOutline(XlsLayoutDirection.RowDirection);
                        newOutline.setFromY(rwFrom); 
                        newOutline.setToY(rwTo);
                        outputWorkbook.getOuputSheet().getOutlines().add(newOutline);
                    }
                    if (layout.isOutlineV()){
                        OutputOutline newOutline = new OutputOutline(XlsLayoutDirection.ColumnDirection);
                        newOutline.setFromX(clFrom); 
                        newOutline.setToX(clTo);
                        outputWorkbook.getOuputSheet().getOutlines().add(newOutline);
                    }
                    if (layout.isOutline()){
                        OutputOutline newOutline = new OutputOutline(null);
                        newOutline.setFromX(clFrom); 
                        newOutline.setToX(clTo);
                        newOutline.setFromY(rwFrom); 
                        newOutline.setToY(rwTo);
                        outputWorkbook.getOuputSheet().getOutlines().add(newOutline);
                    }
                }
                
                //check section merges
                if ((sectionMerges != null) && (sectionMerges).size()>0){
                    int rowTo = layout.getReferences().getRowIndex() - 1 + offsetY + sectionOffset.getYOffset() + ((DinamicLayout)layout).getRegionDimentionY();
                    int colTo = layout.getReferences().getColIndex() - 1 + offsetX + sectionOffset.getXOffset() + ((DinamicLayout)layout).getRegionDimensionX();
                    for (MergeRegion mr : sectionMerges){
                        if (mr.getDirection() == XlsLayoutDirection.RowDirection){
                            mr.setRowTo(rowTo);
                        } else if (mr.getDirection() == XlsLayoutDirection.ColumnDirection){
                            mr.setColumnTo(new Integer(colTo).shortValue());
                        }
                        outputWorkbook.getOuputSheet().getMerges().add(mr);
                    }
                    sectionMerges.clear();
                }
                
            }
        }

        return result;
    }
    
    /**
     * prepare formula for output
     * 
     * @param elements
     */
    private void prepareFormula(List<OutputElement> elements){
        List<Formula> newNotReadyFormula = new ArrayList<Formula>();
        List<OutputElement> result = new ArrayList<OutputElement>();
        
        for (Formula formula : getNotReadyFormula()){
            String iterationInd = formula.getIterationIndex();
            for (XlsFormulaRegions regions : formula.getFormulaRegions()){
                for (XlsFormulaRegion region : regions.getRegions()){
                    CellReference start = new CellReference(region.getStart());
                    CellReference end = null;
                    if (region.isRegion()){
                        end = new CellReference(region.getEnd());
                    }
                    for (OutputElement output : elements){
                        String outputIterationInd = output.getIterationIndex();
                        boolean isIterationIndEquals = false;
                        if ((iterationInd == null) || 
                                ((outputIterationInd != null) && (outputIterationInd.startsWith(iterationInd)))){
                            isIterationIndEquals = true;
                        }
                            
                        if ((output.getTemplateReferences() != null) && isIterationIndEquals &&
                                (start.getCol() == output.getTemplateReferences().getColIndex()) &&
                                (start.getRow() == output.getTemplateReferences().getRowIndex())){
                            region.addStartRegion(output.getOutputReferences());
                        }
                        if ((end != null) && (output.getTemplateReferences() != null) && isIterationIndEquals &&
                                (end.getCol() == output.getTemplateReferences().getColIndex()) && 
                                (end.getRow() == output.getTemplateReferences().getRowIndex())){
                            region.addEndRegion(output.getOutputReferences());
                        }
                    }
                }
            }
            if (formula.isFormulaReady()){
                OutputElement outputFormula = formula.getOutputElement();
                outputFormula.setCellValue(formula.getValue());
                outputFormula.setXlsCellType(formula.getXlsCellType());
                formula.reset();
                result.add(outputFormula);
            } else {
                newNotReadyFormula.add(formula);
            }
        }
        getNotReadyFormula().clear();
        getNotReadyFormula().addAll(newNotReadyFormula);
        elements.addAll(result);
    }
    
    /**
     * prepare static unbound element to output
     * 
     * @param layout
     * @param offsetX
     * @param offsetY
     * @return output element
     */
    private OutputElement prepareStaticUnBoundELement(Layout layout, int offsetX, int offsetY){
        OutputElement element = new OutputElement();
        element.setTemplateReferences(layout.getOriginalReference());
        element.setOutputReferences(layout.getReferences().getCloneWithOffset(offsetX, offsetY));
        element.setIterationIndex(layout.getIterationIndex());
        element.setXlsCellType(layout.getXlsCellType());
        element.setAfterCrosstab(layout.isAfterCrosstab());
        element.setUderCrosstab(layout.isUderCrosstab());
        if (layout instanceof Formula){
            Formula formula = (Formula)layout;
            element.setFormula(true);
            element.setStyleIndex(formula.getStyleIndex());
            if (formula.isFormulaReady()){
                element.setCellValue(formula.getValue());
            } else {
                formula.setOutputElement(element);
                element.setStyleIndex(formula.getStyleIndex());
                getNotReadyFormula().add(formula);
                return null;
            }
        } else if (layout instanceof Label){
            if (layout.getXlsCellType() != HSSFCell.CELL_TYPE_BLANK){
                element.setCellValue(((Label)layout).getValue());
            }
            element.setStyleIndex(((Label)layout).getStyleIndex());
        }
        return element;
    }
    
    /**
     * add new labels to row parent
     * 
     * @param outputElement
     * @param layout
     * @return true if label was added to second parent
     * @throws IOException 
     * @throws OutputValueNotReadyException 
     * @throws CloneNotSupportedException 
     */
    private boolean addNewLabelForParents(OutputElement outputElement, Layout layout) throws OutputValueNotReadyException, IOException, CloneNotSupportedException{
        if ((layout.getParents() != null) && (layout.getParents().size() == 2)){
            DinamicLayout parentColumn = getColumnFromParentLayouts(layout.getParents());
            if ((getElementType() != null) && getElementType().equals(XLSCommands.XLSCommandRow)){
                Label label = new Label();
                int crossOffsX = getCrossTabOffset(XlsLayoutDirection.ColumnDirection);
                int crossOffsY = getCrossTabOffset(XlsLayoutDirection.RowDirection);
                label.setOriginalReference(outputElement.getTemplateReferences().getClonedReference());
                label.setIterationIndex(outputElement.getIterationIndex());
                label.setReferences(outputElement.getOutputReferences().getCloneWithOffset(-crossOffsX, -crossOffsY));
                label.setValue(outputElement.getCellValue());
                label.setStyleIndex(outputElement.getStyleIndex());
                label.setColumnWidth(layout.getColumnWidth());
                label.setRowHeight(layout.getRowHeight());
                label.setXlsCellType(layout.getXlsCellType());
                label.setPageBreak(layout.getPageBreak());
                //set merge
                if (layout instanceof MergedLayout){
                    label.setMerge(((MergedLayout)layout).cloneMerge());
                }
                //removeOldChildsFromParent(parentRow, label.getReferences());
                //parentRow.getChilds().add(label);
                parentColumn.addChild(label);
                return true;
            }
        }
        return false;
    }
    
    /**
     * apply offset for all childs
     * 
     * @param childs
     * @param offsetX
     * @param offsetY
     * @return child list with applied offset
     * @throws CloneNotSupportedException 
     */
    private Set<Layout> applyNewOffsetForAllChilds(Set<Layout> childs, int offsetX, int offsetY) throws CloneNotSupportedException{
        Set<Layout> result = new TreeSet<Layout>(new TemplateCoordinatesComparator<Layout>());
        for (Layout child : childs){
            Layout newChild = child.clone();
            newChild.setReferences(newChild.getReferences().getCloneWithOffset(offsetX, offsetY));
            newChild.setOriginalReference(child.getOriginalReference().getClonedReference());
            if (newChild instanceof DinamicLayout){
                ((DinamicLayout)newChild).setChilds(applyNewOffsetForAllChilds(((DinamicLayout)newChild).getChilds(), offsetX, offsetY));
            }
            result.add(newChild);
        }
        return result;
    }
    
    /**
     * get row from parent layouts
     * 
     * @param list
     * @return row
     */
    private DinamicLayout getColumnFromParentLayouts(List<DinamicLayout> list){
        for (DinamicLayout dinamicLayout : list) {
            if (dinamicLayout.getCommand().getCommand()==XLSCommands.XLSCommandColumn.getCommand()){
                return dinamicLayout;
            }
        } 
        return null;
    }
    
    /**
     * add new layouts to row parent 
     * 
     * @param layout
     * @param offsetX
     * @param offsetY
     * @return true if label was added to second parent
     * @throws CloneNotSupportedException
     * @throws IOException 
     * @throws CommandStructureException 
     * @throws XLSUnknownSourceException 
     */
    private boolean addNewLayoutForParents(Layout layout, int offsetX, int offsetY) throws CloneNotSupportedException, IOException, CommandStructureException, XLSUnknownSourceException{
        if ((layout.getParents() != null) && (layout.getParents().size() == 2)){
            if (!(layout instanceof UnBoundLayout)){
                if (!((layout.getCommand() == XLSCommands.XLSCommandCell) || (layout.getCommand() == XLSCommands.XLSCommandField))){
                    throw new CommandStructureException("wrongCrosstabStructureInerDinamic", layout.getCommand().getCommand(),layout.getReferences().getReference());
                }
            }
            DinamicLayout parentColumn = getColumnFromParentLayouts(layout.getParents());
            if ((getElementType() != null) && getElementType().equals(XLSCommands.XLSCommandRow)){
                //build cross by row (first step) add copy of layout to parent column 
                Layout newLayout = layout.clone();
                int crossOffsX = getCrossTabOffset(XlsLayoutDirection.ColumnDirection);
                int crossOffsY = getCrossTabOffset(XlsLayoutDirection.RowDirection);
               // newLayout.setOriginalReference(layout.getReferences().getClonedReference());
                newLayout.setOriginalReference(layout.getOriginalReference().getClonedReference());
                newLayout.setIterationIndex(layout.getIterationIndex());
                newLayout.setReferences(layout.getReferences().getCloneWithOffset(offsetX-crossOffsX, offsetY-crossOffsY));
                
                if (newLayout instanceof DinamicLayout){
                    //TODO revisit it
                    Set<Layout> newChildSet = applyNewOffsetForAllChilds(((DinamicLayout)newLayout).getChilds(), offsetX-crossOffsX, offsetY-crossOffsY);
                    ((DinamicLayout)newLayout).setChilds(newChildSet);
                }
                
                //removeOldChildsFromParent(parentRow, newLayout.getReferences());
                //parentRow.getChilds().add(newLayout);
                parentColumn.addChild(newLayout);
                return true;
            } else {
                //build cross by column (last step) all parameters must be ready
                if (layout instanceof DinamicLayout){
                   // boolean isAllParamReady = true;
                    for (SqlParametr parametr : ((DinamicLayout)layout).getParametrs()){
                        if (!parametr.isDataLoaded()){
                            //isAllParamReady = false;
                            //Can not resolve parameter {0} in SQL {1}, cell {2}.
                            throw new XLSUnknownSourceException("unknownSQLParameter", parametr.getOriginalName(), ((DinamicLayout)layout).getSqlName(), layout.getReferences().getReference());
                        }
                    }
                    /*
                    if (!isAllParamReady){
                        throw new OutputValueNotReadyException("cantBePrepared", layout.getReferences().getReference());
                        //Can not resolve parameter {0} in SQL {1}, cell {2}.
                        throw new XLSUnknownSourceException("unknownSQLParameter", parametr.getOriginalName(), sql, layout.getReferences().getReference());
                    }
                    */
                }
            }
        }
        return false;
    }
    
    /**
     * prepare element for output
     * 
     * @param layout
     * @param offsetX
     * @param offsetY
     * @param reference
     * @throws SQLException
     * @throws XlsOutputValueNotFoundException 
     * @throws OutputValueNotReadyException 
     * @throws CloneNotSupportedException
     * @throws XLSConnectionBrokenException 
     * @throws XLSDataLockedException 
     * @throws IOException 
     * @throws CommandStructureException 
     * @throws XLSUnknownSourceException 
     */
    public void prepareElement(int offsetX, int offsetY, XlsReference reference) 
            throws SQLException, XlsOutputValueNotFoundException, CloneNotSupportedException, OutputValueNotReadyException, 
                XLSDataLockedException, XLSConnectionBrokenException, IOException, CommandStructureException, XLSUnknownSourceException{
        sectionOffset.setStartingXOffset(offsetX);
        sectionOffset.setStartingYOffset(offsetY);

        if (layout instanceof DinamicLayout){
            sectionName = ((DinamicLayout)layout).getRegionName();
            List<OutputElement> element = prepareBoundElement((BoundLayout)layout, offsetX, offsetY);
            if (!addNewLayoutForParents(layout, offsetX, offsetY)){
                preparedElements.addAll(element);
            }
            //reset crosstab
            if (layout.getCommand() == XLSCommands.XLSCommandCrossTab){
                ((DinamicLayout)layout).resetChilds();
            }
        } else if ((layout instanceof StaticLayout) && (layout instanceof BoundLayout)){
            //prepare field
            OutputElement outputElement = prepareField((StaticLayout)layout, offsetX, offsetY, reference);
            if (outputElement != null){
                // add output element to result list only if this element havs one parent
                // if element have two parents then add to second parent. XLS-26
                //outputElement.setAfterCrosstab(layout.isAfterCrosstab());
                //outputElement.setUderCrosstab(layout.isUderCrosstab());
                if (!addNewLabelForParents(outputElement, layout)){
                    preparedElements.add(outputElement);
                    checkMerge(outputElement, layout);
                    //set row height & column width
                    if (outputWorkbook.getWorkbook().getCellStyleAt(layout.getStyleIndex()).getWrapText() 
                    		&& outputElement.getCellValue()!=null
                    		&& !outputElement.getCellValue().isEpmpty()
                		&& !((layout instanceof MergedLayout) && (((MergedLayout)layout).getMerge() != null))){//no sence 4 merged cells                    	outputWorkbook.getOuputSheet().setRowHeightDefault(outputElement.getOutputReferences().getRowIndex());
                    } else{
                        outputWorkbook.getOuputSheet().setRowHeight(outputElement.getOutputReferences().getRowIndex(), layout.getRowHeight());
                    }
                    outputWorkbook.getOuputSheet().setColumnWidth((new Integer(outputElement.getOutputReferences().getColIndex()).shortValue()), layout.getColumnWidth());
                    //set column page break
                    if ((layout.getPageBreak() == XlsLayoutDirection.ColumnDirection) || (layout.getPageBreak() == XlsLayoutDirection.AnyDirection)){
                        outputWorkbook.getOuputSheet().getColumnPageBreaks().add((new Integer(outputElement.getOutputReferences().getColIndex()).shortValue()));
                    }
                    //set row page break
                    if ((layout.getPageBreak() == XlsLayoutDirection.RowDirection) || (layout.getPageBreak() == XlsLayoutDirection.AnyDirection)){
                        outputWorkbook.getOuputSheet().getRowPageBreaks().add(new Integer(outputElement.getOutputReferences().getRowIndex()));
                    }
                }
            } else {
                addNewLayoutForParents(layout, offsetX, offsetY);
            }
        } else {
            //prepare label or formula
            OutputElement element = prepareStaticUnBoundELement(layout, offsetX , offsetY);
            if ((element != null) && !addNewLayoutForParents(layout, offsetX, offsetY)){
                //element.setAfterCrosstab(layout.isAfterCrosstab());
                //element.setUderCrosstab(layout.isUderCrosstab());
                preparedElements.add(element);
                checkMerge(element, layout);
                //set row height & column width
                if (outputWorkbook.getWorkbook().getCellStyleAt(layout.getStyleIndex()).getWrapText()
                		&& element.getCellValue()!= null 
                		&& !element.getCellValue().isEpmpty() 
                		&& !((layout instanceof MergedLayout) && (((MergedLayout)layout).getMerge() != null))){//no sence 4 merged cells
                    outputWorkbook.getOuputSheet().setRowHeightDefault(element.getOutputReferences().getRowIndex());
                } else{
                    outputWorkbook.getOuputSheet().setRowHeight(element.getOutputReferences().getRowIndex(), layout.getRowHeight());
                }
                outputWorkbook.getOuputSheet().setColumnWidth((new Integer(element.getOutputReferences().getColIndex()).shortValue()), layout.getColumnWidth());
                //set column page break
                if ((layout.getPageBreak() == XlsLayoutDirection.ColumnDirection) || (layout.getPageBreak() == XlsLayoutDirection.AnyDirection)){
                    outputWorkbook.getOuputSheet().getColumnPageBreaks().add((new Integer(element.getOutputReferences().getColIndex()).shortValue()));
                }
                //set row page break
                if ((layout.getPageBreak() == XlsLayoutDirection.RowDirection) || (layout.getPageBreak() == XlsLayoutDirection.AnyDirection)){
                    outputWorkbook.getOuputSheet().getRowPageBreaks().add(new Integer(element.getOutputReferences().getRowIndex()));
                }
            }
        }
    }
    
    /**
     * chacks if sql vs sqlName available in current context
     * 
     * @param sqlName
     * @return
     */
    private boolean isSqlInContext(String sqlName){
        boolean result = false;
        if ((layout instanceof StaticLayout) && (layout instanceof BoundLayout)){
            //field
            result = ((GenericStaticLayout)layout).isSqlAvailable(sqlName);
        } else if (layout instanceof DinamicLayout){
            //any dynamic
            result = ((GenericDinamicLayout)layout).isSqlAvailable(sqlName);
        }
        return result;
    }
    
    /**
     * get value from result set 
     * 
     * @param layout
     * @param sqlName
     * @param columnName
     * @return value
     * @throws SQLException
     * @throws IOException
     * @throws XLSUnknownSourceException
     * @throws XlsOutputValueNotFoundException
     */
    private XLSCellValue getValueFromContext(String sqlName, String columnName, boolean checkValueDistributor) throws SQLException, IOException, XLSUnknownSourceException, XlsOutputValueNotFoundException{
        XLSCellValue result = null;
        if ((recordSource != null) && sqlName.equals(recordSource.getName())){
            result = recordSource.getField(columnName);
            if (result == null){
                result = new XLSCellValue("");
            }
        } else if (getParent() != null){
            result = getParent().getValueFromContext(sqlName, columnName, checkValueDistributor);
        } else if (checkValueDistributor && (outputWorkbook.getDataCache().getValueDistributor() != null)){
            String key = XlsUtils.constructExternalValue(sqlName, columnName);
            String val = outputWorkbook.getDataCache().getValueDistributor().getValue(key);
            if (val != null){
                result = new XLSCellValue(val, XLSDataType.XLSDataText);
            }
            //null - ValueDistributor has no key
        }
        if (result == null){
            throw new XLSUnknownSourceException("unknownSource", sqlName, columnName);
        }
        return result;
    }

    /**
     * set parameter value from recordSource 
     * 
     * @param parameter
     * @param checkValueDistributor
     *      false if sql exist in current context
     * @throws SQLException
     * @throws IOException
     * @throws XLSUnknownSourceException
     * @throws XlsOutputValueNotFoundException
     */
    private void setParameterFromContext(SqlParametr parameter, boolean checkValueDistributor) throws SQLException, IOException, XLSUnknownSourceException, XlsOutputValueNotFoundException{
        String sqlName = parameter.getSqlName();
        String columnName = parameter.getColumnName();
        XLSCellValue value = null;
        boolean isExternalValue = false;
        if ((recordSource != null) && sqlName.equals(recordSource.getName())){
            //get value form current layout
            value = recordSource.getField(columnName);
            /*
            if (value == null){
                //may be not correct 
                value = "";
            }
            */
        } else if (getParent() != null){
            //look for value in parent layout
            getParent().setParameterFromContext(parameter, checkValueDistributor);
            return;
        } else if (checkValueDistributor && (outputWorkbook.getDataCache().getValueDistributor() != null)){
            //look for value in ValueDistributor
            isExternalValue = true;
            String key = XlsUtils.constructExternalValue(sqlName, columnName);
            String val = outputWorkbook.getDataCache().getValueDistributor().getValue(key);
            value = new XLSCellValue(val, XLSDataType.XLSDataText);
            //null - ValueDistributor has no key
        }
        if (value == null){
            throw new XLSUnknownSourceException("unknownSource", sqlName, columnName);
        } else {
            //set parameter properties
            parameter.setExternalValue(isExternalValue);
            parameter.setParamValue(value.getValue());
            parameter.setDataLoaded(true);
        }
    }

    /**
     * get sql map
     * 
     * @return the sql
     */
    private Map<String, SqlElement> getSql() {
        return outputWorkbook.getDataCache().getSqlMap();
    }

    /**
     * get parent instance
     * 
     * @return the parent
     */
    private XlsPrepareElement getParent() {
        return parent;
    }

    /**
     * set new parent instance
     * 
     * @param parent the parent to set
     */
    private void setParent(XlsPrepareElement parent) {
        this.parent = parent;
    }

    /**
     * get current offset by X
     * 
     * @return the currentOffsetX
     */
    protected int getCurrentOffsetX() {
        return currentOffsetX;
    }

    /**
     * set new current offset by X
     * 
     * @param currentOffsetX the currentOffsetX to set
     */
    protected void setCurrentOffsetX(int currentOffsetX) {
        this.currentOffsetX = currentOffsetX;
    }

    /**
     * get current offset by Y
     * 
     * @return the currentOffsetY
     */
    protected int getCurrentOffsetY() {
        return currentOffsetY;
    }

    /**
     * set new current offset by Y
     * 
     * @param currentOffsetY the currentOffsetY to set
     */
    protected void setCurrentOffsetY(int currentOffsetY) {
        this.currentOffsetY = currentOffsetY;
    }

    /**
     * get parent element type
     * 
     * @return the elementType
     */
    public XLSCommands getElementType() {
        return elementType;
    }

    /**
     * set new element type
     * 
     * @param elementType the elementType to set
     */
    public void setElementType(XLSCommands elementType) {
        this.elementType = elementType;
    }

    /**
     * get sql row
     * 
     * @return the sqlRows
     */
    public int getSqlRows() {
        return sqlRows;
    }

    /**
     * get offset list
     * 
     * @return the offsets
     */
    public List<OffsetElement> getOffsets() {
        return offsets;
    }


    /**
     * get section element merges for dynamic layout 
     * @return merges list
     */
    public List<MergeRegion> getElementMerges() {
        if (elementMerges == null){
            elementMerges = new ArrayList<MergeRegion>();
        }
        return elementMerges;
    }

    /**
     * get section merges for dynamic layout
     * @return merges list
     */
    public List<MergeRegion> getSectionMerges() {
        if (sectionMerges == null){ 
            sectionMerges = new ArrayList<MergeRegion>();
        }
        return sectionMerges;
    }
    
    /**
     * @param merge
     * @param reference
     * @throws CommandStructureException
     * @throws IOException
     */
    private void addSectionMerge(MergeRegion merge, String reference) throws CommandStructureException, IOException{
        if (this.sectionName.equalsIgnoreCase(merge.getSectionName())){
            this.getSectionMerges().add(merge);
        } else if (parent != null){
            parent.addSectionMerge(merge, reference);
        } else {
            throw new CommandStructureException("mergeBySectionHasNoParent", merge.getSectionName(), reference);
        }
    }
    
    /**
     * @param element
     * @param layout
     * @throws CloneNotSupportedException
     * @throws CommandStructureException
     * @throws IOException
     */
    private void checkMerge(OutputElement element, Layout layout) throws CloneNotSupportedException, CommandStructureException, IOException{
        if ((layout instanceof MergedLayout) && (((MergedLayout)layout).getMerge() != null)){
            MergeRegion mr = ((MergedLayout)layout).cloneMerge();
            mr.setFrom(element.getOutputReferences().getRowIndex(), (short) element.getOutputReferences().getColIndex());
            if (mr.getMergeType() == XLSMergeType.XLSMergeStatic){
                outputWorkbook.getOuputSheet().getMerges().add(mr);  
            } else if (mr.getMergeType() == XLSMergeType.XLSMergeCurrent){
                if (parent == null){
                    throw new CommandStructureException("mergeByElemetHasNoParent", layout.getReferences().getReference());
                }
                parent.getElementMerges().add(mr);
            } else if (mr.getMergeType() == XLSMergeType.XLSMergeSection){
                addSectionMerge(mr,layout.getReferences().getReference());
            }
        }
    }

    /**
     * @return the preparedElements
     */
    public List<OutputElement> getPreparedElements() {
        return preparedElements;
    }

    /**
     * @return the sectionOffset
     */
    public SectionOffset getSectionOffset() {
        return sectionOffset;
    }

    /**
     * @return the notReadyFormula
     */
    public List<Formula> getNotReadyFormula() {
        return notReadyFormula;
    }
    
    /**
     * look up crosstab in parents
     * @return XlsPrepareElement
     */
    protected XlsPrepareElement getCurrentCrossTab(){
        XlsPrepareElement result = null;
        if (layout.getCommand() == XLSCommands.XLSCommandCrossTab){
            return this;
        } else if (parent != null){
            result = parent.getCurrentCrossTab();
        }
        return result;
    }
    
    /**
     * get crosstab offset
     * 
     * @param offsetDirection
     * @return offset
     */
    protected int getCrossTabOffset(XlsLayoutDirection offsetDirection){
        int result = 0;
        XlsPrepareElement cross = getCurrentCrossTab();
        if (cross != null){
            if (offsetDirection == XlsLayoutDirection.RowDirection){
                result = cross.getSectionOffset().getStartingYOffset();
            } else if (offsetDirection == XlsLayoutDirection.ColumnDirection){
                result = cross.getSectionOffset().getStartingXOffset();
            }
        }
        return result;
    }

    /**
     * @return the notReadyFormulaInCross
     */
    private List<Formula> getNotReadyFormulaInCross() {
        return notReadyFormulaInCross;
    }
    
}
