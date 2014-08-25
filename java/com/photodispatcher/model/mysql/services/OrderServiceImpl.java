package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

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
		String sql="DELETE FROM phcdata.tmp_orders";
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
		
		String sql="{CALL phcdata.sync()}";
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
					" FROM phcdata.orders o"+
					" INNER JOIN phcconfig.order_state os ON o.state = os.id"+
					" INNER JOIN phcconfig.sources s ON o.source = s.id";
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
		String sql="SELECT o.*, s.name source_name, os.name state_name,"+
					" FROM phcdata.orders o"+
					" INNER JOIN phcconfig.order_state os ON o.state = os.id"+
					" INNER JOIN phcconfig.sources s ON o.source = s.id"+
					" WHERE o.id LIKE ?";
		result=runSelect(Order.class,sql, id);
		if(result.isComplete() && !result.getData().isEmpty()){
			Order order=result.getData().get(0);
			SelectResult<OrderExtraInfo> eir=loadExtraIfo(id,"");
			if(eir.isComplete() && !eir.getData().isEmpty()){
				order.setExtraInfo(eir.getData().get(0));
			}else{
				result.cloneError(eir);
			}
		}
		return result;
	}

	@Override
	public SelectResult<Order> loadOrderVsChilds(String id){
		SelectResult<Order> result=loadOrder(id);
		Order order;
		if(result.isComplete() && !result.getData().isEmpty()){
			order=result.getData().get(0);
			//load direct childs
			String sql;
			//subOrders
			sql="SELECT so.*, st.name src_type_name, bt.name proj_type_name"+
					" FROM phcdata.suborders so"+
					" INNER JOIN phcconfig.src_type st ON so.src_type=st.id"+
					" INNER JOIN phcconfig.book_type bt ON so.proj_type=bt.id"+
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
						" FROM phcdata.print_group pg"+
							" INNER JOIN phcdata.orders o ON pg.order_id = o.id"+
							" INNER JOIN phcconfig.sources s ON o.source = s.id"+
							" INNER JOIN phcconfig.order_state os ON pg.state = os.id"+
							" INNER JOIN phcconfig.attr_value p ON pg.paper = p.id"+
							" INNER JOIN phcconfig.attr_value fr ON pg.frame = fr.id"+
							" INNER JOIN phcconfig.attr_value cr ON pg.correction = cr.id"+
							" INNER JOIN phcconfig.attr_value cu ON pg.cutting = cu.id"+
							" INNER JOIN phcconfig.book_type bt ON pg.book_type = bt.id"+
							" INNER JOIN phcconfig.book_part bp ON pg.book_part = bp.id"+
							" LEFT OUTER JOIN phcconfig.lab ON pg.destination = lab.id"+
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
	public SelectResult<Order> loadOrderFull(String id){
		SelectResult<Order> result=loadOrderVsChilds(id);
		Order order;
		if(result.isComplete() && !result.getData().isEmpty()){
			order=result.getData().get(0);
			String sql;
			//files
			if(order.getPrintGroups()!=null && !order.getPrintGroups().isEmpty()){
				sql="SELECT pgf.*, pg.path"+
					" FROM phcdata.print_group pg INNER JOIN phcdata.print_group_file pgf ON pg.id = pgf.print_group"+
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
				 " FROM phcdata.tech_log tl"+
				 " INNER JOIN phcconfig.tech_point tp ON tl.src_id=tp.id"+
				 " INNER JOIN phcconfig.order_state os ON os.id = tp.tech_type"+
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
				" FROM phcdata.order_extra_state es"+
				 " INNER JOIN phcconfig.order_state os ON os.id = es.state"+
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
					" FROM phcdata.order_exstate_prolong es"+
					" INNER JOIN phcconfig.order_state os ON os.id=es.state"+
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
					" FROM phcdata.state_log sl INNER JOIN phcconfig.order_state os ON sl.state = os.id"+
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
		String sql="SELECT o.*, s.name source_name, os.name state_name,"+
					" FROM phcconfig.sources s"+
					" INNER JOIN phcdata.orders o ON o.id= s.id || '_' || ?"+
					" INNER JOIN phcconfig.order_state os ON o.state = os.id"+
					" WHERE s.code=?";
		result=runSelect(Order.class,sql, id, code);
		if(result.isComplete() && !result.getData().isEmpty()){
			Order order=result.getData().get(0);
			SelectResult<OrderExtraInfo> eir=loadExtraIfo(id,"");
			if(eir.isComplete() && !eir.getData().isEmpty()){
				order.setExtraInfo(eir.getData().get(0));
			}else{
				result.cloneError(eir);
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
		
		String sql="SELECT o.*, s.name source_name, os.name state_name,"+
					" FROM phcdata.orders o"+
					" INNER JOIN phcconfig.order_state os ON o.state = os.id"+
					" INNER JOIN phcconfig.sources s ON o.source = s.id"+
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
		String sql="SELECT ei.* FROM phcdata.order_extra_info ei WHERE ei.order_id=? AND ei.sub_id=?";
		return runSelect(OrderExtraInfo.class,sql, id, subId);
	}

	@Override
	public SelectResult<OrderExtraInfo> loadExtraIfoByPG(String pgId){
		String sql="SELECT ei.*, pg.book_type"+
					" FROM phcdata.print_group pg"+
					" INNER JOIN phcdata.order_extra_info ei ON pg.order_id=ei.order_id AND pg.sub_id=ei.sub_id"+
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
		List<OrderExtraInfo> einfos = new ArrayList<OrderExtraInfo>();
		List<SubOrder> subOrders = new ArrayList<SubOrder>();
		List<PrintGroup> printGroups = new ArrayList<PrintGroup>();
		List<PrintGroupFile> pgFiles = new ArrayList<PrintGroupFile>();
		
		//fill lists
		if(order.getExtraInfo()!=null) einfos.add(order.getExtraInfo());
		if(order.getSuborders()!=null && !order.getSuborders().isEmpty()){
			for (SubOrder so : order.getSuborders()){
				if(so.getExtraInfo()!=null) einfos.add(so.getExtraInfo());
				subOrders.add(so);
			}
		}
		if(order.getPrintGroups()!=null && !order.getPrintGroups().isEmpty()){
			for (PrintGroup pg : order.getPrintGroups()){
				printGroups.add(pg);
				if(pg.getFiles()!=null){
					for(PrintGroupFile pgFile : pg.getFiles()) pgFiles.add(pgFile);
				}
			}
		}
		

		result=runUpdate(order);
		if(result.isComplete()) result=runInsertBatch(subOrders);
		if(result.isComplete()) result=runInsertBatch(einfos);
		if(result.isComplete()) result=runInsertBatch(printGroups);
		if(result.isComplete()) result=runInsertBatch(pgFiles);
		
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
				for(PrintGroupFile pgFile : pg.getFiles()) pgFiles.add(pgFile);
			}
		}
		
		if(result.isComplete()) result=runInsertBatch(items);
		if(result.isComplete()) result=runInsertBatch(pgFiles);
		if(result.isComplete()){
			//set order state
			String sql="UPDATE phcdata.orders SET state = ?, state_date = ?, reported_state=0 WHERE id = ? AND state > ?";
			result=runDML(sql,210,dt,order_id,210);
			//reset extra
			sql="UPDATE phcdata.order_extra_state SET state_date=NULL WHERE id=? AND sub_id='' AND state IN (210,250)";
			result=runDML(sql,order_id);
		}
		if(result.isComplete()){
			//set suborders state
			for(String subId : subIds){
				String sql="UPDATE phcdata.suborders SET state = ?, state_date = ? WHERE order_id = ? AND sub_id = ?";
				result=runDML(sql,210,dt,order_id,subId);
				//reset extra
				sql="UPDATE phcdata.order_extra_state SET state_date=NULL WHERE id=? AND sub_id=? AND state IN (210,250)";
				result=runDML(sql, order_id, subId);
			}
		}
		if(result.isComplete()){
			//set parent pg state
			for(String subId : parentIds){
				String sql="UPDATE phcdata.print_group SET state = ?, state_date = ? WHERE id = ?";
				result=runDML(sql,251,dt,subId);
			}
		}
		return result;
	}

	@Override
	public SqlResult cleanUpOrder(String id){
		//PROCEDURE phcdata.orderCleanUp(IN pId VARCHAR(50))
		String sql= "{CALL phcdata.orderCleanUp(?)}";
		return runCall(sql, id); 
	}

	@Override
	public SqlResult cancelOrders(String[] ids){
		SqlResult result= new SqlResult();
		//PROCEDURE phcdata.orderCancel(IN pId VARCHAR(50))
		String sql= "{CALL phcdata.orderCancel(?)}";
		for (String id : ids){
			result=runCall(sql, id);
			if(!result.isComplete()) return result;
		}
		return result;
	}

}
