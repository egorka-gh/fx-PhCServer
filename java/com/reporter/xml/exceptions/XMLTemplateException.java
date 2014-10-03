/**
 * 
 */
package com.reporter.xml.exceptions;

import java.io.IOException;

import com.reporter.exceptions.XLSReporterException;

/**
 * @author Administrator
 *
 */
public class XMLTemplateException extends XLSReporterException{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XMLTemplateException(String key, String... args) throws IOException {
		super(key, args);
	}

	public XMLTemplateException(){
        super();
    }
    
    public XMLTemplateException(String arg0) throws IOException{
        super(arg0);
    }
    
    public XMLTemplateException(Throwable arg0){
        super(arg0);
    }
    
    public XMLTemplateException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
