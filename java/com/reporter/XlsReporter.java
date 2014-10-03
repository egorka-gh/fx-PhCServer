/**
 * 
 */
package com.reporter;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.w3c.dom.Document;

import com.mavaris.webcaravella.element.ValueDistributor;
import com.reporter.data.XLSDataCache;
import com.reporter.document.XLSDocumentPrepareOutput;
import com.reporter.document.XLSDocumentReader;
import com.reporter.document.XLSDocumentWriter;
import com.reporter.document.layout.BoundLayout;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.Layout;
import com.reporter.document.layout.impl.Sheet;
import com.reporter.parser.TemplateParser;
import com.reporter.utils.StringUtils;
import com.reporter.utils.XlsUtils;
import com.reporter.xml.XMLParser;
import com.reporter.xml.data.SqlElement;

/**
 * @author Administrator
 *
 */
public class XlsReporter {

    /**
     * output stream (result document)
     */
	private OutputStream outputStream;
    
    /**
     * xml document
     */
	private Document xmlDoc;
	
    /**
     * Data base Connection
     */
    private Connection connection;

    /**
     * use template workbook for output
     * to keep original xls objects
     * default false - generate new workbook 
     */
    private boolean keepXls;

    /**
     * Default constructor
     *
     */
    public XlsReporter(){
        super();
        keepXls = false;
    }
    
    /**
     * get used sql from all elements
     * 
     * @param childs
     * @return list with sql names
     */
    private List<String> getUsedSqlFromChilds(Set<Layout> childs){
        List<String> result = new ArrayList<String>();
        StringUtils stringUtils = new StringUtils();
        if (childs == null){
            return result;
        }
        for (Layout comp : childs){
            if (comp instanceof BoundLayout){
                BoundLayout temlComp = (BoundLayout)comp;
                if ((comp != null) && (!stringUtils.isEmpty(temlComp.getSqlName())) &&
                        (!result.contains(temlComp.getSqlName()))){
                    result.add(temlComp.getSqlName());
                }
                if (comp instanceof DinamicLayout){
                    List<String> tempSql = getUsedSqlFromChilds(((DinamicLayout)temlComp).getChilds());
                    for (String tempSqlName : tempSql){
                        if (!result.contains(tempSqlName)){
                            result.add(tempSqlName);
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private List<String> getUsedSql(List<Sheet> sheets){
        List<String> result = new ArrayList<String>();
        for (Sheet sheet : sheets){
            List<String> tempSql = getUsedSqlFromChilds(sheet.getChilds());
            for (String sql : tempSql){
                if (!result.contains(sql)){
                    result.add(sql);
                }
            }
            StringUtils stringUtils = new StringUtils();
            if ((!stringUtils.isEmpty(sheet.getSqlName())) &&(!result.contains(sheet.getSqlName()))){
                result.add(sheet.getSqlName());
            }

        }
        return result;
    }
    
    /**
     * process action. load document, parse document, generate output document
     * 
     * @param inputStream
     * @param outputStream
     * @param document
     * @param connection
     * @param valueDistributor
     */
    public void process(InputStream inputStream, OutputStream outputStream, Document document, Connection connection, ValueDistributor valueDistributor){
        setConnection(connection);
        this.outputStream = outputStream;
        this.xmlDoc = document;
        process(inputStream, valueDistributor);
    }
    
    /**
     * @param outputTemplate
     * @param e
     */
    public void fillOutputWithException(HSSFWorkbook outputTemplate, Exception e){
        final short firstCellIndex = 0;
        HSSFSheet sheet = outputTemplate.createSheet("Exception");
        HSSFRow messageRow = sheet.createRow(0);
        HSSFCell messageCell = messageRow.createCell(firstCellIndex);
        HSSFRichTextString richTextMessage = new HSSFRichTextString(e.toString());
        messageCell.setCellValue(richTextMessage);
        
        StackTraceElement[] stackElements = e.getStackTrace();
        for (int i = 0; i < stackElements.length; i++) {
            HSSFRow row = sheet.createRow(i+1);
            HSSFCell cell = row.createCell(firstCellIndex);
            HSSFRichTextString richTextString = new HSSFRichTextString(stackElements[i].toString());
            cell.setCellValue(richTextString);
        }
    }

    /**
     * process action. load document, parse document, generate output document
     * 
     * @param inputStream
     * @param valueDistributor
     */
    private void process(InputStream inputStream, ValueDistributor valueDistributor){
        try {
            /*
        	XLSDocumentReader documentReader = new XLSDocumentReader();
        	HSSFWorkbook workbook = documentReader.readDocument(inputStream);
            */
            XLSDocumentReader documentReader = new XLSDocumentReader(inputStream);
            HSSFWorkbook workbook = documentReader.getDocument();
            inputStream.close();
            
            TemplateParser templateParser = new TemplateParser();
            Map<String, Integer> keepSheetList = new HashMap<String, Integer>();
            List<Sheet> sheets = templateParser.parseDocument(workbook, valueDistributor, keepSheetList);
            //check & set keep mode
            if (!keepSheetList.isEmpty()){
                keepXls = true;
            }
            String schemaName = null;
            if (valueDistributor != null){
                schemaName = valueDistributor.getValue(XMLParser.SCHEMA_PARAMETER_KEY); 
            }
            
            XMLParser xmlParser = new XMLParser();
            Map<String, SqlElement> sqlMap = xmlParser.parseXML(getUsedSql(sheets), this.xmlDoc, schemaName);
            List< SqlElement> beforeReportSqlList = xmlParser.getBeforeReportSql();
        
            XLSDocumentPrepareOutput prepareDoc = new XLSDocumentPrepareOutput();
            //get output workbook
            HSSFWorkbook newWorkbook;
            if (keepXls){
                //get template
                newWorkbook = documentReader.getDocument();
                //delete all names (names not moved when reorder sheets)
                //TODO can be exception when name in keeped sheet == name in generated sheet
                int namesNum = newWorkbook.getNumberOfNames();
                int namesOffset = 0;
                for (int i = 0; i < namesNum; i++) {
                    HSSFName regionName = newWorkbook.getNameAt(namesOffset);
                    if (keepSheetList.containsKey(regionName.getSheetName())){
                        //save names in keeped sheets
                        namesOffset++;    
                    } else {
                        newWorkbook.removeName(namesOffset);
                    }
                }
                /*
                for (int i = 0; i < newWorkbook.getNumberOfNames(); i++) {
                    HSSFName regionName = newWorkbook.getNameAt(i);
                    System.out.println(regionName.getSheetName() + "/" +regionName.getNameName());
                }
                */
                //delete all parsed sheets
                for (Sheet sheetItem : sheets){
                    //remove sheet
                    newWorkbook.removeSheetAt(newWorkbook.getSheetIndex(sheetItem.getSheetName()));
                }
            } else {
                //create new
                newWorkbook = XlsUtils.cloneWorkbook(workbook);
            }
            XLSDataCache dataCache = new XLSDataCache(connection,  sqlMap, valueDistributor);
            dataCache.execute(beforeReportSqlList);
            HSSFWorkbook outputTemplate = prepareDoc.prepareDocument(sheets, dataCache, newWorkbook, workbook, keepSheetList);
            dataCache.flush();
            XLSDocumentWriter documentWriter = new XLSDocumentWriter();
            documentWriter.writeDocument(outputTemplate, outputStream);
        } catch (Exception e) {
            XLSDocumentWriter documentWriter = new XLSDocumentWriter();
        	HSSFWorkbook outputTemplate = new HSSFWorkbook();
            fillOutputWithException(outputTemplate, e);
            documentWriter.writeDocument(outputTemplate, outputStream);
            e.printStackTrace();
        }
    }

        /**
         * @param connection the connection to set
         */
        public void setConnection(Connection connection) {
            this.connection = connection;
        }

}
