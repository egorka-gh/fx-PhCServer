/**
 * 
 */
package com.reporter.document.layout.impl;

import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsLayoutDirection;

/**
 * @author Ilya Ovesnov
 *
 */
public class Column extends GenericDinamicLayout{
    
    /**
     * default constructor
     */
    public Column(){
        super();
        setCommand(XLSCommands.XLSCommandColumn);
        setDirection(XlsLayoutDirection.ColumnDirection);
    }
}
