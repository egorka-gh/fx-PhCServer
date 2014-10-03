/**
 * 
 */
package com.reporter.xml;

/**
 * @author Administrator
 *
 */
public enum XmlElements {
    XmlElementSet("element-set"),
    XmlElement("element"),
    XmlElementName("name"),
    XmlElementBeforeReport("before-report"),
    XmlSql("sql");
    
    private String xmlElementName;
    
    private XmlElements(String xmlElementName){
        setXmlElementName(xmlElementName);
    }

    /**
     * @return the xmlElementName
     */
    public String getXmlElementName() {
        return xmlElementName;
    }

    /**
     * @param xmlElementName the xmlElementName to set
     */
    public void setXmlElementName(String xmlElementName) {
        this.xmlElementName = xmlElementName;
    }

}
