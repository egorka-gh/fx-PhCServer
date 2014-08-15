package com.photodispatcher.model.mysql.services;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.OrderExtraInfo;
import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.Source;
import com.photodispatcher.model.mysql.entities.SqlResult;

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
				" WHERE o.id=?";
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
	public SelectResult<OrderExtraInfo> loadExtraIfo(String id, String subId){
		String sql="SELECT ei.* FROM phcdata.order_extra_info ei WHERE ei.id=? AND ei.sub_id=?";
		return runSelect(OrderExtraInfo.class,sql, id, subId);
	}
	
	@Override
	public DmlResult<Order> addManual(Order order){
		return runInsert(order);
	}

	@Override
	public SqlResult cleanUpOrder(String id){
		SqlResult result;
		//TODO refactor to procedure
		String sql="DELETE FROM phcdata.print_group WHERE order_id = ?";
		result=runDML(sql, id);
		if(!result.isComplete()) return result;

		sql="DELETE FROM phcdata.order_extra_info WHERE id = ?";
		result=runDML(sql, id);
		if(!result.isComplete()) return result;

		sql="DELETE FROM phcdata.suborders WHERE order_id = ?";
		result=runDML(sql, id);
		if(!result.isComplete()) return result;

		sql="UPDATE phcdata.orders SET state = ?, state_date = ? WHERE id = ?";
		result=runDML(sql, 100, new Date(), id);
		if(!result.isComplete()) return result;
		
		sql="INSERT INTO state_log (order_id, pg_id, state, state_date, comment)" +
									" VALUES (?,?,?,?,?)";
		result=runDML(sql, id, "", 100, new Date(), "reset");

		return result; 
	}

	@Override
	public SqlResult cancelOrders(String[] ids){
		SqlResult result= new SqlResult();
		//PROCEDURE phcdata.cancelOrder(IN pId VARCHAR(50))
		String sql= "{CALL phcdata.cancelOrder(?)}";
		for (String id : ids){
			result=runCall(sql, id);
			if(!result.isComplete()) return result;
		}
		return result;
	}

}
