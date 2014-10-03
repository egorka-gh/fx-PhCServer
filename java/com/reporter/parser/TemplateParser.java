/**
 * 
 */
package com.reporter.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.hssf.util.Region;

import com.mavaris.webcaravella.element.ValueDistributor;
import com.reporter.data.XLSUnknownSourceException;
import com.reporter.document.exception.XlsReferenceException;
import com.reporter.document.layout.BoundLayout;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.MergedLayout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.document.layout.XlsReference;
import com.reporter.document.layout.comparators.CoordinatesComparatorOriginalFlow;
import com.reporter.document.layout.impl.Field;
import com.reporter.document.layout.impl.Label;
import com.reporter.document.layout.impl.Sheet;
import com.reporter.parser.exceptions.CommandStructureException;
import com.reporter.parser.exceptions.CommandSyntaxException;
import com.reporter.parser.exceptions.XLSRegionNameException;
import com.reporter.utils.ReferencesUtils;
import com.reporter.utils.XlsUtils;
import com.reporter.xml.exceptions.XMLTemplateException;


/**
 * @author Administrator
 * 
 */
/**
 * @author igor zadenov
 *
 */
public class TemplateParser {

    private static HashMap<String,HSSFName> regionNames = null;

    private static HashMap<String,String> subCommands = null;

    private static Map<String,CrossTab> crossTabs = new HashMap<String,CrossTab>();
    
    private List<Layout> defaultDynamics = new ArrayList<Layout>(); 
    
    private void fillRegionNames(HSSFWorkbook workbook){
        regionNames = new HashMap<String,HSSFName>(); 
        for (int i = 0; i < workbook.getNumberOfNames(); i++) {
            addRegionName(workbook.getNameAt(i));
        }
    }
    
    private void addRegionName(HSSFName regionName){
        if (regionNames == null){
            regionNames = new HashMap<String,HSSFName>(); 
        }
        //TODO check broken refs
        if (!regionName.getReference().startsWith("#")){// check if ref is broken
            String key = regionName.getSheetName();
            //if ((key != null) && (key.trim().length()>0) && regionName.getReference().length()>0){
            if ((key != null) && (key.trim().length()>0)){
                key = key.toLowerCase().trim() + "!" +regionName.getNameName().toLowerCase().trim(); 
                regionNames.put(key, regionName);
            }
        }
    }

    /**
     * @param sheetName
     * @param regionName
     * @return named region reference
     */
    private String getNamedRegionReference(String sheetName, String regionName){
        String result = "";
        if (regionNames != null){
            HSSFName ref = null;
            String key = sheetName.toLowerCase().trim() + "!" +regionName.toLowerCase().trim();
            ref =regionNames.get(key); 
            if (ref != null){
                result = ref.getReference();
            }
        }
        return result;
    }
    
    /**
     * add finded parents to result list
     * 
     * @param result
     * @param parents
     * @throws CommandStructureException
     * @throws IOException 
     */
    private void addNewParentsToList(List<DinamicLayout> result, List<DinamicLayout> parents) throws CommandStructureException, IOException{
        //TODO rewrite exceptions
        // add elements to result if this element not present in result
        for (DinamicLayout parent : parents){
            if (!result.contains(parent)){
                result.add(parent);
            }
        }
        
        // validate result list
        boolean isRowHave = false;
        boolean isColumnHave = false;
        boolean isOtherHave = false;
        for (DinamicLayout res : result){
            if (res.getCommand().equals(XLSCommands.XLSCommandRow)){
                if (isRowHave){
                    throw new CommandStructureException("Cannot find parent region. Two Row cannot be parents.");
                }
                isRowHave = true;
            } else if (res.getCommand().equals(XLSCommands.XLSCommandColumn)){
                if (isColumnHave){
                    throw new CommandStructureException("Cannot find parent region. Two Column cannot be parents.");
                }
                isColumnHave = true;
            } else {
                if (isOtherHave){
                    throw new CommandStructureException("Cannot find parent region");
                }
                isOtherHave = true;
            }
        }
        
        if (isOtherHave && (isRowHave || isColumnHave)){
            throw new CommandStructureException("Cannot find parent region");
        }
    }

    /**
     * get parents for element
     * 
     * @param components
     * @param child
     * @return list with parents
     * @throws CommandStructureException
     * @throws IOException 
     */
    private List<DinamicLayout> getParents(Set<Layout> components, Layout child) throws CommandStructureException, IOException, XlsReferenceException{
        ReferencesUtils referencesUtils = new ReferencesUtils();
    	List<DinamicLayout> result = new ArrayList<DinamicLayout>();
        List<DinamicLayout> tempParent = new ArrayList<DinamicLayout>();
        
        if ((components == null) || (components.size() == 0)){
            return  result;
        }
        if (child.getCommand() == XLSCommands.XLSCommandSheet) {
            return  result;
        }
        CellReference cellRef = child.getReferences().getCellReference();
        for (Layout comp : components){
            if (comp instanceof DinamicLayout){
                RangeAddress ra = comp.getReferences().getRangeAddress();
                if (referencesUtils.isCellInRange(ra, cellRef)){
                    tempParent = getParents(((DinamicLayout)comp).getChilds(), child);
                    if (tempParent.size() == 0){
                        tempParent.add((DinamicLayout)comp);
                    }
                }
                if (tempParent.size() > 0){
                    addNewParentsToList(result, tempParent);
                }
            }
        }
        //TODO chek regions enclosing and intersection
        return result;
    }

    /**
     * checks regions enclosing and intersection
     * 
     * @param parents that holds child 
     * @param child layout
     * @throws CommandStructureException
     * @throws IOException 
     */
    private void checkRegionEnclosing(List<DinamicLayout> parents, Layout child) throws CommandStructureException, IOException, XlsReferenceException{
        ReferencesUtils referencesUtils = new ReferencesUtils();
    	RangeAddress subRange = child.getReferences().getRangeAddress();
        //check enclosing
        for (DinamicLayout dl : parents){
            RangeAddress range = dl.getReferences().getRangeAddress();
            if (!referencesUtils.isRangeInRange(subRange, range)){
                throw new CommandStructureException("wrongRegionsEnclosing",child.getReferences().getReference(),dl.getReferences().getReference());
            }
        }
        //check intersection (crosstab)
        if (parents.size()>1){
            DinamicLayout cl;
            DinamicLayout rw;
            if (parents.get(0).getCommand() == XLSCommands.XLSCommandRow){
                rw = parents.get(0);
                cl = parents.get(1);
            } else {
                rw = parents.get(1);
                cl = parents.get(0);
            }
            if (!referencesUtils.isColumnCrossRow(cl.getReferences().getReference(), rw.getReferences().getReference())){
                throw new CommandStructureException("wrongRegionsIntersection",cl.getReferences().getReference(),rw.getReferences().getReference());
            }
        }
    }
    
    /**
     * gets all unique intersections (at the bottom level from childs)
     * 
     * @param childs to process (pass frame childs) 
     */
    private HashMap<String,CrossingLayouts> getCrossingLayouts(List<Layout> childs){
        HashMap<String,CrossingLayouts> result = new HashMap<String,CrossingLayouts>();
        for (Layout lt : childs){
            if (lt.getParents().size()>1){
                DinamicLayout dlRw;
                DinamicLayout dlCl;
                if (lt.getParents().get(1).getCommand() == XLSCommands.XLSCommandColumn){
                    dlRw = lt.getParents().get(0);
                    dlCl = lt.getParents().get(1);
                } else{
                    dlRw = lt.getParents().get(1);
                    dlCl = lt.getParents().get(0);
                }
                String key = dlCl.getElementName() + "!" + dlRw.getElementName(); 
                result.put(key, new CrossingLayouts(dlCl,dlRw)); 
            }
            if (lt instanceof DinamicLayout){
                HashMap<String,CrossingLayouts> subResult = getCrossingLayouts(((DinamicLayout)lt).getChildList());
                result.putAll(subResult);
            }
        }
        return result;
    }
    
    /**
     * check all crossTabs (on current sheet) structure 
     * and build cross tab in Frame
     * 
     * @param childs Frame layouts
     * @throws CommandStructureException 
     * @throws IOException 
     */
    private void checkCrossTabs(List<Layout> childs) throws CommandStructureException, IOException, XlsReferenceException{
        ReferencesUtils referencesUtils = new ReferencesUtils();
    	HashMap<String,CrossingLayouts> intersections =getCrossingLayouts(childs);
        for (CrossingLayouts intersection : intersections.values()){
            DinamicLayout col =intersection.getColumn();
            DinamicLayout row =intersection.getRow();
            //get all row/column from bottom layout to root row/column
            List<DinamicLayout> allRows = getCrossTabLayouts(row,col);
            List<DinamicLayout> allCols = getCrossTabLayouts(col,row);
            //check root row & root column parents
            DinamicLayout rootRow = allRows.get(allRows.size()-1);
            DinamicLayout rootColumn = allCols.get(allCols.size()-1);
            if (rootRow.getParents().get(0) != rootColumn.getParents().get(0)){
                throw new CommandStructureException("wrongCrosstabParentsDiffer",
                        rootRow.getReferences().getReference(),rootColumn.getReferences().getReference(),
                        rootRow.getParents().get(0).getReferences().getReference(),rootColumn.getParents().get(0).getReferences().getReference());
            }
            //check if cross tab already generated
            String key = rootRow.getReferences().getColIndex() + "~" + rootColumn.getReferences().getRowIndex();
            CrossTab crossTab = crossTabs.get(key);
            if (crossTab == null){
                //chek extra col/row in crosstab
                for (CrossTab ct : crossTabs.values()){
                    if ((ct.getRootRow() == rootRow) || (ct.getRootColumn() == rootColumn)){
                        //wrongCrosstabRoots exception
                        if (ct.getRootRow() != rootRow){
                            throw new CommandStructureException("wrongCrosstabRoots","row",ct.getReferences().getReference(), rootRow.getReferences().getReference());
                        }
                        if (ct.getRootColumn() != rootColumn){
                            throw new CommandStructureException("wrongCrosstabRoots","column",ct.getReferences().getReference(), rootColumn.getReferences().getReference());
                        }
                    }
                }
                //create cross tab
                crossTab = new CrossTab(rootRow, rootColumn);
                //remove rootRow & rootColumn from old parent
                DinamicLayout pr = rootRow.getParents().get(0);
                pr.deleteChild(rootRow);
                pr.deleteChild(rootColumn);
                //change rootRow & rootColumn parents
                //crossTab.getChilds().add(rootRow);
                //crossTab.getChilds().add(rootColumn);
                rootRow.getParents().clear();
                rootRow.getParents().add(crossTab);
                rootColumn.getParents().clear();
                rootColumn.getParents().add(crossTab);
                //add rootRow & rootColumn into crossTab childs 
                //check/add other statics inside crossTab region (add into crossTab childs)
                //check other dynamics inside crossTab region (exception)
                checkCrossTabFrame(crossTab, pr, rootRow, rootColumn);
                //add cross to parent
                pr.getChilds().add(crossTab);
                crossTab.getParents().add(pr);
                //set crosstab intersection region
                crossTab.setIntersection(rootRow.getReferences().getRowIndex(), rootColumn.getReferences().getColIndex(),
                        rootRow.getReferences().getRowNumFromRegionReference(), rootColumn.getReferences().getColNumFromRegionReference());
                crossTabs.put(key, crossTab);
            }

            Map<String, XlsReference> colSql = new HashMap<String, XlsReference>();
            Map<String, XlsReference> rowSql = new HashMap<String, XlsReference>();
            //check intersection & save data sources
            for (DinamicLayout cl : allCols){
                colSql.put(cl.getSqlName(), cl.getReferences());
                for (DinamicLayout rw : allRows){
                    rowSql.put(rw.getSqlName(), rw.getReferences());
                    if (!referencesUtils.isColumnCrossRow(cl.getReferences().getReference(), rw.getReferences().getReference())){
                        //System.out.println(cl.getReferences());
                        //System.out.println(rw.getReferences());
                        throw new CommandStructureException("wrongRegionsIntersection",cl.getReferences().getReference(),rw.getReferences().getReference());
                    }
                }
            }
            //check sql data source
            for (String sqlName : colSql.keySet()){
                if (rowSql.containsKey(sqlName)){
                    //row & column cant use same data source
                    throw new CommandStructureException("crosstabDataConflict", rowSql.get(sqlName).getReference(), colSql.get(sqlName).getReference(), sqlName);
                }
            }
        }
    }
    
    /**
     * scan crosstab parent layout childs and if it inside crosstab
     * move it to crosstab
     * 
     * @param crossTab
     * @param parent
     * @param rootRow
     * @param rootColumn
     * @throws CommandStructureException
     * @throws IOException
     */
    private void checkCrossTabFrame(CrossTab crossTab, DinamicLayout parent, Layout rootRow, Layout rootColumn) throws CommandStructureException, IOException{
        //set ws original ordered layouts (by row then by column)
        Set<Layout> newChilds = new TreeSet<Layout>(new CoordinatesComparatorOriginalFlow<Layout>());
        newChilds.add(rootRow);
        newChilds.add(rootColumn);

        int crossTop = crossTab.getReferences().getRowIndex();
        int crossBot = crossTop + crossTab.getReferences().getRowNumFromRegionReference() - 1;
        int crossLeft = crossTab.getReferences().getColIndex();
        int crossRight = crossLeft + crossTab.getReferences().getColNumFromRegionReference() - 1;
        List<Layout> parentChilds = parent.getChildList();
        for (Layout lt : parentChilds){
            //exclude crosstab,  crosstab root row/column 
            if ((lt != crossTab) && (lt != crossTab.getRootColumn()) && (lt != crossTab.getRootRow())){
                int ltTop = lt.getReferences().getRowIndex();
                int ltBot = ltTop + lt.getReferences().getRowNumFromRegionReference() - 1;
                int ltLeft = lt.getReferences().getColIndex();
                int ltRight = ltLeft + lt.getReferences().getColNumFromRegionReference() - 1;
                //has intersection?
                if ((ltTop <= crossBot) && (ltBot >= crossTop) && 
                        (ltLeft <= crossRight) && (ltRight >= crossLeft)){
                    //is not cross inside lt? (exclude all parents)
                    if (!((ltTop <= crossTop) && (ltBot >= crossBot) && 
                            (ltLeft <= crossLeft) && (ltRight >= crossRight))){
                        //is lt inside cross
                        if ((ltTop >= crossTop) && (ltBot <= crossBot) && 
                                (ltLeft >= crossLeft) && (ltRight <= crossRight)){
                            //if cell/field/label/formula add to cross
                            //else - exception
                            XLSCommands ltType =lt.getCommand(); 
                            if ((ltType == XLSCommands.XLSCommandCell) || (ltType == XLSCommands.XLSCommandField)
                                    || (ltType == XLSCommands.XLSCommandFormula) || (ltType == XLSCommands.XLSCommandLabel)){
                                Layout pr = lt.getParents().get(0);
                                if (pr != null){
                                    ((DinamicLayout)pr).deleteChild(lt);
                                }
                                lt.getParents().clear();
                                lt.getParents().add(crossTab);
                                newChilds.add(lt);
                                //crossTab.getChilds().add(lt);
                            } else {
                                throw new CommandStructureException("wrongCrosstabStructureDinamicDiffer",ltType.getCommand(),"cross table", lt.getReferences().getReference(), crossTab.getReferences().getReference());
                            }
                        } else{
                            //cross tab intersect lt - exception
                            throw new CommandStructureException("wrongCrosstabStructureRegionConflict",lt.getCommand().getCommand(),"cross table", lt.getReferences().getReference(), crossTab.getReferences().getReference());
                        }
                    }
                }
            }
        }
        //add childs to crosstab
        crossTab.getChilds().addAll(new ArrayList<Layout>(newChilds));
    }
    
    /**
     * gets all crosstab layouts from bottom layout
     * 
     * @param fromLayout
     * @param crossLayout bottom crossing layout
     * @throws CommandStructureException 
     * @throws IOException 
     */
    private List<DinamicLayout> getCrossTabLayouts(DinamicLayout fromLayout, DinamicLayout crossLayout) throws CommandStructureException, IOException, XlsReferenceException{
        ReferencesUtils referencesUtils = new ReferencesUtils();
    	List<DinamicLayout> result = new ArrayList<DinamicLayout>();
        DinamicLayout chkLt = fromLayout;
        while (chkLt != null){
            //TODO add check if layout has one dynamic child ws same command
            Layout childDynamic = null;
            for (Layout lt : chkLt.getChildList()){
                //check if child has same command 
                if ((lt instanceof DinamicLayout) && (((DinamicLayout)lt).getCommand() != XLSCommands.XLSCommandCell)){
                    if (((DinamicLayout)lt).getCommand() != chkLt.getCommand()){
                        throw new CommandStructureException("wrongCrosstabStructureDinamicDiffer",chkLt.getCommand().getCommand(),((DinamicLayout)lt).getCommand().getCommand(),chkLt.getReferences().getReference(),lt.getReferences().getReference());
                    }
                    /*
                    if (!result.contains((DinamicLayout)lt)){
                        result.add((DinamicLayout)lt);
                    }
                    */
                    //check if layout has only one dynamic child ws same command
                    if (childDynamic != null){
                        throw new CommandStructureException("wrongCrosstabLevel",chkLt.getCommand().getCommand(),childDynamic.getReferences().getReference(), lt.getReferences().getReference());
                    } else {
                        childDynamic = lt;
                        result.add((DinamicLayout)lt);
                    }
                }
            }
            result.add(chkLt);
            List<DinamicLayout> parents = chkLt.getParents();
            if (parents.size()==0){
                // top layout
                chkLt = null; 
            } else if (parents.size()>1){
                //Row & Column can't has two parents
                throw new CommandStructureException("Wrong crosstab structure, "+ chkLt.getCommand().getCommand()+" has two parents. Region: "+chkLt.getReferences());
            } else {
                DinamicLayout parent = parents.get(0);
                if (parent.getCommand() == XLSCommands.XLSCommandSheet){
                    //top layout (cross layout at sheet level)
                    chkLt = null;
                } else if (referencesUtils.isRangeInRange(crossLayout.getReferences().getReference(), parent.getReferences().getReference())){
                    //top layout (cross layout inside parent)
                    //TODO childs not checked !! Allow only one pair crossing layouts?
                    chkLt = null;
                } else if (parent.getCommand() != chkLt.getCommand()){
                    //check if parent has same type
                    throw new CommandStructureException("Wrong crosstab structure, "+ chkLt.getCommand().getCommand()+" inside " + parent.getCommand().getCommand()+". Regions: "+chkLt.getReferences()+"; "+parent.getReferences());
                } else {
                    chkLt = parent;
                }
            }
        }
        return result;
    }
    
	/**
	 * @param workbook
	 * @return row
	 * @throws XMLTemplateException
	 * @throws CommandStructureException 
     * @throws CommandSyntaxException
	 * @throws XLSRegionNameException 
	 * @throws IOException 
	 * @throws XlsReferenceException 
	 * @throws XLSUnknownSourceException 
	 */
	//public static List<Frame> parseDocument(HSSFWorkbook workbook)
    public List<Sheet> parseDocument(HSSFWorkbook workbook, ValueDistributor valueDistributor, Map<String, Integer> keepSheetList)
			throws XMLTemplateException, CommandStructureException, CommandSyntaxException, XLSRegionNameException, IOException, XlsReferenceException, XLSUnknownSourceException {
        
        List<Sheet> results = new ArrayList<Sheet>();
		// Getting All names from workbook
        fillRegionNames(workbook);
        int SHEET_FOR_PARSE = 1;

		for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
//        for (int sheetIndex = SHEET_FOR_PARSE; sheetIndex <= SHEET_FOR_PARSE; sheetIndex++) {

            crossTabs.clear();
            defaultDynamics.clear();
			// Getting current sheet
			HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
			// Getting current sheet name
			String sheetName = workbook.getSheetName(sheetIndex);
//            Sheet result = new Sheet(sheetName);

            /* preprocess sheet. 
             * transfer original meges to commands
             */
            subCommands = new HashMap<String,String>();
            if (preprocessSheet(sheet)){ 
                //parse sheet    
                Sheet result = new Sheet(sheetName);
    			Iterator<HSSFRow> rowIterator = sheet.rowIterator();
    			while (rowIterator.hasNext()) {
    				HSSFRow row = rowIterator.next();
                    short rowHeight = row.getHeight();
    				Iterator<HSSFCell> cellIterator = row.cellIterator();
    				while (cellIterator.hasNext()) {
    					HSSFCell cell = (HSSFCell) cellIterator.next();
                        short columnWidth = sheet.getColumnWidth(cell.getCellNum());
    					HSSFComment comment = cell.getCellComment();
                        String com = "";
                        if (comment != null) {
                            com = comment.getString().getString();
                        }
                        //Layout cellTemp = null;
                        String ref = sheetName + "!" + new CellReference(row.getRowNum(), cell.getCellNum(), true, true);
                        String subCommand = getSubCommand(row.getRowNum(), cell.getCellNum());
                        //generate layout
                        CommentParser commentParser = new CommentParser();
                        List<Layout> layouts = commentParser.getLayoutFromCell(cell, new XlsReference(ref), subCommand);
                        //statik (Label/Field/Formula) is always last
                        saveCellAttribute(layouts, cell, rowHeight, columnWidth);
                        checkNamedRegion(layouts, workbook, sheet, sheetName, row, cell, com, ref);
                        Layout layoutTemp = combineLayout(layouts);
                        if (layoutTemp.getCommand() == XLSCommands.XLSCommandSheet){
                            //sheet command detected
                            result = (Sheet) layoutTemp;
                        }
                        //look for parents
                        if (layoutTemp.getCommand() != XLSCommands.XLSCommandSheet){//exclude sheet
                            List<DinamicLayout> parents = getParents(result.getChilds(),layoutTemp);
                            if ((parents.size()>1) && (layoutTemp instanceof DinamicLayout) && (layoutTemp.getCommand() != XLSCommands.XLSCommandCell)){
                                throw new CommandStructureException("wrongCrosstabStructureInerDinamic", ((DinamicLayout)layoutTemp).getCommand().getCommand(),layoutTemp.getReferences().getReference());
                            }
                            checkRegionEnclosing(parents, layoutTemp);
                            if ((parents != null) && (parents.size()>0)){ 
                                for (DinamicLayout pr : parents){
                                    pr.getChilds().add(layoutTemp);
                                    layoutTemp.getParents().add(pr);
                                }
                            } else {
                                result.getChilds().add(layoutTemp);
                                layoutTemp.getParents().add(result);
                            }
                        }
    				}
    			}
                //check dynamics ws default region
                checkDefaultDynamics();
                //check fields sql source
                checkFieldsSqlSource(result, valueDistributor);
                //check CrossTabs structure
                checkCrossTabs(new ArrayList<Layout>(result.getChilds()));
                results.add(result);
            } else {
                //keep unprocessed
                keepSheetList.put(sheetName, sheetIndex);
            }
		}
		return results;
	}
    
    /**
     * preproccess sheet
     * check if keep sheet unprocessed (KeepUnprocessed command at first cell)
     * return false if sheet has KeepUnprocessed command
     * convert merges to commands
     * check/add rows & cells to process pagebreaks
     * 
     * @param sheet
     */
    private boolean preprocessSheet(HSSFSheet sheet){
        //check comment in first cell
        HSSFComment comment = sheet.getCellComment( 0, 0);
        String commentStr = "";
        if (comment != null) {
            commentStr = comment.getString().getString().trim();
        }
        if (commentStr.length() > 0) {
            //remove all after ';'
            commentStr = commentStr.split(XLSCommands.XLSCommandDelemiter.getCommand())[0];
            //check is it starts ws '='
            if (commentStr.lastIndexOf(XLSCommands.XLSCommandStart.getCommand())==0){
                //remove "="
                commentStr = commentStr.substring(1).trim().toLowerCase();
                //at last compare ws XLSKeepUnprocessed command
                if ((commentStr.length() > 0) && (commentStr.equals(XLSCommands.XLSKeepUnprocessed.getCommand()))){
                    return false;
                }
            }
        }
        // POI cann't add new cell comments so use map of sub comands 
        //process Row page Breaks
        int[] pageBreaks = sheet.getRowBreaks();
        if ((pageBreaks != null)){
            for (int i = 0; i < pageBreaks.length; i++){
                //check/add cell at first column in each row ws page break
                checkCreateCell(sheet, pageBreaks[i], (short)0);
                //create subcommand
                String key = pageBreaks[i] + "~" + 0;
                String subCommand = XLSCommands.XLSCommandStart.getCommand() + XLSCommands.XLSCommandRowPageBreak.getCommand() + XLSCommands.XLSCommandDelemiter.getCommand();
                addSubCommand(key, subCommand);
            }
        }
    //process column page Breaks
        short[] colPageBreaks = sheet.getColumnBreaks();
        if ((colPageBreaks != null) && (colPageBreaks.length>0)){
            for (int i = 0; i < colPageBreaks.length; i++){
                //check/add cells in first row
                checkCreateCell(sheet, 0, colPageBreaks[i]);
                //create subcommand
                String key = 0 + "~" + colPageBreaks[i];
                String subCommand = XLSCommands.XLSCommandStart.getCommand() + XLSCommands.XLSCommandColumnPageBreak.getCommand() + XLSCommands.XLSCommandDelemiter.getCommand();
                addSubCommand(key, subCommand);
            }
        }
        
        //transfer sheet merges to static merge command
        for (int i = 0; i < sheet.getNumMergedRegions(); i++){
            Region rg = sheet.getMergedRegionAt(i);
            checkCreateCell(sheet, rg.getRowFrom(), rg.getColumnFrom());
            String subCommand = "";
            if (rg.getColumnFrom() < rg.getColumnTo()){
                subCommand = subCommand + " " + XLSCommands.XLSCommandStart.getCommand() + XLSCommands.XLSCommandHMerge.getCommand();
                int wd = rg.getColumnTo()-rg.getColumnFrom()+1;
                subCommand = subCommand + "(" + wd + ")" + XLSCommands.XLSCommandDelemiter.getCommand();
            }
            if (rg.getRowFrom() < rg.getRowTo()){
                subCommand = subCommand + " " + XLSCommands.XLSCommandStart.getCommand() + XLSCommands.XLSCommandVMerge.getCommand();
                int ht = rg.getRowTo()-rg.getRowFrom()+1;
                subCommand = subCommand + "(" + ht + ")" + XLSCommands.XLSCommandDelemiter.getCommand();
            }
            if (subCommand.length()>0){
                String key =rg.getRowFrom() + "~" + rg.getColumnFrom();
                addSubCommand(key, subCommand);
            }
        }
        return true;
    }
    
    /**
     * create blank cell if not exist
     *  
     * @param sheet
     * @param rowNum
     * @param columnNum
     */
    private void checkCreateCell(HSSFSheet sheet, int rowNum, short columnNum){
        HSSFRow row = sheet.getRow(rowNum);
        if (row == null){
            row = sheet.createRow(rowNum);
        }
        HSSFCell cell = row.getCell(columnNum);
        if (cell == null){
            cell = row.createCell(columnNum);
            cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        }
    }
    
    /**
     * save subcommand in map
     * 
     * @param key
     * @param subCommand
     */
    private void addSubCommand(String key, String subCommand){
        String currCmd = subCommands.get(key);
        if (currCmd != null){
            subCommand = currCmd + " " + subCommand; 
        } else {
            subCommands.put(key, subCommand);
        }
    }

    /**
     * get subcommand from map
     * 
     * @param row
     * @param column
     * @return
     */
    private String getSubCommand(int row, int column){
        String key = row + "~" + column;
        String currCmd = subCommands.get(key);
        return (currCmd != null)? currCmd : ""; 
    }

    /**
     * saves xls cell style, height & width
     *  
     * @param layouts
     * @param cell
     * @param rowHeight
     * @param columnWidth
     */
    private void saveCellAttribute(List<Layout> layouts, HSSFCell cell, short rowHeight, short columnWidth){
        for (Layout lt : layouts){
            lt.setStyleIndex(cell.getCellStyle().getIndex());
            lt.setRowHeight(rowHeight);
            lt.setColumnWidth(columnWidth);
        }
    }

    /**
     * check/create & set named region for dynamic layouts
     * 
     * @param layouts
     * @param workbook
     * @param sheet
     * @param sheetName
     * @param row
     * @param cell
     * @param comment
     * @param address
     * @throws XLSRegionNameException
     * @throws IOException
     * @throws XMLTemplateException
     * @throws CommandStructureException
     * @throws CommandSyntaxException
     * @throws XlsReferenceException
     */
    private void checkNamedRegion(List<Layout> layouts, HSSFWorkbook workbook, HSSFSheet sheet, String sheetName,HSSFRow row, HSSFCell cell, String comment, String address) throws XLSRegionNameException, IOException, XMLTemplateException, CommandStructureException, CommandSyntaxException, XlsReferenceException{
        ReferencesUtils referencesUtils = new ReferencesUtils();
        //resolve named range 
        for (Layout lt : layouts){
            if ((lt instanceof DinamicLayout) && (lt.getCommand() != XLSCommands.XLSCommandSheet)){
                if (((DinamicLayout)lt).isRegionSpecified()){
                    //resolve region address
                    String regRef = getNamedRegionReference(sheetName, ((DinamicLayout)lt).getRegionName());
                    if (regRef.length()>0){
                        lt.setReferences(new XlsReference(regRef));
                    } else {
                        throw new XLSRegionNameException("regionNameNotExists",((DinamicLayout)lt).getRegionName(),comment,address);
                    }
                    if (!referencesUtils.isCellTopOfRange(address, regRef)){
                        //XLS-48
                        throw new XLSRegionNameException("regionAddressConflict",((DinamicLayout)lt).getRegionName(),address,comment);
                    }
                } else {
                    /* uder refactoring
                    // create new Region
                    String regName = createRegion(workbook, (BoundLayout)lt, sheet, sheetName, row, cell);
                    ((DinamicLayout)lt).setRegionName(regName);
                    lt.setReferences(new XlsReference(getNamedRegionReference(sheetName, regName)));
                    */
                    //set dummy RegionName
                    ((DinamicLayout)lt).setRegionName(lt.getReferences().getReference());
                    ((DinamicLayout)lt).hasDummyRegionName(true);
                    //save for postprocessing
                    defaultDynamics.add(lt);
                }
            }
        }
    }

    /**
     * combine layout list from getLayoutFromCell into layout 
     * 
     * @param layouts
     * @return
     * @throws CommandStructureException
     * @throws IOException
     */
    private Layout combineLayout(List<Layout> layouts) throws CommandStructureException, IOException{
        /*
        //for debug chek template wsout data src (without dinamiks)
        {
            Layout child = null;
            for (Layout lt : layouts){
                if ((lt.getCommand() == XLSCommands.XLSCommandField) || (lt.getCommand() == XLSCommands.XLSCommandLabel)){
                    //only one static expected
                    child = lt; 
                }
            }
            if (child.getCommand() == XLSCommands.XLSCommandField){
                Label label = new Label(child.getReferences());
                label.setValue("Field - " + ((Field) child).getSqlName() + "." +  ((Field) child).getColumnName());
                label.setStyleIndex(child.getStyleIndex());
                label.setXlsCellType(child.getXlsCellType());
                label.setColumnWidth(child.getColumnWidth());
                label.setRowHeight(child.getRowHeight());
                label.setPageBreak(child.getPageBreak());
                //set merge
                if (child instanceof MergedLayout){
                    try {
                        label.setMerge(((MergedLayout)child).cloneMerge());
                    } catch (CloneNotSupportedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                child = label;
            }
            return child;
        }
        */
        if (layouts.size() == 1){
            return layouts.get(0);
        }
        Layout result = null;
        Layout child = null;
        List<DinamicLayout> dynamics = new ArrayList<DinamicLayout>();
        
        //detect sheet layout and static layout (fild or label) 
        for (Layout lt : layouts){
            if (lt.getCommand() == XLSCommands.XLSCommandSheet){
                //only one sheet expected
                result = lt;
            } else if ((lt.getCommand() == XLSCommands.XLSCommandField) || (lt.getCommand() == XLSCommands.XLSCommandLabel)){
                //only one static expected
                child = lt; 
            } else {
                dynamics.add((DinamicLayout) lt);
            }
        }
        
        //order dynamic by region size
        List<DinamicLayout> ordered = new ArrayList<DinamicLayout>();
        while (dynamics.size()>0){
            //select layout ws max region
            DinamicLayout maxLt = null;
            for (DinamicLayout dlt : dynamics){
                if ((maxLt == null) || 
                        ((maxLt.getReferences().getRowNumFromRegionReference() < dlt.getReferences().getRowNumFromRegionReference()) ||
                        (maxLt.getReferences().getColNumFromRegionReference() < dlt.getReferences().getColNumFromRegionReference()))){
                    maxLt = dlt;
                }
            }
            ordered.add(maxLt);
            dynamics.remove(maxLt);
        }
        if (ordered.size() == 0){
            if (result != null){
                ((DinamicLayout) result).getChilds().add(child);
                child.getParents().add((DinamicLayout) result);
            } else {
                result = child;
            }
        } else {
            //add child to bottom layout
            ordered.get(ordered.size()-1).getChilds().add(child);
            child.getParents().add(ordered.get(ordered.size()-1));
            //check & combine
            for (int i = 1; i < ordered.size(); i++) {
                DinamicLayout ltChild =ordered.get(i);
                DinamicLayout ltParent =ordered.get(i-1);
                //check enclosing
                ReferencesUtils referencesUtils = new ReferencesUtils();
                RangeAddress subRange = ltChild.getReferences().getRangeAddress();
                RangeAddress range = ltParent.getReferences().getRangeAddress();
                if (!referencesUtils.isRangeInRange(subRange, range)){
                    throw new CommandStructureException("wrongRegionsEnclosing",ltChild.getReferences().getReference(),ltParent.getReferences().getReference());
                }
                ltParent.getChilds().add(ltChild);
                ltChild.getParents().add(ltParent);
            }
            if (result != null){
                ((DinamicLayout) result).getChilds().add(ordered.get(0));
                ordered.get(0).getParents().add((DinamicLayout) result);
            } else {
                result = ordered.get(0);
            }
            
        }
        return result;
    }
    
    /**
     * checks & build dynamic layouts ws default region
     */
    private void checkDefaultDynamics(){
        while (defaultDynamics.size()>0){
            Layout lt = defaultDynamics.get(0);
            defaultDynamics.remove(0);
            //don't expand cell 
            if (lt.getCommand() != XLSCommands.XLSCommandCell){
                DinamicLayout parent = lt.getParents().get(0);
                Layout nextLt = nextInDefaultRegion(parent, lt, ((DinamicLayout)lt).getDirection());
                int resize =0;
                while (nextLt != null){
                    resize++;
                    //move next layout to current layout childs
                    parent.deleteChild(nextLt);
                    nextLt.getParents().clear();
                    nextLt.getParents().add((DinamicLayout) lt);
                    ((DinamicLayout) lt).getChilds().add(nextLt);
                    //look for next layout
                    nextLt = nextInDefaultRegion(parent, nextLt, ((DinamicLayout)lt).getDirection());
                }
                if (resize != 0){
                    //resize reference
                    if (((DinamicLayout)lt).getDirection() == XlsLayoutDirection.RowDirection){
                        lt.getReferences().grow(0, resize);
                    } else {
                        lt.getReferences().grow(resize, 0);
                    }
                }
            }
        }
    }
    
    /**
     * look for next layout to connect to default region
     * 
     * @param parent
     * @param afterLayout
     * @param direction
     * @return
     */
    private Layout nextInDefaultRegion(DinamicLayout parent, Layout afterLayout, XlsLayoutDirection direction){
        Layout result = null;
        int currColumn = afterLayout.getReferences().getColIndex();
        int currRow = afterLayout.getReferences().getRowIndex();
        for (Layout lt : parent.getChildList()){
            if (lt != afterLayout){
                if (direction == XlsLayoutDirection.RowDirection){
                    //finde next in row
                    if ((lt.getReferences().getColIndex() == currColumn + 1) && (lt.getReferences().getRowIndex() == currRow)){
                        result = lt;
                        break;
                    }
                } else {
                    //finde next in column
                    if ((lt.getReferences().getColIndex() == currColumn) && (lt.getReferences().getRowIndex() == currRow+1)){
                        result = lt;
                        break;
                    }
                }
            }
        }
        if (result != null){
            //next found 
            //check it
            XLSCommands nextCommand = result.getCommand();
            if (nextCommand  == XLSCommands.XLSCommandLabel){
                if ((((Label) result).getValue() == null) || (((Label) result).getValue().toString().length() == 0)){
                    //empty label
                    result = null;
                }
            } else if ((nextCommand  == XLSCommands.XLSCommandCrossTab) || (nextCommand  == XLSCommands.XLSCommandFrame)){
                //cant join ws cross or frame
                result = null;
            } else if ((nextCommand  == XLSCommands.XLSCommandCell) || (nextCommand  == XLSCommands.XLSCommandRow) || (nextCommand  == XLSCommands.XLSCommandColumn)){
                //is dynamic ws default region 
                if (!defaultDynamics.contains(result)){
                    result = null;
                } else if ((nextCommand  == XLSCommands.XLSCommandRow) || (nextCommand  == XLSCommands.XLSCommandColumn)){
                    //has same direction?
                    if (((DinamicLayout)result).getDirection() != direction){
                        result = null;
                    }
                }
            }
            //field or formula - ok
        }
        return result;
    }
    
    /**
     * check data source for all fields
     * 
     * @param dinamicLayout
     * @param valueDistributor
     * @throws XLSUnknownSourceException
     * @throws IOException
     */
    private void checkFieldsSqlSource(DinamicLayout dinamicLayout, ValueDistributor valueDistributor) throws XLSUnknownSourceException, IOException{
        //look for all fields 
        for (Layout fld: dinamicLayout.getChildList()){
            // check field sql source
            if (fld.getCommand() == XLSCommands.XLSCommandField){
                boolean sqlAvailable = false;
                for (DinamicLayout lt : fld.getParents()) {
                    if (lt.isSqlAvailable(((BoundLayout)fld).getSqlName())){
                        sqlAvailable = true;
                        break;
                    }
                }
                if (!sqlAvailable){
                    BoundLayout field = (BoundLayout)fld;
                    String key = XlsUtils.constructExternalValue(field.getSqlName(), field.getColumnName());
                    if (valueDistributor.getValue(key) == null){
                        throw new XLSUnknownSourceException("unknownSource",field.getSqlName(), field.getReferences().getReference());
                    }   
                }
            } else if (fld instanceof DinamicLayout){
                //check dynamic child 
                checkFieldsSqlSource((DinamicLayout)fld,valueDistributor);
            }
        }
    }
}

