/**
 * 
 */
package com.reporter.document.exception;

import java.io.IOException;

import com.reporter.exceptions.XLSReporterException;

/**
 * @author Administrator
 *
 */
public class XlsReferenceException extends XLSReporterException{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XlsReferenceException(String key, String... args) throws IOException {
        super(key, args);
    }
    
    public XlsReferenceException(){
        super();
    }
    
    public XlsReferenceException(String arg0) throws IOException{
        super(arg0);
    }
    
    public XlsReferenceException(Throwable arg0){
        super(arg0);
    }
    
    public XlsReferenceException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
