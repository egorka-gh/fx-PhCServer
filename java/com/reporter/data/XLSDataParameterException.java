/**
 * 
 */
package com.reporter.data;

import java.io.IOException;

import com.reporter.exceptions.XLSReporterException;

/**
 * @author Ilya Ovesnov
 *
 */
public class XLSDataParameterException extends XLSReporterException{
    
	private static final long serialVersionUID = 1L;

	public XLSDataParameterException(String key, String... args)
			throws IOException {
		super(key, args);
	}

    public XLSDataParameterException(){
        super();
    }
    
    public XLSDataParameterException(String arg0) throws IOException{
        super(arg0);
    }
    
    public XLSDataParameterException(Throwable arg0){
        super(arg0);
    }
    
    public XLSDataParameterException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
