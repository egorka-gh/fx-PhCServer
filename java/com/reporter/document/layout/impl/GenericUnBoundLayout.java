/**
 * 
 */
package com.reporter.document.layout.impl;

import com.reporter.document.layout.UnBoundLayout;

/**
 * @author Ilya Ovesnov
 *
 */
public class GenericUnBoundLayout extends GenericLayout implements UnBoundLayout {
    
    /**
     * Default constructor
     */
    public GenericUnBoundLayout(){
        setReadyForOutput(true);
    }

}
