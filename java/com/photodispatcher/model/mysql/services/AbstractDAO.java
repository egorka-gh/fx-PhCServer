package com.photodispatcher.model.mysql.services;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

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
	public static final int MAX_RETRY_ATTEMPTS=5;
	public static final int RETRY_WAIT_TIME=2000;
	public static final int RETRY_WAIT_DEV=1000;

	private static Logger logger = Logger.getLogger(AbstractDAO.class.getName());
	
	protected boolean hideTrace=false;
	
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
					if(!hideTrace) e.printStackTrace();
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
					if(!hideTrace) e.printStackTrace();
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
			if(!hideTrace) e.printStackTrace();
		}finally{
			SqlClosureElf.quietClose(connection);
		}
		return result;
	}

	protected <T> SqlResult runInsertOrUpdate(T target){
		SqlResult result= new SqlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			//TODO refactor to SqlClosure
			OrmElf.insertOrUpdateObject(connection, target);
		} catch (SQLException e) {
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			if(!hideTrace) e.printStackTrace();
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
			if(!hideTrace) e.printStackTrace();
		}finally{
			SqlClosureElf.quietClose(connection);
		}
		return result;
	}
	
	
	protected <T> SqlResult runPersistBatch(List<T> targetList){
		SqlResult result=new SqlResult();
		if(targetList==null) return result;
		
		List<T> insertList=new ArrayList<T>();
		List<T> updateList=new ArrayList<T>();

		for(T item : targetList){
			if(item instanceof AbstractEntity){
				if(((AbstractEntity) item).getPersistState()==0){
					insertList.add(item);
				}else if(((AbstractEntity) item).getPersistState()==-1){
					updateList.add(item);
				}
			}
		}
		if(!insertList.isEmpty()){
			result=runInsertBatch(insertList);
		}
		if(result.isComplete() && !updateList.isEmpty()){
			result=runUpdateBatch(updateList);
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
			if(!hideTrace) e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				if(!hideTrace) e1.printStackTrace();
			}
		}finally{
			if(connection!=null){
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					if(!hideTrace) e.printStackTrace();
				}
			}
			SqlClosureElf.quietClose(connection);
		}
		return result;
	}

	protected <T> SqlResult runInesrtUpdateBatch(List<T> targetList){
		SqlResult result= new SqlResult();
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//TODO refactor to SqlClosure
			OrmElf.insertOrUpdateListBatched(connection, targetList);
			connection.commit();
			for(T item : targetList) if(item instanceof AbstractEntity) ((AbstractEntity) item).setPersistState(1);
		} catch (SQLException e) {
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			if(!hideTrace) e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				if(!hideTrace) e1.printStackTrace();
			}
		}finally{
			if(connection!=null){
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					if(!hideTrace) e.printStackTrace();
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
			if(!hideTrace) e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				if(!hideTrace) e1.printStackTrace();
			}
		}finally{
			if(connection!=null){
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					if(!hideTrace) e.printStackTrace();
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
			if(!hideTrace) e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				if(!hideTrace) e1.printStackTrace();
			}
		}finally{
			if(connection!=null){
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					if(!hideTrace) e.printStackTrace();
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
			if(!hideTrace) e.printStackTrace();
		}finally{
			SqlClosureElf.quietClose(connection);
		}
		return result;
	}
	
	protected SqlResult runCall(String sql, Object... args){
		SqlResult result= new SqlResult();
		Connection connection = null;
		Random random= new Random();
		for (int i = 0; i < MAX_RETRY_ATTEMPTS; i++) {
			try {
				connection=ConnectionFactory.getConnection();
				OrmWriter.executeCall(connection, sql, args);
				if(i>0) logger.info("Complite after deadlock, attempt:"+i);
				i = MAX_RETRY_ATTEMPTS;
			} catch (SQLException e) {
				try {
					connection.rollback();
				} catch (SQLException e1) {
					//e1.printStackTrace();
				}
				if(e.getErrorCode()!=1213 || i==(MAX_RETRY_ATTEMPTS-1)){
					//not deadlock or max attempt reached
					result.setComplete(false);
					result.setErrCode(e.getErrorCode());
					result.setErrMesage(e.getMessage());
					if(e.getErrorCode()==1213){
						logger.severe("Deadlock error attempt:"+i+"; Code:"+e.getErrorCode()+"; Message:"+e.getMessage()+"; sql:"+sql);
					}else{
						if(!hideTrace) logger.severe("SQLException Code:"+e.getErrorCode()+"; Message:"+e.getMessage()+"; sql:"+sql);
					}
					i = MAX_RETRY_ATTEMPTS;
					if(!hideTrace) e.printStackTrace();
				}else{
					//restart deadlock
					logger.warning("Deadlock detected, attempt:"+i+"; Code:"+e.getErrorCode()+"; Message:"+e.getMessage()+"; sql:"+sql);
					try {
						Thread.sleep(RETRY_WAIT_TIME+random.nextInt(RETRY_WAIT_DEV));
					} catch (InterruptedException e1) {
						if(!hideTrace) e1.printStackTrace();
					}
					logger.warning("Restart after deadlock.");
				}
			}finally{
				SqlClosureElf.quietClose(connection);
			}
		}
		return result;
	}

	protected <T> SelectResult<T> runCallSelect(final Class<T> type, final String sql, final Object...args){
		final SelectResult<T> result= new SelectResult<T>();
		
		result.setData(new SqlClosure<List<T>>(ConnectionFactory.getDataSource()) {
			public List<T> execute(Connection connection) {
				try {
					CallableStatement pstmt = connection.prepareCall(sql);
					return OrmElf.statementToList(pstmt, type, args);
				} catch (SQLException e) {
					result.setComplete(false);
					result.setErrCode(e.getErrorCode());
					result.setErrMesage(e.getMessage());
					result.setSql(sql);
					if(!hideTrace) e.printStackTrace();
					return null;
				}
			}
		}.execute());		
		
		return result;
	}

}
