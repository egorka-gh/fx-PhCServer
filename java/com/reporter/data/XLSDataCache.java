package com.reporter.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mavaris.webcaravella.element.ValueDistributor;
import com.reporter.data.data.DynamicRecordSource;
import com.reporter.data.data.StaticRecordSource;
import com.reporter.data.data.XLSRecordSource;
import com.reporter.document.exception.XlsOutputValueNotFoundException;
import com.reporter.document.layout.DinamicLayout;
import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.impl.Sheet;
import com.reporter.utils.XlsUtils;
import com.reporter.xml.data.SqlElement;
import com.reporter.xml.data.SqlParametr;

public class XLSDataCache {

    private Connection connection;
    private Map<String, SqlElement> sqlElements;
    private Map<String, XLSRecordSource> recordSources;
    private ValueDistributor valueDistributor;
    
    /**
     * @param connection
     * @param sqlMap
     * @param valueDistributor
     */
    public XLSDataCache(Connection connection, Map<String, SqlElement> sqlMap, ValueDistributor valueDistributor) {
        super();
        this.connection = connection;
        this.sqlElements =sqlMap;
        this.recordSources = new HashMap<String, XLSRecordSource>(); 
        this.valueDistributor = valueDistributor;
    }

    public XLSRecordSource getRecordSet(DinamicLayout layout, List<SqlParametr> parameters) throws SQLException, XlsOutputValueNotFoundException, CloneNotSupportedException, XLSDataLockedException, XLSConnectionBrokenException, IOException, XLSUnknownSourceException{
        boolean isUnboundLayout = false;
        String unboundLayoutValue = "";
        XLSRecordSource result = null;
        String sqlName = layout.getSqlName();
        //check unbound sheet
        if (((sqlName == null) || (sqlName.length() == 0)) && (layout.getCommand() == XLSCommands.XLSCommandSheet)){
            isUnboundLayout = true;
            sqlName = "~~sheet~" + ((Sheet)layout).getSheetName().toLowerCase();
            unboundLayoutValue = ((Sheet)layout).getSheetName();
        }
        //check frame
        if ((layout.getCommand() == XLSCommands.XLSCommandFrame) || (layout.getCommand() == XLSCommands.XLSCommandCrossTab)){
            isUnboundLayout = true;
            sqlName = "~~farme~" + layout.getRegionName(); 
        }
        //look in cache
        if ((sqlName != null) && (sqlName.length() > 0)){
            result = recordSources.get(sqlName);
        } else {
            //TODO exception dynamic has no record source ?  
        }
        SqlElement sqlElement = sqlElements.get(sqlName);
        if (result == null){
            //create source
            if (sqlElement != null){
                DynamicRecordSource drs = new DynamicRecordSource(layout, sqlElement, connection);
                drs.runSql(parameters);
                result = drs;
            } else if (isUnboundLayout){
                //create static source for unbound layout 
                result = new StaticRecordSource(sqlName, unboundLayoutValue);  
            } else if (valueDistributor != null) {
                //lookup in valueDistributor
                String key = XlsUtils.constructExternalValue(sqlName, layout.getColumnName());
                String value = valueDistributor.getValue(key);
                if (value != null){
                    result = new StaticRecordSource(sqlName, value);
                }
            } 
            //put into cache
            if (result != null){
                result.setOwner(layout);
                recordSources.put(sqlName, result);
            } else {
                //exception unknown source
                throw new XLSUnknownSourceException("unknownSource", sqlName, layout.getReferences().getReference());
            }
        } else {//reopen
            result.setOwner(layout);
            if ((sqlElement != null) && (parameters != null) && (sqlElement.getParametrs().size() != parameters.size())){
                throw new XlsOutputValueNotFoundException("wrongSQLparametersNumber", sqlName, layout.getReferences().getReference());
            }
            result.runSql(parameters);
        }
        //set group field
        if (result != null){
            if ((layout.getCommand() != XLSCommands.XLSCommandCell) && (layout.getCommand() != XLSCommands.XLSCommandFrame)){
                result.setGroupField(layout.getColumnName());
            }
        }
        return result;
    }
    
    /**
     * @return the sqlMap
     */
    public Map<String, SqlElement> getSqlMap() {
        return sqlElements;
    }
    
    public void flush() throws SQLException, XLSDataLockedException, IOException{
      for (XLSRecordSource rs :  recordSources.values()){
          rs.release(rs.getOwner());
      }
      recordSources.clear();
    }

    public ValueDistributor getValueDistributor() {
        return valueDistributor;
    }

    public void execute(List<SqlElement> sqlList) throws XLSUnknownSourceException, IOException, SQLException, XLSExecSqlException{
        for (SqlElement sqlElement : sqlList){
            PreparedStatement st= connection.prepareStatement(sqlElement.getSql());
            //prepare parametrs
            String paramVals = "";
            int i = 1;
            for (SqlParametr parameter : sqlElement.getParametrs()){
                String key = XlsUtils.constructExternalValue(parameter.getSqlName(), parameter.getColumnName());
                String val = getValueDistributor().getValue(key);
                if (val == null){
                    throw new XLSUnknownSourceException("unknownSource", parameter.getSqlName(), parameter.getColumnName());
                } else {
                    //set parameter value
                    st.setString(i, val);
                    paramVals = paramVals +"("+ i +")"+ key + ":'" + val +"';";
                }
                i++;
            }
            try {
                st.execute();
            } catch (SQLException e) {
                throw new XLSExecSqlException("execSqlErr", e.getMessage() ,sqlElement.getSql(), paramVals);
            }
        }
    }
}
