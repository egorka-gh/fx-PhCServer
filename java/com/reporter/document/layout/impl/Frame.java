/**
 * 
 */
package com.reporter.document.layout.impl;

import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsLayoutDirection;


/**
 * @author Igor Zhadenov
 *
 */
public class Frame extends GenericDinamicLayout{
    
    /**
     * default constructor
     */
    public Frame(){
        super();
        setCommand(XLSCommands.XLSCommandFrame);
        setDirection(XlsLayoutDirection.AnyDirection);
    }

}
