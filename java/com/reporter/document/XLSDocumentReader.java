/**
 * 
 */
package com.reporter.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem; 


/**
 * @author Administrator
 *
 */
public class XLSDocumentReader {

    private POIFSFileSystem poiFs;
    
    /**
     *default constructor 
     */
    public XLSDocumentReader() {
        super();
    }
    /**
     *constructor 2 initialize internal POIFSFileSystem 
     *can create multiply copies of workbook
     */
    public XLSDocumentReader(InputStream inputStream) throws IOException {
        super();
        poiFs = new POIFSFileSystem(inputStream);
    }

    /**
     * siple method to get HSSFWorkbook
     * @param inputStream
     * @return workbook
     */
    public HSSFWorkbook readDocument(InputStream inputStream){
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook(inputStream, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wb;
    }

    /**
     * get new HSSFWorkbook from poiFs 
     * @return workbook
     */
    public HSSFWorkbook getDocument(){
        if (poiFs == null){
            return null;
        }
        HSSFWorkbook wb = null;
         
        try {
            wb = new HSSFWorkbook(poiFs, true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return wb;
    }
}
