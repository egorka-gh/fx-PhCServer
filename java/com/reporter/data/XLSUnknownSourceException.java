package com.reporter.data;

import java.io.IOException;
import com.reporter.exceptions.XLSReporterException;

public class XLSUnknownSourceException extends XLSReporterException {
    private static final long serialVersionUID = 1L;

    public XLSUnknownSourceException(String key, String... args) throws IOException {
        super(key, args);
    }

    public XLSUnknownSourceException(){
        super();
    }

    public XLSUnknownSourceException(String arg0) throws IOException{
        super(arg0);
    }

    public XLSUnknownSourceException(Throwable arg0){
        super(arg0);
    }

    public XLSUnknownSourceException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
