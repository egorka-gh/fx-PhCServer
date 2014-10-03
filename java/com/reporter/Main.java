/**
 * 
 */
package com.reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.mavaris.webcaravella.element.ValueDistributorImpl;
import com.reporter.constants.Constants;
import com.reporter.temp.XlsDataBaseConnection;

/**
 * @author Administrator
 *
 */
public class Main {
    
    
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws FileNotFoundException{
        String documentName;
        String xmlName;
        String outputName;
        
		if (args.length==3){
	        documentName = args[0];
	        xmlName = args[1];
	        outputName = args[2];
		}else if(args.length==2){
	        System.out.println("Defaults output filename constant(output.xls) used. Next params should be defined. 'source xls', 'data xml' ['output xls']");
	        documentName = args[0];
	        xmlName = args[1];
	        outputName = Constants.DOCUMENT_FOR_OUTPUT + Constants.DOCUMENT_EXT;
		}else{
	        System.out.println("Defaults filenames constants used. Next params should be defined. 'source xls', 'data xml' ['output xls']");
			documentName = Constants.DOCUMENT_FOR_PARSE_ROW + Constants.DOCUMENT_EXT;
	        xmlName = Constants.XML_DOCUMENT + Constants.XML_EXT;
	        outputName = Constants.DOCUMENT_FOR_OUTPUT + Constants.DOCUMENT_EXT;
		}
		File inFile = new File(documentName);
		File outFile = new File(outputName);
		File xmlFile = new File(xmlName);
		InputStream inputStream = new FileInputStream(inFile);
		OutputStream outputStream = new FileOutputStream(outFile);
		InputStream xmlStream = new FileInputStream(xmlFile);
		
        XlsDataBaseConnection xlsCnn =new XlsDataBaseConnection();
        xlsCnn.createConnection();
        Connection cnn = xlsCnn.getCn();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(xmlStream);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return ;
        }
        
        XlsReporter reporter = new XlsReporter();
        reporter.setConnection(cnn);
        //reporter.process(documentName);
        reporter.process(inputStream, outputStream, document, cnn, new ValueDistributorImpl());
        
        try {
            cnn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}

}
