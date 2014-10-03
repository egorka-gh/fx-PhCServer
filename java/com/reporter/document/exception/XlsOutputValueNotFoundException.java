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
public class XlsOutputValueNotFoundException extends XLSReporterException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XlsOutputValueNotFoundException(String key, String... args)
			throws IOException {
		super(key, args);
	}

	public XlsOutputValueNotFoundException(){
        super();
    }
    
    public XlsOutputValueNotFoundException(String arg0) throws IOException{
        super(arg0);
    }
    
    public XlsOutputValueNotFoundException(Throwable arg0){
        super(arg0);
    }
    
    public XlsOutputValueNotFoundException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
