package com.photodispatcher.model.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionFactory {
	private static final String DEFAULT_DATASOURCE_NAME = "dataSource";

	private static Map<String, DataSource> dsMap;
	private static Context envContext = null;

	static {
		Context initContext;
		dsMap = new HashMap<String, DataSource>(5);
		try {
			initContext = new InitialContext();
			envContext = (Context)initContext.lookup("java:/comp/env");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection(String dsKey) throws SQLException{
		DataSource ds=getDataSource(dsKey);
		Connection conn=null;
		if(ds!=null) conn=ds.getConnection();
		return conn;
	}

	public static Connection getConnection() throws SQLException{
		return getConnection(DEFAULT_DATASOURCE_NAME);
	}

	public static DataSource getDataSource(String dsKey){
		if (dsMap.containsKey(dsKey)) return dsMap.get(dsKey); 
		DataSource ds;
		try {
			ds = (DataSource) envContext.lookup(dsKey);
		} catch (NamingException e) {
			System.out.println("Can't resolve DataSource " + dsKey);
			ds=null;
		}
		dsMap.put(dsKey, ds);
		return ds;
	}
	

	public static DataSource getDataSource(){
		return getDataSource(DEFAULT_DATASOURCE_NAME );
	}

}
