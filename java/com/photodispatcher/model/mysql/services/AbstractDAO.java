package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;

import org.sansorm.OrmElf;
import org.sansorm.SqlClosure;
import org.sansorm.SqlClosureElf;
import org.sansorm.internal.OrmWriter;


public abstract class AbstractDAO {

	protected <T> SelectResult<T> runSelect(final Class<T> type, final String sql, final Object...args){
		final SelectResult<T> result= new SelectResult<T>();
		
		result.setData(new SqlClosure<List<T>>(ConnectionFactory.getDataSource()) {
			public List<T> execute(Connection connection) {
				try {
					PreparedStatement pstmt = connection.prepareStatement(sql);
					return OrmElf.statementToList(pstmt, type, args);
				} catch (SQLException e) {
					result.setComplete(false);
					result.setErrCode(e.getErrorCode());
					result.setErrMesage(e.getMessage());
					result.setSql(sql);
					e.printStackTrace();
					return null;
				}
			}
		}.execute());		
		
		return result;
	}
	
	protected <T> DmlResult runInsert(T target, String[] columnNames){
		DmlResult result= new DmlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			OrmElf.insertObject(connection, target, columnNames);
		} catch (SQLException e) {
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			e.printStackTrace();
		}finally{
			SqlClosureElf.quietClose(connection);
		}
		return result;
	}

	protected <T> DmlResult runUpdate(T target, String[] columnNames){
		DmlResult result= new DmlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			OrmElf.updateObject(connection, target, columnNames);
		} catch (SQLException e) {
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			e.printStackTrace();
		}finally{
			SqlClosureElf.quietClose(connection);
		}
		return result;
	}
	
	protected <T> DmlResult runUpdateBatch(List<T> targetList, String[] columnNames){
		DmlResult result= new DmlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			OrmElf.updateListBatched(connection, targetList);
			connection.commit();
		} catch (SQLException e) {
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}finally{
			if(connection!=null){
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			SqlClosureElf.quietClose(connection);
		}
		return result;
	}

	protected <T> DmlResult runInsertBatch(List<T> targetList, String[] columnNames){
		DmlResult result= new DmlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			OrmElf.insertListBatched(connection, targetList);
			connection.commit();
		} catch (SQLException e) {
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}finally{
			if(connection!=null){
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			SqlClosureElf.quietClose(connection);
		}
		return result;
	}

	protected DmlResult runDML(String sql, boolean autoClose, Object... args){
		DmlResult result= new DmlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			OrmWriter.executeUpdate(connection, sql, args);
		} catch (SQLException e) {
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			e.printStackTrace();
		}finally{
			if(autoClose) SqlClosureElf.quietClose(connection);
		}
		return result;
	}
}
