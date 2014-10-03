/**
 * 
 */
package com.reporter.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * @author Administrator
 *
 */
public class XLSDocumentWriter {

    /**
     * write document
     * 
     * @param workbook
     */
    public void writeDocument(HSSFWorkbook workbook, OutputStream outputStream){
        try {
            workbook.write(outputStream);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
