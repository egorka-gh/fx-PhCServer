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
import com.photodispatcher.model.mysql.entities.MailPackageBox;
import com.photodispatcher.model.mysql.entities.MailPackageBoxItem;
import com.photodispatcher.model.mysql.entities.MailPackageProperty;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.OrderBook;
import com.photodispatcher.model.mysql.entities.OrderExtraInfo;
import com.photodispatcher.model.mysql.entities.PrintGroup;
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
			//result=runInsertOrUpdate(item);
			OrmElf.insertOrUpdateObject(connection, item);
			/*
			if(item.getPersistState()==0){
				//add new
				OrmElf.insertObject(connection, item);
			}else{
				//update
				//if(item.getPersistState()==-1)
				OrmElf.updateObject(connection, item);
			}
			*/
			
			
			//insert/update props
			if(item.getProperties()!=null){
				OrmElf.insertOrUpdateListBatched(connection, item.getProperties());
			}
			//insert/update barcodes
			if(item.getBarcodes()!=null){
				OrmElf.insertOrUpdateListBatched(connection, item.getBarcodes());
			}
			/*
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

	private SelectResult<MailPackageBox> loadBoxInternal(String boxId){
		//load box 
	 	String sql="SELECT pb.*, os.name state_name"+
					" FROM package_box pb"+
					  " INNER JOIN order_state os ON os.id = pb.state"+
					" WHERE pb.box_id = ?";
	 	SelectResult<MailPackageBox> result = runSelect(MailPackageBox.class, sql, boxId);
		if(!result.isComplete()){
			return result;
		}
		if (result.getData().size() !=1){
			//error
			result.setComplete(false);
			result.setErrMesage("Короб " + boxId + " не найден");
			return result;
		}
		MailPackageBox box=result.getData().get(0);
		//load box items
		SelectResult<MailPackageBoxItem> biRes;
		sql="SELECT bi.box_id, bi.order_id, bi.alias, bi.item_from, bi.item_to, '' sub_id, bt.id book_type, bt.name book_type_name, bi.state, bi.state_date, os.name state_name"+
			  " FROM package_box_item bi"+
			  " INNER JOIN order_state os ON os.id=bi.state"+ 
			  " INNER JOIN print_group pg ON pg.order_id = bi.order_id AND bi.alias = IFNULL(pg.alias, pg.path)"+ 
			  " INNER JOIN book_type bt ON pg.book_type = bt.id"+
			  " WHERE bi.box_id = ?"+ 
			  " GROUP BY bi.box_id, bi.order_id, bi.alias, bi.item_from, bi.item_to, bt.id, bt.name, bi.state, bi.state_date, os.name ";
		biRes = runSelect(MailPackageBoxItem.class, sql, box.getBoxID());
		if(!biRes.isComplete()){
			result.cloneError(biRes);
			return result;
		}
		//fill items with printgroups
		if (biRes.getData()!= null){
			for(MailPackageBoxItem it:biRes.getData()){
				sql="SELECT pg.* FROM print_group pg WHERE pg.order_id = ? AND IFNULL(pg.alias, pg.path) = ?";
				SelectResult<PrintGroup> pgRes = runSelect(PrintGroup.class, sql, it.getOrderID(), it.getAlias());
				if(!pgRes.isComplete()){
					result.cloneError(pgRes);
					return result;
				}
				it.setPrintGroups(pgRes.getData());
			}
		}
		box.setItems(biRes.getData());
		
		//fill box books
		SelectResult<OrderBook> obRes = loadBoxBooks(box.getBoxID());
		if(!obRes.isComplete()){
			result.cloneError(obRes);
			return result;
		}
		box.setBooks(obRes.getData());
		
		return result;
	}

	@Override
	public SelectResult<MailPackage> loadBox(int source, int groupID, String boxId){
		SelectResult<MailPackage> result = load(source, groupID);
		if (!result.isComplete()){
			return result;
		}
		
		if (boxId.length()==0){
			//create default box
			//packageBoxCreate(IN pSource int, IN pGroup int)
			String sql="{CALL packageBoxCreate(?, ?)}";
			SqlResult r = runCall(sql, source, groupID);
			if(!r.isComplete()){
				result.cloneError(r);
				return result;
			}
			boxId=String.valueOf(source)+"-"+String.valueOf(groupID);
			//TODO barcode, price weight is not filled
		}
		//load box 
	 	SelectResult<MailPackageBox> bres = loadBoxInternal(boxId);
		if(!bres.isComplete()){
			result.cloneError(bres);
			return result;
		}
		MailPackageBox box=bres.getData().get(0);
		//remove orders not in box
		List<Order> ol = new ArrayList<Order>();
		for (Order o : result.getData().get(0).getOrders()){
			for (MailPackageBoxItem bi : box.getItems()){
				if (o.getId().equals(bi.getOrderID())){
					ol.add(o);
					break;
				}
			}
		}
		result.getData().get(0).setOrders(ol);

		result.getData().get(0).setBox(box);
		
		SelectResult<RackSpace>  rsRes =  loadBoxSpace(box.getSource() , box.getPackageID(), box.getBoxID());
		if(!rsRes.isComplete()){
			result.cloneError(rsRes);
			return result;
		}
		
		if (rsRes.getData()!=null && !rsRes.getData().isEmpty()){
			result.getData().get(0).setRackSpace(rsRes.getData().get(0));	
		}
		
		return result;
	}

	@Override
	public SelectResult<MailPackage> loadBoxByPG(String printgroupID, int book){
		SelectResult<MailPackage> result = new SelectResult<MailPackage>();
		String sql="SELECT o.group_id AS package_id, o.source, IFNULL(bi.box_id, '') AS box_id"+
					 " FROM print_group pg"+
					   " INNER JOIN orders o ON o.id = pg.order_id"+
					   " LEFT OUTER JOIN package_box_item bi ON bi.order_id = pg.order_id AND bi.alias = pg.path"+
					     " AND (pg.book_type NOT IN (1, 2, 3) OR ? BETWEEN bi.item_from AND bi.item_to)"+
					 " WHERE pg.id = ?";
		SelectResult<MailPackageBox> bres=runSelect(MailPackageBox.class, sql, book, printgroupID);
		if(!bres.isComplete()){
			result.cloneError(bres);
			return result;
		}
		if(bres.getData()==null || bres.getData().isEmpty()) return result;
		MailPackageBox box = bres.getData().get(0);
		
		return loadBox(box.getSource(), box.getPackageID(), box.getBoxID());
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
		return res;
	}

	@Override
	public SelectResult<MailPackage> loadByClient(int source, int client){
		
		String sql="SELECT t.source, t.group_id id, t.client_id, t.state, t.state_date, t.src_date, t.min_ord_state, os.name state_name, os1.name min_ord_state_name, s.name source_name, s.code source_code, t.orders_num"+
				  " FROM (SELECT o.source, o.group_id, o.client_id, MAX(o.state) state, MAX(o.state_date) state_date, MIN(o.src_date) src_date, MIN(o.state) min_ord_state, COUNT(*) orders_num"+
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
	public SelectResult<MailPackageBox> loadBoxesByState(int state){
		String sql="SELECT pb.*, s.name source_name, s.code source_code, os.name state_name"+
					" FROM package_box pb"+
					  " INNER JOIN sources s ON pb.source = s.id"+
					  " INNER JOIN order_state os ON os.id = pb.state"+
					" WHERE pb.state = ?"+
					" ORDER BY pb.state_date";
		SelectResult<MailPackageBox> res=runSelect(MailPackageBox.class,sql, state);
		return res;
	}

	@Override
	public SelectResult<Order> loadChildOrders(int source, int id){
		SelectResult<Order> result;
		String sql="SELECT o.*, s.name source_name, s.code source_code, os.name state_name"+
					" FROM orders o"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" INNER JOIN sources s ON o.source = s.id"+
					" WHERE o.source = ? AND o.group_id=? AND o.state<500";
		result=runSelect(Order.class,sql, source, id);
		if (result.isComplete()){
			OrderService osvc = new OrderServiceImpl();
			for (Order o : result.getData()){
				SelectResult<OrderExtraInfo> res  = osvc.loadExtraIfo(o.getId(), "");
				if(!res.isComplete()){
					result.cloneError(res);
					return result;
				}
				if (res.getData().size()>0) o.setExtraInfo(res.getData().get(0));
			}
		}
		return result;
	}
	
	/*
	private SelectResult<OrderBook> loadDefaultBoxBooks(int source, int packageID){
		//load books for whole package 
		//TODO add subID?
		String sql="SELECT t.order_id, t.alias, t.book_type, t.book, t.state, os.name state_name, bt.name book_type_name"+
			 " FROM (SELECT pg.order_id, pg.alias, pg.book_type, IFNULL(ob.book,0) book, MIN(IFNULL(ob.state,pg.state)) state"+
			     " FROM orders o"+
			       " INNER JOIN print_group pg ON o.id = pg.order_id AND pg.is_reprint = 0"+
			       " LEFT OUTER JOIN order_books ob ON ob.pg_id = pg.id"+
			      " WHERE o.source = ? AND o.group_id = ?"+
			     " GROUP BY pg.order_id, pg.alias, pg.book_type, ob.book) t"+
			   " INNER JOIN order_state os ON t.state = os.id"+
			   " INNER JOIN book_type bt ON t.book_type = bt.id" ;
		return runSelect(OrderBook.class,sql, source, packageID);
	}
	*/
	
	private SelectResult<OrderBook> loadBoxBooks(String boxID){
		String sql="SELECT pg.order_id, pg.alias, pg.book_type, IFNULL(ob.pg_id, pg.id) pg_id, IFNULL(ob.target_pg, pg.id) target_pg, IFNULL(ob.is_rejected, 0) is_rejected, IFNULL(ob.is_reject, 0) is_reject, IFNULL(ob.book, 0) book, IFNULL(ob.state, pg.state) state, os.name state_name, bt.name book_type_name"+
					" FROM package_box pb"+
					  " INNER JOIN package_box_item bi ON pb.box_id = bi.box_id"+
					  " INNER JOIN print_group pg ON pg.order_id = bi.order_id AND pg.alias = bi.alias AND pg.is_reprint = 0"+
					  " INNER JOIN book_type bt ON pg.book_type = bt.id"+
					  " LEFT OUTER JOIN order_books ob ON ob.pg_id = pg.id AND ob.book BETWEEN bi.item_from AND bi.item_to"+
					  " LEFT OUTER JOIN order_state os ON os.id = IFNULL(ob.state, pg.state)"+
					" WHERE pb.box_id = ?";
		return runSelect(OrderBook.class,sql, boxID);
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
		
		result=persist(item);
		//result=runInsertOrUpdate(item);
		
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
					//" AND sub_id = '' AND state > 450 AND state < ? AND state_date IS NULL";
					" AND sub_id = '' AND state < ? AND state_date IS NULL";
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
		String sql="SELECT s.id source, dt.id delivery_type, IFNULL(dtd.site_id, 0) site_id, IFNULL(dtd.set_send, 0) set_send, dt.name delivery_type_name, s.name source_name"+
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
		String sql="SELECT dtd.*  FROM delivery_type_dictionary dtd WHERE dtd.delivery_type!=0 ORDER BY dtd.source, dtd.delivery_type" ;
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
	public SelectResult<FieldValue> getProductsCount(int source, int id){
		String sql= "SELECT IF(bt.id = 0, 'Фото', bt.name) label, IF(bt.id = 0, SUM(pg.prints), SUM(pg.book_num)) value"+
					 " FROM orders o"+
					   " INNER JOIN print_group pg ON pg.order_id = o.id AND pg.is_reprint = 0 AND pg.book_part IN (0, 2, 5)"+
					   " INNER JOIN book_type bt ON pg.book_type = bt.id"+
					  " WHERE o.group_id = ? AND o.source = ? AND o.state < 500"+
					 " GROUP BY IF(bt.id = 0, 'Фото', bt.name)";
		return  runSelect(FieldValue.class, sql, id, source);
	}

	@Override
	public SelectResult<RackSpace> getOrderSpace(String orderId){
		//PROCEDURE packageGetOrderSpace(IN pOrderId varchar(50))
		String sql= "{CALL packageGetOrderSpace(?)}";
		return  runCallSelect(RackSpace.class, sql, orderId);
	}

	@Override
	public SqlResult setRackSpace(String orderId, int space){
		
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
		SqlResult res = runDML(sql, space);
		if (! res.isComplete()){
			return res;
		}
		sql = "UPDATE rack_space rs SET rs.package_source = 0, rs.package_id = 0, rs.box_id = '' WHERE rs.id = ?";
		return runDML(sql, space);
	}

	@Override
	public SelectResult<RackSpace> usedSpaces(int techPoint, int state){
		String sql= "SELECT rs.*, r.name rack_name, s.name source_name, pb.state, pb.state_date, os.name state_name, pb.box_num"+
					" FROM rack_tech_point rtp"+
					  " INNER JOIN rack_space rs ON rtp.rack = rs.rack AND rs.package_id != 0"+
					  " INNER JOIN rack r ON rs.rack = r.id"+
					  " INNER JOIN sources s ON rs.package_source = s.id"+
					  " INNER JOIN package_box pb ON rs.box_id = pb.box_id"+
					  " INNER JOIN order_state os ON pb.state = os.id"+
					" WHERE rtp.tech_point = ? AND pb.state = ?"+
					" ORDER BY r.name, rs.name";
		return runSelect(RackSpace.class, sql, techPoint, state);
	}

	@Override
	public SelectResult<RackSpace> getBoxSpace(int source,int pPackage, String box, int techPoint){
		// load or allocate rack space 
		//packageGetBoxSpace(IN pSource int, IN pPackage int, IN pBox varchar(50), IN pTechPoint int)
		String sql= "{CALL packageGetBoxSpace(?, ?, ?, ?)}";
		return runCallSelect(RackSpace.class, sql, source, pPackage, box, techPoint);
	}
	@Override
	public SelectResult<RackSpace> loadBoxSpace(int source,int pPackage, String box){
		// load rack space 
		String sql= "SELECT rs.*, r.name rack_name"+
					 " FROM rack_space rs INNER JOIN rack r ON rs.rack = r.id"+
					 " WHERE rs.package_source = ? AND rs.package_id = ? AND rs.box_id = ?";
		return runSelect(RackSpace.class, sql, source, pPackage, box);
	}

	@Override
	public SqlResult BoxStartOTK(String boxID, String pgID){
		//packageBoxStartOTK(IN pBoxID VARCHAR(50), IN pPgID VARCHAR(50))
		String sql= "{CALL packageBoxStartOTK(?,?)}";
		return runCall(sql, boxID, pgID);
	}

	
	@Override
	public SelectResult<RackSpace> inventorySpaces(int rack){
		//TODO not in use (old ver)
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

	@Override
	public SqlResult  setBoxItemOTK(String boxID, String orderID, String alias){
		//packageSetBoxItemOTK(IN pBox varchar(50), IN pOrder varchar(50), IN pAlias varchar(100))
		String sql= "{CALL packageSetBoxItemOTK(?,?,?)}";
		SelectResult<FieldValue> fv =runCallSelect(FieldValue.class, sql, boxID, orderID, alias);
		if (!fv.isComplete()) return fv;
		SqlResult r = new SqlResult();
		r.setComplete(true);
		r.setResultCode(fv.getData().get(0).getValue());
		return r;
	}

	@Override
	public SqlResult  setBoxOTK(String boxID){
		//packageSetBoxOTK(IN pBox varchar(50))
		String sql= "{CALL packageSetBoxOTK(?)}";
		SelectResult<FieldValue> fv =runCallSelect(FieldValue.class, sql, boxID);
		if (!fv.isComplete()) return fv;
		SqlResult r = new SqlResult();
		r.setComplete(true);
		r.setResultCode(fv.getData().get(0).getValue());
		return r;
	}

	@Override
	public SqlResult setBoxPacked(String boxID){
		//packageSetBoxPacked(IN pBox varchar(50))
		String sql= "{CALL packageSetBoxPacked(?)}";
		SelectResult<FieldValue> fv =runCallSelect(FieldValue.class, sql, boxID);
		if (!fv.isComplete()) return fv;
		SqlResult r = new SqlResult();
		r.setComplete(true);
		r.setResultCode(fv.getData().get(0).getValue());
		return r;
	}

	@Override
	public SqlResult setBoxSend(String boxID){
		//packageSetBoxSend(IN pBox varchar(50))
		String sql= "{CALL packageSetBoxSend(?)}";
		SelectResult<FieldValue> fv =runCallSelect(FieldValue.class, sql, boxID);
		if (!fv.isComplete()) return fv;
		SqlResult r = new SqlResult();
		r.setComplete(true);
		r.setResultCode(fv.getData().get(0).getValue());
		return r;
	}

	@Override
	public SqlResult setBoxIncomplete(String boxId, MailPackageBoxItem[] items, OrderBook[] books ){
		String sql = "UPDATE package_box pb SET pb.state = 449, pb.state_date = NOW() WHERE pb.box_id = ?";
		SqlResult r = runDML(sql, boxId);
		if(!r.isComplete()) return r;
		sql = "UPDATE package_box_item pbi SET pbi.state = 449 WHERE pbi.box_id = ? AND pbi.order_id = ? AND pbi.alias = ?";
		for(MailPackageBoxItem it : items){
			r = runDML(sql, it.getBoxID(), it.getOrderID(), it.getAlias());
			if(!r.isComplete()) return r;
		}
		OrderStateServiceImpl svc = new OrderStateServiceImpl();
		for (OrderBook book : books){
			r = svc.setEntireBookState(book);
			if(!r.isComplete()) return r;
		}
		return r;
	}

}
