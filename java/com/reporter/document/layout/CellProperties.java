package com.reporter.document.layout;


public class CellProperties {
//    /**
//     * cell value
//     */
//    private String value;
//    
//    /**
//     * cell value type
//     */
//    private int valueType;
//
//    /**
//     * cell style
//     */
//    private HSSFCellStyle cellStyle;
//
//    /**
//     * 
//     */
//    public CellProperties() {
//        super();
//        // TODO Auto-generated constructor stub
//    }
//
//    public CellProperties(HSSFCell cell) {
//        super();
//        this.valueType = cell.getCellType();
//        if (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR){
//            this.valueType = HSSFCell.CELL_TYPE_BLANK;
//        }
//        this.cellStyle = cell.getCellStyle();
//        if ((cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) || (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR)){
//            this.value = "";
//        } else if(cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
//            this.value = String.valueOf(cell.getNumericCellValue());
//        } else if(cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN){
//            this.value = String.valueOf(cell.getBooleanCellValue());
//        } else {
//            this.value =cell.getRichStringCellValue().toString();
//        }
//    }
//
//    /**
//     * fills HSSFCell with value and sets style
//     * @param cell
//     */
//    public void fillCell(HSSFCell cell) {
//        cell.setCellType(this.valueType);
//        if (this.valueType == HSSFCell.CELL_TYPE_BLANK){
//            // = "";
//        } else if(this.valueType == HSSFCell.CELL_TYPE_NUMERIC){
//            cell.setCellValue(Double.parseDouble(this.value));
//        } else if(cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN){
//            cell.setCellValue(Boolean.parseBoolean(this.value));
//        } else {
//            cell.setCellValue(new HSSFRichTextString(this.value));
//        }
//        cell.setCellStyle(this.cellStyle);
//    }
//
//    /**
//     * @return the cellStyle
//     */
//    public HSSFCellStyle getCellStyle() {
//        return cellStyle;
//    }
//
//    /**
//     * @param cellStyle the cellStyle to set
//     */
//    public void setCellStyle(HSSFCellStyle cellStyle) {
//        this.cellStyle = cellStyle;
//    }
//
//
//    /**
//     * @param value the value to set
//     */
//
//    /**
//     * @return the valueType
//     */
//    public int getValueType() {
//        return valueType;
//    }
//
//    /**
//     * @param valueType the valueType to set
//     */
//    public void setValueType(int valueType) {
//        this.valueType = valueType;
//    }
//
//    /**
//     * @return the value
//     */
//    public String getValue() {
//        return value;
//    }
//
//    /**
//     * @param value the value to set
//     */
//    public void setValue(String value) {
//        this.value = value;
//    }
//    

}
