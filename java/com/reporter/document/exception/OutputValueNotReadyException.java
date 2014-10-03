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
public class OutputValueNotReadyException extends XLSReporterException{

	public OutputValueNotReadyException(String key, String... args)
			throws IOException {
		super(key, args);
	}

	private static final long serialVersionUID = 1L;

	public OutputValueNotReadyException(){
        super();
    }
    
    public OutputValueNotReadyException(String arg0) throws IOException{
        super(arg0);
    }
    
    public OutputValueNotReadyException(Throwable arg0){
        super(arg0);
    }
    
    public OutputValueNotReadyException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
