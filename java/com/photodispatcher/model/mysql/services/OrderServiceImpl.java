package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("orderService")
public class OrderServiceImpl extends AbstractDAO implements OrderService {
	
	@Override
	public SqlResult beginSync(){
		//clear temp
		String sql="DELETE FROM phcdata.tmp_orders";
		return runDML(sql);
	}

	@Override
	public SqlResult addSyncItems(List<OrderTemp> items){
		return runInsertBatch(items);
	}


}
