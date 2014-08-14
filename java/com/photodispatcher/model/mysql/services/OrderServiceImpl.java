package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.springframework.stereotype.Service;

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
}
