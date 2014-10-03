package com.reporter.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.RangeAddress;


/**
 * @author egOrka
 *
 */

public class CommandParser {

    public int parseWorkbook(HSSFWorkbook workbook){
    	//dirty (work only if everething ok)
    	List<CommandTemplate> result = new ArrayList<CommandTemplate>();
    	HSSFSheet sheet = workbook.getSheetAt(1);
    	String sheetName = workbook.getSheetName(1);
    	/*
        for (int i = 0; i < workbook.getNumberOfSheets(); i++){
        	sheet = workbook.getSheetAt(i);
        }       
    	*/
        Iterator rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()){
            HSSFRow row = (HSSFRow)rowIterator.next();
            Iterator cellIterator = row.cellIterator();
            while (cellIterator.hasNext()){
                HSSFCell cell = (HSSFCell)cellIterator.next();
                HSSFComment comment = cell.getCellComment();
                if (comment != null){
                    HSSFRichTextString com = comment.getString();
                    String []commands = com.getString().split(";");
                    for (String cmd : commands){
                    	/*
                    	cmd = cmd.replace("\n", " ");
                    	cmd = cmd.replace("\f", " ");
                    	cmd = cmd.replace("\r", " ");
                    	*/
	                    CommandTemplate cmdTmp = new CommandTemplate(0, cmd.trim());
	                    if (cmdTmp.isCommand()){
	                    	//check region and build default if needed
	                    	//set xls reference by name or by default region
	                    	if (cmdTmp.getRegionName() != null &&
	                    		cmdTmp.getRegionName().length()>0){
	                    		// set by name
	                    		// dirty (no range name checking)
	                    		
	                    		cmdTmp.setRangeReference(workbook.getNameAt(
	                    									workbook.getNameIndex(
	                    											cmdTmp.getRegionName())).getReference());
	                    											
	                    	} else {
	                    		// set by cell or default region
	                    		// dirty 
	                    		cmdTmp.setRangeReference(sheetName + "!" +
	                    			new RangeAddress(cell.getCellNum()+1,row.getRowNum()+1,
	                    							 cell.getCellNum()+1,row.getRowNum()+1).getAddress());
	                    	}
	                    	result.add(cmdTmp);
	                    }
                    }    
                }
            }
        }
        System.out.println("********* CommandParser *********");
        for (CommandTemplate cmdTmp : result){
        	System.out.println(cmdTmp.getDbgString());
        }
        System.out.println("********* CommandParser *********");
        return result.size();
    }
}
