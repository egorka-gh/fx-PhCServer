package com.reporter.parser.exceptions;

import java.io.IOException;
import com.reporter.exceptions.XLSReporterException;

public class XLSRegionNameException extends XLSReporterException {
    private static final long serialVersionUID = 1L;

    public XLSRegionNameException(String key, String... args)
            throws IOException {
        super(key, args);
    }

    public XLSRegionNameException(){
        super();
    }
    
    public XLSRegionNameException(String arg0) throws IOException{
        super(arg0);
    }
    
    public XLSRegionNameException(Throwable arg0){
        super(arg0);
    }
    
    public XLSRegionNameException(String arg0, Throwable arg1){
        super(arg0, arg1);
    }

}
