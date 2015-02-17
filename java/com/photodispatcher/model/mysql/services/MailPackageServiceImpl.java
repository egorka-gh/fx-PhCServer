package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.SQLException;

import org.sansorm.OrmElf;
import org.sansorm.SqlClosureElf;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.MailPackage;
import com.photodispatcher.model.mysql.entities.MailPackageBarcode;
import com.photodispatcher.model.mysql.entities.MailPackageProperty;
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
	public SelectResult<MailPackage> load(int source, int id){
		String sql="SELECT p.*, s.name source_name, s.code source_code, os.name state_name"+
					  " FROM package p"+
					   " INNER JOIN sources s ON s.id = p.source"+
					   " INNER JOIN order_state os ON p.state = os.id"+
					  " WHERE p.source = ? AND p.id = ?";
		SelectResult<MailPackage> result=runSelect(MailPackage.class, sql, source, id);
		if(!result.isComplete()) return result;
		if(result.getData()==null || result.getData().isEmpty()) return result;
		
		MailPackage item=result.getData().get(0);

		//load props
		sql="SELECT pp.*, at.name property_name"+
			  " FROM package_prop pp"+
			    " INNER JOIN attr_type at ON at.attr_fml = 6 AND at.field = pp.property"+
			  " WHERE pp.source = ? AND pp.id = ?";
		SelectResult<MailPackageProperty> resProp=runSelect(MailPackageProperty.class, sql, source, id);
		if(!resProp.isComplete()){
			result.cloneError(resProp);
			return result;
		}
		item.setProperties(resProp.getData());
		
		//load orders
		SelectResult<Order> resOrd=loadChildOrders(source, id);
		if(!resOrd.isComplete()){
			result.cloneError(resOrd);
			return result;
		}
		item.setOrders(resOrd.getData());
		
		//load barcodes
		sql="SELECT pb.* FROM package_barcode pb WHERE pb.source = ? AND pb.id = ?";
		SelectResult<MailPackageBarcode> resBar=runSelect(MailPackageBarcode.class, sql, source, id);
		if(!resBar.isComplete()){
			result.cloneError(resBar);
			return result;
		}
		item.setBarcodes(resBar.getData());
		
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

	@Override
	public SelectResult<Order> loadChildOrders(int source, int id){
		SelectResult<Order> result;
		String sql="SELECT o.*, s.name source_name, s.code source_code, os.name state_name"+
					" FROM orders o"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" INNER JOIN sources s ON o.source = s.id"+
					" WHERE o.source = ? AND o.group_id=?";
		result=runSelect(Order.class,sql, source, id);
		return result;
	}
	
	@Override
	public SqlResult startPackaging(MailPackage item, boolean force){
		int resultCode=0;
		SqlResult result= new SqlResult();
		if(!force){
			String sql;
			/*="SELECT t.source, t.group_id id, t.client_id, t.min_ord_state, os.name min_ord_state_name FROM"+
						" (SELECT o.source, o.group_id, o.client_id, MIN(o.state) min_ord_state"+
						  " FROM orders o"+
						  " WHERE o.source = ? AND o.group_id = ?"+
						  " GROUP BY o.source, o.group_id, o.client_id) t"+
						  " INNER JOIN order_state os ON os.id = t.min_ord_state";
						  */
			sql="SELECT o.source, o.group_id, o.client_id, MIN(o.state) min_ord_state"+
				  " FROM orders o"+
				  " WHERE o.source = ? AND o.group_id = ?"+
				  " GROUP BY o.source, o.group_id, o.client_id";
			SelectResult<MailPackage> subres=runSelect(MailPackage.class,sql, item.getSource(), item.getId());
			if(!subres.isComplete()){
				result.cloneError(subres);
				return result;
			}
			if(subres.getData()!= null && !subres.getData().isEmpty()) resultCode=subres.getData().get(0).getMin_ord_state();
			if(resultCode<450){
				result.setResultCode(resultCode);
				return result;
			}
		}
		item.setState(455);
		result=persist(item);
		if(result.isComplete()) result.setResultCode(455);
		//TODO start orders extrastate
		
		return result;
	}

	

}
