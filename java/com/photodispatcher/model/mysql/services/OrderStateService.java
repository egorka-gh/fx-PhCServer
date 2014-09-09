package com.photodispatcher.model.mysql.services;

import java.util.Date;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.OrderExtraState;
import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StateLog;

@RemoteDestination(id="orderStateService", source="orderStateService")
public interface OrderStateService {

	public SelectResult<OrderState> loadAll();

	public SqlResult logState(StateLog item);
	public SqlResult logStateByPGroup(String pgId, int state, String comment);
	public SelectResult<StateLog> loadStateLogs(Date from, boolean onlyErrors);

	public SqlResult extraStateStart(String orderId, String subId, int state, Date date);
	public SqlResult extraStateSet(String orderId, String subId, int state, Date date);
	public SqlResult extraStateReset(String orderId, String subId, int state);
	public SqlResult extraStateProlong(String orderId, String subId, int state, String comment);
	//public SqlResult extraStateSetByPGroup(String pgId, int state);

	public SqlResult printPost(String pgId, int lab);
	public SqlResult printEndManual(String[] pgIds);
	public SqlResult printCancel(String[] pgIds);
	public SqlResult printGroupMarkInPrint(String pgId);
	
	public SelectResult<OrderExtraState> loadMonitorEState(int startState, int techState, int endState);
	public SqlResult extraStateStartMonitor(String orderId, String subId, int stateStart, int stateStop);

}
