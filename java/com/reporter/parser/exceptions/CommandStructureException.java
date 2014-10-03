package com.reporter.parser.exceptions;

import java.io.IOException;

import com.reporter.exceptions.XLSReporterException;

/**
 * @author igor zadenov
 *
 */
public class CommandStructureException extends XLSReporterException{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommandStructureException(String key, String... args)
			throws IOException {
		super(key, args);
	}

	public CommandStructureException() {
    }

    public CommandStructureException(String arg0) throws IOException {
        super(arg0);
    }

    public CommandStructureException(Throwable arg0) {
        super(arg0);
    }

    public CommandStructureException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
