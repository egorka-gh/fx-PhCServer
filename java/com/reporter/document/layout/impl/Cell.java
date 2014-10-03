/**
 * 
 */
package com.reporter.document.layout.impl;

import com.reporter.document.layout.XLSCommands;

/**
 * @author Ilya Ovesnov
 */
public class Cell extends GenericDinamicLayout{
    
    /**
     * default constructor
     */
    public Cell(){
        super();
        this.setCommand(XLSCommands.XLSCommandCell);
    }
    
}
