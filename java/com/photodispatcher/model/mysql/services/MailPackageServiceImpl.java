package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.SQLException;

import org.sansorm.OrmElf;
import org.sansorm.SqlClosureElf;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.MailPackage;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.SelectResult;
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
			//insert/update barcodes
			if(item.getBarcodes()!=null){
				OrmElf.insertOrUpdateListBatched(connection, item.getBarcodes());
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
	
	@Override
	public SelectResult<MailPackage> loadReady4Mail(){
		String sql="SELECT t.source, t.group_id id, t.client_id, 450 state, t.min_ord_state, os.name state_name, os1.name min_ord_state_name, s.name source_name, s.code source_code"+
				  " FROM (SELECT o.source, o.group_id, o.client_id, MIN(o1.state) min_ord_state"+
				     " FROM orders o"+
				       " INNER JOIN orders o1 ON o.source = o1.source AND o.group_id = o1.group_id"+
				     " WHERE o.state = 450 AND o.group_id != 0"+
				     " GROUP BY o.source, o.group_id, o.client_id) t"+
				    " INNER JOIN order_state os ON os.id = 450"+
				    " INNER JOIN order_state os1 ON os1.id = t.min_ord_state"+
				    " INNER JOIN sources s ON s.id = t.source"+
				  " ORDER BY t.min_ord_state DESC";
		SelectResult<MailPackage> res=runSelect(MailPackage.class,sql);
		/*
		if(res.isComplete()){
			for ( MailPackage mp : res.getData()){
				sql="SELECT o.id, o.state, o.state_date, s.code source_code, os.name state_name"+
					 " FROM orders o"+
					   " INNER JOIN order_state os ON o.state = os.id"+
					   " INNER JOIN sources s ON o.source = s.id"+
					  " WHERE o.source = ? AND o.group_id = ?";
				SelectResult<Order> subRes=runSelect(Order.class, sql, mp.getSource(), mp.getId());
				if(subRes.isComplete()){
					mp.setOrders(subRes.getData());
				}else{
					res.cloneError(subRes);
				}
			}
		}
		*/
		return res;
	}


}
