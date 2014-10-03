/**
 * 
 */
package com.mavaris.webcaravella.element;

/**
 * @author Administrator
 *
 */
public class ValueDistributorImpl implements ValueDistributor {

    /* (non-Javadoc)
     * @see com.mavaris.webcaravella.element.ValueDistributor#getValue(java.lang.String)
     */
    public String getValue(String elementName) {
        if ((elementName != null) && (elementName.toUpperCase().trim().equals("TEST_PARAM"))){
            return "TEST!!!!";
            //return new Double(13245).toString();
        } else if ((elementName != null) && (elementName.toLowerCase().trim().equals(":dt."))){
            return "l1.";
            /*
        } else if ((elementName != null) && (elementName.toLowerCase().trim().equals("rep1_col"))){
            return "opana";
            */
        }
        // TODO Auto-generated method stub
        return null;
    }

}
