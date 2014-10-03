package com.reporter.data;

import java.io.IOException;
import com.reporter.exceptions.XLSReporterException;

public class XLSExecSqlException extends XLSReporterException {
    private static final long serialVersionUID = 1L;

    public XLSExecSqlException(String key, String... args) throws IOException {
        super(key, args);
    }

    public XLSExecSqlException(){
        super();
    }

    public XLSExecSqlException(String arg0) throws IOException{
        super(arg0);
    }

    public XLSExecSqlException(Throwable arg0){
        super(arg0);
    }

    public XLSExecSqlException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
