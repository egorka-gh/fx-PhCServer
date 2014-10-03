/**
 * 
 */
package com.reporter.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.*;


import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.reporter.data.XLSDataParameterException;
import com.reporter.xml.data.SqlElement;
import com.reporter.xml.data.SqlParametr;
import com.reporter.xml.exceptions.XMLTemplateException;
import com.reporter.xml.exceptions.XmlParameterException;

/**
 * @author Administrator
 *
 */
public class XMLParser {
    
    /**
     * schema name key 4 value distributor
     */
    public static final String SCHEMA_PARAMETER_KEY = ":dt.";

    /**
     * source name (SQL name) schema name
     */
    public static final String SCHEMA_RESERVED_SOURCE_NAME = "dt";

    private Map<String, SqlElement> sql;
    private List<SqlElement> beforeReportSql;
    
    public XMLParser(){
        this.sql = new TreeMap<String, SqlElement>();
        this.beforeReportSql = new ArrayList<SqlElement>();
    }
    
    /**
     * @return the sql
     */
    public Map<String, SqlElement> getSql() {
        return sql;
    }

    /**
     * @param sql the sql to set
     */
    public void setSql(Map<String, SqlElement> sql) {
        this.sql = sql;
    }

    /**
     * read xml
     * 
     * @param usedSql
     * @param document
     * @throws XMLTemplateException
     * @throws XLSDataParameterException
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws SAXException 
     */
    private void readXml(List<String> usedSql, Document document, String schemaName) throws XMLTemplateException, XLSDataParameterException, ParserConfigurationException, SAXException, IOException{
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document document = builder.parse(inputStream);
        document.getDocumentElement().normalize();
        
        NodeList elementList= document.getElementsByTagName(XmlElements.XmlElement.getXmlElementName());
        for (int i = 0; i < elementList.getLength(); i++){
            SqlElement sqlElement = new SqlElement();
            Node node = elementList.item(i);
            //get sql name
            Node elementName = node.getAttributes().getNamedItem(XmlElements.XmlElementName.getXmlElementName());
            sqlElement.setSqlName(elementName.getFirstChild().getNodeValue().toLowerCase());
            //check before report attrib
            Node elementBefore = node.getAttributes().getNamedItem(XmlElements.XmlElementBeforeReport.getXmlElementName());
            boolean beforeReport = elementBefore != null;
            if ((beforeReport) && (!elementBefore.getFirstChild().getNodeValue().trim().equals("1"))){
                continue;
            }
            Element nodeElement = (Element)node;
            
            NodeList sqlList = nodeElement.getElementsByTagName(XmlElements.XmlSql.getXmlElementName());
            if (sqlList.getLength() != 1){
                throw new XMLTemplateException("uniqueSQL", nodeElement.getTagName());
            }
            Node sql = sqlList.item(0);
            Node sqlDataNode = sql.getFirstChild();
//                sqlData.
//                CDATASection sqlData = (CDATASection)sqlDataNode;
            if (sqlDataNode.getNextSibling() == null){
                sqlElement.setSql(sqlDataNode.getNodeValue().trim());
            } else {
                sqlElement.setSql(sqlDataNode.getNextSibling().getNodeValue().trim());
            }
            //if exists schema name parameter check RESERVED_SOURCE_NAME
            if ((schemaName != null) && (schemaName.length()>0 )){
                if (sqlElement.getSqlName().toLowerCase().equals(XMLParser.SCHEMA_RESERVED_SOURCE_NAME)){
                    //TODO exception reservedSQLName
                    throw new XMLTemplateException("reservedSQLName", XMLParser.SCHEMA_RESERVED_SOURCE_NAME);
                }
            }
            if (beforeReport){
                parseSqlParametrs(sqlElement, schemaName);
                getBeforeReportSql().add(sqlElement);
            }else if (usedSql.contains(sqlElement.getSqlName())){
                if (getSql().get(sqlElement.getSqlName()) != null){
                    throw new XMLTemplateException("uniqueSQL", sqlElement.getSqlName());
                }
                parseSqlParametrs(sqlElement, schemaName);
                getSql().put(sqlElement.getSqlName(), sqlElement);
            }
        }
            
    }
    
    /**
     * parse sql parametrs
     * 
     * @param sqlElement
     * @throws XLSDataParameterException
     * @throws IOException 
     */
    private void parseSqlParametrs(SqlElement sqlElement, String schemaName) throws XLSDataParameterException, IOException{
        List<SqlParametr> params = new ArrayList<SqlParametr>();
        //Pattern pt = Pattern.compile(":[a-zA-Z_[-]0-9]+.[a-zA-Z_[-]0-9]+");
        //changed for ValueDistributor can be simple value name (egorka) 
        //Pattern pt = Pattern.compile(":[a-zA-Z_[-]\\.0-9]+");
        // [^:]: skips '::', caravela use '::' to mark simple ':' 
        Pattern pt = Pattern.compile("[^:]:[a-zA-Z_[-]\\.0-9]+");
        Matcher mtch = pt.matcher(sqlElement.getSql());
        StringBuffer sb = new StringBuffer();
        boolean schemaReplaced = false;
        while (mtch.find()) {
            String paramString = mtch.group();
            //save [^:] char
            String preFix = paramString.substring(0,1);
            //remove [^:] char
            paramString = paramString.substring(1);
            //check schemaName
            if ((schemaName != null) && (paramString.toLowerCase().startsWith(SCHEMA_PARAMETER_KEY))){
                //replace with schemaName
                String replaceSchema = schemaName;
                if (paramString.length() > SCHEMA_PARAMETER_KEY.length()){
                    replaceSchema = replaceSchema + paramString.substring(SCHEMA_PARAMETER_KEY.length());
                }
                mtch.appendReplacement(sb, preFix + replaceSchema);
                schemaReplaced = true;
            } else {
                //create parametr
                SqlParametr param = new SqlParametr();
                param.setOriginalName(paramString);
                paramString = paramString.substring(1); //remove ":"
                String []paramWords = paramString.split("\\.");
                param.setSqlName(paramWords[0].toLowerCase());
                if (paramWords.length == 2){  
                    param.setColumnName(paramWords[1].toLowerCase());
                } else if (paramWords.length > 2){
                    throw new XLSDataParameterException("parseParameter", paramString, sqlElement.getSqlName());
                }
                params.add(param);
                //replace with ?
                mtch.appendReplacement(sb, preFix + "?");
            }
        }
        mtch.appendTail(sb);
        //check if all ':' parsed
        /*
        if (sb.indexOf(":") != -1){
            throw new XLSDataParameterException("parseParameter", sb.substring(sb.indexOf(":")), sqlElement.getSqlName());
        }
        */
        //check if all ':' parsed? exclude ('::')
        String tmString = sb.toString().replace("::","");
        if (tmString.indexOf(":") != -1){
            throw new XLSDataParameterException("parseParameter", tmString.substring(tmString.indexOf(":")), sqlElement.getSqlName());
        }
        //replace '::' ws ':'
        tmString = sb.toString().replace("::",":");
        sqlElement.setParametrs(params);
        sqlElement.setSql(tmString);
        /*
        if ((params.size()>0) || schemaReplaced) {
            sqlElement.setSql(tmString);
        }
        */
    }
    
    /**
     * @param usedSql
     * @param xmlDoc
     * @return sql elements
     * @throws XMLTemplateException
     * @throws XLSDataParameterException
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public Map<String, SqlElement> parseXML(List<String> usedSql, Document xmlDoc, String schemaName) throws XMLTemplateException, XLSDataParameterException, XmlParameterException, ParserConfigurationException, SAXException, IOException{
        readXml(usedSql, xmlDoc, schemaName);
        //changed for ValueDistributor 
        // leave parametrs unchecked untill runtime (egorka) 
        //checkParametrs();
        return getSql();
    }

    /**
     * @return the beforeReportSql
     */
    public List<SqlElement> getBeforeReportSql() {
        return beforeReportSql;
    }

}
