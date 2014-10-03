package com.reporter.parser;

import com.reporter.document.layout.XLSCommands;
import java.util.*;


public class CommandTemplate {
	/**
	 * is command parsed and valid
	 */
	private boolean isValid;
    /**
     * Sheet index
     */
    private int sheetIndx;

    /**
     * Command
     */
    private String commandName;
    
    /**
     * Range Name 
     */
    private String rangeName;
    
    /**
     * Range reference 
     */
    private String rangeReference;
    
    /**
     * sql name
     */
    private String sqlName;

    /**
     * Group or Bound Field
     */
    private String fieldName;

    /**
     * Outline command
     * change to enum OutlineType 
     */
    private String outlineCmd;
    
    public CommandTemplate(int sheetIndex, String command){
    	super();
    	this.sheetIndx = sheetIndex;
    	parse(command);
    }
    
    private void parse(String command){
        /* dirty (work only if everething ok) 
         * 1 = - command, parse it 	
         * 2 $ - standart command else short form $Field command
         * 3 to lower and split by "."
        */   	
     if (command == null) return;
     String sTmp = command.trim();
     if (sTmp.startsWith("=") == false) return;
     String []cmdItems = sTmp.split("\\.");
     // add cmdItems.length check
     sTmp = cmdItems[0];
     //get command (add char case checking)
     int subStrLenth = sTmp.indexOf("(");
     if (subStrLenth > 1) sTmp = sTmp.substring(0,subStrLenth).trim();
     Set<String> keyWords = new HashSet<String>();
	  	keyWords.add(XLSCommands.XLSCommandSheet.getCommand());
	 	keyWords.add(XLSCommands.XLSCommandRow.getCommand());
	 	keyWords.add(XLSCommands.XLSCommandColumn.getCommand());
	 	keyWords.add(XLSCommands.XLSCommandCell.getCommand());
	 	keyWords.add(XLSCommands.XLSCommandField.getCommand());
//	 	keyWords.add(XLSCommands.XLSCommandLabel.getCommand());
	 	keyWords.add(XLSCommands.XLSCommandVMerge.getCommand());
	 	keyWords.add(XLSCommands.XLSCommandHMerge.getCommand());
	 
     if (keyWords.contains(sTmp)){
    	 this.commandName = sTmp;
     } else {
    	 //Suppose it's brief field command
    	 this.commandName =	XLSCommands.XLSCommandField.getCommand();
    	 this.sqlName = sTmp;
     }
     //get region name 
     if (subStrLenth > 1){
    	 sTmp = cmdItems[0].substring(subStrLenth+1).trim();
    	 //add closed ")" check
    	 subStrLenth = sTmp.indexOf(")");
    	 if (subStrLenth>0){
        	 sTmp = sTmp.substring(0,subStrLenth).trim();
    	 }
    	 this.rangeName = sTmp;
     }
     //get SQL name
     if (cmdItems.length>1){
    	 sTmp = cmdItems[1].trim();
    	 if (this.sqlName != null) {
        	 //Suppose it's brief field command
    		 this.fieldName = sTmp;
    	 } else {
    		 this.sqlName = sTmp;
    	 }
     }
     //get optional Field |GroupField |OutlineType
     if (cmdItems.length>2){
    	 sTmp = cmdItems[2].trim();
    	 if (sTmp.startsWith("Group(")){
    		 // Group field
    		 sTmp = sTmp.substring(7,sTmp.length());
    		 //add check for closed ")"
    		 this.fieldName =sTmp.split(")")[0].trim(); 
    	 } else if (sTmp.equalsIgnoreCase("HOutline") ||
    			 	sTmp.equalsIgnoreCase("VOutline")){
    		 //outline type
    		 this.outlineCmd = sTmp;
    	 } else {
    		 this.fieldName = sTmp;
    	 }
     }
     return;	
    }
    
    public boolean isCommand(){
    	if (this.commandName == null) return false;
    	return this.commandName.length()>0;
    }
    public void setRangeReference(String xlsReference){
    	this.rangeReference = xlsReference;
    }
    public String getRegionName(){
    	return this.rangeName;
    }
    public String getDbgString(){
    	String result = 
    		"commandName " + this.commandName + " ; " +
    		"rangeName " + this.rangeName + 
    		" (" + this.rangeReference + ") ; " +
    		"sqlName " + this.sqlName + " ; " +
    		"fieldName " + this.fieldName + " ; " +
    		"outlineCmd " + this.outlineCmd; 
    	return result;
    }

	public boolean isValid() {
		return isValid;
	}

	public int getSheetIndx() {
		return sheetIndx;
	}
}