package com.photodispatcher.model.mysql.services;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.SelectResult;

@Service("orderStateService")
public class OrderStateServiceImpl extends AbstractDAO implements OrderStateService {

	@Override
	public SelectResult<OrderState> loadAll(){
		SelectResult<OrderState> result;
		String sql="SELECT * FROM phcconfig.order_state ORDER BY id";
		result=runSelect(OrderState.class, sql);
		return result;
	}
}
