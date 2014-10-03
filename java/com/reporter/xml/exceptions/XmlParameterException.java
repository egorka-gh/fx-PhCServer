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
public class XmlParameterException extends XLSReporterException{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XmlParameterException(String key, String... args) throws IOException {
		super(key, args);
	}

	public XmlParameterException(){
        super();
    }
    
    public XmlParameterException(String arg0) throws IOException{
        super(arg0);
    }
    
    public XmlParameterException(Throwable arg0){
        super(arg0);
    }
    
    public XmlParameterException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
