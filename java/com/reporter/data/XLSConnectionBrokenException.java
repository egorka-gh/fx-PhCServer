package com.reporter.data;

import java.io.IOException;
import com.reporter.exceptions.XLSReporterException;

public class XLSConnectionBrokenException extends XLSReporterException {
    private static final long serialVersionUID = 1L;

    public XLSConnectionBrokenException(String key, String... args) throws IOException {
        super(key, args);
    }

    public XLSConnectionBrokenException(){
        super();
    }

    public XLSConnectionBrokenException(String arg0) throws IOException{
        super(arg0);
    }

    public XLSConnectionBrokenException(Throwable arg0){
        super(arg0);
    }

    public XLSConnectionBrokenException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
