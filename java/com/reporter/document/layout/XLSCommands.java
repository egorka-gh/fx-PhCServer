/**
 * 
 */
package com.reporter.document.layout;

/**
 * @author Administrator
 *
 */
public enum XLSCommands {
    XLSCommandDelemiter(";", (short) -3),
    XLSCommandPrefix("$", (short) -2),
    XLSCommandStart("=", (short) -1),
    XLSCommandSheet("$Sheet", (short) 1),
    XLSCommandRow("$Row", (short) 2),
    XLSCommandColumn("$Column", (short) 3),
    XLSCommandCell("$Cell", (short) 4),
    XLSCommandField("$Field", (short) 5),
    XLSCommandVMerge("$VMerge", (short) 6),
    XLSCommandHMerge("$HMerge", (short) 7),
    XLSCommandHOutline("HOutline", (short) 8),
    XLSCommandVOutline("VOutline", (short) 9),
    XLSCommandLabel("$Label", (short) 10),
    XLSCommandFrame("$Frame", (short) 11),
    XLSCommandOutline("Outline", (short) 12),
    XLSCommandGroup("Group", (short) 13),
    XLSCommandNull("Null", (short) 14),
    XLSCommandFormula("Formula", (short) 15),
    XLSCommandCrossTab("CrossTab", (short) 16),
    XLSCommandRowPageBreak("$RowPageBreak", (short) 17),
    XLSCommandColumnPageBreak("$ColumnPageBreak", (short) 18),
    XLSCommandVersionInfo("$VersionInfo", (short) 19),
    XLSKeepUnprocessed("$KeepUnprocessed", (short) 20);
    
    private XLSCommands(String command, short index){
        this.command = command.toLowerCase();
        this.commandIndex = index;
    }
    
    private String command;
    private short commandIndex;

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command.toLowerCase();
    }

    /**
     * @return the command index
     */
    public short getIndex() {
        return commandIndex;
    }

    /**
     * @param index the command index to set
     */
    public void setIndex(short index) {
        this.commandIndex = index;
    }
}
