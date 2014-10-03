package com.reporter.data;

import java.io.IOException;
import com.reporter.exceptions.XLSReporterException;

public class XLSDataLockedException extends XLSReporterException {
    private static final long serialVersionUID = 1L;

    public XLSDataLockedException(String key, String... args) throws IOException {
        super(key, args);
    }

    public XLSDataLockedException(){
        super();
    }

    public XLSDataLockedException(String arg0) throws IOException{
        super(arg0);
    }

    public XLSDataLockedException(Throwable arg0){
        super(arg0);
    }

    public XLSDataLockedException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }

}
