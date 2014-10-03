package com.reporter.exceptions;

import java.io.IOException;
import java.lang.Exception;


public class XLSReporterException extends Exception {

	private static final long serialVersionUID = 1L;

	public XLSReporterException() {
		super();
	}
	
	public XLSReporterException(String message, Throwable cause) {
		super(message, cause);
	}

	public XLSReporterException(String key) throws IOException {
		super(ExceptionMessageUtil.getMessage(key));
	}

	public XLSReporterException(Throwable cause) {
		super(cause);
	}

	public XLSReporterException(String key, String... args) throws IOException{
	    super(ExceptionMessageUtil.getMessage(key, args));
	}
	
}
