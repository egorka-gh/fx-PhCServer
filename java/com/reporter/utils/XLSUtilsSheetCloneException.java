package com.reporter.utils;

import java.io.IOException;
import com.reporter.exceptions.XLSReporterException;

public class XLSUtilsSheetCloneException extends XLSReporterException {
    private static final long serialVersionUID = 1L;

    public XLSUtilsSheetCloneException(String key, String... args)
            throws IOException {
        super(key, args);
    }

    public XLSUtilsSheetCloneException(){
        super();
    }
    
    public XLSUtilsSheetCloneException(String arg0) throws IOException{
        super(arg0);
    }
    
    public XLSUtilsSheetCloneException(Throwable arg0){
        super(arg0);
    }
    
    public XLSUtilsSheetCloneException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }
}
