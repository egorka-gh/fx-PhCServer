package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sansorm.OrmElf;
import org.sansorm.SqlClosureElf;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.DeliveryType;
import com.photodispatcher.model.mysql.entities.DeliveryTypeDictionary;
import com.photodispatcher.model.mysql.entities.FieldValue;
import com.photodispatcher.model.mysql.entities.MailPackage;
import com.photodispatcher.model.mysql.entities.MailPackageBarcode;
import com.photodispatcher.model.mysql.entities.MailPackageProperty;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.RackOrders;
import com.photodispatcher.model.mysql.entities.RackOrdersLog;
import com.photodispatcher.model.mysql.entities.RackSpace;
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
			
			/*
			//insert/update props
			if(item.getProperties()!=null){
				OrmElf.insertOrUpdateListBatched(connection, item.getProperties());
			}
			//insert/update barcodes
			if(item.getBarcodes()!=null){
				OrmElf.insertOrUpdateListBatched(connection, item.getBarcodes());
			}
			//insert/update messages
			if(item.getMessages()!=null){
				OrmElf.insertOrUpdateListBatched(connection, item.getMessages());
			}
			*/
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
		String sql="SELECT t.source, t.group_id id, t.client_id, 450 state, t.min_ord_state, os.name state_name, os1.name min_ord_state_name, s.name source_name, s.code source_code, t.orders_num, t.state_date"+
				  " FROM (SELECT o.source, o.group_id, o.client_id, MIN(o1.state) min_ord_state, COUNT(DISTINCT o1.id) orders_num, MAX(o1.state_date) state_date"+
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
	public SelectResult<MailPackage> loadByClient(int source, int client){
		String sql="SELECT t.source, t.group_id id, t.client_id, t.state, t.state_date, t.min_ord_state, os.name state_name, os1.name min_ord_state_name, s.name source_name, s.code source_code, t.orders_num"+
				  " FROM (SELECT o.source, o.group_id, o.client_id, MAX(o.state) state, MAX(o.state_date) state_date, MIN(o.state) min_ord_state, COUNT(*) orders_num"+
				  		" FROM orders o"+
				        " WHERE o.source = ? AND o.client_id = ? AND o.state BETWEEN 100 AND 450"+
				        " GROUP BY o.source, o.group_id, o.client_id) t"+
				    " INNER JOIN order_state os ON os.id = t.state"+
				    " INNER JOIN order_state os1 ON os1.id = t.min_ord_state"+
				    " INNER JOIN sources s ON s.id = t.source"+
				  " ORDER BY t.min_ord_state DESC";
		SelectResult<MailPackage> res=runSelect(MailPackage.class,sql, source, client);
		return res;
	}

	@Override
	public SelectResult<MailPackage> loadByState(int state){
		String sql="SELECT p.*, os.name state_name, s.name source_name, s.code source_code" +
					 " FROM package p" +
					   " INNER JOIN order_state os ON os.id = p.state" +
					   " INNER JOIN sources s ON s.id = p.source" +
					  " WHERE p.state = ?" +
					 " ORDER BY p.state_date";
		SelectResult<MailPackage> res=runSelect(MailPackage.class,sql, state);
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
	public SqlResult getStateByOrders(int source, int id){
		int resultCode=0;
		SqlResult result= new SqlResult();
		String sql="SELECT o.source, o.group_id, o.client_id, MIN(o.state) min_ord_state"+
				" FROM orders o"+
				" WHERE o.source = ? AND o.group_id = ?"+
				" GROUP BY o.source, o.group_id, o.client_id";
		SelectResult<MailPackage> subres=runSelect(MailPackage.class,sql, source, id);
		if(!subres.isComplete()){
			result.cloneError(subres);
			return result;
		}
		if(subres.getData()!= null && !subres.getData().isEmpty()) resultCode=subres.getData().get(0).getMin_ord_state();
		result.setResultCode(resultCode);
		return result;
	}

	@Override
	public SqlResult getStateByPackages(int source, List<Integer> packageIds){
		int resultCode=0;
		SqlResult result= new SqlResult();
		if(packageIds==null || packageIds.isEmpty()) return result;

		StringBuilder sbIn=new StringBuilder("");
		for(Integer id : packageIds){
			sbIn.append(id).append(",");
		}
		sbIn.deleteCharAt(sbIn.length() - 1);
		String sIn=sbIn.toString();
		//move orders
		StringBuilder sb= new StringBuilder("SELECT IFNULL(MAX(p.state),0) state FROM package p WHERE p.source = ? AND p.id IN (");
		sb.append(sIn).append(")");
		String sql=sb.toString();

		SelectResult<MailPackage> subres=runSelect(MailPackage.class,sql, source);
		if(!subres.isComplete()){
			result.cloneError(subres);
			return result;
		}
		if(subres.getData()!= null && !subres.getData().isEmpty()) resultCode=subres.getData().get(0).getState();
		result.setResultCode(resultCode);
		return result;
		
	}

	@Override
	public SqlResult startState(MailPackage item){
		SqlResult result= new SqlResult();
		
		//result=persist(item);
		result=runInsertOrUpdate(item);
		
		if(!result.isComplete()) return result;

		String sql;
		//ignore errors
		//set orders state
		sql="UPDATE orders o"+
			 " SET o.state=?, o.state_date=?"+
			 " WHERE o.source=? AND o.group_id=?";
		runDML(sql, item.getState(), item.getState_date(), item.getSource(), item.getId() );
		
		//stop previous extra states
		stopPreviousExtraStates(item);
		
		//start new extra states
		sql="INSERT IGNORE INTO order_extra_state (id, sub_id, state, start_date)"+
			 " SELECT o.id, '', ?, ?"+
			   " FROM orders o"+
			   " WHERE o.source = ? AND o.group_id = ?";
		runDML(sql, item.getState(), item.getState_date(), item.getSource(), item.getId() );
		
		if(item.getState()>=460){
			//clean rack spaces
			sql="DELETE FROM rack_orders WHERE order_id IN (SELECT id FROM orders o WHERE o.source=? AND o.group_id=?)";
			SqlResult subResult= runDML(sql, item.getSource(), item.getId() );
			if(!subResult.isComplete()){
				result.cloneError(subResult);
			}
		}
		
		return result;
	}

	@Override
	public SqlResult stopPreviousExtraStates(MailPackage item){
		String sql="UPDATE order_extra_state"+
					" SET state_date = ?"+
					" WHERE id IN (SELECT o.id"+
								   " FROM orders o"+
								   " WHERE o.source = ? AND o.group_id = ?)"+
					" AND sub_id = '' AND state > 450 AND state < ? AND state_date IS NULL";
		return runDML(sql, item.getState_date(), item.getSource(), item.getId(), item.getState());
	}

	@Override
	public SqlResult join(int source, int targetId, List<Integer> joinIds){
		SqlResult result;
		if(joinIds==null || joinIds.isEmpty()) return new SqlResult();
		//build in list
		StringBuilder sbIn=new StringBuilder("");
		for(Integer id : joinIds){
			sbIn.append(id).append(",");
		}
		sbIn.deleteCharAt(sbIn.length() - 1);
		String sIn=sbIn.toString();
		//move orders
		StringBuilder sb= new StringBuilder("UPDATE orders SET group_id = ? WHERE source=? AND group_id IN (");
		sb.append(sIn).append(")");
		String sql=sb.toString();
		result=runDML(sql, targetId, source);
		
		/*
		//cancel joined 
		if(result.isComplete()){
			sb= new StringBuilder("UPDATE package p SET p.state = 511, p.state_date = NOW() WHERE p.source=? AND p.id IN (");
			sb.append(sIn).append(")");
			sql=sb.toString();
			result=runDML(sql, source);
		}
		*/

		return result;
	}

	@Override
	public SelectResult<DeliveryType> loadDeliveryType(){
		String sql="SELECT * FROM delivery_type dt WHERE dt.id!=0" ;
		return runSelect(DeliveryType.class,sql);
	}
	@Override
	public SqlResult persistsDeliveryTypeBatch(List<DeliveryType> items){
		return runUpdateBatch(items);
	}

	@Override
	public SelectResult<DeliveryTypeDictionary> loadDeliveryTypeDictionar4Edit(int source){
		String sql="SELECT s.id source, dt.id delivery_type, IFNULL(dtd.site_id, 0) site_id, dt.name delivery_type_name, s.name source_name"+
					 " FROM sources s"+
					   " INNER JOIN delivery_type dt"+
					   " LEFT OUTER JOIN delivery_type_dictionary dtd ON dt.id = dtd.delivery_type AND dtd.source = s.id"+
					 " WHERE s.id = ? AND dt.id != 0"+
					 " ORDER BY dt.id" ;
		return runSelect(DeliveryTypeDictionary.class,sql, source);
	}
	
	@Override
	public SqlResult persistsDeliveryTypeDictionaryBatch(List<DeliveryTypeDictionary> items){
		List<DeliveryTypeDictionary> updateList= new ArrayList<DeliveryTypeDictionary>();
		for(DeliveryTypeDictionary item : items){
			if(item.getSite_id()>0){
				updateList.add(item);
			}
		}
		return runInesrtUpdateBatch(updateList);
	}

	@Override
	public SelectResult<DeliveryTypeDictionary> loadDeliveryTypeDictionary(){
		String sql="SELECT dtd.*  FROM delivery_type_dictionary dtd  ORDER BY dtd.source, dtd.delivery_type" ;
		return runSelect(DeliveryTypeDictionary.class,sql);
	}

	@Override
	public SelectResult<RackSpace> loadRackSpaces(MailPackage mailPackage){
		String sql="SELECT r.name rack_name, rs.*"+
				  " FROM orders o"+
				   " INNER JOIN rack_orders ro ON o.id = ro.order_id"+
				   " INNER JOIN rack_space rs ON rs.id = ro.space"+
				   " INNER JOIN rack r ON r.id = rs.rack"+
				  " WHERE o.source = ? AND o.group_id = ?"+
				  " GROUP BY r.id, rs.id"+
				  " ORDER BY r.name, rs.name";
		SelectResult<RackSpace> res=runSelect(RackSpace.class, sql, mailPackage.getSource(), mailPackage.getId());
		return res;
	}

	@Override
	public SelectResult<RackSpace> getRackSpaces(String orderId, int techPoint){
		//PROCEDURE packageGetSpaces(IN pOrderId varchar(50), IN pTechPoint int)
		String sql= "{CALL packageGetSpaces(?,?)}";
		return  runCallSelect(RackSpace.class, sql, orderId, techPoint);
	}

	@Override
	public SelectResult<RackSpace> getOrderSpace(String orderId){
		//PROCEDURE packageGetOrderSpace(IN pOrderId varchar(50))
		String sql= "{CALL packageGetOrderSpace(?)}";
		return  runCallSelect(RackSpace.class, sql, orderId);
	}

	@Override
	public SqlResult setRackSpace(String orderId, int space){
		/*
		String sql="INSERT IGNORE INTO rack_orders (order_id, space) VALUES (?, ?)";
		return runDML(sql, orderId, space);
		*/
		
		//PROCEDURE packageSetOrderSpace(IN pOrderId varchar(50), IN pSpace int)
		SqlResult res= new SqlResult(); 
		String sql= "{CALL packageSetOrderSpace(?,?)}";
		SelectResult<FieldValue> callRes=runCallSelect(FieldValue.class, sql, orderId, space);
		if(!callRes.isComplete()){
			res.cloneError(callRes);
		}else{
			if(callRes.getData()!=null && !callRes.getData().isEmpty()){
				res.setResultCode(callRes.getData().get(0).getValue());
			}
		}
		return res;
	}

	@Override
	public SqlResult resetRackSpace(String orderId){
		String sql="DELETE FROM rack_orders WHERE order_id = ?";
		return runDML(sql, orderId);
	}

	@Override
	public SqlResult clearSpace(int space){
		String sql="DELETE FROM rack_orders WHERE space= ?";
		return runDML(sql, space);
	}

	@Override
	public SelectResult<RackSpace> inventorySpaces(int rack){
		SelectResult<RackSpace> result;
		String sql= "SELECT rs.*, r.name rack_name, IFNULL((SELECT MAX(0) FROM rack_orders ro WHERE ro.space = rs.id), 1) empty"+
					" FROM rack_space rs"+
					" INNER JOIN rack r ON r.id = rs.rack"+
					" WHERE ? IN (0, rs.rack)"+
					" ORDER BY r.name, rs.name";
		result=runSelect(RackSpace.class, sql, rack);
		/*
		if(result.isComplete()){
			if(result.getData()!=null && !result.getData().isEmpty()){
				sql="SELECT ro.order_id id, o.state, os.name state_name, o.source, o.group_id"+
					 " FROM rack_orders ro"+
					   " INNER JOIN orders o ON o.id = ro.order_id"+
					   " INNER JOIN order_state os ON o.state = os.id"+
					 " WHERE ro.space = ?";
				for(RackSpace item : result.getData()){
					SelectResult<Order> subResult=runSelect(Order.class, sql, item.getId());
					if(!subResult.isComplete()){
						result.cloneError(subResult);
						break;
					}
					item.setEmpty(subResult.getData().isEmpty());
					item.setOrders(subResult.getData());
				}
			}
		}
		*/
		return  result;
	}

	@Override
	public SelectResult<RackOrders> inventoryRackOrders(int rack){
		String sql= "SELECT ro.*, o.source, s.name source_name, o.group_id, rs.name space_name, r.id rack, r.name rack_name"+
					 " FROM rack_orders ro"+
					   " INNER JOIN rack_space rs ON rs.id = ro.space"+
					   " INNER JOIN rack r ON r.id = rs.rack"+
					   " LEFT OUTER JOIN orders o ON o.id = ro.order_id"+
					   " LEFT OUTER JOIN sources s ON s.id=o.source"+
					 " WHERE ? IN (0, r.id)";
		return runSelect(RackOrders.class, sql, rack);
	}

	@Override
	public SelectResult<RackOrdersLog> loadOrderSpacesHistory(String order){
		String sql= "SELECT rol.*"+
					 " FROM rack_orders_log rol"+
					 " WHERE rol.order_id LIKE ?"+
					 " ORDER BY rol.event_time DESC";
		return runSelect(RackOrdersLog.class, sql, "%"+order);
	}

}
