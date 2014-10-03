package com.reporter.temp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

public class PropertiesLoader {

    /**
     * default constructor
     */
    public PropertiesLoader() {
        super();
    }
    
    /**
     * load properties
     * 
     * @return properties
     * @throws IOException
     */
    public Properties loadProperties() throws IOException {
    	 Properties properties = new Properties();
    	 File file = new File("simple.properties");
         FileInputStream fileInputStream = new FileInputStream(file);
    	 properties.load(fileInputStream);
    
    	 Object[] args = {1,2};
    	 
    	 MessageFormat format= new MessageFormat(properties.get("test").toString()); 
    
    	 System.out.println(format.format(args)); 
    	return null;
    }
}
