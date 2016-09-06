package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.sansorm.OrmElf;
import org.sansorm.SqlClosureElf;
import org.sansorm.internal.OrmWriter;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.OrderFile;
import com.photodispatcher.model.mysql.entities.OrderLoad;
import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.Source;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StateLog;

@Service("orderLoadService")
public class OrderLoadServiceImpl extends AbstractDAO implements OrderLoadService {

	@Override
	public SqlResult beginSync(){
		//clear tmp_orders table
		String sql="DELETE FROM tmp_orders";
		return runDML(sql);
	}

	@Override
	public SqlResult addSyncItems(List<OrderTemp> items){
		return runInsertBatch(items);
	}

	@Override
	public SelectResult<Source> sync(){
		SqlResult sync;
		SelectResult<Source> result= new SelectResult<Source>();
		
		String sql="{CALL sync_4load()}";
		sync=runCall(sql);
		if(!sync.isComplete()){
			result.cloneError(sync);
			return result;
		}
		SourceServiceImpl svc= new SourceServiceImpl(); 
		result=svc.loadAll(Source.LOCATION_TYPE_SOURCE);
		
		return result;
	}

	@Override
	public SelectResult<Order> loadByState(int stateFrom, int stateTo){
		String sql="SELECT o.*, s.name source_name, os.name state_name"+
					" FROM orders_load o"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" INNER JOIN sources s ON o.source = s.id";
		String  where="";
		if(stateFrom!=-1){
			where+=" o.state>=?";
		}
		if(stateTo!=-1){
			if(where.length()>0) where+=" AND";
			where+=" o.state<?";
		}
		if(where.length()>0) where=" WHERE"+where;
		sql+=where;
		
		if(stateFrom==-1 && stateTo==-1){
			return runSelect(Order.class,sql);
		}else if(stateFrom!=-1 && stateTo==-1){
			return runSelect(Order.class,sql,stateFrom);
		}else if(stateFrom==-1 && stateTo!=-1){
			return runSelect(Order.class,sql,stateTo);
		}
		return runSelect(Order.class, sql, stateFrom, stateTo);
	}

	@Override
	public SelectResult<OrderLoad> findeById(String id){
		String sql="SELECT o.*, s.name source_name, s.code source_code, os.name state_name"+
				" FROM orders_load o"+
				" INNER JOIN order_state os ON o.state = os.id"+
				" INNER JOIN sources s ON o.source = s.id"+
				" WHERE o.id LIKE ?";
		return runSelect(OrderLoad.class, sql, id);
	}

	@Override
	public SelectResult<OrderLoad> loadById(String id){
		SelectResult<OrderLoad> result=new SelectResult<OrderLoad>(); 
		String sql="SELECT o.*, s.name source_name, s.code source_code, os.name state_name"+
					" FROM orders_load o"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" INNER JOIN sources s ON o.source = s.id"+
					" WHERE o.id=?";
		result= runSelect(OrderLoad.class, sql, id);
		if(result.getData()!=null && !result.getData().isEmpty()){
			sql="SELECT of.*, os.name state_name"+
					" FROM order_files of"+
					" INNER JOIN order_state os ON of.state = os.id"+
					" WHERE order_id = ?";
			SelectResult<OrderFile> subResult= runSelect(OrderFile.class, sql, id);
			if(!subResult.isComplete()){
				result.cloneError(subResult);
			}else{
				result.getData().get(0).setFiles(subResult.getData());
			}
		}
		return result;
	}

	@Override
	public SelectResult<OrderLoad> loadFull(String id){
		SelectResult<OrderLoad> result=loadById(id);
		if(result.isComplete() && result.getData()!=null && !result.getData().isEmpty()){
			String sql= "SELECT sl.*, os.name state_name"+
						" FROM state_log sl INNER JOIN order_state os ON sl.state = os.id"+
						" WHERE sl.order_id = ?";
			SelectResult<StateLog> slgRes=runSelect(StateLog.class, sql, id);
			if(!slgRes.isComplete()){
				result.cloneError(slgRes);
			}else{
				result.getData().get(0).setStateLog(slgRes.getData());
			}
		}
		return result;
	}

	
	@Override
	public SqlResult saveFile(OrderFile file){
		return runUpdate(file);// runInsertOrUpdate(file);
	}

	@Override
	public SqlResult saveFiles(List<OrderFile> items){
		return runInesrtUpdateBatch(items);
	}

	@Override
	public SqlResult save(OrderLoad order, int fromState){
		SqlResult result= new SqlResult();
		if(order==null) return result;

		if(!checkState(order.getId(), fromState)){
			result.setComplete(false);
			result.setErrCode(-322);
			result.setErrMesage("Ожидаемый статус заказа: " +fromState);
			return result;
		}

		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//update order
			OrmElf.updateObject(connection, order);
			if(order.getFiles()!=null && !order.getFiles().isEmpty()){
				//save files
				OrmElf.insertOrUpdateListBatched(connection, order.getFiles());
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
	public SelectResult<OrderLoad> merge(OrderLoad order, int fromState){
		SelectResult<OrderLoad> result= new SelectResult<OrderLoad>();
		if(order==null) return result;
		if(!checkState(order.getId(), fromState)){
			result.setComplete(false);
			result.setErrCode(-322);
			result.setErrMesage("Ожидаемый статус заказа: " +fromState);
			return result;
		}

		Connection connection = null;
		String sql="";
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//update order
			OrmElf.updateObject(connection, order);
			//sql="UPDATE orders_load SET ftp_folder = ?, fotos_num = ? WHERE id = ?";
			//OrmWriter.executeUpdate(connection, sql, order.getFtp_folder(), order.getFotos_num(), order.getId());

			//merge files
			//set not merged mark
			sql="UPDATE order_files of SET of.chk=1 WHERE of.order_id=?";
			OrmWriter.executeUpdate(connection, sql, order.getId());
			if(order.getFiles()!=null && !order.getFiles().isEmpty()){
				for(OrderFile of : order.getFiles()){
					//delete loaded and changed 
					sql="DELETE FROM order_files"+
							" WHERE order_id=? AND file_name=? AND state>=0 AND (size!=? OR hash_remote!=?)";
					OrmWriter.executeUpdate(connection, sql,
							order.getId(), of.getFile_name(), of.getSize(), of.getHash_remote());
					//add new, update files vs err, set merged mark 4 loaded
					sql="INSERT INTO order_files (order_id, file_name, state, state_date , size, hash_remote, chk)"+
							" VALUES (?,?,?,?,?,?,0)"+
							" ON DUPLICATE KEY UPDATE"+
							" state_date = IF(state < 0, NOW(), state_date), state = IF(state < 0, 102, state), size = ?, hash_remote = ?, chk = 0";
					OrmWriter.executeUpdate(connection, sql,
							order.getId(), of.getFile_name(), of.getState(), of.getState_date(), of.getSize(), of.getHash_remote(),
							of.getSize(), of.getHash_remote());
				}
			}
			//delete not merged
			sql="DELETE FROM order_files WHERE order_id=? AND chk=1";
			OrmWriter.executeUpdate(connection, sql, order.getId());
			
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
		if(result.isComplete()){
			return loadById(order.getId());
		}else{
			return result;
		}
	}

	private Boolean checkState(String id, int state){
		if(state<=0) return true;
		SelectResult<OrderLoad> res=loadById(id);
		if(!res.isComplete()) return false;
		if(res.getData()==null || res.getData().isEmpty()) return true;
		return res.getData().get(0).getState()==state;
	}
}
