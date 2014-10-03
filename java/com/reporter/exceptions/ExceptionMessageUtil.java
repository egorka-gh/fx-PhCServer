
package com.reporter.exceptions;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class ExceptionMessageUtil {
	
	public static String getMessage(String key, String... args) throws IOException{
		ResourceBundle bundle = ResourceBundle.getBundle("com.reporter.exceptions.exceptions");
        String errText = bundle.getString(key);
        errText = MessageFormat.format(errText, args); 
		return errText;
	}
}
