package com.photodispatcher.model.mysql.services;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("orderStateService")
public class OrderStateServiceImpl extends AbstractDAO implements OrderStateService {

	@Override
	public SelectResult<OrderState> loadAll(){
		SelectResult<OrderState> result;
		String sql="SELECT * FROM phcconfig.order_state ORDER BY id";
		result=runSelect(OrderState.class, sql);
		return result;
	}
	
	@Override
	public SqlResult extraStateStart(String orderId, String subId, int techPoint){
		//PROCEDURE phcdata.extraStateStart(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pTechPoint INT)
		String sql= "{CALL phcdata.extraStateStart(?,?,?)}";
		return runCall(sql, orderId, subId, techPoint);
	}

	@Override
	public SqlResult extraStateSet(String orderId, String subId, int techPoint){
		//PROCEDURE phcdata.extraStateSet(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pTechPoint INT)
		String sql= "{CALL phcdata.extraStateSet(?,?,?)}";
		return runCall(sql, orderId, subId, techPoint);
	}

	@Override
	public SqlResult extraStateReset(String orderId, String subId, int state){
		//PROCEDURE phcdata.extraStateReset(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pState int)
		String sql= "{CALL phcdata.extraStateReset(?,?,?)}";
		return runCall(sql, orderId, subId, state);
	}

	@Override
	public SqlResult extraStateProlong(String orderId, String subId, int state, String comment){
		//PROCEDURE phcdata.extraStateProlong(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pState int, IN pComment VARCHAR(250))
		String sql= "{CALL phcdata.extraStateReset(?,?,?,?)}";
		return runCall(sql, orderId, subId, state, comment);
	}

}
