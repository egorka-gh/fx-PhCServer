/**
 * 
 */
package com.reporter.utils;

/**
 * @author Administrator
 *
 */
public class StringUtils {
    
    /**
     * check is parametr empty
     * 
     * @param param
     * @return true is empty
     */
    public boolean isEmpty(String param){
        return param == null || param.length() == 0;
    }
}
