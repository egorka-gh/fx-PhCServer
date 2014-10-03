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
public class Row extends GenericDinamicLayout{
    
    /**
     * default constructor
     */
    public Row(){
        super();
        setCommand(XLSCommands.XLSCommandRow);
        setDirection(XlsLayoutDirection.RowDirection);
    }

}
