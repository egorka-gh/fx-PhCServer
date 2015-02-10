package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.SQLException;

import org.sansorm.OrmElf;
import org.sansorm.SqlClosureElf;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.MailPackage;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("mailPackageService")
public class MailPackageServiceImpl extends AbstractDAO implements MailPackageService{

	
	@Override
	public SqlResult persist(MailPackage item){
		SqlResult result=new SqlResult();
		result.setComplete(true);
		
		//run in transaction
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			
			//insert/update package
			if(item.getPersistState()==0){
				//add new
				OrmElf.insertObject(connection, item);
			}else{
				//update
				//if(item.getPersistState()==-1)
				OrmElf.updateObject(connection, item);
			}
			
			//insert/update props
			if(item.getProperties()!=null){
				OrmElf.insertOrUpdateListBatched(connection, item.getProperties());
			}
			
			//attempt to commit
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

}
