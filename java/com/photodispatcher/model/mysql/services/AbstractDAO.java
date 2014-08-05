package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.AbstractEntity;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

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

	protected <T> DmlResult<T> getObject(final Class<T> type, final Object... ids){
		final DmlResult<T> result= new DmlResult<T>();
		
		result.setItem(new SqlClosure<T>(ConnectionFactory.getDataSource()) {
			public T execute(Connection connection) {
				try {
					return OrmElf.objectById(connection, type, ids);
				} catch (SQLException e) {
					result.setComplete(false);
					result.setErrCode(e.getErrorCode());
					result.setErrMesage(e.getMessage());
					e.printStackTrace();
					return null;
				}
			}
		}.execute());		
		
		return result;
	}

	protected <T> DmlResult<T> runInsert(T target){
		DmlResult<T> result= new DmlResult<T>();
		result.setItem(target);
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			//TODO refactor to SqlClosure
			OrmElf.insertObject(connection, target);
			if (target instanceof AbstractEntity) ((AbstractEntity )target).setPersistState(1);
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

	protected <T> DmlResult<T> runUpdate(T target){
		DmlResult<T> result= new DmlResult<T>();
		result.setItem(target);
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			//TODO refactor to SqlClosure
			OrmElf.updateObject(connection, target);
			if (target instanceof AbstractEntity) ((AbstractEntity )target).setPersistState(1);
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
	
	protected <T> SqlResult runUpdateBatch(List<T> targetList){
		SqlResult result= new SqlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//TODO refactor to SqlClosure
			OrmElf.updateListBatched(connection, targetList);
			connection.commit();
			for(T item : targetList) if(item instanceof AbstractEntity) ((AbstractEntity) item).setPersistState(1);
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

	protected <T> SqlResult runInsertBatch(List<T> targetList){
		SqlResult result= new SqlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//TODO refactor to SqlClosure
			OrmElf.insertListBatched(connection, targetList);
			connection.commit();
			for(T item : targetList) if(item instanceof AbstractEntity) ((AbstractEntity) item).setPersistState(1);
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

	protected <T> SqlResult runDeleteBatch(List<T> targetList){
		SqlResult result= new SqlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//TODO refactor to SqlClosure
			OrmElf.deleteListBatched(connection, targetList);
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

	protected SqlResult runDML(String sql, Object... args){
		SqlResult result= new SqlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			//TODO refactor to SqlClosure
			OrmWriter.executeUpdate(connection, sql, args);
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
}
