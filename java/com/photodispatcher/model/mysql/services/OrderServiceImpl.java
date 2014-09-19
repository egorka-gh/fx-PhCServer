package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sansorm.OrmElf;
import org.sansorm.SqlClosureElf;
import org.sansorm.internal.OrmWriter;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.OrderExtraInfo;
import com.photodispatcher.model.mysql.entities.OrderExtraState;
import com.photodispatcher.model.mysql.entities.OrderExtraStateProlong;
import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.PrintGroupFile;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.Source;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StateLog;
import com.photodispatcher.model.mysql.entities.SubOrder;
import com.photodispatcher.model.mysql.entities.TechLog;

@Service("orderService")
public class OrderServiceImpl extends AbstractDAO implements OrderService {
	
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
		
		String sql="{CALL sync()}";
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
					" FROM orders o"+
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
		sql+=" ORDER BY o.src_date";
		
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
	public SelectResult<Order> loadOrder(String id){
		SelectResult<Order> result;
		String sql="SELECT o.*, s.name source_name, s.code source_code, os.name state_name"+
					" FROM orders o"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" INNER JOIN sources s ON o.source = s.id"+
					" WHERE o.id LIKE ?";
		result=runSelect(Order.class,sql, id);
		if(result.isComplete()){
			if(!result.getData().isEmpty()){
				Order order=result.getData().get(0);
				SelectResult<OrderExtraInfo> eir=loadExtraIfo(id,"");
				if(eir.isComplete()){
					if(!eir.getData().isEmpty()) order.setExtraInfo(eir.getData().get(0));
				}else{
					result.cloneError(eir);
				}
			}
		}
		return result;
	}

	@Override
	public SelectResult<SubOrder> loadSubOrderByPg(String pgId){
		SelectResult<SubOrder> result;
		String sql="SELECT pg.order_id, pg.sub_id, sr.name source_name, sr.code source_code,"+
						" os.name state_name, IFNULL(s.state, o.state) state, IFNULL(s.state_date, o.state_date) state_date,"+
						" IFNULL(s.prt_qty, pg.book_num) prt_qty"+
					" FROM print_group pg"+
					" INNER JOIN orders o ON pg.order_id = o.id"+
					" INNER JOIN sources sr ON o.source = sr.id"+
					" LEFT OUTER JOIN suborders s ON pg.sub_id = s.sub_id"+
					" LEFT OUTER JOIN order_state os ON os.id= IFNULL(s.state, o.state)"+
					" WHERE pg.id = ?";
		result=runSelect(SubOrder.class,sql, pgId);
		if(!result.isComplete()) return result;
		if(!result.getData().isEmpty()){
			SubOrder order=result.getData().get(0);
			SelectResult<OrderExtraInfo> eir=loadExtraIfo(order.getOrder_id(),order.getSub_id());
			if(eir.isComplete()){
			 	if(!eir.getData().isEmpty()) order.setExtraInfo(eir.getData().get(0));
			}else{
				result.cloneError(eir);
			}
		}
		return result;
	}
	@Override
	public SelectResult<SubOrder> loadSubOrderByOrder(String orderId){
		SelectResult<SubOrder> result;
		String sql="SELECT o.id order_id, '' sub_id, sr.name source_name, sr.code source_code,"+
					  " os.name state_name, o.state, o.state_date,"+
					  " (SELECT MAX(pg.book_num) FROM print_group pg WHERE o.id=pg.order_id AND pg.sub_id='') prt_qty"+
					" FROM orders o"+ 
					" INNER JOIN sources sr ON o.source = sr.id"+
					" INNER JOIN order_state os ON os.id= o.state"+
					" WHERE o.id LIKE ? AND NOT EXISTS(SELECT 1 FROM suborders s WHERE s.order_id = o.id)"+
					" UNION ALL"+
					" SELECT s.order_id, s.sub_id, sr.name source_name, sr.code source_code,"+
					   " os.name state_name, s.state, s.state_date, s.prt_qty"+
					" FROM suborders s"+
					" INNER JOIN orders o ON s.order_id = o.id"+
					" INNER JOIN sources sr ON o.source = sr.id"+
					" INNER JOIN order_state os ON os.id= s.state"+
					" WHERE s.order_id LIKE ?";
		result=runSelect(SubOrder.class,sql, orderId, orderId);
		
		if(!result.isComplete()) return result;
		if(!result.getData().isEmpty()){
			SubOrder order=result.getData().get(0);
			SelectResult<OrderExtraInfo> eir=loadExtraIfo(order.getOrder_id(),order.getSub_id());
			if(eir.isComplete()){
				if(!eir.getData().isEmpty()) order.setExtraInfo(eir.getData().get(0));
			}else{
				result.cloneError(eir);
			}
		}
		return result;
	}

	@Override
	public SelectResult<SubOrder> loadSubOrdersOtk(){
		String sql="SELECT * FROM suborderOtkV";
		return runSelect(SubOrder.class,sql);
	}

	
	@Override
	public SelectResult<Order> loadOrderVsChilds(String id){
		SelectResult<Order> result=loadOrder(id);
		Order order;
		if(!result.isComplete()) return result;
		if(!result.getData().isEmpty()){
			order=result.getData().get(0);
			//load direct childs
			String sql;
			//subOrders
			sql="SELECT so.*, st.name src_type_name, bt.name proj_type_name"+
					" FROM suborders so"+
					" INNER JOIN src_type st ON so.src_type=st.id"+
					" INNER JOIN book_type bt ON so.proj_type=bt.id"+
					" WHERE so.order_id=?";
			SelectResult<SubOrder> soRes=runSelect(SubOrder.class,sql, id);
			if(!soRes.isComplete()){
				result.cloneError(soRes);
				return result;
			}
			order.setSuborders(soRes.getData());
			//subOrders extra info
			if(order.getSuborders()!=null){
				for(SubOrder so :order.getSuborders()){
					SelectResult<OrderExtraInfo> soei=loadExtraIfo(id, so.getSub_id());
					if(!soei.isComplete()){
						result.cloneError(soei);
						return result;
					}
					if(soei.getData()!=null && !soei.getData().isEmpty()) so.setExtraInfo(soei.getData().get(0));
				}
			}
			//pgs
			sql="SELECT pg.*, o.source source_id, s.name source_name, o.ftp_folder order_folder, os.name state_name,"+
							" p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name,"+
							" lab.name lab_name, bt.name book_type_name, bp.name book_part_name"+
						" FROM print_group pg"+
							" INNER JOIN orders o ON pg.order_id = o.id"+
							" INNER JOIN sources s ON o.source = s.id"+
							" INNER JOIN order_state os ON pg.state = os.id"+
							" INNER JOIN attr_value p ON pg.paper = p.id"+
							" INNER JOIN attr_value fr ON pg.frame = fr.id"+
							" INNER JOIN attr_value cr ON pg.correction = cr.id"+
							" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
							" INNER JOIN book_type bt ON pg.book_type = bt.id"+
							" INNER JOIN book_part bp ON pg.book_part = bp.id"+
							" LEFT OUTER JOIN lab ON pg.destination = lab.id"+
						" WHERE pg.order_id=?";
			SelectResult<PrintGroup> pgRes=runSelect(PrintGroup.class,sql, id);
			if(!pgRes.isComplete()){
				result.cloneError(pgRes);
				return result;
			}
			order.setPrintGroups(pgRes.getData());
		}
		return result;
	}

	@Override
	public SelectResult<Order> loadOrder4Otk(String id, String sub_id){
		SelectResult<Order> result=loadOrder(id);
		Order order;
		if(!result.isComplete()) return result;
		if(sub_id==null) sub_id="";
		if(!result.getData().isEmpty()){
			order=result.getData().get(0);
			boolean hasSo=sub_id.length()>0;
			//load direct childs
			String sql;
			if(hasSo){
			//subOrders
				sql="SELECT so.*, st.name src_type_name, bt.name proj_type_name"+
						" FROM suborders so"+
						" INNER JOIN src_type st ON so.src_type=st.id"+
						" INNER JOIN book_type bt ON so.proj_type=bt.id"+
						" WHERE so.order_id=? AND so.sub_id=?";
				SelectResult<SubOrder> soRes=runSelect(SubOrder.class,sql, id, sub_id);
				if(!soRes.isComplete()){
					result.cloneError(soRes);
					return result;
				}
				SubOrder so;
				if(!soRes.getData().isEmpty()){
					so=soRes.getData().get(0);
					order.setSuborders(new ArrayList<SubOrder>());
					order.getSuborders().add(so);
					//subOrders extra info
					SelectResult<OrderExtraInfo> soei=loadExtraIfo(id, so.getSub_id());
					if(!soei.isComplete()){
						result.cloneError(soei);
						return result;
					}
					if(soei.getData()!=null && !soei.getData().isEmpty()){
						so.setExtraInfo(soei.getData().get(0));
						order.setExtraInfo(so.getExtraInfo());
					}
				}
			}
			//pgs
			sql="SELECT pg.*,"+
						" p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name,"+
						" bt.name book_type_name, bp.name book_part_name"+
					" FROM print_group pg"+
						" INNER JOIN attr_value p ON pg.paper = p.id"+
						" INNER JOIN attr_value fr ON pg.frame = fr.id"+
						" INNER JOIN attr_value cr ON pg.correction = cr.id"+
						" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
						" INNER JOIN book_type bt ON pg.book_type = bt.id"+
						" INNER JOIN book_part bp ON pg.book_part = bp.id"+
					" WHERE pg.order_id=? AND pg.sub_id=?";
			SelectResult<PrintGroup> pgRes=runSelect(PrintGroup.class,sql, id, sub_id);
			if(!pgRes.isComplete()){
				result.cloneError(pgRes);
				return result;
			}
			order.setPrintGroups(pgRes.getData());

			/*
			//techlog books
			sql="SELECT (tl.sheet DIV 100)*100 sheet, MIN(tl.log_date) log_date"+
				" FROM tech_point tp"+
				" INNER JOIN tech_log tl ON tl.src_id=tp.id"+
				" WHERE tp.tech_type=450 AND tl.sheet>99 AND tl.order_id=? AND tl.sub_id=?"+
				" GROUP BY (tl.sheet DIV 100)*100";
			SelectResult<TechLog> tlRes=runSelect(TechLog.class, sql, id, sub_id);
			if(!tlRes.isComplete()){
				result.cloneError(tlRes);
				return result;
			}
			order.setTechLog(tlRes.getData());
			*/
		}
		return result;
	}

	@Override
	public SelectResult<Order> loadOrderFull(String id){
		SelectResult<Order> result=loadOrderVsChilds(id);
		if(!result.isComplete()) return result; 
		Order order;
		if(!result.getData().isEmpty()){
			order=result.getData().get(0);
			String sql;
			//files
			if(order.getPrintGroups()!=null && !order.getPrintGroups().isEmpty()){
				sql="SELECT pgf.*, pg.path"+
					" FROM print_group pg INNER JOIN print_group_file pgf ON pg.id = pgf.print_group"+
					" WHERE pg.id = ?";
				for (PrintGroup pg : order.getPrintGroups()){
					SelectResult<PrintGroupFile> fRes=runSelect(PrintGroupFile.class,sql, pg.getId());
					if(!fRes.isComplete()){
						result.cloneError(fRes);
						return result;
					}
					pg.setFiles(fRes.getData());
				}
			}
			//techlog
			sql="SELECT tl.* , tp.name tech_point_name, tp.tech_type tech_state, os.name tech_state_name"+
				 " FROM tech_log tl"+
				 " INNER JOIN tech_point tp ON tl.src_id=tp.id"+
				 " INNER JOIN order_state os ON os.id = tp.tech_type"+
				 " WHERE tl.order_id=?"+
				 " ORDER BY tl.log_date";
			SelectResult<TechLog> tlRes=runSelect(TechLog.class, sql, id);
			if(!tlRes.isComplete()){
				result.cloneError(tlRes);
				return result;
			}
			order.setTechLog(tlRes.getData());
			
			//extraState
			sql="SELECT es.*, os.name state_name"+
				" FROM order_extra_state es"+
				 " INNER JOIN order_state os ON os.id = es.state"+
				 " WHERE es.id = ?"+
				" ORDER BY IFNULL(es.state_date, es.start_date)";
			SelectResult<OrderExtraState> esRes=runSelect(OrderExtraState.class, sql, id);
			if(!esRes.isComplete()){
				result.cloneError(esRes);
				return result;
			}
			order.setExtraState(esRes.getData());
			
			//extraStateProlong
			sql="SELECT es.*, os.name state_name"+
					" FROM order_exstate_prolong es"+
					" INNER JOIN order_state os ON os.id=es.state"+
					" WHERE es.id=?"+
					" ORDER BY es.state_date";
			SelectResult<OrderExtraStateProlong> espRes=runSelect(OrderExtraStateProlong.class, sql, id);
			if(!espRes.isComplete()){
				result.cloneError(espRes);
				return result;
			}
			order.setExtraStateProlong(espRes.getData());

			//state log
			sql="SELECT sl.*, os.name state_name"+
					" FROM state_log sl INNER JOIN order_state os ON sl.state = os.id"+
					" WHERE sl.order_id = ?";
			SelectResult<StateLog> slgRes=runSelect(StateLog.class, sql, id);
			if(!slgRes.isComplete()){
				result.cloneError(slgRes);
				return result;
			}
			order.setStateLog(slgRes.getData());
		}
		return result;
	}

	@Override
	public SelectResult<Order> loadOrderBySrcCode(String code, String id){
		SelectResult<Order> result;
		String sql="SELECT o.*, s.name source_name, os.name state_name"+
					" FROM sources s"+
					" INNER JOIN orders o ON o.id= s.id || '_' || ?"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" WHERE s.code=?";
		result=runSelect(Order.class,sql, id, code);
		if(result.isComplete()){
			if(!result.getData().isEmpty()){
				Order order=result.getData().get(0);
				SelectResult<OrderExtraInfo> eir=loadExtraIfo(id,"");
				if(eir.isComplete() && !eir.getData().isEmpty()){
					order.setExtraInfo(eir.getData().get(0));
				}else{
					result.cloneError(eir);
				}
			}
		}
		return result;
	}

	@Override
	public SelectResult<Order> loadOrdersByIds(List<String> ids){
		SelectResult<Order> result= new SelectResult<Order>();
		result.setData(new ArrayList<Order>());
		SelectResult<Order> subResult;
		List<String> inList= new ArrayList<String>();
		StringBuilder in= new StringBuilder("");
		for(String id : ids){
			if(in.length()>200){
				inList.add(in.toString());
				in= new StringBuilder("");
			}
			if(in.length()>0) in.append(",");
			in.append("'"); in.append(id); in.append("'");
		}
		if(in.length()>0) inList.add(in.toString());
		
		String sql="SELECT o.*, s.name source_name, os.name state_name"+
					" FROM orders o"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" INNER JOIN sources s ON o.source = s.id"+
					" WHERE o.id ";
		for (String where :inList){
			subResult=runSelect(Order.class,sql+"IN ("+where+")");
			if(subResult.isComplete()){
				result.getData().addAll(subResult.getData());
			}else{
				result.cloneError(subResult);
				break;
			}
		}
		return result;
	}

	@Override
	public SelectResult<OrderExtraInfo> loadExtraIfo(String id, String subId){
		String sql="SELECT ei.* FROM order_extra_info ei WHERE ei.id=? AND ei.sub_id=?";
		return runSelect(OrderExtraInfo.class,sql, id, subId);
	}

	@Override
	public SelectResult<OrderExtraInfo> loadExtraIfoByPG(String pgId){
		String sql="SELECT ei.*, pg.book_type"+
					" FROM print_group pg"+
					" INNER JOIN order_extra_info ei ON pg.order_id=ei.id AND pg.sub_id=ei.sub_id"+
					" WHERE pg.id=?";
		return runSelect(OrderExtraInfo.class,sql, pgId);
	}
	
	@Override
	public DmlResult<Order> addManual(Order order){
		return runInsert(order);
	}

	@Override
	public SqlResult fillUpOrder(Order order){
		SqlResult result= new SqlResult();
		if(order==null) return result;
		if(order.getState()<199){
			result.setComplete(false);
			result.setErrMesage("Не допустимый статус");
			return result;
		}
		List<OrderExtraInfo> einfos = new ArrayList<OrderExtraInfo>();
		List<SubOrder> subOrders = new ArrayList<SubOrder>();
		List<PrintGroup> printGroups = new ArrayList<PrintGroup>();
		List<PrintGroupFile> pgFiles = new ArrayList<PrintGroupFile>();
		
		//fill lists
		if(order.getExtraInfo()!=null){
			order.getExtraInfo().setId(order.getId());
			order.getExtraInfo().setSub_id("");
			einfos.add(order.getExtraInfo());
		}
		if(order.getSuborders()!=null && !order.getSuborders().isEmpty()){
			for (SubOrder so : order.getSuborders()){
				so.setOrder_id(order.getId());
				if(so.getExtraInfo()!=null){
					so.getExtraInfo().setId(order.getId());
					so.getExtraInfo().setSub_id(so.getSub_id());
					einfos.add(so.getExtraInfo());
				}
				subOrders.add(so);
			}
		}
		if(order.getPrintGroups()!=null && !order.getPrintGroups().isEmpty()){
			for (PrintGroup pg : order.getPrintGroups()){
				pg.setOrder_id(order.getId());
				printGroups.add(pg);
				if(pg.getFiles()!=null){
					for(PrintGroupFile pgFile : pg.getFiles()){
						pgFile.setPrint_group(pg.getId());
						pgFiles.add(pgFile);
					}
				}
			}
		}

		/*
		result=runUpdate(order);
		if(result.isComplete()) result=runInsertBatch(subOrders);
		if(result.isComplete()) result=runInsertBatch(einfos);
		if(result.isComplete()) result=runInsertBatch(printGroups);
		if(result.isComplete()) result=runInsertBatch(pgFiles);
		*/
		
		//run in transaction
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//update order
			OrmElf.updateObject(connection, order);
			//add subOrders
			OrmElf.insertListBatched(connection, subOrders);
			//add extra info
			OrmElf.insertListBatched(connection, einfos);
			//add printGroups
			OrmElf.insertListBatched(connection, printGroups);
			//add files
			OrmElf.insertListBatched(connection, pgFiles);
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
	public SqlResult addReprintPGroups(List<PrintGroup> items){
		SqlResult result= new SqlResult();
		if(items==null ||items.isEmpty()) return result;
		
		String order_id="";
		Set<String> subIds= new HashSet<String>();
		Set<String> parentIds= new HashSet<String>();
		List<PrintGroupFile> pgFiles = new ArrayList<PrintGroupFile>();
		Date dt =new Date();
		for (PrintGroup pg : items){
			order_id=pg.getOrder_id();
			if(pg.getSub_id()!=null && pg.getSub_id().length()>0) subIds.add(pg.getSub_id());
			if(pg.getReprint_id()!=null && pg.getReprint_id().length()>0) parentIds.add(pg.getReprint_id());
			if(pg.getFiles()!=null){
				for(PrintGroupFile pgFile : pg.getFiles()){
					pgFile.setPrint_group(pg.getId());
					pgFiles.add(pgFile);
				}
			}
		}

		/*
		if(result.isComplete()) result=runInsertBatch(items);
		if(result.isComplete()) result=runInsertBatch(pgFiles);
		if(result.isComplete()){
			//set order state
			String sql="UPDATE orders SET state = ?, state_date = ?, reported_state=0 WHERE id = ? AND state > ?";
			result=runDML(sql,210,dt,order_id,210);
			//reset extra
			sql="UPDATE order_extra_state SET state_date=NULL WHERE id=? AND sub_id='' AND state IN (210,250)";
			result=runDML(sql,order_id);
		}
		if(result.isComplete()){
			//set suborders state
			for(String subId : subIds){
				String sql="UPDATE suborders SET state = ?, state_date = ? WHERE order_id = ? AND sub_id = ?";
				result=runDML(sql,210,dt,order_id,subId);
				//reset extra
				sql="UPDATE order_extra_state SET state_date=NULL WHERE id=? AND sub_id=? AND state IN (210,250)";
				result=runDML(sql, order_id, subId);
			}
		}
		if(result.isComplete()){
			//set parent pg state
			for(String subId : parentIds){
				String sql="UPDATE print_group SET state = ?, state_date = ? WHERE id = ?";
				result=runDML(sql,251,dt,subId);
			}
		}
		*/
		
		//run in transaction
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			
			//add printGroups
			OrmElf.insertListBatched(connection, items);
			//add files
			OrmElf.insertListBatched(connection, pgFiles);
			//set order state
			String sql="UPDATE orders SET state = ?, state_date = ?, reported_state=0 WHERE id = ? AND state > ?";
			OrmWriter.executeUpdate(connection, sql, 210, dt, order_id, 210);
			//reset extra
			sql="UPDATE order_extra_state SET state_date=NULL WHERE id=? AND sub_id='' AND state IN (210,250)";
			OrmWriter.executeUpdate(connection, sql,order_id);
			//set suborders state
			for(String subId : subIds){
				sql="UPDATE suborders SET state = ?, state_date = ? WHERE order_id = ? AND sub_id = ?";
				OrmWriter.executeUpdate(connection, sql, 210, dt, order_id, subId);
				//reset extra
				sql="UPDATE order_extra_state SET state_date=NULL WHERE id=? AND sub_id=? AND state IN (210,250)";
				OrmWriter.executeUpdate(connection, sql, order_id, subId);
			}
			//set parent pg state
			for(String subId : parentIds){
				sql="UPDATE print_group SET state = ?, state_date = ? WHERE id = ?";
				OrmWriter.executeUpdate(connection, sql, 251, dt, subId);
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
	public SqlResult cleanUpOrder(String id){
		//PROCEDURE orderCleanUp(IN pId VARCHAR(50))
		String sql= "{CALL orderCleanUp(?)}";
		return runCall(sql, id); 
	}

	@Override
	public SqlResult cancelOrders(String[] ids){
		SqlResult result= new SqlResult();
		//PROCEDURE orderCancel(IN pId VARCHAR(50))
		String sql= "{CALL orderCancel(?)}";
		for (String id : ids){
			result=runCall(sql, id);
			if(!result.isComplete()) return result;
		}
		return result;
	}

}
