package com.reporter.parser.exceptions;

import java.io.IOException;

import com.reporter.exceptions.XLSReporterException;

public class CommandSyntaxException extends XLSReporterException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommandSyntaxException(String key, String... args)
			throws IOException {
		super(key, args);
	}

	public CommandSyntaxException() {
    }

    public CommandSyntaxException(String arg0) throws IOException {
        super(arg0);
    }

    public CommandSyntaxException(Throwable arg0) {
        super(arg0);
    }

    public CommandSyntaxException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
