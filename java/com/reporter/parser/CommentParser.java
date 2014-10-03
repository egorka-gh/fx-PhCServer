package com.reporter.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFComment;

import com.reporter.constants.Constants;
import com.reporter.data.XLSCellValue;
import com.reporter.document.layout.BoundLayout;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.MergeRegion;
import com.reporter.document.layout.MergedLayout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XLSMergeType;
import com.reporter.document.layout.XlsFormulaRegions;
import com.reporter.document.layout.XlsLayoutDirection;
import com.reporter.document.layout.XlsReference;
import com.reporter.document.layout.impl.Cell;
import com.reporter.document.layout.impl.Column;
import com.reporter.document.layout.impl.Field;
import com.reporter.document.layout.impl.Formula;
import com.reporter.document.layout.impl.Frame;
import com.reporter.document.layout.impl.Label;
import com.reporter.document.layout.impl.Row;
import com.reporter.document.layout.impl.Sheet;
import com.reporter.parser.exceptions.CommandStructureException;
import com.reporter.parser.exceptions.CommandSyntaxException;
import com.reporter.utils.XlsUtils;

/**
 * @author igor zadenov
 *
 */
public class CommentParser {
    
    /**
     * pattern to indent formula regions
     */
//    private static final String FORMULA_PATTERN = "\\$[A-Z](\\$)?[0-9]+(:(\\$)?[A-Z](\\$)?[0-9]+)?";
    private static final String FORMULA_PATTERN = "\\$[A-Z]{1,2}(\\$)?[0-9]+(:(\\$)?[A-Z]{1,2}(\\$)?[0-9]+)?";

    /**
     * parse xls cell comment
     * 
     * @param cell
     * @param reference
     * @return instance of Layout object
     * @throws CommandStructureException 
     * @throws CommandSyntaxException
     * @throws IOException 
     */
    public List<Layout> getLayoutFromCell(HSSFCell cell, XlsReference reference, String subCommand) throws CommandStructureException, CommandSyntaxException, IOException{
        //don't kill (rfactoring)
        List <DinamicLayout> ltFrames = new ArrayList<DinamicLayout>(); //frames
        DinamicLayout ltDynamic = null; //sheet || row || column
        DinamicLayout ltCell = null;
        Layout ltFieldOrLabel = null;
        XlsLayoutDirection pageBreak = XlsLayoutDirection.NoneDirection; 
        
        //Layout result = null;
        List <Layout> result = new ArrayList<Layout>(); 

        MergeRegion mergeReg = null;
        HSSFComment cellComment = cell.getCellComment();
        if ((cellComment != null) || (subCommand.length()>0)){
            //HSSFRichTextString com = cellComment.getString();
            String comment = "";
            if (cellComment != null){
                comment = cellComment.getString().getString();
            }
            comment = comment + subCommand;
            String []comments = comment.split(XLSCommands.XLSCommandDelemiter.getCommand());
            for (String commentItem : comments){
                String tempComment = commentItem.toLowerCase().trim();
                //check is it command
                if ((tempComment.length()==0) || (tempComment.charAt(0) != XLSCommands.XLSCommandStart.getCommand().charAt(0))){
                    continue;
                }
                //check double '='. Lost of ';'  (XLS-44)
                if (tempComment.lastIndexOf(XLSCommands.XLSCommandStart.getCommand())>0){
                    throw new CommandSyntaxException("commandDelemiterLost",XLSCommands.XLSCommandStart.getCommand(),XLSCommands.XLSCommandDelemiter.getCommand(),comment);
                }
                //remove "="
                tempComment = tempComment.substring(1).trim();
                //check double '$'. Lost of ';'  (XLS-44)
                if (tempComment.lastIndexOf(XLSCommands.XLSCommandPrefix.getCommand())>0){
                    throw new CommandSyntaxException("commandDelemiterLost",XLSCommands.XLSCommandPrefix.getCommand(),XLSCommands.XLSCommandDelemiter.getCommand(),comment);
                }
                String command = getCommand(tempComment);
                if ((command.charAt(0) != XLSCommands.XLSCommandPrefix.getCommand().charAt(0)) ||
                        (command.equals(XLSCommands.XLSCommandField.getCommand()))){
                    //field command
                    if (ltFieldOrLabel != null){
                        throw new CommandStructureException("notOneFieldFound", comment, reference.getReference());
                    }
                    Field field = new Field();
                    field.setReferences(reference);
                    field.setStyleIndex(cell.getCellStyle().getIndex());
                    field.setXlsCellType(cell.getCellType());
                    setSqlInfo(field,tempComment);
                    setOutline(field,tempComment, reference);
                    ltFieldOrLabel = field;
                } else if (command.equals(XLSCommands.XLSCommandVersionInfo.getCommand())){
                    //VersionInfo command detected ignore all other commands
                    Label label = new Label(reference);
                    label.setValue(new XLSCellValue("Version info: " + Constants.VERSION_INFO));
                    label.setStyleIndex(cell.getCellStyle().getIndex());
                    label.setXlsCellType(cell.getCellType());
                    ltFieldOrLabel =label;
                    result.clear();
                    result.add(ltFieldOrLabel);
                    return result;
                } else if (command.equals(XLSCommands.XLSCommandSheet.getCommand())){
                    //sheet command
                    if (ltDynamic != null){  
                        throw new CommandStructureException("wrongCommandStructure",XLSCommands.XLSCommandSheet.getCommand(),((BoundLayout)result).getCommand().getCommand(),comment);   
                    }
                    if ((reference.getColIndex()!=0) && (reference.getRowIndex()!=0)){
                        throw new CommandStructureException("wrongSheetDeclaration", comment, reference.getReference());   
                    }
                    ltDynamic = new Sheet(reference.getSheetName());
                    setSqlInfo(ltDynamic,tempComment);
                    checkDefaultField(ltDynamic,tempComment,cell);//that's ok
                    setOutline(ltDynamic,tempComment, reference);
                } else if (command.equals(XLSCommands.XLSCommandRow.getCommand())){
                    //row command
                    if (ltDynamic != null){  
                        throw new CommandStructureException("wrongCommandStructure",XLSCommands.XLSCommandRow.getCommand(),((BoundLayout)result).getCommand().getCommand(),comment);   
                    }
                    ltDynamic = new Row();
                    ltDynamic.setReferences(reference);
                    ltDynamic.setXlsCellType(cell.getCellType());
                    setSqlInfo(ltDynamic,tempComment);
                    setOutline(ltDynamic,tempComment, reference);
                    ltDynamic.setRegionName(getNamedRegion(tempComment));

                    Field fld = checkDefaultField(ltDynamic,tempComment,cell);
                    ltFieldOrLabel = checkField(ltFieldOrLabel, fld, comment, reference.getReference());

                } else if (command.equals(XLSCommands.XLSCommandColumn.getCommand())){
                    //column command
                    if (ltDynamic != null){  
                        throw new CommandStructureException("wrongCommandStructure",XLSCommands.XLSCommandColumn.getCommand(),((BoundLayout)result).getCommand().getCommand(),comment);   
                    }
                    ltDynamic = new Column();
                    ltDynamic.setReferences(reference);
                    ltDynamic.setXlsCellType(cell.getCellType());
                    setSqlInfo(ltDynamic,tempComment);
                    setOutline(ltDynamic,tempComment, reference);
                    ltDynamic.setRegionName(getNamedRegion(tempComment));

                    Field fld = checkDefaultField(ltDynamic,tempComment,cell);
                    ltFieldOrLabel = checkField(ltFieldOrLabel, fld, comment, reference.getReference());

                } else if (command.equals(XLSCommands.XLSCommandCell.getCommand())){
                    //cell command
                    if (ltCell != null){  
                        throw new CommandStructureException("wrongCommandStructure", XLSCommands.XLSCommandCell.getCommand(),((BoundLayout)result).getCommand().getCommand(),comment);
                    }
                    ltCell = new Cell();
                    ltCell.setReferences(reference);
                    ltCell.setStyleIndex(cell.getCellStyle().getIndex());
                    ltCell.setXlsCellType(cell.getCellType());
                    setSqlInfo(ltCell,tempComment);
                    setOutline(ltCell,tempComment, reference);
                    ltCell.setRegionName(getNamedRegion(tempComment));

                } else if (command.equals(XLSCommands.XLSCommandFrame.getCommand())){
                    //frame command
                    String region = getNamedRegion(tempComment);
                    if ((region == null) || (region.length() == 0)){
                        throw new CommandStructureException("wrongFrameDeclaration", comment,reference.getReference());
                    }
                    Frame ltFrame = new Frame();
                    ltFrame.setReferences(reference);
                    ltFrame.setRegionName(region);
                    setOutline(ltFrame,tempComment, reference);
                    ltFrames.add(ltFrame);
                } else if (command.equals(XLSCommands.XLSCommandHMerge.getCommand()) || command.equals(XLSCommands.XLSCommandVMerge.getCommand())){
                    //merge command
                    MergeRegion newMerge = createMergeRegion(tempComment, comment, reference.getReference());
                    if (mergeReg == null){
                        mergeReg = newMerge;
                    } else {
                        //check merges
                        //same direction HMerge+HMerge or VMerge+VMerge
                        if (mergeReg.getDirection() == newMerge.getDirection()){
                            throw new CommandStructureException("moreThanOneMerge", comment,reference.getReference());
                        }
                        //non static merge combination
                        if ((newMerge.getMergeType() != XLSMergeType.XLSMergeStatic) && (mergeReg.getMergeType() != XLSMergeType.XLSMergeStatic)){
                            throw new CommandStructureException("wrongMergeCombination", comment,reference.getReference());
                        }
                        //extend merge
                        MergeRegion tmMerge = (newMerge.getMergeType() == XLSMergeType.XLSMergeStatic) ? mergeReg : newMerge;
                        newMerge = (newMerge.getMergeType() == XLSMergeType.XLSMergeStatic) ? newMerge : mergeReg;
                        //TODO add check if already extended (multiply static merges)
                        if (tmMerge.getDirection() == XlsLayoutDirection.ColumnDirection){
                            tmMerge.setHeight(newMerge.getHeight());
                        } else {
                            tmMerge.setWidth(newMerge.getWidth());
                        }
                        mergeReg = tmMerge;
                    }
                } else if (command.equals(XLSCommands.XLSCommandColumnPageBreak.getCommand()) || command.equals(XLSCommands.XLSCommandRowPageBreak.getCommand())){
                    //page break command
                    if (command.equals(XLSCommands.XLSCommandColumnPageBreak.getCommand())){
                        pageBreak = XlsLayoutDirection.ColumnDirection;
                    }
                    if (command.equals(XLSCommands.XLSCommandRowPageBreak.getCommand())){
                        if (pageBreak == XlsLayoutDirection.ColumnDirection){
                            pageBreak = XlsLayoutDirection.AnyDirection;
                        } else{
                            pageBreak = XlsLayoutDirection.RowDirection;
                        }
                    }
                } else {
                    throw new CommandSyntaxException("unsupportedCommand",tempComment,comment);
                }
            } // end for
            
            //check cell without field
            if ((ltCell != null) && (ltFieldOrLabel == null)){
                Field fld = createFieldForCell(ltCell, reference);
                ltFieldOrLabel = checkField(ltFieldOrLabel, fld, comment, reference.getReference());
            }
        }
        
        if (ltFieldOrLabel == null){
            ltFieldOrLabel = createLabel(cell, reference);
        }
        //save page break
        ltFieldOrLabel.setPageBreak(pageBreak);
        //add merge command
        ((MergedLayout) ltFieldOrLabel).setMerge(mergeReg);
        //buld result
        if (ltDynamic != null){
            result.add(ltDynamic);
        }
        if (ltFrames.size()>0){
            result.addAll(ltFrames);
        }
        if (ltCell != null){
            result.add(ltCell);
        }
        result.add(ltFieldOrLabel);
        return result;
    }
    
    /**
     * parse formula regions
     * 
     * @param formula
     * @param formulaValue
     */
    private void parseFormulaRegions(Formula formula, String formulaValue){
        Pattern pt = Pattern.compile(FORMULA_PATTERN);
        Matcher matcher = pt.matcher(formulaValue);
        int start = 0;
        while (matcher.find()){
            formula.getValueBlocks().add(formulaValue.substring(start, matcher.start()));
            formula.getFormulaRegions().add(new XlsFormulaRegions(matcher.group()));
            start = matcher.end();
        }
        formula.getValueBlocks().add(formulaValue.substring(start));
    }
    
    /**
     * create label
     * 
     * @param cell
     * @param reference
     * @return new label
     */
    private Label createLabel(HSSFCell cell, XlsReference reference){
        if (cell.getCellType() != HSSFCell.CELL_TYPE_FORMULA){
            Label label = new Label(reference);
            label.setValue(cell);
            label.setStyleIndex(cell.getCellStyle().getIndex());
            label.setXlsCellType(cell.getCellType());
            return label;
        } else {
            Formula formula = new Formula(reference);
            parseFormulaRegions(formula, cell.getCellFormula());
            formula.setStyleIndex(cell.getCellStyle().getIndex());
            formula.setXlsCellType(cell.getCellType());
            return formula;
        }
    }

    /**
     * get name of region from comment command
     * 
     * get named region
     * @param commentComand
     * @return name of region
     */
    private String getNamedRegion(String commentComand){
        String result = null;
        String []element = commentComand.split("\\.");
        if (element.length > 0){
            int begin = element[0].indexOf("(") + 1;
            int end = element[0].indexOf(")");
            if ((begin > 0) && (end > begin)){
                result = element[0].substring(begin, end).trim().toLowerCase();
            }
        }
        return result;
    }
    
    
    /**
     * set sql info
     * 
     * @param destLayout
     * @param command
     * @throws IOException 
     * @throws CommandStructureException 
     */
    private void setSqlInfo(BoundLayout destLayout, String command) throws CommandStructureException, IOException{
        String []subCmd = command.split("\\.");
        if (command.charAt(0)=='$'){ //standart command
            if (subCmd.length > 1){
                destLayout.setSqlName(subCmd[1].trim());
            } else {//TODO else exception?
                destLayout.setSqlName("");
            }
            if (subCmd.length > 2){
                if ((destLayout.getCommand() == XLSCommands.XLSCommandCell) || (destLayout.getCommand() == XLSCommands.XLSCommandField)){
                    destLayout.setColumnName(subCmd[2].trim());
                } else {
                    //look for group command
                    //can be at index 2 or 3 (if command ws default field )
                    for (int k = 2; k < subCmd.length; k++){
                        String tmStr = subCmd[k].trim();
                        if (tmStr.toLowerCase().startsWith(XLSCommands.XLSCommandGroup.getCommand() + "(")){
                            int i = tmStr.indexOf(")");
                            if (i>0){
                                destLayout.setColumnName(tmStr.substring(6,i).trim());
                            } else {
                                destLayout.setColumnName(tmStr.substring(6).trim());
                            }
                        }
                    }
                }
            } else {
                destLayout.setColumnName("");
            }
        } else { //brief field command
            destLayout.setSqlName(subCmd[0].trim());
            if (subCmd.length > 1){
                destLayout.setColumnName(subCmd[1].trim());
            } else {
                destLayout.setColumnName("");
            }
        }
    }

    /**
     * set outline info
     * 
     * @param destLayout
     * @param command
     * @throws IOException 
     * @throws CommandStructureException 
     */
    private void setOutline(Layout destLayout, String command, XlsReference cellReference) throws CommandStructureException, IOException{
        boolean hasHoutline = false;
        boolean hasVoutline = false;
        boolean hasOutline = false;
        String []subCmd = command.split("\\.");
        for (int i = 0; i < subCmd.length; i++){
            if (subCmd[i].trim().equalsIgnoreCase(XLSCommands.XLSCommandHOutline.getCommand())){
                if (hasHoutline ||hasVoutline || hasOutline){
                    //double declaration
                    throw new CommandStructureException("moreThanOneOutline", command, cellReference.getReference());
                }
                hasHoutline = true;
            }
            if (subCmd[i].trim().equalsIgnoreCase(XLSCommands.XLSCommandVOutline.getCommand())){
                if (hasHoutline ||hasVoutline || hasOutline){
                    //double declaration
                    throw new CommandStructureException("moreThanOneOutline", command, cellReference.getReference());
                }
                hasVoutline = true;
            }
            if (subCmd[i].trim().equalsIgnoreCase(XLSCommands.XLSCommandOutline.getCommand())){
                if (hasHoutline || hasOutline || hasVoutline){
                    throw new CommandStructureException("moreThanOneOutline", command, cellReference.getReference());
                }
                hasOutline = true;
            }
            if ((hasHoutline ||hasVoutline || hasOutline) && (i<2)){
                //wrong position
                throw new CommandStructureException("wrongOutlinePosition", command, cellReference.getReference());
            }
        }
        if (hasHoutline ||hasVoutline || hasOutline){
            if ((destLayout instanceof BoundLayout) && 
                    ((((BoundLayout)destLayout).getCommand() == XLSCommands.XLSCommandColumn) || (((BoundLayout)destLayout).getCommand() == XLSCommands.XLSCommandRow))){
                ((BoundLayout)destLayout).setOutlineH(hasHoutline);
                ((BoundLayout)destLayout).setOutlineV(hasVoutline);
                ((BoundLayout)destLayout).setOutline(hasOutline);
            } else {
                // can't has outline
                throw new CommandStructureException("outlineNotAllowed", command, cellReference.getReference());
            }
        }
    }
    
    /**
     * create merged region by command
     * @param command
     * @return
     * @throws IOException 
     * @throws CommandSyntaxException 
     */
    private MergeRegion createMergeRegion(String command, String comment, String ref) throws CommandSyntaxException, IOException{
        MergeRegion result = null;
        XlsLayoutDirection direction = XlsLayoutDirection.NoneDirection;
        if (command.startsWith(XLSCommands.XLSCommandHMerge.getCommand())){
            direction = XlsLayoutDirection.ColumnDirection;
        } else if (command.startsWith(XLSCommands.XLSCommandVMerge.getCommand())){ 
            direction = XlsLayoutDirection.RowDirection;
        } else {
            return null;
        }
        //get parametr
        int start = command.indexOf("(")+1;
        if (start > 0){
            // get size or section name
            //TODO add double "(" and ")" check
            int end = command.indexOf(")");
            if (end == -1){
                throw new CommandSyntaxException("subCommandNotClosed", command, comment, ref);
            }
            String subCmd = command.substring(start, end).trim();
            try {
                Integer staticVal =  Integer.valueOf(subCmd);
                //static merge
                if (direction == XlsLayoutDirection.ColumnDirection){
                    result = new MergeRegion(1, staticVal.shortValue());
                } else {
                    result = new MergeRegion( staticVal, (short)1);
                }
                result.setDirection(direction);
            } catch (NumberFormatException e) {
                //merge by section subCmd
                result = new MergeRegion(subCmd);
                result.setDirection(direction);
            }
        } else {
            // merge by current element
            result = new MergeRegion();
            result.setDirection(direction);
        }
        return result;
    }

    /**
     * @param currentField
     * @param newField
     * @param comment
     * @param reference
     * @throws CommandStructureException
     * @throws IOException
     */
    private Layout checkField(Layout currentField, Field newField, String comment, String reference) throws CommandStructureException, IOException{
        Layout result = currentField;
        if (newField == null){
            return result; 
        }
        if ((currentField == null) || (currentField.getCommand() == XLSCommands.XLSCommandLabel)){
            result = newField;
        } else {
            String sSql = ((BoundLayout)currentField).getSqlName();
            if (sSql.length() == 0){
                result = newField;
            } else {
                String sColumn = ((BoundLayout)currentField).getColumnName();
                if ((!sSql.equals(newField.getSqlName())) || (!sColumn.equals(newField.getColumnName()))){
                    throw new CommandStructureException("notOneFieldFound", comment, reference);
                }
            }
        }
        return result; 
    }
    
    /**
     * check field declaration in command for $Sheet/$Row/$Column
     * @param destLayout
     * @param command
     * @param cell
     */
    private Field checkDefaultField(BoundLayout destLayout, String command, HSSFCell cell){
        Field result = null;
        if (command.charAt(0)=='$'){ //standart command
            String []subCmd = command.split("\\.");
            if (subCmd.length > 2){
                String tmStr = subCmd[2].trim();
                if (!(tmStr.toLowerCase().startsWith(XLSCommands.XLSCommandGroup.getCommand() + "("))
                        && !(subCmd[2].contains(XLSCommands.XLSCommandOutline.getCommand()))){
                    //System.out.println("Default Field found"); 
                    if (destLayout.getCommand() == XLSCommands.XLSCommandSheet){
                        //set field for sheet name
                        ((Sheet) destLayout).setCaptionField(tmStr);
                    } else {
                        //create field
                        //createDefaultField(destLayout,subCmd[1],tmStr,cell);
                        Field field = new Field();
                        field.setReferences(destLayout.getReferences());
                        field.setStyleIndex(cell.getCellStyle().getIndex());  
                        field.setSqlName(destLayout.getSqlName());
                        field.setColumnName(tmStr);
                        field.setXlsCellType(cell.getCellType());
                        /*
                        ((DinamicLayout)destLayout).getChilds().add(field);
                        field.getParents().add(((DinamicLayout)destLayout));
                        */
                        result = field;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * create default field for cell
     * @param checkLayout
     * @param reference
     */
    private Field createFieldForCell(Layout checkLayout, XlsReference reference){
        Field result = null;
        if ((checkLayout instanceof DinamicLayout) && (((DinamicLayout)checkLayout).getCommand() == XLSCommands.XLSCommandCell)){
            DinamicLayout cell = (DinamicLayout)checkLayout;
            result = new Field();
            result.setReferences(reference);
            result.setStyleIndex(cell.getStyleIndex());
            //setSqlInfo
            result.setSqlName(cell.getSqlName());
            result.setColumnName(cell.getColumnName());
            result.setXlsCellType(cell.getXlsCellType());
            /*
            if (!cell.hasChilds()){
                result = new Field();
                result.setReferences(reference);
                result.setStyleIndex(cell.getStyleIndex());
                //setSqlInfo
                result.setSqlName(cell.getSqlName());
                result.setColumnName(cell.getColumnName());
                cell.getChilds().add(result);
            }
            */
        }
        return result;
    }
    
    /**
     * extract command from comment
     * 
     * @param comment
     * @return
     */
    private String getCommand(String comment){
        //=$<layout>([<range>])[.<recordset>[.Group(<field>)]][.HOutline |VOutline];
        String result = "";
        //word till first .
        String []subString = comment.split("\\.");
        result = subString[0];
        //word till first (
        subString = result.split("\\(");
        result = subString[0].trim();
        return result;
    }
}
