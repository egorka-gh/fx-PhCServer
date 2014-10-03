/**
 * 
 */
package com.reporter.document.layout.impl;

import com.reporter.document.layout.StaticLayout;
import com.reporter.document.layout.XLSCommands;


/**
 * @author Ilya Ovesnov
 *
 */
public class Field extends GenericStaticLayout implements StaticLayout{

    /**
     * default constructor
     */
    public Field(){
        super();
        this.setCommand(XLSCommands.XLSCommandField);
    }
}
