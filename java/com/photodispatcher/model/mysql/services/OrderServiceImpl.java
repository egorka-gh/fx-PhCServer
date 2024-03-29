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
import com.photodispatcher.model.mysql.entities.OrderBook;
import com.photodispatcher.model.mysql.entities.OrderExtraInfo;
import com.photodispatcher.model.mysql.entities.OrderExtraMessage;
import com.photodispatcher.model.mysql.entities.OrderExtraState;
import com.photodispatcher.model.mysql.entities.OrderExtraStateProlong;
import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.PrintGroupFile;
import com.photodispatcher.model.mysql.entities.PrintGroupReject;
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
	public SelectResult<Order> loadDownloadErrs(){
		//lock (105) and lock time > 1 hour or err (120)
		String sql="SELECT o.*, s.name source_name, os.name state_name"+
					" FROM orders o"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" INNER JOIN sources s ON o.source = s.id"+
					" WHERE o.state IN(105,120) AND (o.state=120 OR o.state_date < (NOW() - INTERVAL 30 MINUTE))"+
					" ORDER BY o.state_date";
		return runSelect(Order.class, sql);
	}

	@Override
	public SelectResult<Order> loadBuildErrs(){
		//lock (157) and lock time > 1 hour or err (170)
		String sql="SELECT o.*, s.name source_name, os.name state_name"+
					" FROM orders o"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" INNER JOIN sources s ON o.source = s.id"+
					" WHERE o.state IN(157,170) AND (o.state=170 OR o.state_date < (NOW() - INTERVAL 30 MINUTE))"+
					" ORDER BY o.state_date";
		return runSelect(Order.class, sql);
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
						" IFNULL(s.prt_qty, if(pg.book_type=0, pg.prints, pg.book_num)) prt_qty, pg.book_type proj_type"+
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
	public SelectResult<SubOrder> loadSubOrderByOrder(String orderId, String code){
		//PROCEDURE phcconfig.findeSubOrderByOrder(IN pOrderId varchar(50), IN pSrcCode char(1))
		String sql= "{CALL findeSubOrderByOrder(?,?)}";
		SelectResult<SubOrder> result=runCallSelect(SubOrder.class, sql, orderId, code);

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
	public SelectResult<PrintGroup> loadCompoChilds(String compoID){
		String sql;
		
		sql="SELECT pg.*, o.source source_id, s.name source_name, o.ftp_folder order_folder, os.name state_name,"+
				" p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name, la.value laminat_name,"+
				" lab.name lab_name, bt.name book_type_name, bp.name book_part_name, IFNULL(so.alias, pg.path) alias, ct.name compo_type_name"+
			" FROM print_group pg"+
				" INNER JOIN orders o ON pg.order_id = o.id"+
				" INNER JOIN sources s ON o.source = s.id"+
				" INNER JOIN order_state os ON pg.state = os.id"+
				" INNER JOIN attr_value p ON pg.paper = p.id"+
				" INNER JOIN attr_value fr ON pg.frame = fr.id"+
				" INNER JOIN attr_value cr ON pg.correction = cr.id"+
				" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
				" INNER JOIN attr_value la ON pg.laminat = la.id"+
				" INNER JOIN book_type bt ON pg.book_type = bt.id"+
				" INNER JOIN book_part bp ON pg.book_part = bp.id"+
				" INNER JOIN compo_type ct ON pg.compo_type = ct.id"+
				" LEFT OUTER JOIN lab ON pg.destination = lab.id"+
				" LEFT OUTER JOIN suborders so ON so.order_id = pg.order_id AND so.sub_id = pg.sub_id"+
			" WHERE pg.id IN "+
				" (SELECT pg1.id FROM print_group pg"+
					" INNER JOIN order_books ob ON ob.compo_pg = pg.id"+
					" INNER JOIN print_group pg1 ON pg1.id = ob.pg_id"+
					" WHERE pg.order_id = ? AND pg.is_reprint = 0)";
		SelectResult<PrintGroup> result=runSelect(PrintGroup.class,sql, compoID);
		if(!result.isComplete()) return result;
		//load childs
		if (result.getData()!=null && !result.getData().isEmpty() ){
			sql="SELECT pgf.*, pg.path"+
					" FROM print_group pg INNER JOIN print_group_file pgf ON pg.id = pgf.print_group"+
					" WHERE pg.id = ?";
			for (PrintGroup pg : result.getData()){
				//files
				SelectResult<PrintGroupFile> fRes=runSelect(PrintGroupFile.class,sql, pg.getId());
				if(!fRes.isComplete()){
					result.cloneError(fRes);
					return result;
				}
				pg.setFiles(fRes.getData());
				//books
				SelectResult<OrderBook> bRes = loadPGBooks(pg.getId());
				if(!bRes.isComplete()){
					result.cloneError(bRes);
					return result;
				}
				pg.setBooks(bRes.getData());
			}			
		}

		return result;
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
							" p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name, la.value laminat_name,"+
							" lab.name lab_name, bt.name book_type_name, bp.name book_part_name, IFNULL(so.alias, pg.path) alias, ct.name compo_type_name"+
						" FROM print_group pg"+
							" INNER JOIN orders o ON pg.order_id = o.id"+
							" INNER JOIN sources s ON o.source = s.id"+
							" INNER JOIN order_state os ON pg.state = os.id"+
							" INNER JOIN attr_value p ON pg.paper = p.id"+
							" INNER JOIN attr_value fr ON pg.frame = fr.id"+
							" INNER JOIN attr_value cr ON pg.correction = cr.id"+
							" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
							" INNER JOIN attr_value la ON pg.laminat = la.id"+
							" INNER JOIN book_type bt ON pg.book_type = bt.id"+
							" INNER JOIN book_part bp ON pg.book_part = bp.id"+
							" INNER JOIN compo_type ct ON pg.compo_type = ct.id"+
							" LEFT OUTER JOIN lab ON pg.destination = lab.id"+
							" LEFT OUTER JOIN suborders so ON so.order_id = pg.order_id AND so.sub_id = pg.sub_id"+
						" WHERE pg.order_id=?";
			SelectResult<PrintGroup> pgRes=runSelect(PrintGroup.class,sql, id);
			if(!pgRes.isComplete()){
				result.cloneError(pgRes);
				return result;
			}
			order.setPrintGroups(pgRes.getData());

			SelectResult<OrderBook> obRes=loadOrderBooks(id);
			if(!obRes.isComplete()){
				result.cloneError(obRes);
				return result;
			}
			order.setBooks(obRes.getData());

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
						" p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name, la.value laminat_name,"+
						" bt.name book_type_name, bp.name book_part_name"+
					" FROM print_group pg"+
						" INNER JOIN attr_value p ON pg.paper = p.id"+
						" INNER JOIN attr_value fr ON pg.frame = fr.id"+
						" INNER JOIN attr_value cr ON pg.correction = cr.id"+
						" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
						" INNER JOIN attr_value la ON pg.laminat = la.id"+
						" INNER JOIN book_type bt ON pg.book_type = bt.id"+
						" INNER JOIN book_part bp ON pg.book_part = bp.id"+
					" WHERE pg.order_id=? AND pg.sub_id=?";
			SelectResult<PrintGroup> pgRes=runSelect(PrintGroup.class,sql, id, sub_id);
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
			
			SelectResult<OrderBook> obRes=loadOrderBooks(id);
			if(!obRes.isComplete()){
				result.cloneError(obRes);
				return result;
			}
			order.setBooks(obRes.getData());
		}
		return result;
	}

	@Override
	public SelectResult<OrderBook> loadOrderBooks(String orderId){
		String sql="SELECT pg.order_id, pg.sub_id, os.name state_name, pg.book_part, bp.name book_part_name, s.name staff_name, sat.name sa_type_name, sa.remark sa_remark, ob.*, ct.name compo_type_name"+
				 " FROM print_group pg"+
				   " INNER JOIN order_books ob ON pg.id = ob.pg_id"+
				   " INNER JOIN order_state os ON ob.state = os.id"+
				   " INNER JOIN book_part bp ON bp.id = pg.book_part"+
				   " INNER JOIN compo_type ct ON ob.compo_type = ct.id"+
				   " LEFT OUTER JOIN print_group_rejects pgr ON pg.id = pgr.print_group AND pgr.book=ob.book"+
				   " LEFT OUTER JOIN staff_activity sa ON pgr.activity = sa.id"+
				   " LEFT OUTER JOIN staff s ON s.id = sa.staff"+
				   " LEFT OUTER JOIN staff_activity_type sat ON sa.sa_type = sat.id"+
				  " WHERE pg.order_id = ?"+
				 " ORDER BY pg.sub_id, ob.pg_id, ob.book";
		return runSelect(OrderBook.class, sql, orderId);
	}

	@Override
	public SelectResult<OrderBook> loadPGBooks(String pgId){
		String sql="SELECT pg.order_id, pg.sub_id, os.name state_name, pg.book_part, bp.name book_part_name, s.name staff_name, sat.name sa_type_name, sa.remark sa_remark, ob.*, ct.name compo_type_name"+
				 " FROM print_group pg"+
				   " INNER JOIN order_books ob ON pg.id = ob.pg_id"+
				   " INNER JOIN order_state os ON ob.state = os.id"+
				   " INNER JOIN book_part bp ON bp.id = pg.book_part"+
				   " INNER JOIN compo_type ct ON ob.compo_type = ct.id"+
				   " LEFT OUTER JOIN print_group_rejects pgr ON pg.id = pgr.print_group AND pgr.book=ob.book"+
				   " LEFT OUTER JOIN staff_activity sa ON pgr.activity = sa.id"+
				   " LEFT OUTER JOIN staff s ON s.id = sa.staff"+
				   " LEFT OUTER JOIN staff_activity_type sat ON sa.sa_type = sat.id"+
				  " WHERE pg.id = ?"+
				 " ORDER BY ob.book";
		return runSelect(OrderBook.class, sql, pgId);
	}
	
	@Override
	public SelectResult<OrderBook> loadSubOrderBooks(String orderId, String subId){
		if(subId==null) subId="";
		String sql="SELECT pg.order_id, pg.sub_id, os.name state_name, pg.book_part, bp.name book_part_name, s.name staff_name, sat.name sa_type_name, sa.remark sa_remark, ob.*, ct.name compo_type_name"+
				 " FROM print_group pg"+
				   " INNER JOIN order_books ob ON pg.id = ob.pg_id"+
				   " INNER JOIN compo_type ct ON ob.compo_type = ct.id"+
				   " INNER JOIN order_state os ON ob.state = os.id"+
				   " INNER JOIN book_part bp ON bp.id = pg.book_part"+
				   " LEFT OUTER JOIN print_group_rejects pgr ON pg.id = pgr.print_group AND pgr.book=ob.book"+
				   " LEFT OUTER JOIN staff_activity sa ON pgr.activity = sa.id"+
				   " LEFT OUTER JOIN staff s ON s.id = sa.staff"+
				   " LEFT OUTER JOIN staff_activity_type sat ON sa.sa_type = sat.id"+
				  " WHERE pg.order_id = ? AND pg.sub_id = ?"+
				 " ORDER BY pg.sub_id, ob.pg_id, ob.book";
		return runSelect(OrderBook.class, sql, orderId, subId);
	}

	@Override
	public SelectResult<Order> loadOrderBySrcCode(String code, String id){
		SelectResult<Order> result;
		String sql="SELECT o.*, s.name source_name, os.name state_name"+
					" FROM sources s"+
					" INNER JOIN orders o ON o.id = CONCAT_WS('_', s.id, ?)"+
					" INNER JOIN order_state os ON o.state = os.id"+
					" WHERE s.code=?";
		result=runSelect(Order.class,sql, id, code);
		if(result.isComplete()){
			if(!result.getData().isEmpty()){
				Order order=result.getData().get(0);
				SelectResult<OrderExtraInfo> eir=loadExtraIfo(id,"");
				if(!eir.isComplete()){
					result.cloneError(eir);
				} else if(!eir.getData().isEmpty()){
					order.setExtraInfo(eir.getData().get(0));
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
		SelectResult<OrderExtraInfo> result=runSelect(OrderExtraInfo.class,sql, id, subId);
		if(result.isComplete() && !result.getData().isEmpty()){
			SelectResult<OrderExtraMessage> subRes=loadExtraMessages(id,subId);
			if(!subRes.isComplete()){
				result.cloneError(subRes);
			}else{
				result.getData().get(0).setMessagesLog(subRes.getData());
			}
		}
		return result;
	}

	public SelectResult<OrderExtraMessage> loadExtraMessages(String id, String subId){
		String sql="SELECT * FROM order_extra_message oem WHERE oem.id=? AND oem.sub_id=? ORDER BY lod_key";
		return runSelect(OrderExtraMessage.class,sql, id, subId);
	}

	@Override
	public SelectResult<OrderExtraInfo> loadExtraIfoByPG(String pgId){
		String sql="SELECT ei.*, pg.book_type, pg.book_part"+
					" FROM print_group pg"+
					" INNER JOIN order_extra_info ei ON pg.order_id=ei.id AND pg.sub_id=ei.sub_id"+
					" WHERE pg.id=?";
		SelectResult<OrderExtraInfo> result=runSelect(OrderExtraInfo.class,sql, pgId);
		if(result.isComplete() && !result.getData().isEmpty()){
			SelectResult<OrderExtraMessage> subRes=loadExtraMessages(result.getData().get(0).getId(),result.getData().get(0).getSub_id());
			if(!subRes.isComplete()){
				result.cloneError(subRes);
			}else{
				result.getData().get(0).setMessagesLog(subRes.getData());
			}
		}
		return result;
	}

	@Override
	public SelectResult<PrintGroup> loadReprintsByPG(String pgId){
		String sql="SELECT pg1.*"+
				 " FROM print_group pg"+
				   " INNER JOIN print_group pg1 ON pg.order_id = pg1.order_id"+
				 " WHERE pg.id = ? AND IF(pg.is_reprint = 1, pg.reprint_id, pg.id) IN (pg1.id, pg1.reprint_id)";		
		return runSelect(PrintGroup.class,sql, pgId);
	}

	@Override
	public SelectResult<PrintGroupReject> loadRejects4PG(String pgId){
		String sql="SELECT pgr.*, IFNULL(tu.name,'') thech_unit_name"+
					 " FROM print_group pg"+
					   " INNER JOIN print_group pg1 ON pg1.order_id = pg.order_id AND pg.id IN (pg1.reprint_id, pg1.id) AND pg1.state < 450"+
					   " INNER JOIN print_group_rejects pgr ON pgr.print_group = pg1.id"+
					   " LEFT OUTER JOIN tech_unit tu ON pgr.thech_unit=tu.id"+
					 " WHERE pg.id = ?";		
		return runSelect(PrintGroupReject.class,sql, pgId);
	}

	@Override
	public SelectResult<PrintGroupReject> loadRejects4Order(String pgId){
		String sql="SELECT pgr.*, IFNULL(tu.name,'') thech_unit_name"+
					 " FROM print_group pg"+
					   " INNER JOIN print_group pg1 ON pg1.order_id = pg.order_id AND pg.sub_id=pg1.sub_id AND pg1.is_reprint=1 AND pg1.state < 450"+
					   " INNER JOIN print_group_rejects pgr ON pgr.print_group = pg1.id"+
					   " LEFT OUTER JOIN tech_unit tu ON pgr.thech_unit=tu.id"+
					 " WHERE pg.id = ?";		
		return runSelect(PrintGroupReject.class,sql, pgId);
	}

	@Override
	public DmlResult<Order> addManual(Order order){
		return runInsert(order);
	}

	@Override
	public SqlResult persistExtraInfo(OrderExtraInfo info){
		SqlResult result= new SqlResult();
		if(info==null) return result;
		result=runInsertOrUpdate(info);
		if(result.isComplete() && info.getMessagesLog()!=null){
			//SqlResult subres=runInsertBatch(info.getMessagesLog());
			SqlResult subres=runInesrtUpdateBatch(info.getMessagesLog());
			if(!subres.isComplete()) result.cloneError(subres);
		}
		return result;
	}

	@Override
	public SqlResult fillUpOrder(Order order){
		SqlResult result= new SqlResult();
		if(order==null) return result;
		if(order.getState()<170){
			result.setComplete(false);
			result.setErrMesage("Не допустимый статус");
			return result;
		}
		List<OrderExtraInfo> einfos = new ArrayList<OrderExtraInfo>();
		List<OrderExtraMessage> emsg = new ArrayList<OrderExtraMessage>();
		List<SubOrder> subOrders = new ArrayList<SubOrder>();
		List<PrintGroup> printGroups = new ArrayList<PrintGroup>();
		List<PrintGroupFile> pgFiles = new ArrayList<PrintGroupFile>();
		
		//fill lists
		if(order.getExtraInfo()!=null){
			order.getExtraInfo().setId(order.getId());
			order.getExtraInfo().setSub_id("");
			einfos.add(order.getExtraInfo());
			if(order.getExtraInfo().getMessagesLog()!=null) emsg.addAll(order.getExtraInfo().getMessagesLog());
		}
		if(order.getSuborders()!=null && !order.getSuborders().isEmpty()){
			for (SubOrder so : order.getSuborders()){
				so.setOrder_id(order.getId());
				so.setState(order.getState());
				so.setState_date(order.getState_date());
				if(so.getExtraInfo()!=null){
					so.getExtraInfo().setId(order.getId());
					so.getExtraInfo().setSub_id(so.getSub_id());
					einfos.add(so.getExtraInfo());
					if(so.getExtraInfo().getMessagesLog()!=null) emsg.addAll(so.getExtraInfo().getMessagesLog());
				}
				subOrders.add(so);
			}
		}
		if(order.getPrintGroups()!=null && !order.getPrintGroups().isEmpty()){
			for (PrintGroup pg : order.getPrintGroups()){
				pg.setOrder_id(order.getId());
				if (pg.getSub_id() == null ) pg.setSub_id("");
				pg.setState(order.getState());
				pg.setState_date(order.getState_date());
				printGroups.add(pg);
				if(pg.getFiles()!=null){
					for(PrintGroupFile pgFile : pg.getFiles()){
						pgFile.setPrint_group(pg.getId());
						pgFiles.add(pgFile);
					}
				}
			}
		}

		//run in transaction
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//update order
			OrmElf.updateObject(connection, order);
			//add subOrders
			//OrmElf.insertListBatched(connection, subOrders);
			OrmElf.insertOrUpdateListBatched(connection, subOrders);
			//add extra info
			OrmElf.insertOrUpdateListBatched(connection, einfos);
			//add extra messages
			OrmElf.insertOrUpdateListBatched(connection, emsg);
			//add printGroups
			OrmElf.insertOrUpdateListBatched(connection, printGroups);
			//add files
			OrmElf.insertListBatched(connection, pgFiles);
			//add books
			OrmWriter.executeCall(connection, "{CALL fill_order(?)}", order.getId());
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
	public SqlResult saveVsSuborders(Order order){
		SqlResult result= new SqlResult();
		if(order==null) return result;

		List<OrderExtraInfo> einfos = new ArrayList<OrderExtraInfo>();
		List<OrderExtraMessage> emsg = new ArrayList<OrderExtraMessage>();
		List<SubOrder> subOrders = new ArrayList<SubOrder>();
		
		//fill lists
		if(order.getExtraInfo()!=null){
			order.getExtraInfo().setId(order.getId());
			order.getExtraInfo().setSub_id("");
			einfos.add(order.getExtraInfo());
			if(order.getExtraInfo().getMessagesLog()!=null) emsg.addAll(order.getExtraInfo().getMessagesLog());
		}
		if(order.getSuborders()!=null && !order.getSuborders().isEmpty()){
			for (SubOrder so : order.getSuborders()){
				so.setOrder_id(order.getId());
				if(so.getExtraInfo()!=null){
					so.getExtraInfo().setId(order.getId());
					so.getExtraInfo().setSub_id(so.getSub_id());
					einfos.add(so.getExtraInfo());
					if(so.getExtraInfo().getMessagesLog()!=null) emsg.addAll(so.getExtraInfo().getMessagesLog());
				}
				subOrders.add(so);
			}
		}
		
		//run in transaction
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			//update order
			OrmElf.updateObject(connection, order);
			//add subOrders
			//OrmElf.insertListBatched(connection, subOrders);
			OrmElf.insertOrUpdateListBatched(connection, subOrders);
			//add extra info
			OrmElf.insertOrUpdateListBatched(connection, einfos);
			//add extra messages
			OrmElf.insertOrUpdateListBatched(connection, emsg);
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
		
		//String order_id="";
		Set<String> subIds= new HashSet<String>();
		Set<String> parentIds= new HashSet<String>();
		List<PrintGroupFile> pgFiles = new ArrayList<PrintGroupFile>();
		List<PrintGroupReject> pgRejects = new ArrayList<PrintGroupReject>();
		Date dt =new Date();
		for (PrintGroup pg : items){
			//order_id=pg.getOrder_id();
			if(pg.getSub_id()!=null && pg.getSub_id().length()>0) subIds.add(pg.getSub_id());
			if(pg.getReprint_id()!=null && pg.getReprint_id().length()>0) parentIds.add(pg.getReprint_id());
			if(pg.getFiles()!=null){
				for(PrintGroupFile pgFile : pg.getFiles()){
					pgFile.setPrint_group(pg.getId());
					pgFiles.add(pgFile);
				}
			}
			if(pg.getRejects()!=null){
				for(PrintGroupReject pgReject : pg.getRejects()){
					pgReject.setPrint_group(pg.getId());
					pgRejects.add(pgReject);
				}
			}
		}
		
		//run in transaction
		Connection connection = null;
		try {
			connection=ConnectionFactory.getConnection();
			connection.setAutoCommit(false);
			
			//add printGroups
			OrmElf.insertListBatched(connection, items);
			//add files
			OrmElf.insertListBatched(connection, pgFiles);
			//add rejects
			OrmElf.insertListBatched(connection, pgRejects);

			String sql;
			for (PrintGroup pg : items){
				// add reprint books
				OrmWriter.executeCall(connection, "{CALL fill_books_reject(?)}", pg.getId());
				
				/* start print_post extra state (subid=pgid)*/
				sql="INSERT IGNORE INTO order_extra_state (id, sub_id, state, start_date) VALUES (?, ?, ?, ?)";
				OrmWriter.executeUpdate(connection, sql, pg.getOrder_id(), pg.getId(), 210, ((pg.getState_date()==null)?dt:pg.getState_date()));
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
	public SqlResult cleanUpOrder(String id, int state){
		//PROCEDURE orderCleanUp(IN pId varchar(50), IN pState int)
		String sql= "{CALL orderCleanUp(?,?)}";
		return runCall(sql, id, state); 
	}

	@Override
	public SqlResult cancelOrders(String[] ids, int state){
		SqlResult result= new SqlResult();
		//PROCEDURE orderCancel(IN pId VARCHAR(50), IN pState INT)
		String sql= "{CALL orderCancel( ?, ?)}";
		for (String id : ids){
			result=runCall(sql, id, state);
			if(!result.isComplete()) return result;
		}
		return result;
	}

	@Override
	public SelectResult<Order> load4CleanFS(int source, int state, int days, int limit){
		String sql="SELECT o.* FROM orders o"+
					" WHERE o.source = ? AND o.state >= ? AND o.state_date <= DATE_SUB(CURDATE(), INTERVAL ? DAY) AND o.clean_fs = 0" +
		    		" LIMIT "+Integer.toString(limit);
		return runSelect(Order.class, sql, source, state, days );
	}

	@Override
	public SqlResult markCleanFS(String[] ids){
		//TODO refactor batch update?
		SqlResult result= new SqlResult();
		String sql="UPDATE orders o SET clean_fs=1 WHERE o.id=?";

		for (String id : ids){
			runDML(sql, id);
		}
		return result;
	}

	@Override
	public SqlResult setState(Order order){
		String sql="UPDATE orders o SET o.state=?, o.state_date=? WHERE o.id=?";
		return runDML(sql, order.getState(), order.getState_date(), order.getId());
	}

	@Override
	public SqlResult setSuborderState(SubOrder suborder){
		//PROCEDURE forwardSubOrderState(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pDate datetime)
		String sql= "{CALL forwardSubOrderState( ?, ?, ?, ?)}";
		return runCall(sql, suborder.getOrder_id(), suborder.getSub_id(), suborder.getState(), suborder.getState_date());
	}

	@Override
	public SqlResult setStateBatch(List<Order> orders){
		return runUpdateBatch(orders);
	}

	@Override
	public SqlResult captureState(String orderId, int fromState, int toState, String owner){
		SqlResult result= new SqlResult();
		String sql="UPDATE orders o SET o.state=?, o.state_date=?, o.lock_owner=? WHERE o.id=? AND o.state=?";
		Connection connection = null;
		Date dt=new Date();
		boolean updated=false;
		try {
			connection=ConnectionFactory.getConnection();
			updated=OrmWriter.executeUpdate(connection, sql, toState, dt, owner, orderId, fromState)>0;
		} catch (SQLException e){
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			updated=false;
		}finally{
			SqlClosureElf.quietClose(connection);
		}

		if(!updated && owner!= null && !owner.isEmpty()){
			sql="SELECT o.id FROM orders o WHERE o.id=? AND o.state=? AND o.lock_owner=?";
			SelectResult<Order> res=runSelect(Order.class, sql, orderId, toState, owner);
			if(res.isComplete() && !res.getData().isEmpty()) updated=true;
		}

		if(updated){
			result.setResultCode(toState);
			//log
			sql="INSERT IGNORE INTO log_capture (log_date, lock_key, lock_owner, from_state, to_state, result) VALUES(?,?,?,?,?,?)";
			runDML(sql, dt, orderId, owner, fromState, toState, result.getResultCode());
		}else{
			result.setResultCode(0);
		}
		return result;
	}

	@Override
	public SqlResult getLock(String key, String owner){
		//PROCEDURE lock_get (IN pkey varchar(100), IN powner varchar(50))
		String sql= "{CALL lock_get( ?, ?)}";
		
		Connection connection = null;
		boolean complited=false;
		SqlResult result= new SqlResult();
		try {
			connection=ConnectionFactory.getConnection();
			OrmWriter.executeCall(connection, sql, key, owner);
			complited=true;
		} catch (SQLException e){
			result.setComplete(false);
			result.setErrCode(e.getErrorCode());
			result.setErrMesage(e.getMessage());
			complited=false;
		}finally{
			SqlClosureElf.quietClose(connection);
		}

		if(complited){
			result.setResultCode(1);
			// log
			sql="INSERT IGNORE INTO log_capture (log_date, lock_key, lock_owner, result) VALUES(?, ?, ?, ?)";
			runDML(sql, new Date(), key, owner, result.getResultCode());
		}else{
			result.setResultCode(0);
		}
		
		return result;
	}

	@Override
	public SqlResult releaseLock(String key, String owner){
		//PROCEDURE PROCEDURE lock_release (IN pkey varchar(100))
		String sql= "{CALL lock_release (?, ?)}";
		return runCall(sql, key, owner);
	}

	@Override
	public SqlResult clearLocks(){
		//PROCEDURE lock_clear()
		String sql= "{CALL lock_clear()}";
		SqlResult res=runCall(sql);
		sql="DELETE FROM log_capture WHERE log_date < DATE_SUB(CURDATE(), INTERVAL 10 DAY)";
		runDML(sql);
		return res;
	}

	@Override
	public SqlResult createCompo( int waiteLimit){
		return runCall("{CALL compo_create(?)}", waiteLimit);
	}

}
